# VOID FPS (0.2.0-dev.1 — DEVELOPMENT)

Fabric performance mod for **Minecraft Java 26.3**, aimed at low-end PCs (including Intel HD 520).

> **Honest status:** this is a development branch, not a finished performance release. It currently contains the opt-in benchmark recorder only; no rendering optimization is active yet. The 0.2.0 development work must add optimizations only after validating their exact Minecraft 26.3 behavior and testing them in-game.

## Requirements
- Windows 10 x64, **JDK 25**
- Minecraft Java 26.3
- Fabric Loader 0.19.5
- Fabric API **0.164.0+26.3**
- Gradle 9.6.0 (or the configured Gradle wrapper)

## Build (Windows)
1. Open a terminal in the project folder.
2. Run `gradlew.bat build --console=plain`.
3. The development JAR is produced under `build/libs/`.

## Install
1. Use a Fabric Loader 0.19.5 profile for Minecraft 26.3.
2. Install Fabric API 0.164.0+26.3.
3. Put the built VOID FPS JAR in `%APPDATA%\.minecraft\mods` (or the launcher profile's mods folder).
4. Launch the Fabric 26.3 profile.

## Benchmark recorder (opt-in)
Add this JVM argument to enable recording:

```
-Dvoidfps.benchmark=true
```

Optional warm-up override:

```
-Dvoidfps.benchmark.warmup=15
```

Results are written to `.minecraft\voidfps\` as CSV and summary text files. The recorder samples the game's FPS counter once per second; it does not provide true per-frame frametimes and does not itself improve FPS.

## Rendering optimization status
No rendering optimization is currently enabled. Vanilla already performs hidden-face culling, so VOID FPS must not duplicate it. The checked 26.3 bytecode shows that `SectionCompiler.compile(SectionPos, RenderSectionRegion, VertexSorting, SectionBufferBuilderPack)` traverses all 16×16×16 block positions, skips air, handles block entities and fluids, and calls `ModelBlockRenderer.tesselateBlock` for model blocks. That identifies a real hot-path candidate, but does not by itself prove which safe change will improve steady-state gameplay.

The next implementation must be supported by the actual target signatures and measurements, preserve lighting/AO/tint/transparency/fluid behavior, and be kept only if it passes the build and in-game regression tests.

## Important
This branch has not yet been compiled or launched as a 0.2.0 development build. Do not treat the version number as evidence of a completed optimization or FPS gain.
