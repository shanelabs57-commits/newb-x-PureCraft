#ifndef SKY_H
#define SKY_H
#include "detection.h"
#include "noise.h"

struct nl_skycolor { vec3 zenith; vec3 horizon; vec3 horizonEdge; };

vec3 spectrum(float x){vec3 s=vec3(x-.5,x,x+.5);s=smoothstep(1.,0.,abs(s));return s*s;}
vec3 getUnderwaterCol(vec3 FOG_COLOR){return 2.*NL_UNDERWATER_TINT*FOG_COLOR*FOG_COLOR;}
vec3 getEndZenithCol(){return NL_END_ZENITH_COL;}
vec3 getEndHorizonCol(){return NL_END_HORIZON_COL;}

nl_skycolor nlEndSkyColors(nl_environment env){
  nl_skycolor s;s.zenith=getEndZenithCol();s.horizon=getEndHorizonCol();s.horizonEdge=s.horizon;return s;
}

nl_skycolor nlOverworldSkyColors(nl_environment env){
  nl_skycolor s;
  float f=1.+2.*(1.-max(-env.dayFactor,0.)),n=step(env.dayFactor,0.);
  s.zenith=mix(NL_DAY_ZENITH_COL,NL_NIGHT_ZENITH_COL*f,n);
  s.horizon=mix(NL_DAY_HORIZON_COL,NL_NIGHT_HORIZON_COL*f,n);
  s.horizonEdge=mix(NL_DAY_EDGE_COL,NL_NIGHT_EDGE_COL*f,n);
  float d=1.-env.dayFactor*env.dayFactor;d*=d*d;d*=mix(1.,d*d,n);
  s.zenith=mix(s.zenith,NL_DAWN_ZENITH_COL,d);
  s.horizon=mix(s.horizon,NL_DAWN_HORIZON_COL,d);
  s.horizonEdge=mix(s.horizonEdge,NL_DAWN_EDGE_COL,d);
  float zh=dot(s.zenith,vec3_splat(.33)),hh=dot(s.horizon,vec3_splat(.33));
  float r=env.rainFactor*NL_SKY_RAIN_MIX_FACTOR;
  s.zenith=mix(s.zenith,NL_RAIN_ZENITH_COL*zh,r);
  s.horizon=mix(s.horizon,NL_RAIN_HORIZON_COL*hh,r);
  s.horizonEdge=mix(s.horizonEdge,s.horizon,env.rainFactor);
  if(env.underwater){
    vec3 u=env.fogCol*env.fogCol*NL_UNDERWATER_TINT;
    s.zenith=mix(2.*u,u*zh,.8);s.horizon=mix(2.*u,u*hh,.8);s.horizonEdge=s.horizon;
  }
  return s;
}

nl_skycolor nlSkyColors(nl_environment env){if(env.end)return nlEndSkyColors(env);return nlOverworldSkyColors(env);}

vec3 renderOverworldSky(nl_skycolor c,nl_environment e,vec3 v,bool plane){
  float a=abs(v.y),m=.5+.5*v.y/(.4+a);
  vec2 g=clamp(.5-.5*vec2(dot(e.sunDir,v),dot(e.moonDir,v)),0.,1.);
  vec2 g1=1.-mix(sqrt(g),g,e.rainFactor),g2=g1*g1,g4=g2*g2,g8=g4*g4;
  float mg=(g8.x+g8.y)*m*(1.-.9*e.rainFactor);
  float vh=1.-v.y*v.y,vh2=vh*vh;
  vh2=mix(vh2,mix(1.,vh2*vh2,NL_SKY_VOID_FACTOR),step(v.y,0.));vh2=mix(vh2,1.,mg);
  float vh4=vh2*vh2,g1v=vh4*vh4,g2v=.8*g1v+.2*vh2;
  g1v*=g1v;g1v=mix(g1v*g1v,1.,mg);g2v=mix(g2v,1.,mg);
  float d=1.-e.dayFactor*e.dayFactor,df=mix(1.,g2.x,d*d);
  vec3 sky=mix(c.horizon,c.horizonEdge,g1v*df*df);sky=mix(c.zenith,sky,g2v*df);
  sky*=.5+.5*g2v;
  sky*=(1.+(2.*mg+7.*mg*mg)*m)*mix(1.,m,NL_SKY_VOID_DARKNESS);
  if(!plane){float s=max(0.,(mg-.22)/.78);s*=s;s*=s;sky*=1.+15.*s*(1.-e.rainFactor);}
#ifdef NL_RAINBOW
  if(!e.underwater){float rf=.5+.5*v.y;rf*=rf;rf*=mix(NL_RAINBOW_CLEAR,NL_RAINBOW_RAIN,e.rainFactor);rf*=.5+.5*e.dayFactor;sky+=spectrum(24.2*(.85-g.x))*rf*c.horizon;}
#endif
  return sky;
}

vec3 nlRenderAurora(vec3 v,nl_environment e,float t){
  float night=1.-smoothstep(-.05,.18,e.dayFactor),rain=1.-e.rainFactor,up=clamp(v.y,0.,1.),vis=smoothstep(.08,.48,up);
  if(vis<=.001)return vec3_splat(0.);
  vec3 p=v;p.xz/=max(p.y,.12);p.xz*=.65;p.x+=t*.015;
  float n0=noise3D(vec3(p.x*1.4,p.y*.18,0.)),n1=noise3D(vec3(p.x*3.+t*.0225,p.y*.45,1.7)),n2=noise3D(vec3(p.x*7.-t*.03,p.y*.9,4.3));
  float wave=.5+.5*sin(p.x*4.+n0*5.+t*.045);
  float c=smoothstep(.28,.72,wave+n1*.35-n2*.18);
  c*=smoothstep(.08,.35,up)*(1.-smoothstep(.72,1.,up))*(.45+.55*n2);
  vec3 col=mix(NL_AURORA_COL1,NL_AURORA_COL2,smoothstep(.25,.9,c));
  return col*(.55+1.65*c*c)*vis*night*rain*1.35;
}

vec3 renderEndSky(vec3 h,vec3 z,vec3 v,float t){
  t*=.1;float a=atan2(v.x,v.z);
  float n1=.5+.5*sin(3.*a+t+10.*v.x*v.y);
  float n2=.5+.5*sin(5.*a+.5*t+5.*n1+.1*sin(40.*a-4.*t));
  float w=.7*n2*n1+.3*n1,gr=.5+.5*v.y,st=w*(1.-gr*gr*gr);
  st+=(1.-st)*smoothstep(1.-w,-1.,v.y);
  float f=.3*st+.7*smoothstep(1.,-.5,v.y),q=st*st,g=q*q;q*=q;
  vec3 sky=mix(z,h,f*f);
  sky+=(.1*st+2.*g*g*g+q*q*q)*vec3(2.,.5,0.);
  return sky+.25*st*spectrum(sin(2.*v.x*v.y+t));
}

vec3 nlRenderSky(nl_skycolor c,nl_environment e,vec3 v,float t,bool plane){
  v.y=-v.y;
  if(e.end)return renderEndSky(c.horizon,c.zenith,v,t);
  vec3 sky=renderOverworldSky(c,e,v,plane);
#ifdef NL_AURORA
  if(!e.underwater)sky+=nlRenderAurora(v,e,t);
#endif
  return sky;
}

vec3 nlRenderShootingStar(vec3 v,vec3 F,float t){
  float h=t/(NL_SHOOTING_STAR_DELAY+NL_SHOOTING_STAR_PERIOD),h0=floor(h);
  t=(NL_SHOOTING_STAR_DELAY+NL_SHOOTING_STAR_PERIOD)*(h-h0);t=min(t/NL_SHOOTING_STAR_PERIOD,1.);
  float t0=t*t,t1=1.-t0;t1*=t1;t1*=t1;t1*=t1;
  float r=fract(sin(h0)*43758.545313),a=6.2831*r,ca=cos(a),sa=sin(a);
  vec2 uv=v.xz*(6.+4.*r);uv=vec2(ca*uv.x+sa*uv.y,-sa*uv.x+ca*uv.y);
  uv.x+=t1-t;uv.x-=2.*r+3.5;uv.y+=v.y*3.;
  float g=1.-min(abs((uv.x-.95)*20.),1.),s=1.-min(abs(8.*uv.y),1.);
  s*=s*s*smoothstep(-1.+1.96*t1,.98-t,uv.x);s*=s*s*smoothstep(1.,.98-t0,uv.x);
  s*=(1.-t1)*(1.-t0)*(.7+16.*g*g)*max(1.-F.r-F.g-F.b,0.);
  return s*vec3(.8,.9,1.);
}

vec3 nlRenderGalaxy(vec3 v,vec3 fog,nl_environment e,float t){
  if(e.underwater)return vec3_splat(0.);
  t*=NL_GALAXY_SPEED;float cb=sin(.2*t),sb=cos(.2*t);
  v.xy=mul(mat2(cb,sb,-sb,cb),v.xy);
  float n0=.5+.5*sin(5.*v.x)*sin(5.*v.y-.5*t)*sin(5.*v.z+.5*t);
  float n1=noise3D(15.*v+sin(.85*t+1.3)),n2=noise3D(50.*v+n1+sin(.7*t+1.));
  float n3=smoothstep(.04,.3,noise3D(200.*v-10.*sin(.4*t+.5))+.02*n2);
  float gd=v.x+.1*v.y+.1*sin(10.*v.z+.2*t),st=n1*n2*n3*n3*(1.+70.*gd*gd);
  st=(1.-st)/(1.+400.*st);
  vec3 stars=(.8+.2*sin(vec3(8.,6.,10.)*(2.*n1+.8*n2)+vec3(0.,.4,.82)))*st;
  float mask=abs(v.x)-.15*n1+.04*n2+.25*n0,g=1.-(v.x*v.x+.03*n1+.2*n0);
  g*=g;g*=g*g;g*=1.-.3*smoothstep(.2,.3,mask);g*=1.-.2*smoothstep(.3,.4,mask);g*=1.-.1*smoothstep(.2,.1,mask);
  vec3 gc=normalize(vec3(n0,cos(2.*v.y),sin(v.x+n0)));
  stars+=(.4*g+.012)*mix(vec3(.5),gc*gc,NL_GALAXY_VIBRANCE);
  return stars*mix(1.,NL_GALAXY_DAY_VISIBILITY,e.dayFactor)*(1.-e.rainFactor);
}
#endif
