package com.nxfx21.infabric.tier2_boundary;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.effects.Emerald;
import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.effects.Strength;
import com.nxfx21.infabric.managers.CooldownManager;
import com.nxfx21.infabric.testrunner.FabricTestHarness;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BoundaryCasesTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T2-BOUND-01: Null and unknown key handling in InfuseEffect.fromString")
    public void testNullAndUnknownKeys() {
        assertNull(InfuseEffect.fromString(null));
        assertNull(InfuseEffect.fromString("unknown_effect_xyz"));
        assertNull(InfuseEffect.fromString("aug_nonexistent"));
    }

    @Test
    @DisplayName("TC-T2-BOUND-02: Invalid serialized ID handling in InfuseEffect.deserialize")
    public void testInvalidSerializedIds() {
        assertNull(InfuseEffect.deserialize(99)); // ID 99 not registered
        assertNull(InfuseEffect.deserialize(199)); // ID 99 augmented not registered
        assertNull(InfuseEffect.deserialize(-1));
    }

    @Test
    @DisplayName("TC-T2-BOUND-03: Invalid Registration ID > 100 Guard")
    public void testInvalidRegistrationId() {
        InfuseEffect dummyInvalid = new InfuseEffect("invalid", 101, false, java.awt.Color.BLACK, net.minecraft.entity.boss.BossBar.Color.WHITE) {
            @Override public void equip(net.minecraft.server.network.ServerPlayerEntity owner) {}
            @Override public void unequip(net.minecraft.server.network.ServerPlayerEntity owner) {}
            @Override public void activateSpark(net.minecraft.server.network.ServerPlayerEntity owner) {}
            @Override public InfuseEffect getRegularVersion() { return this; }
            @Override public InfuseEffect getAugmentedVersion() { return this; }
            @Override public com.nxfx21.infabric.Message getName() { return null; }
            @Override public com.nxfx21.infabric.Message getLore() { return null; }
        };

        assertFalse(InfuseEffect.register(dummyInvalid), "Effect with ID > 100 must be rejected!");
    }

    @Test
    @DisplayName("TC-T2-BOUND-04: Cooldown negative/zero timestamp boundary")
    public void testCooldownNegativeBounds() {
        UUID playerUUID = UUID.randomUUID();
        CooldownManager.setCooldown(playerUUID, "speed", 0);
        assertFalse(CooldownManager.isOnCooldown(playerUUID, "speed"));
        assertEquals(0L, CooldownManager.getCooldownTimeLeft(playerUUID, "speed"));

        CooldownManager.setDuration(playerUUID, "speed", 0);
        assertFalse(CooldownManager.isEffectActive(playerUUID, "speed"));
        assertEquals(0L, CooldownManager.getEffectTimeLeft(playerUUID, "speed"));
    }

    @Test
    @DisplayName("TC-T2-BOUND-05: Emerald lock map boundary expiration")
    public void testEmeraldLockMapBoundary() {
        UUID playerUUID = UUID.randomUUID();
        long now = System.currentTimeMillis();

        // Lock set to exactly 1ms in the past
        Emerald.lockedPlayers.put(playerUUID, now - 1L);
        assertFalse(Emerald.isLocked(playerUUID));
        assertFalse(Emerald.lockedPlayers.containsKey(playerUUID), "Expired entry must be cleaned up from map");
    }

    @Test
    @DisplayName("TC-T2-BOUND-06: Equals and hashcode parity checks")
    public void testEqualsAndHashCode() {
        Emerald e1 = new Emerald(false);
        Emerald e2 = new Emerald(false);
        Emerald aug = new Emerald(true);

        assertEquals(e1, e2);
        assertNotEquals(e1, aug);
        assertNotEquals(e1, null);
        assertNotEquals(e1, "emerald_string");
    }

    @Test
    @DisplayName("TC-T2-BOUND-07: Strength HP boundary thresholds math")
    public void testStrengthHpBoundaries() {
        net.minecraft.server.network.ServerPlayerEntity player = org.mockito.Mockito.mock(net.minecraft.server.network.ServerPlayerEntity.class, org.mockito.Mockito.withSettings().stubOnly());
        UUID testUuid = UUID.randomUUID();
        org.mockito.Mockito.when(player.getUuid()).thenReturn(testUuid);
        Infuse.getInstance().getDataManager().setEffect(testUuid, "1", new Strength());

        float base = 5.0f;
        float[] testHps = {1.9f, 2.0f, 3.9f, 4.0f, 5.9f, 6.0f};
        float[] expectedExtra = {3.0f, 2.0f, 2.0f, 1.0f, 1.0f, 0.0f};

        for (int i = 0; i < testHps.length; i++) {
            org.mockito.Mockito.when(player.getHealth()).thenReturn(testHps[i]);
            float totalDamage = Strength.getExtraDamage(player, base);
            assertEquals(base + expectedExtra[i], totalDamage, "Hp boundary check for hp=" + testHps[i]);
        }
    }

    @Test
    @DisplayName("TC-T2-BOUND-08: Zero/Empty data boundaries")
    public void testEmptyDataBoundaries() {
        assertNull(InfuseEffect.fromString(""));
        assertNull(InfuseEffect.fromString("   "));
        assertNull(InfuseEffect.deserialize(0));
    }
}
