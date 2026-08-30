#ifndef INSTANCING
  $input v_worldPos, v_underwaterRainTimeDay
#endif

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/main.sh>
  uniform vec4 TimeOfDay;
  uniform vec4 FogColor;
#endif

// Pure sky only.
// No clouds.h / no cloud rendering.
// Warm golden-hour grade is applied across the daytime sky.

vec3 goldenSky(vec3 skyColor, float dayFactor, float viewY) {
  float day = smoothstep(0.05, 0.95, dayFactor);

  // Warm morning/day palette.
  vec3 warmSun = vec3(1.075, 0.965, 0.885);

  // Slightly brighter night so the sky is still visible.
  vec3 nightLift = vec3(1.035, 1.025, 1.055);

  skyColor *= mix(nightLift, warmSun, day);

  // Soft golden tint strongest around the brighter daytime sky.
  float horizon = 1.0 - smoothstep(0.05, 0.85, abs(viewY));
  float goldenMask = day * (0.35 + 0.65 * horizon);

  skyColor += vec3(0.045, 0.018, -0.005) * goldenMask;

  // Small overall daytime lift.
  skyColor *= 1.0 + 0.035 * day;

  return skyColor;
}

void main() {
#ifndef INSTANCING

  vec3 viewDir = normalize(v_worldPos);

  nl_environment env;
  env.end = false;
  env.nether = false;
  env.underwater = v_underwaterRainTimeDay.x > 0.5;
  env.rainFactor = v_underwaterRainTimeDay.y;
  env.dayFactor = v_underwaterRainTimeDay.w;
  env.fogCol = FogColor.rgb;

  // Keep the normal Newb sun/environment calculation.
  env = calculateSunParams(env, TimeOfDay.x);

  nl_skycolor skycol = nlOverworldSkyColors(env);

  // Normal overworld sky only.
  // No clouds, no clouds.h, no cloud functions.
  vec3 skyColor = nlRenderSky(
    skycol,
    env,
    -viewDir,
    v_underwaterRainTimeDay.z,
    true
  );

  skyColor = goldenSky(
    skyColor,
    env.dayFactor,
    viewDir.y
  );

  // Preserve optional stars from the shader configuration.
#ifdef NL_SHOOTING_STAR
  skyColor += NL_SHOOTING_STAR *
    nlRenderShootingStar(
      viewDir,
      env.fogCol,
      v_underwaterRainTimeDay.z
    );
#endif

#ifdef NL_GALAXY_STARS
  skyColor += NL_GALAXY_STARS *
    nlRenderGalaxy(
      viewDir,
      env.fogCol,
      env,
      v_underwaterRainTimeDay.z
    );
#endif

  skyColor = colorCorrection(skyColor);

  gl_FragColor = vec4(skyColor, 1.0);

#else

  gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);

#endif
}
