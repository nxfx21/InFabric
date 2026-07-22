package com.catadmirer.infuseSMP.tier1_coverage;

import com.catadmirer.infuseSMP.effects.Feather;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.testrunner.FabricTestHarness;
import com.catadmirer.infuseSMP.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FeatherEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-FEATHER-01: Verify Feather basic properties and registration")
    public void testBasicProperties() {
        Feather feather = new Feather();
        assertEquals("feather", feather.getKey());
        assertEquals(3, feather.getId());
        assertFalse(feather.isAugmented());
        assertEquals("feather", feather.toString());

        Feather augFeather = new Feather(true);
        assertTrue(augFeather.isAugmented());
        assertEquals("aug_feather", augFeather.toString());

        assertTrue(InfuseEffect.isRegistered(feather));
    }

    @Test
    @DisplayName("TC-T1-FEATHER-02: Serialization and Deserialization")
    public void testSerialization() {
        Feather regular = new Feather(false);
        Feather augmented = new Feather(true);

        assertEquals(3, regular.serialize());
        assertEquals(103, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(3);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(103);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-FEATHER-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("feather");
        InfuseEffect augmented = InfuseEffect.fromString("aug_feather");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("feather", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-FEATHER-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Feather feather = new Feather();
        assertNotNull(feather.getPotionColor());
        assertNotNull(feather.getRitualColor());
        assertTrue(feather.getIcon() > 0);
        assertTrue(feather.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-FEATHER-05: Feather Config & Micro-Mechanics validation")
    public void testFeatherConfigAndMechanics() {
        Feather feather = new Feather();
        assertEquals("feather", feather.getKey());
        assertFalse(feather.isAugmented());

        var config = com.catadmirer.infuseSMP.Infuse.getInstance().getMainConfig();
        assertEquals(60, config.cooldown(feather));
        assertEquals(30, config.duration(feather));
    }
}
