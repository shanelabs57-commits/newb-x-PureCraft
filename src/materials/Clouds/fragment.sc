$input v_color0

#if NL_CLOUD_TYPE >= 2
  $input v_color1, v_color2, v_dayFactor
#endif

#include <bgfx_shader.sh>
#include <newb/config.h>
#include <newb/main.sh>


// ============================================================================
// CAMERA
// ============================================================================

uniform vec4 CameraPosition;


// ============================================================================
// AURORA NOISE TEXTURE
//
// Ito ang texture na gagamitin ng bagong Aurora.
// Pangalan dapat tugma sa nasa fragment.sc.json.
// ============================================================================

uniform sampler2D noisevoxels;


// ============================================================================
// CLOUD PARAMETERS
// ============================================================================

#define NL_CLOUD_PARAMS(x) \
  NL_CLOUD2##x##STEPS, \
  NL_CLOUD2##x##THICKNESS, \
  NL_CLOUD2##x##RAIN_THICKNESS, \
  NL_CLOUD2##x##VELOCITY, \
  NL_CLOUD2##x##SCALE, \
  NL_CLOUD2##x##DENSITY, \
  NL_CLOUD2##x##SHAPE


// ============================================================================
// MAIN
// ============================================================================

void main() {

  // --------------------------------------------------------------------------
  // Default color
  // --------------------------------------------------------------------------

  vec4 color = v_color0;


  #if NL_CLOUD_TYPE >= 2


    // ------------------------------------------------------------------------
    // VIEW DIRECTION
    // ------------------------------------------------------------------------

    vec3 vDir =
      normalize(
        v_color0.xyz
      );


    // ------------------------------------------------------------------------
    // CLOUD WORLD POSITION
    // ------------------------------------------------------------------------

    vec3 cloudPos =
      v_color0.xyz;


    cloudPos.xz +=
      CameraPosition.xz;


    // ========================================================================
    // CLOUD TYPE 2
    // ROUNDED / VOLUMETRIC CLOUDS
    // ========================================================================

    #if NL_CLOUD_TYPE == 2


      // ----------------------------------------------------------------------
      // MAIN CLOUD LAYER
      // ----------------------------------------------------------------------

      color =
        renderCloudsRounded(
          vDir,
          cloudPos,
          v_color1.w,
          v_color2.w,
          v_color2.rgb,
          v_color1.rgb,
          NL_CLOUD_PARAMS(_)
        );


      // ----------------------------------------------------------------------
      // SECOND CLOUD LAYER
      // ----------------------------------------------------------------------

      #ifdef NL_CLOUD2_LAYER2

        vec2 parallax =
          vDir.xz /
          abs(vDir.y) *
          NL_CLOUD2_LAYER2_OFFSET;


        vec3 offsetPos =
          cloudPos;


        offsetPos.xz +=
          parallax;


        vec4 color2 =
          renderCloudsRounded(
            vDir,
            offsetPos,
            v_color1.a,
            v_color2.a * 2.0,
            v_color2.rgb,
            v_color1.rgb,
            NL_CLOUD_PARAMS(_LAYER2_)
          );


        color =
          mix(
            color2,
            color,
            0.2 +
            0.8 *
            color.a
          );

      #endif


      // ----------------------------------------------------------------------
      // ENHANCED TEXTURE-BASED AURORA
      // ----------------------------------------------------------------------

      #ifdef NL_AURORA

        vec4 aurora =
          renderAurora(
            cloudPos,
            v_color2.a,
            v_color1.a,
            v_dayFactor
          );


        /*
         * Aurora appears behind/in-between clouds.
         * Thick clouds hide more Aurora.
         */

        color +=
          aurora *
          (
            1.0 -
            0.95 *
            color.a
          );

      #endif


      // ----------------------------------------------------------------------
      // ORIGINAL SKY ALPHA
      // ----------------------------------------------------------------------

      color.a *=
        v_color0.a;


    // ========================================================================
    // CLOUD TYPE 3
    // FLAT / PROCEDURAL CLOUDS
    // ========================================================================

    #else


      // ----------------------------------------------------------------------
      // HEIGHT PARALLAX
      // ----------------------------------------------------------------------

      vDir.xz *=
        0.3 +
        v_color0.w;


      // ----------------------------------------------------------------------
      // CLOUD COORDINATES
      // ----------------------------------------------------------------------

      vec2 p =
        vDir.xz /
        (
          0.015 +
          0.035 *
          abs(vDir.y)
        );


      p +=
        0.035 *
        CameraPosition.xz;


      // ----------------------------------------------------------------------
      // RENDER CLOUDS
      // ----------------------------------------------------------------------

      vec4 clouds =
        renderClouds(
          p,
          v_color2.w,
          v_color1.w,
          v_color2.rgb,
          v_color1.rgb,
          NL_CLOUD3_SCALE,
          NL_CLOUD3_SPEED,
          NL_CLOUD3_SHADOW
        );


      color =
        clouds;


      // ----------------------------------------------------------------------
      // ENHANCED TEXTURE-BASED AURORA
      // ----------------------------------------------------------------------

      #ifdef NL_AURORA


        /*
         * Same positioning system used by the original Aurora.
         */

        p.xy *=
          34.7;


        vec4 aurora =
          renderAurora(
            p.xyy,
            v_color2.w,
            v_color1.w,
            v_dayFactor
          );


        /*
         * Clouds cover Aurora depending on cloud alpha.
         */

        color +=
          aurora *
          (
            1.0 -
            0.95 *
            color.a
          );


      #endif


      // ----------------------------------------------------------------------
      // CLOUD FADE
      // ----------------------------------------------------------------------

      color.a *=
        smoothstep(
          0.0,
          0.7,
          vDir.y
        );


    #endif


    // ========================================================================
    // FINAL COLOR CORRECTION
    // ========================================================================

    color.rgb =
      colorCorrection(
        color.rgb
      );


  #endif


  // ==========================================================================
  // FINAL OUTPUT
  // ==========================================================================

  gl_FragColor =
    color;

}
