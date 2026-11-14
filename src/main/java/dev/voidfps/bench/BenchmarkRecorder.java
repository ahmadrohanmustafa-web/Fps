package dev.voidfps.bench;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Opt-in benchmark logger. Enabled ONLY by the JVM argument -Dvoidfps.benchmark=true.
 * No overlay, no keybind, no settings screen, no effect on rendering.
 *
 * Once per second while a world is loaded it records FPS (as reported by the game's own counter),
 * JVM heap use and process CPU load to  <game dir>/voidfps/benchmark-<time>.csv  and writes a
 * summary file when the game closes.
 *
 * Limitation: the game's FPS counter is a once-per-second integer, so this measures average/minimum
 * FPS and second-to-second consistency, NOT true per-frame times. For frame-time graphs use
 * an external tool such as CapFrameX or PresentMon.
 */
public final class BenchmarkRecorder {
    private static final long ONE_SECOND_NANOS = 1_000_000_000L;

    private final int warmupSeconds = Integer.getInteger("voidfps.benchmark.warmup", 10);
    private final FrameStats stats = new FrameStats();

    private BufferedWriter csv;
    private Path summaryPath;
    private long lastSampleNanos;
    private int secondsInWorld;
    private boolean wasInWorld;

    public static void registerIfEnabled() {
        if (!Boolean.getBoolean("voidfps.benchmark")) {
            return;
        }
        new BenchmarkRecorder().register();
    }

    private void register() {
        try {
            Path dir = FabricLoader.getInstance().getGameDir().resolve("voidfps");
            Files.createDirectories(dir);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path csvPath = dir.resolve("benchmark-" + stamp + ".csv");
            summaryPath = dir.resolve("benchmark-" + stamp + ".summary.txt");
            csv = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8);
            csv.write("second_in_world,fps,heap_used_mib,process_cpu_percent,counted\n");
            csv.flush();
        } catch (IOException e) {
            System.err.println("[VOID FPS] Could not open benchmark output, recorder disabled: " + e);
            return;
        }
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> finish());
        System.out.println("[VOID FPS] Benchmark recorder enabled (warm-up " + warmupSeconds + "s ignored in summary).");
    }

    private void onTick(Minecraft client) {
        boolean inWorld = client.level != null;
        if (!inWorld) {
            wasInWorld = false;
            return;
        }
        long now = System.nanoTime();
        if (!wasInWorld) {
            wasInWorld = true;
            lastSampleNanos = now;
            secondsInWorld = 0;
            return;
        }
        if (now - lastSampleNanos < ONE_SECOND_NANOS) {
            return;
        }
        lastSampleNanos = now;
        secondsInWorld++;

        int fps = client.getFps();
        boolean counted = secondsInWorld > warmupSeconds;
        if (counted) {
            stats.add(fps);
        }
        try {
            csv.write(String.format(Locale.ROOT, "%d,%d,%d,%.1f,%b%n",
                    secondsInWorld, fps, SystemSampler.heapUsedMiB(), SystemSampler.processCpuPercent(), counted));
            csv.flush();
        } catch (IOException e) {
            System.err.println("[VOID FPS] Benchmark write failed: " + e);
        }
    }

    private void finish() {
        try {
            if (csv != null) csv.close();
            if (summaryPath != null) {
                String text = "VOID FPS benchmark summary (warm-up " + warmupSeconds + "s excluded)\n"
                        + stats.summary() + "\n";
                Files.writeString(summaryPath, text, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("[VOID FPS] Could not finish benchmark files: " + e);
        }
    }
}
