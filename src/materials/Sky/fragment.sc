#ifndef INSTANCING
  $input v_worldPos, v_underwaterRainTimeDay
#endif

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/main.sh>

  uniform vec4 TimeOfDay;
  uniform vec4 FogColor;
  uniform vec4 FogAndDistanceControl;
#endif


// ============================================================
// SIMPLE HASH
// ============================================================

float skyHash(vec2 p)
{
  p = fract(
    p * vec2(
      127.1,
      311.7
    )
  );

  p += dot(
    p,
    p + 19.19
  );

  return fract(
    p.x * p.y
  );
}


// ============================================================
// SMOOTH CLOUD NOISE
// ============================================================

float skyNoise(vec2 p)
{
  vec2 i =
    floor(p);

  vec2 f =
    fract(p);

  f =
    f * f *
    (3.0 - 2.0 * f);


  float a =
    skyHash(i);

  float b =
    skyHash(
      i + vec2(1.0, 0.0)
    );

  float c =
    skyHash(
      i + vec2(0.0, 1.0)
    );

  float d =
    skyHash(
      i + vec2(1.0, 1.0)
    );


  return mix(
    mix(
      a,
      b,
      f.x
    ),
    mix(
      c,
      d,
      f.x
    ),
    f.y
  );
}


// ============================================================
// MULTI-LAYER CLOUD
// ============================================================

float skyCloud(vec2 p)
{
  float n = 0.0;

  n +=
    skyNoise(
      p * 0.75
    ) * 0.55;

  n +=
    skyNoise(
      p * 1.50
    ) * 0.30;

  n +=
    skyNoise(
      p * 3.00
    ) * 0.15;

  return n;
}


// ============================================================
// MAIN
// ============================================================

void main()
{
  #ifndef INSTANCING

    // --------------------------------------------------------
    // VIEW DIRECTION
    // --------------------------------------------------------

    vec3 viewDir =
      normalize(
        v_worldPos
      );


    // --------------------------------------------------------
    // ENVIRONMENT
    // --------------------------------------------------------

    nl_environment env;

    env.end =
      false;

    env.nether =
      false;

    env.underwater =
      v_underwaterRainTimeDay.x > 0.5;

    env.rainFactor =
      v_underwaterRainTimeDay.y;

    env.dayFactor =
      v_underwaterRainTimeDay.w;

    env.fogCol =
      FogColor.rgb;


    env =
      calculateSunParams(
        env,
        TimeOfDay.x
      );


    // --------------------------------------------------------
    // VANILLA / NEWB SKY
    // --------------------------------------------------------

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


    // ========================================================
    // CINEMATIC CLOUDS
    // ========================================================

    if (
      viewDir.y > 0.025 &&
      env.dayFactor > 0.0
    )
    {
      // ------------------------------------------------------
      // Perspective projection
      // ------------------------------------------------------

      vec2 cloudUV =
        viewDir.xz /
        max(
          viewDir.y,
          0.035
        );


      // ------------------------------------------------------
      // Cloud scale
      // ------------------------------------------------------

      cloudUV *=
        2.35;


      // ------------------------------------------------------
      // Wind movement
      // ------------------------------------------------------

      cloudUV +=
        vec2(
          v_underwaterRainTimeDay.z * 0.003,
          v_underwaterRainTimeDay.z * 0.001
        );


      // ------------------------------------------------------
      // Cloud noise
      // ------------------------------------------------------

      float cloudNoise =
        skyCloud(
          cloudUV
        );


      // ------------------------------------------------------
      // Soft cloud shape
      // ------------------------------------------------------

      float cloudMask =
        smoothstep(
          0.48,
          0.72,
          cloudNoise
        );


      // ------------------------------------------------------
      // Day/night fade
      // ------------------------------------------------------

      cloudMask *=
        env.dayFactor;


      // ------------------------------------------------------
      // Horizon fade
      // ------------------------------------------------------

      cloudMask *=
        smoothstep(
          0.035,
          0.16,
          viewDir.y
        );


      // ------------------------------------------------------
      // Cloud colors
      // ------------------------------------------------------

      vec3 cloudShadow =
        vec3(
          0.68,
          0.58,
          0.70
        );


      vec3 cloudWhite =
        vec3(
          1.00,
          0.98,
          0.94
        );


      vec3 cloudColor =
        mix(
          cloudShadow,
          cloudWhite,
          0.82
        );


      // ------------------------------------------------------
      // Blend clouds into sky
      // ------------------------------------------------------

      skyColor =
        mix(
          skyColor,
          cloudColor,
          cloudMask * 0.78
        );
    }


    // ========================================================
    // SHOOTING STARS
    // ========================================================

    #ifdef NL_SHOOTING_STAR

      skyColor +=
        NL_SHOOTING_STAR *
        nlRenderShootingStar(
          viewDir,
          env.fogCol,
          v_underwaterRainTimeDay.z
        );

    #endif


    // ========================================================
    // GALAXY STARS
    // ========================================================

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


    // ========================================================
    // COLOR CORRECTION
    // ========================================================

    skyColor =
      colorCorrection(
        skyColor
      );


    // ========================================================
    // OUTPUT
    // ========================================================

    gl_FragColor =
      vec4(
        skyColor,
        1.0
      );


  #else

    // --------------------------------------------------------
    // INSTANCING
    // --------------------------------------------------------

    gl_FragColor =
      vec4(
        0.0,
        0.0,
        0.0,
        0.0
      );

  #endif
}

