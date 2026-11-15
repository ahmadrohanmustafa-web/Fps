# VOID FPS — Development build report (0.2.0-dev.1)

## Status
This is a development branch, not a finished optimization release. The client still activates only the opt-in benchmark recorder. No rendering optimization or FPS improvement is claimed.

## Current runtime feature
| Feature | Status |
|---|---|
| Opt-in benchmark recorder (`-Dvoidfps.benchmark=true`) | Existing implementation; user has previously built and launched the 0.1.0 project locally |
| Chunk/block rendering optimizations | Not implemented |
| Entity-rendering optimizations | Not implemented |
| Rendering mixins | None verified in the inspected source tree |

## Development environment configured
- Minecraft 26.3
- Fabric Loader 0.19.5
- Fabric API 0.164.0+26.3 (aligned to the user's reported working installation)
- Java 25
- Gradle 9.6.0 (user-reported local environment)

The remote editing environment has not run Gradle or launched Minecraft. The updated branch must be built and tested on the user's machine before it can be called a working 0.2.0 build.

## Verified source and bytecode
- `VoidFpsClient` registers `BenchmarkRecorder` only.
- The checked `SectionCompiler.compile(SectionPos, RenderSectionRegion, VertexSorting, SectionBufferBuilderPack)` bytecode scans the 16×16×16 section positions, handles opacity/visibility, block entities, fluids, model tessellation and mesh construction.
- The checked `ModelBlockRenderer` uses reusable scratch objects and caches.
- See `docs/0.2.0-RENDERING-INVESTIGATION.md` for details and limits of the profiling evidence.

## Remaining validation
- Run `gradlew.bat build --console=plain` on the target JDK 25 / Gradle 9.6.0 setup.
- Confirm the generated JAR loads in Minecraft 26.3 with Fabric Loader 0.19.5 and Fabric API 0.164.0+26.3.
- Perform visual regression checks before enabling any rendering mixin.
- Measure vanilla and modded performance under the same repeatable scene and settings.

## Important
The development version number is not evidence of a completed optimization. Do not advertise an FPS gain until a real runtime change has been compiled, launched, and measured.
