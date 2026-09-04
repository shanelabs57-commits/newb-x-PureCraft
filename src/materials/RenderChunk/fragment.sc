$input v_color, v_texcoord0, v_lightmapUV

#include <bgfx_shader.sh>
#include <newb/main.sh>

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_SeasonsTexture);
SAMPLER2D_AUTOREG(s_LightMapTexture);


// ============================================================
// GOLDEN HOUR LIGHTING
// ============================================================

vec3 goldenHourLighting(
    vec3 color,
    vec2 lightUV
) {

    float skyLight =
        clamp(
            lightUV.y,
            0.0,
            1.0
        );


    float day =
        smoothstep(
            0.30,
            0.85,
            skyLight
        );


    vec3 warmSun =
        vec3(
            1.075,
            0.985,
            0.900
        );


    vec3 nightLift =
        vec3(
            1.045,
            1.035,
            1.015
        );


    color *=
        mix(
            nightLift,
            warmSun,
            day
        );


    float night =
        1.0 -
        day;


    color *=
        1.0 +
        0.075 *
        night;


    color *=
        1.0 +
        0.035 *
        day;


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
// DEPTH
// ============================================================

#if defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY)

    gl_FragColor =
        vec4(
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


    diffuse.rgb *=
        v_color.rgb;


// ============================================================
// ALPHA
// ============================================================

#ifdef ALPHA_TEST

    if (
        diffuse.a <
        0.5
    ) {

        discard;
    }

#endif


// ============================================================
// SEASONS
// ============================================================

#if defined(SEASONS)

    diffuse.rgb *=
        texture2D(
            s_SeasonsTexture,
            v_texcoord0
        ).rgb;

#endif


// ============================================================
// GOLDEN HOUR LIGHTING
// ============================================================

    diffuse.rgb =
        goldenHourLighting(
            diffuse.rgb,
            v_lightmapUV
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
