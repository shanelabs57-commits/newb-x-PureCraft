#ifndef INSTANCING
$input v_texcoord0, v_posTime
#endif

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/main.sh>

  SAMPLER2D_AUTOREG(s_SkyTexture);
#endif

// ============================================================
// END SKY — VIOLET GRADIENT
// Adapted from the Shader Editor version to the existing
// Bedrock EndSky inputs. No resolution/gl_FragCoord needed.
// ============================================================

vec3 endSkyViolet(vec3 direction) {
  // Use the sky direction instead of screen UV so the gradient
  // works correctly with the existing skybox geometry.
  float y = clamp(direction.y * 0.5 + 0.5, 0.0, 1.0);

  vec3 bottomColor = vec3(0.10, 0.05, 0.20);
  vec3 topColor    = vec3(0.22, 0.12, 0.42);

  return mix(bottomColor, topColor, y);
}

void main() {
  #ifndef INSTANCING
    vec3 direction = normalize(v_posTime.xyz);

    // Violet End dimension gradient.
    vec3 color = endSkyViolet(direction);

    // Keep the existing EndSky texture/stars from the pack.
    vec4 diffuse = texture2D(s_SkyTexture, v_texcoord0);
    color += 2.8 * diffuse.rgb;

    color = colorCorrection(color);

    gl_FragColor = vec4(color, 1.0);
  #else
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
  #endif
}
