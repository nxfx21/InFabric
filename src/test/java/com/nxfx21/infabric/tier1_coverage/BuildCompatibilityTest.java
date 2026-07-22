package com.nxfx21.infabric.tier1_coverage;

import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.testrunner.FabricTestHarness;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BuildCompatibilityTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-BUILD-01: Verify Java runtime version is Java 21+")
    public void testJavaVersion() {
        int feature = Runtime.version().feature();
        assertTrue(feature >= 21, "Execution environment must run Java 21 or higher! Actual: Java " + feature);
    }

    @Test
    @DisplayName("TC-T1-BUILD-02: Verify all 15 effect IDs registered")
    public void testAllRegisteredEffects() {
        Map<Integer, InfuseEffect> registered = InfuseEffect.getRegisteredEffects();
        assertTrue(registered.size() >= 15, "All 15 effects must be registered!");

        Set<String> expectedKeys = Set.of(
            "emerald", "ender", "feather", "fire", "frost",
            "haste", "heart", "invis", "ocean", "regen",
            "speed", "strength", "thunder", "apophis", "thief"
        );

        for (String key : expectedKeys) {
            InfuseEffect effect = InfuseEffect.fromString(key);
            assertNotNull(effect, "Effect '" + key + "' must be retrievable from registry!");
            assertEquals(key, effect.getKey());
        }
    }

    @Test
    @DisplayName("TC-T1-BUILD-03: Verify Identifier keys and component constants")
    public void testIdentifierConstants() {
        assertEquals("infuse", InfuseEffect.EFFECT_KEY.getNamespace());
        assertEquals("effect_key", InfuseEffect.EFFECT_KEY.getPath());
        assertEquals("infuse", InfuseEffect.AUG_MODEL.getNamespace());
        assertEquals("aug", InfuseEffect.AUG_MODEL.getPath());
    }

    @Test
    @DisplayName("TC-T1-BUILD-04: Verify Mod ID and Mod Instance Initialization")
    public void testModInstance() {
        assertEquals("infabric", com.nxfx21.infabric.Infuse.MOD_ID);
        assertNotNull(com.nxfx21.infabric.Infuse.getInstance());
    }

    @Test
    @DisplayName("TC-T1-BUILD-05: Verify Effect ID Constants (1-15)")
    public void testEffectIdConstants() {
        assertEquals(1, com.nxfx21.infabric.EffectIds.EMERALD);
        assertEquals(2, com.nxfx21.infabric.EffectIds.ENDER);
        assertEquals(3, com.nxfx21.infabric.EffectIds.FEATHER);
        assertEquals(4, com.nxfx21.infabric.EffectIds.FIRE);
        assertEquals(5, com.nxfx21.infabric.EffectIds.FROST);
        assertEquals(6, com.nxfx21.infabric.EffectIds.HASTE);
        assertEquals(7, com.nxfx21.infabric.EffectIds.HEART);
        assertEquals(8, com.nxfx21.infabric.EffectIds.INVIS);
        assertEquals(9, com.nxfx21.infabric.EffectIds.OCEAN);
        assertEquals(10, com.nxfx21.infabric.EffectIds.REGEN);
        assertEquals(11, com.nxfx21.infabric.EffectIds.SPEED);
        assertEquals(12, com.nxfx21.infabric.EffectIds.STRENGTH);
        assertEquals(13, com.nxfx21.infabric.EffectIds.THUNDER);
        assertEquals(14, com.nxfx21.infabric.EffectIds.APOPHIS);
        assertEquals(15, com.nxfx21.infabric.EffectIds.THIEF);
    }
}
