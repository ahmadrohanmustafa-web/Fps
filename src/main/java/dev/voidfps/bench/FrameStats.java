package dev.voidfps.bench;

import java.util.Arrays;

/**
 * Collects per-second FPS samples and computes summary statistics.
 * Pure Java (no Minecraft classes) so it is unit-tested outside the game.
 */
public final class FrameStats {
    private int[] samples = new int[64];
    private int count;

    public void add(int fps) {
        if (fps < 0) {
            return;
        }
        if (count == samples.length) {
            samples = Arrays.copyOf(samples, count * 2);
        }
        samples[count++] = fps;
    }

    public int count() {
        return count;
    }

    public double mean() {
        if (count == 0) return 0;
        long sum = 0;
        for (int i = 0; i < count; i++) sum += samples[i];
        return (double) sum / count;
    }

    public int min() {
        if (count == 0) return 0;
        int m = Integer.MAX_VALUE;
        for (int i = 0; i < count; i++) m = Math.min(m, samples[i]);
        return m;
    }

    public int max() {
        if (count == 0) return 0;
        int m = Integer.MIN_VALUE;
        for (int i = 0; i < count; i++) m = Math.max(m, samples[i]);
        return m;
    }

    /** Population standard deviation of the per-second FPS values (lower = more consistent). */
    public double stdDev() {
        if (count == 0) return 0;
        double mean = mean();
        double acc = 0;
        for (int i = 0; i < count; i++) {
            double d = samples[i] - mean;
            acc += d * d;
        }
        return Math.sqrt(acc / count);
    }

    /** Nearest-rank percentile, p in (0,100]. percentile(1) is the "1% low" of the per-second samples. */
    public int percentile(double p) {
        if (p <= 0 || p > 100) throw new IllegalArgumentException("p must be in (0,100]");
        if (count == 0) return 0;
        int[] sorted = Arrays.copyOf(samples, count);
        Arrays.sort(sorted);
        int rank = (int) Math.ceil(p / 100.0 * count);
        return sorted[Math.max(0, rank - 1)];
    }

    public String summary() {
        return String.format(java.util.Locale.ROOT,
                "samples=%d avgFps=%.1f minFps=%d maxFps=%d p1Fps=%d p5Fps=%d stdDevFps=%.2f",
                count, mean(), min(), max(), percentile(1), percentile(5), stdDev());
    }
}
