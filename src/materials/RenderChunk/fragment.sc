$input v_color0, v_color1, v_fog, v_refl, v_texcoord0, v_lightmapUV, v_extra

#include <bgfx_shader.sh>
#include <newb/main.sh>

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_SeasonsTexture);
SAMPLER2D_AUTOREG(s_LightMapTexture);

/*
  PURECRAFT / NEWB-INSPIRED RENDERCHUNK

  Golden-hour terrain lighting:
  - Warm sunlight during bright/day conditions
  - Slightly lifted nights
  - Keeps existing Newb material effects
*/


// ============================================================
// GOLDEN HOUR LIGHTING
// ============================================================

vec3 goldenHourLighting(
  vec3 color,
  vec2 lightUV
) {

  // Sky light level
  float skyLight =
    clamp(
      lightUV.y,
      0.0,
      1.0
    );


  // Day factor
  float day =
    smoothstep(
      0.30,
      0.85,
      skyLight
    );


  // Warm daytime sunlight
  vec3 warmSun =
    vec3(
      1.075,
      0.985,
      0.900
    );


  // Slightly lifted night lighting
  vec3 nightLift =
    vec3(
      1.045,
      1.035,
      1.015
    );


  // Apply day/night tint
  color *=
    mix(
      nightLift,
      warmSun,
      day
    );


  // ==========================================================
  // NIGHT BRIGHTNESS
  // ==========================================================

  float night =
    1.0 - day;

  color *=
    1.0 +
    0.075 *
    night;


  // Small daytime brightness boost
  color *=
    1.0 +
    0.035 *
    day;


  // ==========================================================
  // SUBTLE CINEMATIC WARMTH
  // ==========================================================

  float luminance =
    dot(
      color,
      vec3(
        0.2126,
        0.7152,
        0.0722
      )
    );


  float warmMask =
    smoothstep(
      0.20,
      0.90,
      luminance
    ) *
    day;


  color +=
    vec3(
      0.018,
      0.008,
      -0.004
    ) *
    warmMask;


  return color;
}


// ============================================================
// MAIN
// ============================================================

void main() {


// ============================================================
// DEPTH / INSTANCING
// ============================================================

#if defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY) || defined(INSTANCING)

  gl_FragColor =
    vec4(
      1.0,
      1.0,
      1.0,
      1.0
    );

  return;

#endif


// ============================================================
// MATERIAL TEXTURE
// ============================================================

  vec4 diffuse =
    texture2D(
      s_MatTexture,
      v_texcoord0
    );


  vec4 color =
    v_color0;


// ============================================================
// ALPHA TEST
// ============================================================

#ifdef ALPHA_TEST

  if (diffuse.a < 0.6) {
    discard;
  }

#endif


// ============================================================
// SEASONS
// ============================================================

#if defined(SEASONS) && (defined(OPAQUE) || defined(ALPHA_TEST))

  diffuse.rgb *=
    mix(
      vec3(
        1.0,
        1.0,
        1.0
      ),

      texture2D(
        s_SeasonsTexture,
        v_color1.xy
      ).rgb * 2.0,

      v_color1.z
    );

#endif


// ============================================================
// NEWB GLOW
// ============================================================

  vec3 glow =
    nlGlow(
      s_MatTexture,
      v_texcoord0,
      v_extra.a
    );


// ============================================================
// MATERIAL COLOR CURVE
// ============================================================

  diffuse.rgb *=
    diffuse.rgb;


// ============================================================
// TRANSPARENT MATERIALS
// ============================================================

#if defined(TRANSPARENT) && !(defined(SEASONS) || defined(RENDER_AS_BILLBOARDS))

  if (v_extra.b > 0.9) {

    diffuse.rgb =
      vec3_splat(
        1.0 -
        NL_WATER_TEX_OPACITY *
        (
          1.0 -
          diffuse.b *
          1.8
        )
      );

    diffuse.a =
      color.a;
  }

#else

  diffuse.a =
    1.0;

#endif


// ============================================================
// VERTEX COLOR
// ============================================================

  diffuse.rgb *=
    color.rgb;


// ============================================================
// GLOW
// ============================================================

  diffuse.rgb +=
    glow;


// ============================================================
// WATER REFLECTION
// ============================================================

  if (v_extra.b > 0.9) {

    diffuse.rgb +=
      v_refl.rgb *
      v_refl.a;


  } else if (v_refl.a > 0.0) {

    float dy =
      abs(
        dFdy(
          v_extra.g
        )
      );


    if (dy < 0.0002) {

      float mask =
        v_refl.a *
        (
          clamp(
            v_extra.r * 10.0,
            8.2,
            8.8
          )
          -
          7.8
        );


      diffuse.rgb *=
        1.0 -
        0.6 *
        mask;


      diffuse.rgb +=
        v_refl.rgb *
        mask;
    }
  }


// ============================================================
// GOLDEN HOUR TERRAIN LIGHTING
// ============================================================

  diffuse.rgb =
    goldenHourLighting(
      diffuse.rgb,
      v_lightmapUV
    );


// ============================================================
// FOG
// ============================================================

  diffuse.rgb =
    mix(
      diffuse.rgb,
      v_fog.rgb,
      v_fog.a
    );


// ============================================================
// NEWB COLOR CORRECTION
// ============================================================

  diffuse.rgb =
    colorCorrection(
      diffuse.rgb
    );


// ============================================================
// OUTPUT
// ============================================================

  gl_FragColor =
    diffuse;
}
