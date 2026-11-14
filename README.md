# VOID FPS (0.1.0 — UNCOMPILED, UNTESTED IN-GAME)

Fabric performance mod for **Minecraft Java 26.3**, aimed at low-end PCs (HP ProBook 450 G3 / Intel HD 520).

> **Honest status:** this source tree has **not been compiled** and the mod has **not been launched in Minecraft 26.3**.
> Release 0.1.0 contains **no rendering optimization yet**. Its only runtime feature is an opt-in benchmark
> recorder, so you can measure vanilla vs. modded performance, plus a tool that reads the real 26.3 jar so
> optimizations can be written against verified method signatures. See `BUILD-REPORT.md`.

## Requirements
- Windows 10 x64, **JDK 25** (`winget install EclipseAdoptium.Temurin.25.JDK`)
- **Gradle** once, only to create the wrapper (`winget install Gradle.Gradle`) — or open the folder in IntelliJ IDEA, which sets Gradle up itself
- Internet for the first build (downloads Minecraft, Fabric Loom, Fabric API)

## Build (Windows)
1. Unzip the project, open a terminal in the folder.
2. Run `setup-and-build.bat`.
3. Success prints `build-output\VOID-FPS.jar`. Failure: copy the red error text and send it back.

## Install
1. Install **Fabric Loader 0.19.5 or newer** for Minecraft 26.3 (fabricmc.net/use) and **Fabric API 0.161.0+26.3**.
2. Put `VOID-FPS.jar` in `%APPDATA%\.minecraft\mods` (or your launcher profile's mods folder).
3. Launch the Fabric 26.3 profile. Without extra settings the mod does nothing visible.

## Measure performance (the only active feature)
1. In your launcher profile add the JVM argument:  `-Dvoidfps.benchmark=true`
   (optional: `-Dvoidfps.benchmark.warmup=15` = seconds ignored after joining a world; default 10)
2. Join the **same world, same spot, same settings**, stand still ~2 minutes, quit.
3. Results appear in `.minecraft\voidfps\`: `benchmark-*.csv` (per second: FPS, heap MiB, process CPU %)
   and `benchmark-*.summary.txt` (average, min, 1%/5% low, std-dev).
4. The recorder lives inside the mod, so it cannot log a mod-free run. For a vanilla baseline use the same steps with
   F3 FPS or an external tool (CapFrameX / PresentMon). The recorder only logs; it does not change rendering.

Limitation: the game exposes FPS as a once-per-second integer, so this measures average/minimum FPS and
second-to-second consistency, **not** true per-frame times.

## Next step: make real optimizations (10 minutes of your time)
After the first successful build, run from the project folder:

    java tools/Discover.java

It reads the Minecraft 26.3 jar Gradle downloaded and writes `voidfps-target-report.txt`. Send that file back:
the first rendering optimizations get written against those exact signatures. Details: `docs/NEXT-STEPS.md`.

## Troubleshooting
- *Plugin `net.fabricmc.fabric-loom` / `1.17-SNAPSHOT` not found:* open https://maven.fabricmc.net/ and the Fabric
  template generator (fabricmc.net/develop/template), pick 26.3, and copy its `loom_version` into `gradle.properties`.
- *`fabric-api` artifact not found:* copy the current 26.3 version from modrinth.com/mod/fabric-api into `fabric_api_version`.
- *`Unsupported class file major version`:* the toolchain lags Minecraft's LWJGL 3.4.3; pin LWJGL to 3.4.1 for the build
  (BUILD-REPORT.md, "Known risks").
- *Compile error mentioning `getFps` or `level`:* Minecraft renamed it; `tools/Discover.java` reports whether they exist.
- *Gradle 9.6.0 download fails:* use the newest 9.x from gradle.org/releases in `setup-and-build.bat`.
