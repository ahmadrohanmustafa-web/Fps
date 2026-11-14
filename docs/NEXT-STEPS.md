# From benchmark tool to real optimizations

Why no optimization shipped in 0.1.0: every rendering optimization needs a mixin into a specific Minecraft method,
and the 26.3 chunk/render code is not available to me offline. Guessing signatures produces mods that crash or
silently do nothing. The workflow below replaces guessing with reading.

## Step 1 — build and run the benchmark (README)
Get a vanilla-vs-VOID-FPS baseline on your laptop first (F3 FPS / CapFrameX for vanilla).

## Step 2 — run `java tools/Discover.java`
Produces `voidfps-target-report.txt` containing:
- which expected rendering classes exist (`SectionCompiler`, `ModelBlockRenderer`, `LevelRenderer`, …) and their
  real names in 26.3,
- every method signature in the chunk-compile package,
- the bytecode of vanilla `Block.shouldRenderFace`,
- whether the APIs the benchmark uses (`Minecraft.getFps()`, `Minecraft.level`) exist.

## Step 3 — decide what is worth doing (expected, not yet verified)
- **Hidden-face culling is probably already done by vanilla.** Vanilla Minecraft has long skipped faces covered by an
  opaque neighbor during chunk meshing (`Block.shouldRenderFace` with a cached shape comparison). The report's bytecode
  will confirm or refute this for 26.3. If confirmed, re-implementing it adds risk with no gain.
- More likely wins on an i5-6200U + HD 520: chunk rebuild/upload scheduling, per-frame allocations in the render path,
  entity-render culling. These need profiling data (use the Minecraft profiler / JFR) before any code.

## Step 4 — write each optimization against the report, then
1. add the mixin only for methods present in the report,
2. compare screenshots vanilla vs modded at fixed camera positions,
3. run the benchmark recorder before/after,
4. keep it only if FPS or consistency improves measurably; otherwise delete it.
