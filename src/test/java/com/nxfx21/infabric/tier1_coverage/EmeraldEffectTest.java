package com.nxfx21.infabric.tier1_coverage;

import com.nxfx21.infabric.effects.Emerald;
import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.testrunner.FabricTestHarness;
import com.nxfx21.infabric.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EmeraldEffectTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-EMERALD-01: Verify Emerald basic properties and registration")
    public void testBasicProperties() {
        Emerald emerald = new Emerald();
        assertEquals("emerald", emerald.getKey());
        assertEquals(1, emerald.getId());
        assertFalse(emerald.isAugmented());
        assertEquals("emerald", emerald.toString());

        Emerald augEmerald = new Emerald(true);
        assertTrue(augEmerald.isAugmented());
        assertEquals("aug_emerald", augEmerald.toString());

        assertTrue(InfuseEffect.isRegistered(emerald));
    }

    @Test
    @DisplayName("TC-T1-EMERALD-02: Serialization and Deserialization")
    public void testSerialization() {
        Emerald regular = new Emerald(false);
        Emerald augmented = new Emerald(true);

        assertEquals(1, regular.serialize());
        assertEquals(101, augmented.serialize());

        InfuseEffect deserializedRegular = InfuseEffect.deserialize(1);
        InfuseEffect deserializedAugmented = InfuseEffect.deserialize(101);

        assertNotNull(deserializedRegular);
        assertNotNull(deserializedAugmented);
        TestUtils.assertEffectEquals(regular, deserializedRegular);
        TestUtils.assertEffectEquals(augmented, deserializedAugmented);
    }

    @Test
    @DisplayName("TC-T1-EMERALD-03: String lookup via InfuseEffect.fromString")
    public void testFromString() {
        InfuseEffect regular = InfuseEffect.fromString("emerald");
        InfuseEffect augmented = InfuseEffect.fromString("aug_emerald");

        assertNotNull(regular);
        assertNotNull(augmented);
        assertFalse(regular.isAugmented());
        assertTrue(augmented.isAugmented());
        assertEquals("emerald", regular.getKey());
        assertEquals("emerald", augmented.getKey());
    }

    @Test
    @DisplayName("TC-T1-EMERALD-04: Lock status tracking (Emerald.isLocked)")
    public void testLockStatus() {
        UUID playerUUID = UUID.randomUUID();
        assertFalse(Emerald.isLocked(playerUUID));

        // Lock for 1 second
        Emerald.lockedPlayers.put(playerUUID, System.currentTimeMillis() + 1000L);
        assertTrue(Emerald.isLocked(playerUUID));

        // Expire lock
        Emerald.lockedPlayers.put(playerUUID, System.currentTimeMillis() - 100L);
        assertFalse(Emerald.isLocked(playerUUID));
    }

    @Test
    @DisplayName("TC-T1-EMERALD-05: Icon and Color resolution")
    public void testIconsAndColors() {
        Emerald emerald = new Emerald();
        assertNotNull(emerald.getPotionColor());
        assertNotNull(emerald.getRitualColor());
        assertTrue(emerald.getIcon() > 0);
        assertTrue(emerald.getActiveIcon() > 0);
    }

    @Test
    @DisplayName("TC-T1-EMERALD-06: Config & Enchantment bonus verification")
    public void testConfigAndEnchantmentBonus() {
        var config = com.nxfx21.infabric.Infuse.getInstance().getMainConfig();
        assertTrue(config.emeraldPreserveConsumables());
        assertTrue(config.emeraldEnchantBonus());
        assertTrue(config.emeraldExpPerHit() > 0);

        int[] powers = new int[]{10, 15, 20};
        int[] levels = new int[]{1, 2, 3};
        // Without emerald effect equipped, applyEnchantmentBonus should not mutate arrays
        Emerald.applyEnchantmentBonus(null, powers, levels);
        assertEquals(10, powers[0]);
        assertEquals(1, levels[0]);
    }

    @Test
    @DisplayName("TC-T1-EMERALD-07: Emerald null-safety and boundary conditions")
    public void testEmeraldNullSafety() {
        // Ensure static methods handle null inputs gracefully without throwing exceptions
        Emerald.onAttack(null, null);
        Emerald.onConsume(null, null);
        Emerald.applyEnchantmentBonus(null, null, null);
        Emerald.onTenHit(null, null);
        Emerald.cleanupInventory(null, null);
        assertFalse(Emerald.isLocked(null));
    }
}
