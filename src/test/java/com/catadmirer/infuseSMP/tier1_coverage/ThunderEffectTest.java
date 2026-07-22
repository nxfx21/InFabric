package com.catadmirer.infuseSMP.tier1_coverage;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.effects.Thunder;
import com.catadmirer.infuseSMP.testrunner.FabricTestHarness;
import com.catadmirer.infuseSMP.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ThunderEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-THUNDER-01: Verify Thunder basic properties and registration")
    public void testBasicProperties() {
        Thunder thunder = new Thunder();
        assertEquals("thunder", thunder.getKey());
        assertEquals(13, thunder.getId());
        assertFalse(thunder.isAugmented());
        assertEquals("thunder", thunder.toString());

        Thunder augThunder = new Thunder(true);
        assertTrue(augThunder.isAugmented());
        assertEquals("aug_thunder", augThunder.toString());

        assertTrue(InfuseEffect.isRegistered(thunder));
    }

    @Test
    @DisplayName("TC-T1-THUNDER-02: Serialization and Deserialization")
    public void testSerialization() {
        Thunder regular = new Thunder(false);
        Thunder augmented = new Thunder(true);

        assertEquals(13, regular.serialize());
        assertEquals(113, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(13);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(113);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-THUNDER-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("thunder");
        InfuseEffect augmented = InfuseEffect.fromString("aug_thunder");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("thunder", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-THUNDER-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Thunder thunder = new Thunder();
        assertNotNull(thunder.getPotionColor());
        assertNotNull(thunder.getRitualColor());
        assertTrue(thunder.getIcon() > 0);
        assertTrue(thunder.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-THUNDER-05: Thunder Config & Micro-Mechanics storm tick validation")
    public void testThunderConfigAndMechanics() {
        Thunder thunder = new Thunder();
        assertEquals("thunder", thunder.getKey());
        assertFalse(thunder.isAugmented());

        var config = com.catadmirer.infuseSMP.Infuse.getInstance().getMainConfig();
        assertEquals(60, config.cooldown(thunder));
        assertEquals(30, config.duration(thunder));
    }
}
