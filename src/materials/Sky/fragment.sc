#ifndef INSTANCING
  $input v_worldPos, v_underwaterRainTimeDay
#endif

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/main.sh>

  uniform vec4 TimeOfDay;
  uniform vec4 FogColor;
  uniform vec4 FogAndDistanceControl;

  // ==========================================
  // AURORA TEXTURES
  // ==========================================
  uniform sampler2D s_AuroraUnbound;
  uniform sampler2D s_AuroraVoxels;
#endif

void main() {
#ifndef INSTANCING

  // ==========================================
  // VIEW DIRECTION
  // ==========================================
  vec3 viewDir = normalize(v_worldPos);


  // ==========================================
  // ENVIRONMENT
  // ==========================================
  nl_environment env;

  env.end = false;
  env.nether = false;

  env.underwater =
    v_underwaterRainTimeDay.x > 0.5;

  env.rainFactor =
    v_underwaterRainTimeDay.y;

  env.dayFactor =
    v_underwaterRainTimeDay.w;

  env.fogCol =
    FogColor.rgb;

  env = calculateSunParams(
    env,
    TimeOfDay.x
  );


  // ==========================================
  // SKY COLORS
  // ==========================================
  nl_skycolor skycol =
    nlOverworldSkyColors(env);


  // ==========================================
  // MAIN SKY
  // ==========================================
  vec3 skyColor =
    nlRenderSky(
      skycol,
      env,
      -viewDir,
      v_underwaterRainTimeDay.z,
      true
    );


  // ==========================================
  // SHOOTING STARS
  // ==========================================
#ifdef NL_SHOOTING_STAR

  skyColor +=
    NL_SHOOTING_STAR *
    nlRenderShootingStar(
      viewDir,
      env.fogCol,
      v_underwaterRainTimeDay.z
    );

#endif


  // ==========================================
  // GALAXY STARS
  // ==========================================
#ifdef NL_GALAXY_STARS

  skyColor +=
    NL_GALAXY_STARS *
    nlRenderGalaxy(
      viewDir,
      env.fogCol,
      env,
      v_underwaterRainTimeDay.z
    );

#endif


  // ==========================================
  // AURORA
  // ==========================================
#ifdef NL_AURORA

  if (!env.underwater) {

    skyColor +=
      nlRenderAurora(
        viewDir,
        env,
        v_underwaterRainTimeDay.z
      );

  }

#endif


  // ==========================================
  // COLOR CORRECTION
  // ==========================================
  skyColor =
    colorCorrection(skyColor);


  // ==========================================
  // OUTPUT
  // ==========================================
  gl_FragColor =
    vec4(
      skyColor,
      1.0
    );

#else

  gl_FragColor =
    vec4(
      0.0,
      0.0,
      0.0,
      0.0
    );

#endif
}
