$input v_color, v_texcoord0, v_lightmapUV

#include <bgfx_shader.sh>
#include <newb/main.sh>

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_SeasonsTexture);


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

    // Warm sunlight
    vec3 warmSun =
        vec3(
            1.075,
            0.985,
            0.900
        );

    // Slightly brighter night
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

    // Small night brightness boost
    color *=
        1.0 +
        0.075 *
        night;

    // Small daytime brightness boost
    color *=
        1.0 +
        0.035 *
        day;

    // Cinematic warmth
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


// ============================================================
// VERTEX COLOR
// ============================================================

    diffuse.rgb *=
        v_color.rgb;

    diffuse.a *=
        v_color.a;


// ============================================================
// ALPHA TEST
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

    vec3 seasonsColor =
        texture2D(
            s_SeasonsTexture,
            v_texcoord0
        ).rgb;

    diffuse.rgb *=
        seasonsColor;

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
