package com.nxfx21.infabric.tier1_coverage;

import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.effects.Invis;
import com.nxfx21.infabric.testrunner.FabricTestHarness;
import com.nxfx21.infabric.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InvisEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-INVIS-01: Verify Invis basic properties and registration")
    public void testBasicProperties() {
        Invis invis = new Invis();
        assertEquals("invis", invis.getKey());
        assertEquals(8, invis.getId());
        assertFalse(invis.isAugmented());
        assertEquals("invis", invis.toString());

        Invis augInvis = new Invis(true);
        assertTrue(augInvis.isAugmented());
        assertEquals("aug_invis", augInvis.toString());

        assertTrue(InfuseEffect.isRegistered(invis));
    }

    @Test
    @DisplayName("TC-T1-INVIS-02: Serialization and Deserialization")
    public void testSerialization() {
        Invis regular = new Invis(false);
        Invis augmented = new Invis(true);

        assertEquals(8, regular.serialize());
        assertEquals(108, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(8);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(108);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-INVIS-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("invis");
        InfuseEffect augmented = InfuseEffect.fromString("aug_invis");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("invis", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-INVIS-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Invis invis = new Invis();
        assertNotNull(invis.getPotionColor());
        assertNotNull(invis.getRitualColor());
        assertTrue(invis.getIcon() > 0);
        assertTrue(invis.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-INVIS-05: Invis Config & Vanish tracking set validation")
    public void testInvisConfigAndMechanics() {
        Invis invis = new Invis();
        assertEquals("invis", invis.getKey());
        assertFalse(invis.isAugmented());

        var config = com.nxfx21.infabric.Infuse.getInstance().getMainConfig();
        assertFalse(config.invisHideKills());
        assertFalse(config.invisHideDeaths());
        assertNotNull(Invis.activeVanish);
    }
}
