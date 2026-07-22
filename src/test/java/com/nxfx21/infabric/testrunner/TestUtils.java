package com.nxfx21.infabric.testrunner;

import com.nxfx21.infabric.effects.InfuseEffect;
import java.util.UUID;

public class TestUtils {
    public static final UUID DUMMY_UUID_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID DUMMY_UUID_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID DUMMY_UUID_3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

    public static void assertEffectEquals(InfuseEffect expected, InfuseEffect actual) {
        if (expected == null && actual == null) return;
        if (expected == null || actual == null) {
            throw new AssertionError("Expected: " + expected + ", but actual: " + actual);
        }
        if (!expected.equals(actual)) {
            throw new AssertionError("Effect mismatch! Expected: " + expected + " (id=" + expected.getId() + ", aug=" + expected.isAugmented() + "), actual: " + actual + " (id=" + actual.getId() + ", aug=" + actual.isAugmented() + ")");
        }
    }
}
