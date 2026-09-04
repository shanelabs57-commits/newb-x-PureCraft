#ifndef CLOUDS_H
#define CLOUDS_H

#include "detection.h"
#include "noise.h"
#include "sky.h"

// ============================================================================
// SIMPLE CLOUDS 2D NOISE
// ============================================================================

float cloudNoise2D(vec2 p, highp float t, float rain) {
    t *= NL_CLOUD1_SPEED;

    p += t;
    p.y += 3.0 * sin(0.3 * p.x + 0.1 * t);

    vec2 p0 = floor(p);
    vec2 u = p - p0;

    u *= u * (3.0 - 2.0 * u);

    float n = mix(
        mix(rand(p0), rand(p0 + vec2(1.0, 0.0)), u.x),
        mix(rand(p0 + vec2(0.0, 1.0)), rand(p0 + vec2(1.0, 1.0)), u.x),
        u.y
    );

    n *= 0.5 +
         0.5 *
         sin(p.x * 0.6 - 0.5 * t) *
         sin(p.y * 0.6 + 0.8 * t);

    n = min(n * (1.0 + rain), 1.0);

    return n * n;
}


// ============================================================================
// SIMPLE CLOUDS
// ============================================================================

vec4 renderCloudsSimple(
    nl_skycolor skycol,
    vec3 pos,
    highp float t,
    float rain
) {
    pos.xz *= NL_CLOUD1_SCALE;

    float d = cloudNoise2D(
        pos.xz,
        t,
        rain
    );

    vec4 col = vec4(
        skycol.horizonEdge + skycol.zenith,
        smoothstep(0.1, 0.6, d)
    );

    col.rgb +=
        1.5 *
        dot(col.rgb, vec3(0.3, 0.4, 0.3)) *
        smoothstep(0.6, 0.2, d) *
        col.a;

    col.rgb *= 1.0 - 0.8 * rain;

    return col;
}


// ============================================================================
// ROUNDED CLOUDS
// ============================================================================

float cloudDf(
    vec3 pos,
    float rain,
    vec2 boxiness
) {
    boxiness *= 0.999;

    vec2 p0 = floor(pos.xz);

    vec2 u = max(
        (pos.xz - p0 - boxiness.x) /
        (1.0 - boxiness.x),
        0.0
    );

    u *= u * (3.0 - 2.0 * u);

    vec4 r = vec4(
        rand(p0),
        rand(p0 + vec2(1.0, 0.0)),
        rand(p0 + vec2(1.0, 1.0)),
        rand(p0 + vec2(0.0, 1.0))
    );

    r = smoothstep(
        0.1001 + 0.2 * rain,
        0.1 + 0.2 * rain * rain,
        r
    );

    float n = mix(
        mix(r.x, r.y, u.x),
        mix(r.w, r.z, u.x),
        u.y
    );

    n *=
        1.0 -
        1.5 *
        smoothstep(
            boxiness.y,
            2.0 - boxiness.y,
            2.0 * abs(pos.y - 0.5)
        );

    n = max(
        1.25 * (n - 0.2),
        0.0
    );

    n *= n * (3.0 - 2.0 * n);

    return n;
}


vec4 renderCloudsRounded(
    vec3 vDir,
    vec3 vPos,
    float rain,
    float time,
    vec3 horizonCol,
    vec3 zenithCol,
    const int steps,
    const float thickness,
    const float thickness_rain,
    const float speed,
    const vec2 scale,
    const float density,
    const vec2 boxiness
) {
    float height =
        7.0 * mix(
            thickness,
            thickness_rain,
            rain
        );

    float stepsf = float(steps);

    vec3 deltaP;

    deltaP.y = 1.0;

    deltaP.xz =
        height *
        scale *
        vDir.xz /
        (0.02 + 0.98 * abs(vDir.y));

    vec3 pos;

    pos.y = 0.0;

    pos.xz =
        scale *
        (
            vPos.xz +
            vec2(1.0, 0.5) *
            (time * speed)
        );

    pos += deltaP;

    deltaP /= -stepsf;

    vec2 d = vec2(0.0, 1.0);

    for (int i = 1; i <= steps; i++) {

        float m =
            cloudDf(
                pos,
                rain,
                boxiness
            );

        d.x += m;

        d.y =
            mix(
                d.y,
                pos.y,
                m
            );

        pos += deltaP;
    }

    d.x *= smoothstep(
        0.03,
        0.1,
        d.x
    );

    d.x /=
        (stepsf / density) +
        d.x;

    if (vPos.y < 0.0) {
        d.y = 1.0 - d.y;
    }

    vec4 col =
        vec4(
            zenithCol + horizonCol,
            d.x
        );

    col.rgb +=
        dot(
            col.rgb,
            vec3(0.3, 0.4, 0.3)
        ) *
        d.y *
        d.y;

    col.rgb *=
        1.0 -
        0.8 * rain;

    return col;
}


// ============================================================================
// CLOUD NOISE
// ============================================================================

float cloudsNoiseVr(
    vec2 p,
    float t
) {
    float n =
        fastVoronoi2(
            p + t,
            1.8
        );

    n *=
        fastVoronoi2(
            3.0 * p + t,
            1.5
        );

    n *=
        fastVoronoi2(
            9.0 * p + t,
            0.4
        );

    n *=
        fastVoronoi2(
            27.0 * p + t,
            0.1
        );

    return n * n;
}


// ============================================================================
// REALISTIC CLOUD RENDERING
// ============================================================================

vec4 renderClouds(
    vec2 p,
    float t,
    float rain,
    vec3 horizonCol,
    vec3 zenithCol,
    const vec2 scale,
    const float velocity,
    const float shadow
) {
    p *= scale;
    t *= velocity;

    float a =
        cloudsNoiseVr(
            p,
            t
        );

    float b =
        cloudsNoiseVr(
            p +
            NL_CLOUD3_SHADOW_OFFSET * scale,
            t
        );

    p =
        1.4 * p.yx +
        vec2(7.8, 9.2);

    t *= 0.5;

    float c =
        cloudsNoiseVr(
            p,
            t
        );

    float d =
        cloudsNoiseVr(
            p +
            NL_CLOUD3_SHADOW_OFFSET * scale,
            t
        );

    vec2 tr =
        vec2(0.6, 0.7) -
        0.12 * rain;

    a =
        smoothstep(
            tr.x,
            tr.y,
            a
        );

    c =
        smoothstep(
            tr.x,
            tr.y,
            c
        );

    b *=
        smoothstep(
            0.2,
            0.8,
            b
        );

    d *=
        smoothstep(
            0.2,
            0.8,
            d
        );

    vec4 col;

    col.a =
        a +
        c * (1.0 - a);

    col.rgb =
        horizonCol +
        horizonCol.ggg;

    col.rgb =
        mix(
            col.rgb,
            0.5 *
            (
                zenithCol +
                zenithCol.ggg
            ),
            shadow *
            mix(
                b,
                d,
                c
            )
        );

    col.rgb *=
        1.0 -
        0.7 * rain;

    return col;
}


// ============================================================================
// ENHANCED AURORA
// ============================================================================

#ifdef NL_AURORA

float auroraPow2(float x) {
    return x * x;
}


float auroraClamp01(float x) {
    return clamp(x, 0.0, 1.0);
}


float auroraSqrt(float x) {
    return sqrt(max(x, 0.0));
}


// ============================================================================
// AURORA GENERATOR
// ============================================================================

vec3 getAurora(
    vec3 vDir,
    float time,
    float dither
) {
    float VdotU =
        clamp(
            vDir.y,
            0.0,
            1.0
        );

    float visibility =
        auroraSqrt(
            auroraClamp01(
                VdotU * 4.5 -
                0.225
            )
        );

    visibility *=
        4.0 -
        VdotU * 0.9;

    if (visibility <= 1.0) {
        return vec3(0.0, 0.0, 0.0);
    }

    vec3 aurora =
        vec3(0.0, 0.0, 0.0);

    vec3 wpos =
        vDir;

    wpos.xz /=
        max(
            wpos.y,
            0.1
        );

    vec2 cameraPosM =
        vec2(0.0, 0.0);

    cameraPosM.x +=
        time * 10.0;

    const int sampleCount = 10;
    const int sampleCountP = 20;

    float ditherM =
        dither +
        10.0;

    float auroraAnimate = 0.0;


    for (
        int i = 0;
        i < sampleCount;
        i++
    ) {

        float current =
            auroraPow2(
                (
                    float(i) +
                    ditherM
                ) /
                float(sampleCountP)
            );

        vec2 planePos =
            wpos.xz *
            (
                0.8 +
                current
            ) *
            10.0 +
            cameraPosM;

        planePos *=
            0.0007;

        float noise =
            texture2D(
                noisevoxels,
                planePos
            ).r;

        noise =
            auroraPow2(
                auroraPow2(
                    auroraPow2(
                        auroraPow2(
                            1.0 -
                            0.8 *
                            abs(
                                noise -
                                0.5
                            )
                        )
                    )
                )
            );

        noise *=
            texture2D(
                noisevoxels,
                planePos * 8.0 +
                auroraAnimate
            ).b;

        noise *=
            texture2D(
                noisevoxels,
                planePos -
                auroraAnimate
            ).g;

        float currentM =
            1.0 -
            current;

        vec3 auroraDark =
            vec3(
                0.035,
                0.08,
                0.55
            );

        vec3 auroraBright =
            vec3(
                0.08,
                0.45,
                2.80
            );

        float colorMix =
            auroraPow2(
                auroraPow2(
                    currentM
                )
            );

        vec3 auroraColor =
            mix(
                auroraDark,
                auroraBright,
                colorMix
            );

        aurora +=
            noise *
            currentM *
            auroraColor;
    }

    aurora *=
        2.8;

    return
        aurora *
        visibility /
        float(sampleCount);
}


// ============================================================================
// AURORA INTEGRATION
// ============================================================================

vec4 renderAurora(
    vec3 p,
    float t,
    float rain,
    float dayFactor
) {
    t *=
        NL_AURORA_VELOCITY;

    p.xz *=
        NL_AURORA_SCALE;

    vec3 vDir =
        normalize(p);

    float dither =
        fract(
            sin(
                dot(
                    p.xz,
                    vec2(
                        12.9898,
                        78.233
                    )
                )
            ) *
            43758.5453
        );

    vec3 aurora =
        getAurora(
            vDir,
            t,
            dither
        );

    float mask =
        max(
            -dayFactor,
            0.0
        );

    mask *= mask;

    mask *=
        1.0 -
        0.8 * rain;

    aurora *=
        NL_AURORA *
        mask;

    float alpha =
        clamp(
            dot(
                aurora,
                vec3(
                    0.2126,
                    0.7152,
                    0.0722
                )
            ) *
            0.35,
            0.0,
            1.0
        );

    return vec4(
        aurora,
        alpha
    );
}

#endif


// ============================================================================
// CLOUD + AURORA REFLECTION
// ============================================================================

vec4 nlCloudAuroraReflection(
    nl_skycolor skycol,
    nl_environment env,
    vec3 viewDir,
    vec3 wPos,
    vec3 CAMERA_POS,
    highp float t
) {
    vec2 cloudPos =
        wPos.xz;

    cloudPos +=
        (
            187.0 -
            (
                wPos.y +
                CAMERA_POS.y
            )
        ) *
        viewDir.xz /
        viewDir.y;

    float fade =
        clamp(
            2.0 -
            0.005 *
            length(cloudPos),
            0.0,
            1.0
        );

    cloudPos +=
        CAMERA_POS.xz;

    vec4 refl =
        vec4(0.0, 0.0, 0.0, 0.0);


#ifdef NL_AURORA

    vec4 aurora =
        renderAurora(
            cloudPos.xyy,
            t,
            env.rainFactor,
            smoothstep(
                0.2,
                -0.2,
                env.dayFactor
            )
        );

    aurora.a *=
        fade;

    refl =
        vec4(
            2.0 *
            aurora.rgb *
            aurora.a,
            aurora.a
        );

#endif


#if NL_CLOUD_TYPE == 1

    vec4 clouds =
        renderCloudsSimple(
            skycol,
            cloudPos.xyy,
            t,
            env.rainFactor
        );

    clouds.a *=
        fade;

    refl =
        vec4(
            mix(
                refl.rgb,
                clouds.rgb,
                clouds.a
            ),
            min(
                refl.a +
                clouds.a,
                1.0
            )
        );

#endif

    return refl;
}

#endif
