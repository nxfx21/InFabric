package com.catadmirer.infuseSMP.tier1_coverage;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.effects.Speed;
import com.catadmirer.infuseSMP.testrunner.FabricTestHarness;
import com.catadmirer.infuseSMP.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SpeedEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-SPEED-01: Verify Speed basic properties and registration")
    public void testBasicProperties() {
        Speed speed = new Speed();
        assertEquals("speed", speed.getKey());
        assertEquals(11, speed.getId());
        assertFalse(speed.isAugmented());
        assertEquals("speed", speed.toString());

        Speed augSpeed = new Speed(true);
        assertTrue(augSpeed.isAugmented());
        assertEquals("aug_speed", augSpeed.toString());

        assertTrue(InfuseEffect.isRegistered(speed));
    }

    @Test
    @DisplayName("TC-T1-SPEED-02: Serialization and Deserialization")
    public void testSerialization() {
        Speed regular = new Speed(false);
        Speed augmented = new Speed(true);

        assertEquals(11, regular.serialize());
        assertEquals(111, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(11);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(111);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-SPEED-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("speed");
        InfuseEffect augmented = InfuseEffect.fromString("aug_speed");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("speed", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-SPEED-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Speed speed = new Speed();
        assertNotNull(speed.getPotionColor());
        assertNotNull(speed.getRitualColor());
        assertTrue(speed.getIcon() > 0);
        assertTrue(speed.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-SPEED-05: Speed Config & Micro-Mechanics dash multiplier validation")
    public void testSpeedConfigAndMechanics() {
        Speed speed = new Speed();
        assertEquals("speed", speed.getKey());
        assertFalse(speed.isAugmented());

        var config = com.catadmirer.infuseSMP.Infuse.getInstance().getMainConfig();
        assertEquals(2, config.speedDashMultiplier());
        assertEquals(1.5, config.speedPlayerVelocityMultiplier());
    }
}
