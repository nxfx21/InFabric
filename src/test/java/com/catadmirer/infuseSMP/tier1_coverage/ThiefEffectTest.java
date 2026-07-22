package com.catadmirer.infuseSMP.tier1_coverage;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.effects.Thief;
import com.catadmirer.infuseSMP.testrunner.FabricTestHarness;
import com.catadmirer.infuseSMP.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ThiefEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-THIEF-01: Verify Thief basic properties and registration")
    public void testBasicProperties() {
        Thief thief = new Thief();
        assertEquals("thief", thief.getKey());
        assertEquals(15, thief.getId());
        assertFalse(thief.isAugmented());
        assertEquals("thief", thief.toString());

        Thief augThief = new Thief(true);
        assertTrue(augThief.isAugmented());
        assertEquals("aug_thief", augThief.toString());

        assertTrue(InfuseEffect.isRegistered(thief));
    }

    @Test
    @DisplayName("TC-T1-THIEF-02: Serialization and Deserialization")
    public void testSerialization() {
        Thief regular = new Thief(false);
        Thief augmented = new Thief(true);

        assertEquals(15, regular.serialize());
        assertEquals(115, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(15);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(115);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-THIEF-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("thief");
        InfuseEffect augmented = InfuseEffect.fromString("aug_thief");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("thief", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-THIEF-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Thief thief = new Thief();
        assertNotNull(thief.getPotionColor());
        assertNotNull(thief.getRitualColor());
        assertTrue(thief.getIcon() > 0);
        assertTrue(thief.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-THIEF-05: Thief Config & Micro-Mechanics validation")
    public void testThiefConfigAndMechanics() {
        Thief thief = new Thief();
        assertEquals("thief", thief.getKey());
        assertFalse(thief.isAugmented());

        var config = com.catadmirer.infuseSMP.Infuse.getInstance().getMainConfig();
        assertEquals(60, config.cooldown(thief));
        assertEquals(30, config.duration(thief));
    }
}
