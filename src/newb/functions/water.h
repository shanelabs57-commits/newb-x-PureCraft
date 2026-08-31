#ifndef WATER_H
#define WATER_H

#include "utils.h"
#include "detection.h"
#include "sky.h"
#include "clouds.h"
#include "noise.h"

// ============================================================
// NEWB X EARO - COMPLEMENTARY REIMAGINED INSPIRED WATER
// ============================================================

// Schlick Fresnel approximation
float calculateFresnel(float cosR, float r0) {
  float a = 1.0 - cosR;
  float a2 = a * a;
  return r0 + (1.0 - r0) * a2 * a2 * a;
}

// Subtle animated water highlight
float waterHighlight(vec3 p, float t) {
  float n1 = movingNoise2D(
    p.xz * 1.35,
    NL_WATER_WAVE_SPEED * t,
    0.55
  );

  float n2 = movingNoise2D(
    p.zx * 2.7 + vec2(4.7, 2.1),
    NL_WATER_WAVE_SPEED * t * 1.35,
    0.45
  );

  return smoothstep(0.48, 0.82, n1 * 0.65 + n2 * 0.35);
}

// ============================================================
// WATER
// ============================================================

vec4 nlWater(
  inout vec4 color,
  inout vec3 wPos,
  nl_skycolor skycol,
  nl_environment env,
  vec4 COLOR,
  vec3 viewDir,
  vec3 cPos,
  vec3 tiledCpos,
  vec3 gPos,
  vec3 CAMERA_POS,
  vec3 light,
  vec3 torchColor,
  vec2 lit,
  float fractCposY,
  float camDist,
  highp float t
) {

  // ----------------------------------------------------------
  // 1. WATER NORMAL / WAVES
  // ----------------------------------------------------------

  vec2 wavePos = gPos.xz + gPos.yy;

  vec2 bump;

  bump.x = movingNoise2D(
    wavePos,
    NL_WATER_WAVE_SPEED * t,
    0.62
  );

  bump.y = movingNoise2D(
    wavePos * 1.75 + vec2(3.2, 7.1),
    NL_WATER_WAVE_SPEED * t * 1.22,
    0.58
  );

  // Convert noise to centered wave values.
  bump = bump - 0.5;

  vec3 nrm;

  if (fractCposY > 0.0) {

    // Top water surface.
    nrm.x = bump.x * NL_WATER_BUMP;
    nrm.z = bump.y * NL_WATER_BUMP;
    nrm.y = -1.0;

  } else {

    // Side water surface.
    float sideWave =
      0.5 +
      0.5 * sin(
        3.0 * t * NL_WATER_WAVE_SPEED +
        cPos.y * PI_HALF
      );

    nrm.xz =
      normalize(viewDir.xz) +
      bump.y *
      (1.0 - viewDir.xz * viewDir.xz) *
      NL_WATER_BUMP *
      sideWave;

    nrm.y =
      bump.x *
      NL_WATER_BUMP;
  }

  nrm = normalize(nrm);

  // ----------------------------------------------------------
  // 2. REFLECTION VECTOR
  // ----------------------------------------------------------

  float cosR = dot(nrm, viewDir);

  viewDir =
    viewDir -
    2.0 * cosR * nrm;

  // ----------------------------------------------------------
  // 3. SKY REFLECTION
  // ----------------------------------------------------------

  vec3 waterRefl =
    nlRenderSky(
      skycol,
      env,
      viewDir,
      t,
      false
    );

  // Optional clouds / aurora reflection.
  #if defined(NL_CLOUD_AURORA_REFLECTION)

    if (viewDir.y < 0.0) {

      vec4 cloudRefl =
        nlCloudAuroraReflection(
          skycol,
          env,
          viewDir,
          wPos,
          CAMERA_POS,
          t
        );

      waterRefl =
        mix(
          waterRefl,
          cloudRefl.rgb,
          cloudRefl.a
        );
    }

  #endif

  // ----------------------------------------------------------
  // 4. FRESNEL
  // ----------------------------------------------------------

  cosR = abs(cosR);

  // Slightly stronger reflection than the original water.
  float fresnel =
    calculateFresnel(
      cosR,
      0.035
    );

  // Extra grazing-angle reflection.
  float grazing =
    pow(
      1.0 - cosR,
      3.0
    );

  fresnel =
    clamp(
      fresnel + grazing * 0.22,
      0.0,
      1.0
    );

  // ----------------------------------------------------------
  // 5. COMPLEMENTARY-STYLE WATER COLOR
  // ----------------------------------------------------------

  // Shallow water.
  vec3 shallowWater =
    vec3(
      0.055,
      0.34,
      0.43
    );

  // Deep water.
  vec3 deepWater =
    vec3(
      0.008,
      0.075,
      0.13
    );

  // Depth approximation based on viewing angle.
  float depthMask =
    smoothstep(
      0.05,
      0.90,
      1.0 - cosR
    );

  vec3 waterTint =
    mix(
      shallowWater,
      deepWater,
      depthMask
    );

  // ----------------------------------------------------------
  // 6. DAY / NIGHT WATER COLOR
  // ----------------------------------------------------------

  float day =
    clamp(
      env.dayFactor,
      0.0,
      1.0
    );

  // Slightly brighter daytime water.
  waterTint *=
    mix(
      vec3(0.72, 0.82, 0.90),
      vec3(1.00, 1.02, 1.04),
      day
    );

  // Night water becomes deeper blue.
  waterTint =
    mix(
      waterTint,
      waterTint * vec3(0.48, 0.62, 0.85),
      1.0 - day
    );

  // ----------------------------------------------------------
  // 7. WATER SURFACE COLOR
  // ----------------------------------------------------------

  float surfaceMask =
    smoothstep(
      0.0,
      0.75,
      cosR
    );

  vec3 surfaceColor =
    waterTint *
    (0.72 + 0.28 * surfaceMask);

  // Preserve original texture color.
  color.rgb *=
    surfaceColor *
    (0.78 + 0.22 * fresnel);

  // ----------------------------------------------------------
  // 8. SKY REFLECTION STRENGTH
  // ----------------------------------------------------------

  if (!env.end) {

    float reflectionLight =
      0.18 +
      lit.y * 0.95;

    // Stronger reflection at grazing angles.
    reflectionLight *=
      0.55 +
      0.90 * fresnel;

    waterRefl *= reflectionLight;

  }

  // ----------------------------------------------------------
  // 9. SOFT WATER HIGHLIGHTS
  // ----------------------------------------------------------

  float highlight =
    waterHighlight(
      gPos,
      t
    );

  // Sunlight highlight.
  float sunHighlight =
    max(
      0.0,
      dot(
        nrm,
        light
      )
    );

  sunHighlight =
    pow(
      sunHighlight,
      6.0
    );

  // Keep highlights subtle.
  vec3 highlightColor =
    vec3(
      0.72,
      0.88,
      0.96
    );

  float highlightStrength =
    highlight *
    sunHighlight *
    (0.18 + 0.32 * day);

  waterRefl +=
    highlightColor *
    highlightStrength;

  // ----------------------------------------------------------
  // 10. TORCH LIGHT REFLECTION
  // ----------------------------------------------------------

  float tc =
    0.5 +
    0.5 *
    sin(16.0 * viewDir.x) *
    sin(16.0 * viewDir.z);

  tc *= tc;

  waterRefl +=
    torchColor *
    NL_TORCHLIGHT_INTENSITY *
    lit.x *
    tc;

  // ----------------------------------------------------------
  // 11. WATER REFLECTION MASK
  // ----------------------------------------------------------

  #ifdef NL_WATER_REFL_MASK

    float mask =
      0.05 +
      0.05 *
      sin(viewDir.x * 12.0) *
      sin(viewDir.z * 6.0);

    waterRefl *=
      smoothstep(
        mask - 0.2,
        mask + 0.13,
        viewDir.y * viewDir.y
      );

  #endif

  // ----------------------------------------------------------
  // 12. TRANSPARENCY
  // ----------------------------------------------------------

  float opacity =
    1.0 - cosR;

  // More transparent when viewed straight down.
  color.a =
    mix(
      COLOR.a * NL_WATER_TRANSPARENCY,
      1.0,
      opacity * opacity
    );

  // Fresnel increases visible surface reflection.
  color.a =
    mix(
      color.a,
      1.0,
      fresnel * 0.32
    );

  // ----------------------------------------------------------
  // 13. WATER WAVES
  // ----------------------------------------------------------

  #ifdef NL_WATER_WAVE

    if (camDist < 14.0) {

      wPos.y -=
        0.5 *
        (bump.x + 0.5) *
        NL_WATER_BUMP;

    }

  #endif

  // ----------------------------------------------------------
  // 14. FINAL REFLECTION
  // ----------------------------------------------------------

  return vec4(
    waterRefl,
    fresnel
  );
}

#endif
