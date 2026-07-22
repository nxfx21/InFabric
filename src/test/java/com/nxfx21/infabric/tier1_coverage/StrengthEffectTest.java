package com.nxfx21.infabric.tier1_coverage;

import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.effects.Strength;
import com.nxfx21.infabric.managers.CooldownManager;
import com.nxfx21.infabric.testrunner.FabricTestHarness;
import com.nxfx21.infabric.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class StrengthEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-STRENGTH-01: Verify Strength basic properties and registration")
    public void testBasicProperties() {
        Strength strength = new Strength();
        assertEquals("strength", strength.getKey());
        assertEquals(12, strength.getId());
        assertFalse(strength.isAugmented());
        assertEquals("strength", strength.toString());

        Strength augStrength = new Strength(true);
        assertTrue(augStrength.isAugmented());
        assertEquals("aug_strength", augStrength.toString());

        assertTrue(InfuseEffect.isRegistered(strength));
    }

    @Test
    @DisplayName("TC-T1-STRENGTH-02: Serialization and Deserialization")
    public void testSerialization() {
        Strength regular = new Strength(false);
        Strength augmented = new Strength(true);

        assertEquals(12, regular.serialize());
        assertEquals(112, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(12);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(112);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-STRENGTH-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("strength");
        InfuseEffect augmented = InfuseEffect.fromString("aug_strength");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("strength", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-STRENGTH-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Strength strength = new Strength();
        assertNotNull(strength.getPotionColor());
        assertNotNull(strength.getRitualColor());
        assertTrue(strength.getIcon() > 0);
        assertTrue(strength.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-STRENGTH-05: Strength Config & Micro-Mechanics damage math")
    public void testStrengthConfigAndMechanics() {
        Strength strength = new Strength();
        assertEquals("strength", strength.getKey());
        assertFalse(strength.isAugmented());

        var config = com.nxfx21.infabric.Infuse.getInstance().getMainConfig();
        assertTrue(config.strengthLengthenShieldCooldown());
        assertTrue(config.strengthDoubleDamage());

        // Test actual Strength.applySparkAutoCrit method with active spark
        UUID testUUID = UUID.randomUUID();
        CooldownManager.setTimes(testUUID, "strength", 30, 60);
        assertTrue(Strength.shouldAutoCrit(testUUID));
        assertEquals(13.5f, Strength.applySparkAutoCrit(testUUID, 10.0f), 0.001f);
    }
}
