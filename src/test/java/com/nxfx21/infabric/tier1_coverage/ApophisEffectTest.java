package com.nxfx21.infabric.tier1_coverage;

import com.nxfx21.infabric.effects.Apophis;
import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.testrunner.FabricTestHarness;
import com.nxfx21.infabric.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApophisEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-APOPHIS-01: Verify Apophis basic properties and registration")
    public void testBasicProperties() {
        Apophis apophis = new Apophis();
        assertEquals("apophis", apophis.getKey());
        assertEquals(14, apophis.getId());
        assertFalse(apophis.isAugmented());
        assertEquals("apophis", apophis.toString());

        Apophis augApophis = new Apophis(true);
        assertTrue(augApophis.isAugmented());
        assertEquals("aug_apophis", augApophis.toString());

        assertTrue(InfuseEffect.isRegistered(apophis));
    }

    @Test
    @DisplayName("TC-T1-APOPHIS-02: Serialization and Deserialization")
    public void testSerialization() {
        Apophis regular = new Apophis(false);
        Apophis augmented = new Apophis(true);

        assertEquals(14, regular.serialize());
        assertEquals(114, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(14);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(114);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-APOPHIS-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("apophis");
        InfuseEffect augmented = InfuseEffect.fromString("aug_apophis");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("apophis", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-APOPHIS-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Apophis apophis = new Apophis();
        assertNotNull(apophis.getPotionColor());
        assertNotNull(apophis.getRitualColor());
        assertTrue(apophis.getIcon() > 0);
        assertTrue(apophis.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-APOPHIS-05: Apophis Config & Micro-Mechanics validation")
    public void testApophisConfigAndMechanics() {
        Apophis apophis = new Apophis();
        assertEquals("apophis", apophis.getKey());
        assertFalse(apophis.isAugmented());

        var config = com.nxfx21.infabric.Infuse.getInstance().getMainConfig();
        assertEquals(5, config.apophisLootingLevel());
        assertEquals(10.0, config.apophisLockDurationSeconds());
        assertNotNull(Apophis.APOPHIS_BOOST_ID);
        assertNotNull(Apophis.APOPHIS_SPARK_BOOST_ID);
    }
}
