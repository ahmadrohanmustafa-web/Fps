package dev.voidfps.bench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FrameStatsTest {
    @Test
    void emptyIsZero() {
        FrameStats s = new FrameStats();
        assertEquals(0, s.count());
        assertEquals(0.0, s.mean(), 1e-9);
        assertEquals(0, s.min());
        assertEquals(0, s.percentile(1));
    }

    @Test
    void basicStats() {
        FrameStats s = new FrameStats();
        for (int v : new int[] {30, 40, 50, 60}) s.add(v);
        assertEquals(4, s.count());
        assertEquals(45.0, s.mean(), 1e-9);
        assertEquals(30, s.min());
        assertEquals(60, s.max());
        assertEquals(Math.sqrt(125.0), s.stdDev(), 1e-9);
    }

    @Test
    void percentileNearestRank() {
        FrameStats s = new FrameStats();
        for (int i = 1; i <= 100; i++) s.add(i);
        assertEquals(1, s.percentile(1));
        assertEquals(5, s.percentile(5));
        assertEquals(50, s.percentile(50));
        assertEquals(100, s.percentile(100));
    }

    @Test
    void growsPastInitialCapacityAndIgnoresNegative() {
        FrameStats s = new FrameStats();
        for (int i = 0; i < 1000; i++) s.add(60);
        s.add(-1);
        assertEquals(1000, s.count());
        assertEquals(60.0, s.mean(), 1e-9);
    }

    @Test
    void rejectsBadPercentile() {
        assertThrows(IllegalArgumentException.class, () -> new FrameStats().percentile(0));
    }
}
