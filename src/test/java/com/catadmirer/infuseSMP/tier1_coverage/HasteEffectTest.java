package com.catadmirer.infuseSMP.tier1_coverage;

import com.catadmirer.infuseSMP.effects.Haste;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.testrunner.FabricTestHarness;
import com.catadmirer.infuseSMP.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HasteEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-HASTE-01: Verify Haste basic properties and registration")
    public void testBasicProperties() {
        Haste haste = new Haste();
        assertEquals("haste", haste.getKey());
        assertEquals(6, haste.getId());
        assertFalse(haste.isAugmented());
        assertEquals("haste", haste.toString());

        Haste augHaste = new Haste(true);
        assertTrue(augHaste.isAugmented());
        assertEquals("aug_haste", augHaste.toString());

        assertTrue(InfuseEffect.isRegistered(haste));
    }

    @Test
    @DisplayName("TC-T1-HASTE-02: Serialization and Deserialization")
    public void testSerialization() {
        Haste regular = new Haste(false);
        Haste augmented = new Haste(true);

        assertEquals(6, regular.serialize());
        assertEquals(106, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(6);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(106);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-HASTE-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("haste");
        InfuseEffect augmented = InfuseEffect.fromString("aug_haste");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("haste", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-HASTE-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Haste haste = new Haste();
        assertNotNull(haste.getPotionColor());
        assertNotNull(haste.getRitualColor());
        assertTrue(haste.getIcon() > 0);
        assertTrue(haste.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-HASTE-05: Haste Config & Micro-Mechanics enchantment levels validation")
    public void testHasteConfigAndMechanics() {
        Haste haste = new Haste();
        assertEquals("haste", haste.getKey());
        assertFalse(haste.isAugmented());

        var config = com.catadmirer.infuseSMP.Infuse.getInstance().getMainConfig();
        assertEquals(5, config.hasteFortuneLevel());
        assertEquals(10, config.hasteEfficiencyLevel());
        assertEquals(5, config.hasteUnbreakingLevel());
    }
}
