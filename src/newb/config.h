#ifndef NL_CONFIG_H
#define NL_CONFIG_H

/*
  NEWB SHADER MAIN CONFIG
  This part contains base configuration options for the shader.

  TOGGLES
  - If [toggle] is mentioned, then
    options can be commented to disable (by adding '//')
  - eg: #define PLANTS_WAVE    -> this is ON
        //#define PLANTS_WAVE  -> this is OFF

  COLOR VALUES
  - Color format: vec3(red,green,blue)
  - 1.0 means 100%, 0.0 means 0%
  - eg: vec3(1.0,1.0,0.0) = yellow

  VALUES
  - Values must be decimal
  - eg: 32 is wrong, 32.0 is correct

  TYPES
  - Should be integer. options to choose will be mentioned there
  - eg: #define NL_CLOUD_TYPE 2

  Remember to rebuild the shader after making changes.
*/

/* Color correction */
#define NL_TONEMAP_TYPE 3
#define NL_GAMMA 1.33
//#define NL_EXPOSURE 1.3
//#define NL_SATURATION 1.4
//#define NL_TINT
#define NL_TINT_LOW  vec3(0.3,0.5,1.4)
#define NL_TINT_HIGH vec3(1.4,0.7,0.3)

/* Lighting */
#define NL_SUNLIGHT_INTENSITY   3.3
#define NL_TORCHLIGHT_INTENSITY 1.0
#define NL_SHADOW_INTENSITY     0.7
#define NL_MIN_LIGHTING_BOOST   1.5
//#define NL_BLINKING_TORCH
#define NL_CLOUD_SHADOW

/* Ambient light for nether/end */
#define NL_NETHER_AMBIENT vec3(3.0,2.16,1.89)
#define NL_END_AMBIENT    vec3(1.98,1.25,2.3)

/* Sun/moon light color */
#define NL_DAWN_SUNLIGHT_COL   vec3(1.0,0.4,0.1)
#define NL_NOON_SUNLIGHT_COL   vec3(1.0,0.75,0.57)
#define NL_NIGHT_MOONLIGHT_COL vec3(0.01,0.03,0.2)

/* Torch colors */
#define NL_OVERWORLD_TORCH_COL  vec3(1.0,0.52,0.18)
#define NL_UNDERWATER_TORCH_COL vec3(1.0,0.52,0.18)
#define NL_NETHER_TORCH_COL     vec3(1.0,0.52,0.18)
#define NL_END_TORCH_COL        vec3(1.0,0.52,0.18)

/* Fog */
#define NL_FOG 1.0
#define NL_MIST_DENSITY 0.18
#define NL_RAIN_MIST_OPACITY 0.12
#define NL_CLOUDY_FOG 0.1

/* Sky */
#define NL_SKY_VOID_FACTOR     0.5
#define NL_SKY_VOID_DARKNESS   0.3
#define NL_SKY_RAIN_MIX_FACTOR 0.9

/* Sky colors */
#define NL_DAWN_ZENITH_COL   vec3(0.18,0.10,0.42)
#define NL_DAWN_HORIZON_COL  vec3(1.20,0.48,0.25)
#define NL_DAWN_EDGE_COL     vec3(1.35,0.62,0.18)

#define NL_DAY_ZENITH_COL    vec3(0.18,0.42,1.05)
#define NL_DAY_HORIZON_COL   vec3(0.72,0.88,1.05)
#define NL_DAY_EDGE_COL      vec3(0.92,0.95,0.88)

#define NL_NIGHT_ZENITH_COL  vec3(0.004,0.008,0.035)
#define NL_NIGHT_HORIZON_COL vec3(0.012,0.022,0.065)
#define NL_NIGHT_EDGE_COL    vec3(0.025,0.035,0.085)

#define NL_RAIN_ZENITH_COL   vec3(0.24,0.25,0.30)
#define NL_RAIN_HORIZON_COL  vec3(0.38,0.39,0.44)

#define NL_END_ZENITH_COL    vec3(0.08,0.001,0.1)
#define NL_END_HORIZON_COL   vec3(0.6,0.02,0.6)


/* Rainbow */
#define NL_RAINBOW
#define NL_RAINBOW_CLEAR 0.0
#define NL_RAINBOW_RAIN  0.4


/* Ore glow intensity */
#define NL_GLOW_TEX 2.3
#define NL_GLOW_SHIMMER 0.8
#define NL_GLOW_SHIMMER_SPEED 0.9
//#define NL_GLOW_LEAK 0.6


/* Waving */
#define NL_PLANTS_WAVE 0.05
#define NL_LANTERN_WAVE 0.16
#define NL_WAVE_SPEED 2.8
//#define NL_EXTRA_PLANTS_WAVE
#define NL_WAVE_RANGE 13.0


/* Water */
#define NL_WATER_TRANSPARENCY 0.72
#define NL_WATER_BUMP 0.075
#define NL_WATER_WAVE_SPEED 0.65
#define NL_WATER_TEX_OPACITY 0.22
#define NL_WATER_WAVE
//#define NL_WATER_REFL_MASK
#define NL_WATER_TINT vec3(0.42,0.78,0.72)


/* Underwater */
#define NL_UNDERWATER_BRIGHTNESS 0.8
#define NL_CAUSTIC_INTENSITY 1.9
#define NL_UNDERWATER_WAVE 0.1
#define NL_UNDERWATER_STREAKS 1.0
#define NL_UNDERWATER_TINT vec3(0.9,1.0,0.9)


/* Cloud type */
#define NL_CLOUD_TYPE 1 // 0:vanilla, 1:soft, 2:rounded, 3:realistic


/* Vanilla cloud settings */
#define NL_CLOUD0_THICKNESS 2.1
#define NL_CLOUD0_RAIN_THICKNESS 4.0
#define NL_CLOUD0_OPACITY 0.9
#define NL_CLOUD0_MULTILAYER


/* Soft cloud settings */
#define NL_CLOUD1_SCALE vec2(0.016, 0.022)
#define NL_CLOUD1_DEPTH 1.3
#define NL_CLOUD1_SPEED 0.04
#define NL_CLOUD1_DENSITY 0.54
#define NL_CLOUD1_OPACITY 0.9


/* Rounded cloud settings */
#define NL_CLOUD2_THICKNESS 2.1
#define NL_CLOUD2_RAIN_THICKNESS 2.5
#define NL_CLOUD2_STEPS 5
#define NL_CLOUD2_SCALE vec2(0.033, 0.033)
#define NL_CLOUD2_SHAPE vec2(0.5, 0.4)
#define NL_CLOUD2_DENSITY 25.0
#define NL_CLOUD2_VELOCITY 0.8

//#define NL_CLOUD2_LAYER2
#define NL_CLOUD2_LAYER2_OFFSET 143.0
#define NL_CLOUD2_LAYER2_THICKNESS 2.5
#define NL_CLOUD2_LAYER2_RAIN_THICKNESS 3.0
#define NL_CLOUD2_LAYER2_STEPS 3
#define NL_CLOUD2_LAYER2_SCALE vec2(0.03, 0.03)
#define NL_CLOUD2_LAYER2_SHAPE vec2(0.5, 0.4)
#define NL_CLOUD2_LAYER2_DENSITY 25.0
#define NL_CLOUD2_LAYER2_VELOCITY 0.8


/* Realistic cloud settings */
#define NL_CLOUD3_SCALE vec2(0.03, 0.03)
#define NL_CLOUD3_SPEED 0.005
#define NL_CLOUD3_SHADOW 0.9
#define NL_CLOUD3_SHADOW_OFFSET 0.3


/* ============================================================================
   ENHANCED DEEP BLUE AURORA
   Texture-based Aurora system
============================================================================ */

/*
 * Master toggle/intensity.
 *
 * Comment this line to disable Aurora.
 *
 * 0.4 = subtle
 * 1.0 = normal
 * 1.5 = bright
 * 3.0 = extremely bright
 */
#define NL_AURORA 1.2


/*
 * Aurora movement speed.
 */
#define NL_AURORA_VELOCITY 0.03


/*
 * Aurora scale.
 *
 * Lower value = larger formations.
 * Higher value = smaller/more detailed formations.
 */
#define NL_AURORA_SCALE 0.04


/*
 * Final brightness of the texture-based Aurora.
 */
#define NL_AURORA_BRIGHTNESS 2.8


/*
 * Internal animation speed.
 */
#define NL_AURORA_ANIMATION_SPEED 0.02


/*
 * Curtain sharpness multiplier.
 *
 * 0.6 = softer
 * 0.8 = recommended
 * 1.2 = sharper
 */
#define NL_AURORA_SHARPNESS 0.8


/*
 * Deep blue Aurora base color.
 *
 * Dark violet-blue.
 */
#define NL_AURORA_DARK_COLOR vec3(0.035, 0.08, 0.55)


/*
 * Deep blue Aurora bright color.
 *
 * Electric royal blue.
 */
#define NL_AURORA_BRIGHT_COLOR vec3(0.08, 0.45, 2.80)


/*
 * Rain fade.
 *
 * 0.0 = Aurora unaffected by rain
 * 0.8 = recommended
 * 1.0 = almost hidden during rain
 */
#define NL_AURORA_RAIN_FADE 0.8


/*
 * Aurora reflection.
 */
#define NL_CLOUD_AURORA_REFLECTION


/* Shooting star */
#define NL_SHOOTING_STAR 1.0
#define NL_SHOOTING_STAR_PERIOD 6.0
#define NL_SHOOTING_STAR_DELAY 64.0


/* Galaxy */
//#define NL_GALAXY_STARS 2.0
#define NL_GALAXY_VIBRANCE 0.7
#define NL_GALAXY_SPEED 0.03
#define NL_GALAXY_DAY_VISIBILITY 0.0


/* Chunk loading slide animation */
//#define NL_CHUNK_LOAD_ANIM 100.0


/* Sun/Moon */
#define NL_SUN_SIZE 1.0
#define NL_MOON_SIZE 1.0

#define NL_SUN_PATH_YAW 15.0
#define NL_MOON_PATH_YAW 17.0

#define NL_SUN_PATH_TILT 31.0
#define NL_MOON_PATH_TILT -28.0

#define NL_SUN_TILT 45.0
#define NL_MOON_TILT 45.0


/* Fake godrays */
//#define NL_GODRAY 0.3


/* Sky reflection */
//#define NL_GROUND_REFL 0.4

#define NL_GROUND_RAIN_WETNESS 1.0
#define NL_GROUND_RAIN_PUDDLES 0.7


/* Entity */
#define NL_ENTITY_BRIGHTNESS 0.65
#define NL_ENTITY_EDGE_HIGHLIGHT 0.41


/* Weather particles */
#define NL_WEATHER_SPECK 0.6
#define NL_WEATHER_RAIN_SLANT 4.0
#define NL_WEATHER_PARTICLE_SIZE 1.0


/* Lava effects */
#define NL_LAVA_NOISE
//#define NL_LAVA_NOISE_BUMP 0.2
#define NL_LAVA_NOISE_SPEED 0.2


/*
  NEWB SHADER SUBPACK CONFIG
*/


#ifdef LITE

  #define NO_WAVE

  #undef NL_GLOW_SHIMMER
  #undef NL_LAVA_NOISE
  #undef NL_WEATHER_SPECK
  #undef NL_SHOOTING_STAR
  #undef NL_CLOUD_AURORA_REFLECTION
  #undef NL_UNDERWATER_STREAKS
  #undef NL_RAIN_MIST_OPACITY
  #undef NL_CLOUDY_FOG
  #undef NL_ENTITY_EDGE_HIGHLIGHT

#endif


#ifdef NO_WAVE_NO_FOG

  #define NO_WAVE
  #define NO_FOG

#endif


#ifdef NO_FOG

  #undef NL_FOG

#endif


#ifdef NO_WAVE

  #undef NL_PLANTS_WAVE
  #undef NL_LANTERN_WAVE
  #undef NL_UNDERWATER_WAVE
  #undef NL_WATER_WAVE
  #undef NL_RAIN_MIST_OPACITY

#endif


#ifdef CHUNK_ANIM

  #define NL_CHUNK_LOAD_ANIM 100.0

#endif


#ifdef ROUNDED_CLOUDS

  #undef NL_CLOUD_TYPE
  #define NL_CLOUD_TYPE 2

  #undef NL_CLOUD_SHADOW

#endif


#ifdef BOX_CLOUDS

  #undef NL_CLOUD_TYPE
  #define NL_CLOUD_TYPE 0

  #undef NL_CLOUD_SHADOW

#endif


#ifdef REALISTIC_CLOUDS

  #undef NL_CLOUD_TYPE
  #define NL_CLOUD_TYPE 3

  #undef NL_CLOUD_SHADOW

#endif


#endif
