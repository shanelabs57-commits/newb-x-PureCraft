#ifndef INSTANCING
  $input v_fogColor, v_worldPos, v_underwaterRainTimeDay, v_position
#endif

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/main.sh>
#endif

uniform vec4 FogColor;
uniform vec4 ViewPositionAndTime;
uniform vec4 TimeOfDay;

#ifndef INSTANCING
  SAMPLER2D_AUTOREG(s_noisevoxels);
#endif


// ============================================================
// Deep Blue Aurora
// Custom Aurora inspired by Unbound-style flowing ribbons.
// Uses noisevoxels for the animated shape.
// ============================================================

float auroraPow2(float x) {
  return x * x;
}

float auroraClamp01(float x) {
  return clamp(x, 0.0, 1.0);
}

float auroraSqrt(float x) {
  return sqrt(max(x, 0.0));
}


vec3 GetDeepBlueAurora(
  vec3 vDir,
  float time,
  float dither
) {

  // Sky visibility.
  float VdotU = clamp(vDir.y, 0.0, 1.0);

  float visibility =
    auroraSqrt(
      auroraClamp01(
        VdotU * 4.5 - 0.225
      )
    );

  visibility *= 4.0 - VdotU * 0.9;

  if (visibility <= 1.0)
    return vec3(0.0);


  vec3 aurora = vec3(0.0);

  vec3 wpos = vDir;

  wpos.xz /= max(wpos.y, 0.1);


  // Horizontal movement.
  vec2 cameraPosM = vec2(0.0);

  cameraPosM.x += time * 10.0;


  const int sampleCount = 10;
  const int sampleCountP = sampleCount + 10;


  float ditherM = dither + 10.0;


  // Keep the animation smooth.
  float auroraAnimate = time * 0.35;


  for (int i = 0; i < sampleCount; i++) {

    float current =
      auroraPow2(
        (float(i) + ditherM) /
        float(sampleCountP)
      );


    vec2 planePos =
      wpos.xz *
      (0.8 + current) *
      10.0 +
      cameraPosM;


    planePos *= 0.0007;


    // Main large-scale noise.
    float noise =
      texture2D(
        s_noisevoxels,
        planePos
      ).r;


    noise =
      auroraPow2(
        auroraPow2(
          auroraPow2(
            auroraPow2(
              1.0 -
              0.8 *
              abs(noise - 0.5)
            )
          )
        )
      );


    // Medium movement.
    noise *=
      texture2D(
        s_noisevoxels,
        planePos * 8.0 +
        auroraAnimate
      ).b;


    // Fine movement.
    noise *=
      texture2D(
        s_noisevoxels,
        planePos -
        auroraAnimate
      ).g;


    float currentM =
      1.0 - current;


    // ========================================================
    // Deep Blue Aurora Color
    // ========================================================

    vec3 deepBlue =
      vec3(
        0.015,
        0.08,
        0.65
      );


    vec3 brightBlue =
      vec3(
        0.05,
        0.45,
        1.0
      );


    vec3 auroraColor =
      mix(
        deepBlue,
        brightBlue,
        auroraPow2(
          auroraPow2(currentM)
        )
      );


    aurora +=
      noise *
      currentM *
      auroraColor;
  }


  // Overall Aurora strength.
  aurora *= 3.0;


  return
    aurora *
    visibility /
    float(sampleCount);
}


// ============================================================
// Main
// ============================================================

void main() {

#ifndef INSTANCING

  // ----------------------------------------------------------
  // View direction
  // ----------------------------------------------------------

  vec3 viewDir =
    normalize(v_worldPos);

  vec3 skyDir =
    normalize(-viewDir);


  // ----------------------------------------------------------
  // Environment
  // ----------------------------------------------------------

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


  // ----------------------------------------------------------
  // Normal Newb sky calculation
  // ----------------------------------------------------------

  env =
    calculateSunParams(
      env,
      TimeOfDay.x
    );


  nl_skycolor skycol =
    nlOverworldSkyColors(env);


  vec3 skyColor =
    nlRenderSky(
      skycol,
      env,
      skyDir,
      v_underwaterRainTimeDay.z,
      true
    );


  // ----------------------------------------------------------
  // Deep Blue Aurora
  // ----------------------------------------------------------

  float dither =
    fract(
      sin(
        dot(
          gl_FragCoord.xy,
          vec2(
            12.9898,
            78.233
          )
        )
      ) *
      43758.5453
    );


  float auroraTime =
    ViewPositionAndTime.w;


  vec3 aurora =
    GetDeepBlueAurora(
      skyDir,
      auroraTime,
      dither
    );


  // ----------------------------------------------------------
  // Aurora visibility
  // Night only.
  // Fade during sunrise/sunset and rain.
  // ----------------------------------------------------------

  float nightMask =
    smoothstep(
      0.05,
      0.85,
      1.0 - env.dayFactor
    );


  float rainMask =
    1.0 -
    clamp(
      env.rainFactor,
      0.0,
      1.0
    );


  float horizonMask =
    smoothstep(
      0.0,
      0.75,
      skyDir.y
    );


  float auroraMask =
    nightMask *
    rainMask *
    horizonMask;


  aurora *= auroraMask;


  // ----------------------------------------------------------
  // Add Aurora to the normal Newb sky.
  // ----------------------------------------------------------

  skyColor += aurora;


  // ----------------------------------------------------------
  // Shooting stars
  // ----------------------------------------------------------

#ifdef NL_SHOOTING_STAR

  skyColor +=
    NL_SHOOTING_STAR *
    nlRenderShootingStar(
      skyDir,
      env.fogCol,
      v_underwaterRainTimeDay.z
    );

#endif


  // ----------------------------------------------------------
  // Galaxy
  // ----------------------------------------------------------

#ifdef NL_GALAXY_STARS

  skyColor +=
    NL_GALAXY_STARS *
    nlRenderGalaxy(
      skyDir,
      env.fogCol,
      env,
      v_underwaterRainTimeDay.z
    );

#endif


  // ----------------------------------------------------------
  // Final color correction
  // ----------------------------------------------------------

  skyColor =
    colorCorrection(
      skyColor
    );


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
