$input v_color0, v_color1, v_fog, v_refl, v_texcoord0, v_lightmapUV, v_extra

#include <bgfx_shader.sh>
#include <newb/main.sh>

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_SeasonsTexture);
SAMPLER2D_AUTOREG(s_LightMapTexture);


// ============================================================
// CINEMATIC CHUNK LIGHTING
// Soft Minecraft-trailer style lighting grade.
// Keeps nlLighting() as the real light source, then adds a
// subtle ambient lift + warm highlight response.
// ============================================================

vec3 cinematicChunkLighting(vec3 color, vec2 lightUV) {
  // Lightmap-derived sky/ambient level.
  float skyLight = clamp(lightUV.y, 0.0, 1.0);
  float blockLight = clamp(lightUV.x, 0.0, 1.0);

  // Lift dark areas without washing out the night.
  float ambientLift = 0.045 + skyLight * 0.055;

  // Soft local-light response.
  float localLight = smoothstep(0.25, 0.95, blockLight);

  // Warm highlights similar to golden-hour Minecraft renders.
  vec3 warm = vec3(1.045, 1.015, 0.965);
  float luminance = dot(color, vec3(0.2126, 0.7152, 0.0722));
  float highlight = smoothstep(0.35, 0.90, luminance) * (0.20 + 0.25 * skyLight);

  color += color * ambientLift;
  color *= mix(vec3_splat(1.0), warm, highlight * (0.55 + 0.25 * localLight));

  // Gentle contrast: preserve blacks while giving lit blocks more depth.
  color = mix(vec3_splat(0.0), color, 0.985);
  color *= 1.025;

  return color;
}

void main() {
  #if defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY) || defined(INSTANCING)
    gl_FragColor = vec4(1.0,1.0,1.0,1.0);
    return;
  #endif

  vec4 diffuse = texture2D(s_MatTexture, v_texcoord0);
  vec4 color = v_color0;

  #ifdef ALPHA_TEST
    if (diffuse.a < 0.6) {
      discard;
    }
  #endif

  #if defined(SEASONS) && (defined(OPAQUE) || defined(ALPHA_TEST))
    diffuse.rgb *= mix(vec3(1.0,1.0,1.0), texture2D(s_SeasonsTexture, v_color1.xy).rgb * 2.0, v_color1.z);
  #endif

  vec3 glow = nlGlow(s_MatTexture, v_texcoord0, v_extra.a);

  diffuse.rgb *= diffuse.rgb;

  #if defined(TRANSPARENT) && !(defined(SEASONS) || defined(RENDER_AS_BILLBOARDS))
    if (v_extra.b > 0.9) {
      diffuse.rgb = vec3_splat(1.0 - NL_WATER_TEX_OPACITY*(1.0 - diffuse.b*1.8));
      diffuse.a = color.a;
    }
  #else
    diffuse.a = 1.0;
  #endif

  diffuse.rgb *= color.rgb;
  diffuse.rgb += glow;

  if (v_extra.b > 0.9) {
    diffuse.rgb += v_refl.rgb*v_refl.a;
  } else if (v_refl.a > 0.0) {
    // reflective effect - only on xz plane
    float dy = abs(dFdy(v_extra.g));
    if (dy < 0.0002) {
      float mask = v_refl.a*(clamp(v_extra.r*10.0,8.2,8.8)-7.8);
      diffuse.rgb *= 1.0 - 0.6*mask;
      diffuse.rgb += v_refl.rgb*mask;
    }
  }

  // Cinematic lighting pass: after real chunk lighting/reflections,
  // before fog so distant fog keeps its own sky color.
  diffuse.rgb = cinematicChunkLighting(diffuse.rgb, v_lightmapUV);

  diffuse.rgb = mix(diffuse.rgb, v_fog.rgb, v_fog.a);

  diffuse.rgb = colorCorrection(diffuse.rgb);

  gl_FragColor = diffuse;
}

