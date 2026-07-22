package com.nxfx21.infabric.tier1_coverage;

import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.effects.Ocean;
import com.nxfx21.infabric.testrunner.FabricTestHarness;
import com.nxfx21.infabric.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OceanEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-OCEAN-01: Verify Ocean basic properties and registration")
    public void testBasicProperties() {
        Ocean ocean = new Ocean();
        assertEquals("ocean", ocean.getKey());
        assertEquals(9, ocean.getId());
        assertFalse(ocean.isAugmented());
        assertEquals("ocean", ocean.toString());

        Ocean augOcean = new Ocean(true);
        assertTrue(augOcean.isAugmented());
        assertEquals("aug_ocean", augOcean.toString());

        assertTrue(InfuseEffect.isRegistered(ocean));
    }

    @Test
    @DisplayName("TC-T1-OCEAN-02: Serialization and Deserialization")
    public void testSerialization() {
        Ocean regular = new Ocean(false);
        Ocean augmented = new Ocean(true);

        assertEquals(9, regular.serialize());
        assertEquals(109, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(9);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(109);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-OCEAN-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("ocean");
        InfuseEffect augmented = InfuseEffect.fromString("aug_ocean");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("ocean", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-OCEAN-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Ocean ocean = new Ocean();
        assertNotNull(ocean.getPotionColor());
        assertNotNull(ocean.getRitualColor());
        assertTrue(ocean.getIcon() > 0);
        assertTrue(ocean.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-OCEAN-05: Ocean Config & Micro-Mechanics pull parameters validation")
    public void testOceanConfigAndMechanics() {
        Ocean ocean = new Ocean();
        assertEquals("ocean", ocean.getKey());
        assertFalse(ocean.isAugmented());

        var config = com.nxfx21.infabric.Infuse.getInstance().getMainConfig();
        assertEquals(5, config.oceanPullRadius());
        assertEquals(0.5, config.oceanPullStrength());
        assertEquals(5, config.oceanPassiveDrownStrength());
        assertEquals(1, config.oceanPassiveDrownDamage());
    }
}
