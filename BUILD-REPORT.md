# VOID FPS — Build Report (0.1.0)

## Overall status
**Source project only. NOT compiled. NOT launched in Minecraft 26.3. No FPS improvement claimed or measured.**
No `build-output/VOID-FPS.jar` exists because the build environment had no Gradle, no network and no Minecraft files.

## Environment of the author of this package
Java 21 runtime only (no Java 25, no Gradle, no Maven cache, no Minecraft/Fabric files, no network).
Dependency versions were taken from a 26.x baseline table verified on 2026-10-05, not resolved by a live build.

## Implemented runtime features
| Feature | Status |
|---|---|
| Opt-in benchmark recorder (`-Dvoidfps.benchmark=true`): per-second FPS, heap, process CPU → CSV + summary | Written; **needs 26.3 compile + in-game test** |
| Rendering optimizations (face culling, chunk compile, entity culling, …) | **NOT implemented.** No mixins exist in this release. |

## Verified here (actually executed)
- `FrameStats` (avg/min/max/percentile/std-dev) — 5 unit tests (`src/test/.../FrameStatsTest.java`) pass, run on Java 21
  with minimal JUnit stand-ins (the real JUnit run happens in your `gradle build`).
- `SystemSampler` — runs; heap and CPU values returned.
- `tools/Discover.java` — run against a **fake** Minecraft-shaped jar: finds classes, dumps signatures, extracts method
  bytecode, performs the API checks. Not yet run against the real 26.3 jar.
- `fabric.mod.json` — parses as valid JSON.

## NOT verified (needs your machine)
- That the project compiles (Gradle/Loom/Fabric API versions are from a table, not a live resolve).
- `Minecraft.getFps()` and `Minecraft.level` exist in 26.3 under those names (assumed from earlier versions; `Discover.java` checks).
- Fabric API event signatures `ClientTickEvents.END_CLIENT_TICK` and `ClientLifecycleEvents.CLIENT_STOPPING` in 0.161.0.
- Mod loads in 26.3; the recorder writes its files; metadata dependency ranges (`~26.3`) are accepted.
- Everything about performance.

## Known risks
- Loom plugin id/version (`net.fabricmc.fabric-loom` `1.17-SNAPSHOT`) and Gradle 9.6.0 are assumptions to confirm.
- Skill notes: on 26.3 some toolchains fail with `Unsupported class file major version` because of LWJGL 3.4.3's
  Java-27 multi-release classes; the cure is pinning the build-time LWJGL to 3.4.1. Plain Fabric Loom is reported
  to resolve 3.4.1 and build; if yours does not, add that pin.

## Deviations from the requested package
- `gradlew` / `gradlew.bat` / wrapper jar are **not included** (a binary jar cannot be produced offline).
  `setup-and-build.bat` generates them (needs Gradle installed once).
- No `build-output/VOID-FPS.jar`; the folder is created by the build script.
- License in `fabric.mod.json` is set to MIT as a placeholder — change it if you want something else.

## Limitations of the recorder
FPS is read from the game's once-per-second counter, so no true frame-time data; CPU is JVM-process load only.
