package com.nxfx21.infabric.tier1_coverage;

import com.nxfx21.infabric.effects.Ender;
import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.testrunner.FabricTestHarness;
import com.nxfx21.infabric.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnderEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-ENDER-01: Verify Ender basic properties and registration")
    public void testBasicProperties() {
        Ender ender = new Ender();
        assertEquals("ender", ender.getKey());
        assertEquals(2, ender.getId());
        assertFalse(ender.isAugmented());
        assertEquals("ender", ender.toString());

        Ender augEnder = new Ender(true);
        assertTrue(augEnder.isAugmented());
        assertEquals("aug_ender", augEnder.toString());

        assertTrue(InfuseEffect.isRegistered(ender));
    }

    @Test
    @DisplayName("TC-T1-ENDER-02: Serialization and Deserialization")
    public void testSerialization() {
        Ender regular = new Ender(false);
        Ender augmented = new Ender(true);

        assertEquals(2, regular.serialize());
        assertEquals(102, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(2);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(102);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-ENDER-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("ender");
        InfuseEffect augmented = InfuseEffect.fromString("aug_ender");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("ender", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-ENDER-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Ender ender = new Ender();
        assertNotNull(ender.getPotionColor());
        assertNotNull(ender.getRitualColor());
        assertTrue(ender.getIcon() > 0);
        assertTrue(ender.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-ENDER-05: Ender Config & Micro-Mechanics validation")
    public void testEnderConfigAndMechanics() {
        Ender ender = new Ender();
        assertEquals("ender", ender.getKey());
        assertFalse(ender.isAugmented());
        
        var config = com.nxfx21.infabric.Infuse.getInstance().getMainConfig();
        assertEquals(10.0, config.enderPassiveRadius());
        assertEquals(15, config.enderSparkMaxDistance());
        assertTrue(config.enderOnehitMobs());
        assertTrue(config.enderCurseHit());

        java.util.UUID victimId = java.util.UUID.randomUUID();
        com.nxfx21.infabric.GlobalLoop.cursedPlayers.remove(victimId);
        assertFalse(com.nxfx21.infabric.GlobalLoop.cursedPlayers.contains(victimId));
    }

    @Test
    @DisplayName("TC-T1-ENDER-06: Ender null-safety and dragon breath guards")
    public void testEnderNullSafetyAndDragonBreath() {
        // Ensure static methods handle null inputs gracefully without throwing exceptions
        Ender.onAttack(null, null);
        Ender.applyEnderCurse(null, null);
        net.minecraft.util.ActionResult result = Ender.onUseDragonBreath(null, null, net.minecraft.util.Hand.MAIN_HAND);
        assertEquals(net.minecraft.util.ActionResult.PASS, result);
    }
}
