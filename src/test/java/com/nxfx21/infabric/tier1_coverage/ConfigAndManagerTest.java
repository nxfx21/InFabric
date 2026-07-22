package com.nxfx21.infabric.tier1_coverage;

import com.nxfx21.infabric.effects.Emerald;
import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.managers.CooldownManager;

import com.nxfx21.infabric.testrunner.FabricTestHarness;
import com.nxfx21.infabric.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigAndManagerTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T1-MGR-01: CooldownManager duration and cooldown lifecycle")
    public void testCooldownManagerLifecycle() {
        UUID playerUUID = UUID.randomUUID();
        String effectKey = "emerald";

        assertFalse(CooldownManager.isOnCooldown(playerUUID, effectKey));
        assertFalse(CooldownManager.isEffectActive(playerUUID, effectKey));
        assertEquals(0L, CooldownManager.getCooldownTimeLeft(playerUUID, effectKey));
        assertEquals(0L, CooldownManager.getEffectTimeLeft(playerUUID, effectKey));

        // Set duration = 5s, cooldown = 10s (total cooldown = 15s)
        CooldownManager.setTimes(playerUUID, effectKey, 5, 10);

        assertTrue(CooldownManager.isEffectActive(playerUUID, effectKey));
        assertTrue(CooldownManager.isOnCooldown(playerUUID, effectKey));
        assertTrue(CooldownManager.getEffectTimeLeft(playerUUID, effectKey) > 0);
        assertTrue(CooldownManager.getCooldownTimeLeft(playerUUID, effectKey) > 0);

        // Clear specific duration
        CooldownManager.clearSpecificDuration(playerUUID, effectKey);
        assertFalse(CooldownManager.isEffectActive(playerUUID, effectKey));

        // Clear specific cooldown
        CooldownManager.clearSpecificCooldown(playerUUID, effectKey);
        assertFalse(CooldownManager.isOnCooldown(playerUUID, effectKey));

        // Test remove all
        CooldownManager.setTimes(playerUUID, effectKey, 10, 20);
        CooldownManager.removeAllCooldowns(playerUUID);
        assertFalse(CooldownManager.isOnCooldown(playerUUID, effectKey));
    }

    @Test
    @DisplayName("TC-T1-MGR-02: CooldownManager cleanup routines")
    public void testCooldownManagerCleanup() {
        UUID playerUUID = UUID.randomUUID();
        String effectKey = "fire";

        // Set 0 second duration & cooldown (expired immediately)
        CooldownManager.setDuration(playerUUID, effectKey, -1);
        CooldownManager.setCooldown(playerUUID, effectKey, -1);

        CooldownManager.cleanupExpiredDurations();
        CooldownManager.cleanupAllExpiredCooldowns();

        assertFalse(CooldownManager.isEffectActive(playerUUID, effectKey));
        assertFalse(CooldownManager.isOnCooldown(playerUUID, effectKey));
    }

    @Test
    @DisplayName("TC-T1-MGR-03: MainConfig getter defaults validation")
    public void testMainConfigDefaults() {
        var config = com.nxfx21.infabric.Infuse.getInstance().getMainConfig();
        assertNotNull(config);

        assertEquals(15, config.emeraldExpPerHit());
        assertTrue(config.emeraldPreserveConsumables());
        assertTrue(config.emeraldEnchantBonus());
        assertTrue(config.enderOnehitMobs());
        assertTrue(config.enderCurseHit());
        assertTrue(config.strengthLengthenShieldCooldown());
        assertTrue(config.strengthDoubleDamage());
        assertTrue(config.regenCanAlwaysEat());
    }

    @Test
    @DisplayName("TC-T1-MGR-04: HitTracker scheduled task execution")
    public void testHitTrackerScheduler() {
        var tracker = com.nxfx21.infabric.Infuse.getInstance().getHitTracker();
        assertNotNull(tracker);

        boolean[] executed = new boolean[1];
        tracker.scheduleTask(1L, () -> executed[0] = true);
        
        tracker.tick();
        assertTrue(executed[0], "Scheduled task on HitTracker must execute upon tick");
    }

    @Test
    @DisplayName("TC-T1-MGR-05: DataManager slot mapping logic")
    public void testDataManagerSlotMapping() {
        var dataManager = com.nxfx21.infabric.Infuse.getInstance().getDataManager();
        assertNotNull(dataManager);

        UUID testUUID = UUID.randomUUID();
        InfuseEffect emerald = InfuseEffect.fromString("emerald");
        InfuseEffect ender = InfuseEffect.fromString("ender");

        dataManager.setEffect(testUUID, "1", emerald);
        dataManager.setEffect(testUUID, "2", ender);

        assertTrue(dataManager.hasEffect(testUUID, emerald));
        assertTrue(dataManager.hasEffect(testUUID, ender));
        assertEquals(emerald, dataManager.getEffect(testUUID, "1"));
        assertEquals(ender, dataManager.getEffect(testUUID, "2"));

        dataManager.removeEffect(testUUID, "1");
        assertFalse(dataManager.hasEffect(testUUID, emerald));
    }

    @Test
    @DisplayName("TC-T1-MGR-06: InfuseCommandManager command registration including uninfuse")
    public void testUninfuseCommandRegistration() {
        var commandManager = new com.nxfx21.infabric.commands.InfuseCommandManager();
        com.mojang.brigadier.CommandDispatcher<net.minecraft.server.command.ServerCommandSource> dispatcher = new com.mojang.brigadier.CommandDispatcher<>();
        
        // Register commands on dispatcher via reflection/method invocation
        try {
            var method = com.nxfx21.infabric.commands.InfuseCommandManager.class.getDeclaredMethod("registerUninfuseCommand", com.mojang.brigadier.CommandDispatcher.class);
            method.setAccessible(true);
            method.invoke(commandManager, dispatcher);
            assertNotNull(dispatcher.getRoot().getChild("uninfuse"), "/uninfuse command must be registered in Brigadier dispatcher");
            assertNotNull(dispatcher.getRoot().getChild("uninfuse").getChild("slot"), "/uninfuse slot argument must be registered");
            assertNotNull(dispatcher.getRoot().getChild("uninfuse").getChild("target"), "/uninfuse target argument must be registered");
        } catch (Exception e) {
            fail("Failed to register /uninfuse command: " + e.getMessage());
        }
    }
}
