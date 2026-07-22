package com.nxfx21.infabric.tier1_coverage;

import com.nxfx21.infabric.effects.Frost;
import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.testrunner.FabricTestHarness;
import com.nxfx21.infabric.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FrostEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-FROST-01: Verify Frost basic properties and registration")
    public void testBasicProperties() {
        Frost frost = new Frost();
        assertEquals("frost", frost.getKey());
        assertEquals(5, frost.getId());
        assertFalse(frost.isAugmented());
        assertEquals("frost", frost.toString());

        Frost augFrost = new Frost(true);
        assertTrue(augFrost.isAugmented());
        assertEquals("aug_frost", augFrost.toString());

        assertTrue(InfuseEffect.isRegistered(frost));
    }

    @Test
    @DisplayName("TC-T1-FROST-02: Serialization and Deserialization")
    public void testSerialization() {
        Frost regular = new Frost(false);
        Frost augmented = new Frost(true);

        assertEquals(5, regular.serialize());
        assertEquals(105, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(5);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(105);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-FROST-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("frost");
        InfuseEffect augmented = InfuseEffect.fromString("aug_frost");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("frost", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-FROST-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Frost frost = new Frost();
        assertNotNull(frost.getPotionColor());
        assertNotNull(frost.getRitualColor());
        assertTrue(frost.getIcon() > 0);
        assertTrue(frost.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-FROST-05: Frost Config & Micro-Mechanics validation")
    public void testFrostConfigAndMechanics() {
        Frost frost = new Frost();
        assertEquals("frost", frost.getKey());
        assertFalse(frost.isAugmented());

        var config = com.nxfx21.infabric.Infuse.getInstance().getMainConfig();
        assertEquals(60, config.cooldown(frost));
        assertEquals(30, config.duration(frost));
    }

    @Test
    @DisplayName("TC-T1-FROST-06: Frost onMove and Wind Charge freezing null safety")
    public void testFrostMicroMechanicsNullSafety() {
        assertDoesNotThrow(() -> Frost.onMove(null));
        assertDoesNotThrow(() -> Frost.onWindChargeHit(null, null));
        assertDoesNotThrow(() -> Frost.onWindChargeExplode(null, null, 4.0));
    }
}
