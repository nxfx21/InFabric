package com.catadmirer.infuseSMP.tier1_coverage;

import com.catadmirer.infuseSMP.effects.Heart;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.testrunner.FabricTestHarness;
import com.catadmirer.infuseSMP.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HeartEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-HEART-01: Verify Heart basic properties and registration")
    public void testBasicProperties() {
        Heart heart = new Heart();
        assertEquals("heart", heart.getKey());
        assertEquals(7, heart.getId());
        assertFalse(heart.isAugmented());
        assertEquals("heart", heart.toString());

        Heart augHeart = new Heart(true);
        assertTrue(augHeart.isAugmented());
        assertEquals("aug_heart", augHeart.toString());

        assertTrue(InfuseEffect.isRegistered(heart));
    }

    @Test
    @DisplayName("TC-T1-HEART-02: Serialization and Deserialization")
    public void testSerialization() {
        Heart regular = new Heart(false);
        Heart augmented = new Heart(true);

        assertEquals(7, regular.serialize());
        assertEquals(107, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(7);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(107);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-HEART-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("heart");
        InfuseEffect augmented = InfuseEffect.fromString("aug_heart");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("heart", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-HEART-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Heart heart = new Heart();
        assertNotNull(heart.getPotionColor());
        assertNotNull(heart.getRitualColor());
        assertTrue(heart.getIcon() > 0);
        assertTrue(heart.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-HEART-05: Heart Config & Micro-Mechanics modifier IDs validation")
    public void testHeartConfigAndMechanics() {
        Heart heart = new Heart();
        assertEquals("heart", heart.getKey());
        assertFalse(heart.isAugmented());

        assertNotNull(Heart.HEART_BOOST_ID);
        assertNotNull(Heart.HEART_SPARK_BOOST_ID);
        assertEquals("infusesmp", Heart.HEART_BOOST_ID.getNamespace());
        assertEquals("heart_boost", Heart.HEART_BOOST_ID.getPath());
    }
}
