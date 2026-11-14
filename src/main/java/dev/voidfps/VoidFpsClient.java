package dev.voidfps;

import dev.voidfps.bench.BenchmarkRecorder;
import net.fabricmc.api.ClientModInitializer;

/**
 * VOID FPS client entrypoint.
 *
 * Release 0.1.0: the only runtime feature is the opt-in benchmark recorder.
 * There are intentionally NO mixins in this release: rendering optimizations are added only after their
 * target methods have been read from the real Minecraft 26.3 jar (see docs/NEXT-STEPS.md).
 */
public final class VoidFpsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BenchmarkRecorder.registerIfEnabled();
    }
}
