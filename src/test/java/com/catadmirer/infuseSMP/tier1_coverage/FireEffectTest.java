package com.catadmirer.infuseSMP.tier1_coverage;

import com.catadmirer.infuseSMP.effects.Fire;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.testrunner.FabricTestHarness;
import com.catadmirer.infuseSMP.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FireEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-FIRE-01: Verify Fire basic properties and registration")
    public void testBasicProperties() {
        Fire fire = new Fire();
        assertEquals("fire", fire.getKey());
        assertEquals(4, fire.getId());
        assertFalse(fire.isAugmented());
        assertEquals("fire", fire.toString());

        Fire augFire = new Fire(true);
        assertTrue(augFire.isAugmented());
        assertEquals("aug_fire", augFire.toString());

        assertTrue(InfuseEffect.isRegistered(fire));
    }

    @Test
    @DisplayName("TC-T1-FIRE-02: Serialization and Deserialization")
    public void testSerialization() {
        Fire regular = new Fire(false);
        Fire augmented = new Fire(true);

        assertEquals(4, regular.serialize());
        assertEquals(104, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(4);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(104);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-FIRE-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("fire");
        InfuseEffect augmented = InfuseEffect.fromString("aug_fire");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("fire", regular.getKey());
    }

    @Test
    @DisplayName("TC-T1-FIRE-04: Icon and Color resolution")
    public void testIconsAndColors() {
        Fire fire = new Fire();
        assertNotNull(fire.getPotionColor());
        assertNotNull(fire.getRitualColor());
        assertTrue(fire.getIcon() > 0);
        assertTrue(fire.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-FIRE-05: Fire Config & Micro-Mechanics validation")
    public void testFireConfigAndMechanics() {
        Fire fire = new Fire();
        assertEquals("fire", fire.getKey());
        assertFalse(fire.isAugmented());

        var config = com.catadmirer.infuseSMP.Infuse.getInstance().getMainConfig();
        assertEquals(60, config.cooldown(fire));
        assertEquals(30, config.duration(fire));
    }

    @Test
    @DisplayName("TC-T1-FIRE-06: Fire onMove and onEntityShootBow null safety")
    public void testFireMicroMechanicsNullSafety() {
        assertDoesNotThrow(() -> Fire.onMove(null));
        assertDoesNotThrow(() -> Fire.onEntityShootBow(null, null));
    }
}
