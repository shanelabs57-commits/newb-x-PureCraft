#ifndef INSTANCING
  $input v_worldPos, v_underwaterRainTimeDay
#endif

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/main.sh>
  uniform vec4 TimeOfDay;
  uniform vec4 FogColor;
#endif


// ============================================================
// GOLDEN SKY
// ============================================================

vec3 goldenSky(
  vec3 skyColor,
  float dayFactor,
  float viewY
) {
  float day =
    smoothstep(
      0.05,
      0.95,
      dayFactor
    );

  vec3 warmSun =
    vec3(
      1.075,
      0.965,
      0.885
    );

  vec3 nightLift =
    vec3(
      1.035,
      1.025,
      1.055
    );

  skyColor *=
    mix(
      nightLift,
      warmSun,
      day
    );

  float horizon =
    1.0 -
    smoothstep(
      0.05,
      0.85,
      abs(viewY)
    );

  float goldenMask =
    day *
    (
      0.35 +
      0.65 *
      horizon
    );

  skyColor +=
    vec3(
      0.045,
      0.018,
      -0.005
    ) *
    goldenMask;

  skyColor *=
    1.0 +
    0.035 *
    day;

  return skyColor;
}


// ============================================================
// DEEP BLUE AURORA
// ============================================================

vec3 deepBlueAurora(
  vec3 viewDir,
  float time,
  float dayFactor,
  float rainFactor
) {

  // Aurora only at night.
  float night =
    1.0 -
    smoothstep(
      0.05,
      0.35,
      dayFactor
    );


  // Aurora is strongest above the horizon.
  float height =
    smoothstep(
      0.02,
      0.32,
      viewDir.y
    );


  // Fade toward very high sky.
  float upperFade =
    1.0 -
    smoothstep(
      0.72,
      1.0,
      viewDir.y
    );


  float visibility =
    height *
    upperFade *
    night;


  // Rain suppresses aurora.
  visibility *=
    1.0 -
    0.75 *
    rainFactor;


  if (visibility <= 0.001)
    return vec3(0.0);


  // Project the sky direction onto a flat sky plane.
  vec2 p =
    viewDir.xz /
    max(
      viewDir.y,
      0.08
    );


  // Slow horizontal movement.
  p.x +=
    time *
    0.012;


  // Flowing wave layers.
  float wave1 =
    sin(
      p.x * 2.2 +
      sin(p.x * 0.75) * 1.8 +
      time * 0.35
    );


  float wave2 =
    sin(
      p.x * 4.7 -
      time * 0.22 +
      sin(p.x * 1.3) * 1.2
    );


  float wave3 =
    sin(
      p.x * 8.0 +
      time * 0.16
    );


  // Combine waves.
  float waves =
    wave1 * 0.55 +
    wave2 * 0.30 +
    wave3 * 0.15;


  // Convert waves into narrow aurora bands.
  float bands =
    smoothstep(
      0.18,
      0.82,
      waves * 0.5 + 0.5
    );


  // Vertical aurora movement.
  float vertical =
    sin(
      p.x * 1.5 +
      time * 0.25
    ) *
    0.10;


  float ribbon =
    smoothstep(
      0.15,
      0.85,
      viewDir.y +
      vertical
    );


  float intensity =
    bands *
    ribbon *
    visibility;


  // ----------------------------------------------------------
  // DEEP BLUE COLOR
  // ----------------------------------------------------------

  vec3 darkBlue =
    vec3(
      0.008,
      0.025,
      0.16
    );

  vec3 royalBlue =
    vec3(
      0.025,
      0.16,
      0.75
    );

  vec3 electricBlue =
    vec3(
      0.08,
      0.42,
      1.35
    );


  // Main deep-blue gradient.
  vec3 auroraColor =
    mix(
      darkBlue,
      royalBlue,
      intensity
    );


  // Brighter blue highlights.
  auroraColor =
    mix(
      auroraColor,
      electricBlue,
      intensity *
      intensity *
      0.65
    );


  // Soft luminous strength.
  auroraColor *=
    intensity *
    1.35;


  return auroraColor;
}


// ============================================================
// MAIN
// ============================================================

void main() {

#ifndef INSTANCING

  vec3 viewDir =
    normalize(
      v_worldPos
    );


  // ----------------------------------------------------------
  // ENVIRONMENT
  // ----------------------------------------------------------

  nl_environment env;

  env.end =
    false;

  env.nether =
    false;

  env.underwater =
    v_underwaterRainTimeDay.x >
    0.5;

  env.rainFactor =
    v_underwaterRainTimeDay.y;

  env.dayFactor =
    v_underwaterRainTimeDay.w;

  env.fogCol =
    FogColor.rgb;


  // Normal Newb environment.
  env =
    calculateSunParams(
      env,
      TimeOfDay.x
    );


  // ----------------------------------------------------------
  // BASE SKY
  // ----------------------------------------------------------

  nl_skycolor skycol =
    nlOverworldSkyColors(
      env
    );


  vec3 skyColor =
    nlRenderSky(
      skycol,
      env,
      -viewDir,
      v_underwaterRainTimeDay.z,
      true
    );


  // ----------------------------------------------------------
  // GOLDEN HOUR
  // ----------------------------------------------------------

  skyColor =
    goldenSky(
      skyColor,
      env.dayFactor,
      viewDir.y
    );


  // ----------------------------------------------------------
  // DEEP BLUE AURORA
  // ----------------------------------------------------------

  vec3 aurora =
    deepBlueAurora(
      viewDir,
      v_underwaterRainTimeDay.z,
      env.dayFactor,
      env.rainFactor
    );


  skyColor +=
    aurora;


  // ----------------------------------------------------------
  // OPTIONAL SHOOTING STARS
  // ----------------------------------------------------------

#ifdef NL_SHOOTING_STAR

  skyColor +=
    NL_SHOOTING_STAR *
    nlRenderShootingStar(
      viewDir,
      env.fogCol,
      v_underwaterRainTimeDay.z
    );

#endif


  // ----------------------------------------------------------
  // OPTIONAL GALAXY
  // ----------------------------------------------------------

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


  // ----------------------------------------------------------
  // COLOR CORRECTION
  // ----------------------------------------------------------

  skyColor =
    colorCorrection(
      skyColor
    );


  // ----------------------------------------------------------
  // OUTPUT
  // ----------------------------------------------------------

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
