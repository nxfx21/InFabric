package com.catadmirer.infuseSMP.tier1_coverage;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.effects.Regen;
import com.catadmirer.infuseSMP.testrunner.FabricTestHarness;
import com.catadmirer.infuseSMP.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegenEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-REGEN-01: Verify Regen basic properties and registration")
    public void testBasicProperties() {
        Regen regen = new Regen();
        assertEquals("regen", regen.getKey());
        assertEquals(10, regen.getId());
        assertFalse(regen.isAugmented());
        assertEquals("regen", regen.toString());

        Regen augRegen = new Regen(true);
        assertTrue(augRegen.isAugmented());
        assertEquals("aug_regen", augRegen.toString());

        assertTrue(InfuseEffect.isRegistered(regen));
    }

    @Test
    @DisplayName("TC-T1-REGEN-02: Serialization and Deserialization")
    public void testSerialization() {
        Regen regular = new Regen(false);
        Regen augmented = new Regen(true);

        assertEquals(10, regular.serialize());
        assertEquals(110, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(10);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(110);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-REGEN-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("regen");
        InfuseEffect augmented = InfuseEffect.fromString("aug_regen");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("regen", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-REGEN-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Regen regen = new Regen();
        assertNotNull(regen.getPotionColor());
        assertNotNull(regen.getRitualColor());
        assertTrue(regen.getIcon() > 0);
        assertTrue(regen.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-REGEN-05: Regen Config & Micro-Mechanics validation")
    public void testRegenConfigAndMechanics() {
        Regen regen = new Regen();
        assertEquals("regen", regen.getKey());
        assertFalse(regen.isAugmented());

        var config = com.catadmirer.infuseSMP.Infuse.getInstance().getMainConfig();
        assertTrue(config.regenCanAlwaysEat());
        assertEquals(60, config.cooldown(regen));
        assertEquals(30, config.duration(regen));
    }
}
