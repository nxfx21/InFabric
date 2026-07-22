package com.catadmirer.infuseSMP.tier1_coverage;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.testrunner.FabricTestHarness;
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
        assertEquals("infusesmp", com.catadmirer.infuseSMP.Infuse.MOD_ID);
        assertNotNull(com.catadmirer.infuseSMP.Infuse.getInstance());
    }

    @Test
    @DisplayName("TC-T1-BUILD-05: Verify Effect ID Constants (1-15)")
    public void testEffectIdConstants() {
        assertEquals(1, com.catadmirer.infuseSMP.EffectIds.EMERALD);
        assertEquals(2, com.catadmirer.infuseSMP.EffectIds.ENDER);
        assertEquals(3, com.catadmirer.infuseSMP.EffectIds.FEATHER);
        assertEquals(4, com.catadmirer.infuseSMP.EffectIds.FIRE);
        assertEquals(5, com.catadmirer.infuseSMP.EffectIds.FROST);
        assertEquals(6, com.catadmirer.infuseSMP.EffectIds.HASTE);
        assertEquals(7, com.catadmirer.infuseSMP.EffectIds.HEART);
        assertEquals(8, com.catadmirer.infuseSMP.EffectIds.INVIS);
        assertEquals(9, com.catadmirer.infuseSMP.EffectIds.OCEAN);
        assertEquals(10, com.catadmirer.infuseSMP.EffectIds.REGEN);
        assertEquals(11, com.catadmirer.infuseSMP.EffectIds.SPEED);
        assertEquals(12, com.catadmirer.infuseSMP.EffectIds.STRENGTH);
        assertEquals(13, com.catadmirer.infuseSMP.EffectIds.THUNDER);
        assertEquals(14, com.catadmirer.infuseSMP.EffectIds.APOPHIS);
        assertEquals(15, com.catadmirer.infuseSMP.EffectIds.THIEF);
    }
}
