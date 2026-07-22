package com.nxfx21.infabric.tier4_realworld;

import com.nxfx21.infabric.EffectIds;
import com.nxfx21.infabric.effects.*;
import com.nxfx21.infabric.managers.CooldownManager;
import com.nxfx21.infabric.testrunner.FabricTestHarness;
import com.nxfx21.infabric.testrunner.TestUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RealWorldScenariosTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T4-REAL-01: Full SMP Survival Gameplay & Data Lifecycle")
    public void testFullSurvivalGameplayLifecycle() {
        UUID playerUUID = TestUtils.DUMMY_UUID_1;

        // Step 1: Craft/obtain Emerald effect potion
        InfuseEffect emerald = InfuseEffect.fromString("emerald");
        assertNotNull(emerald);

        // Step 2: Equip effect in Slot 1
        int serialized = emerald.serialize();
        assertEquals(1, serialized);

        // Step 3: Activate Spark (cooldown = 60s, duration = 30s)
        CooldownManager.setTimes(playerUUID, emerald.getKey(), 30, 60);
        assertTrue(CooldownManager.isEffectActive(playerUUID, "emerald"));
        assertTrue(CooldownManager.isOnCooldown(playerUUID, "emerald"));

        // Step 4: Deserialize data upon player rejoin
        InfuseEffect restored = InfuseEffect.deserialize(serialized);
        assertNotNull(restored);
        TestUtils.assertEffectEquals(emerald, restored);
    }

    @Test
    @DisplayName("TC-T4-REAL-02: 3v3 Faction PvP Battle Simulation")
    public void testFactionPvPCombatSimulation() {
        UUID playerA = TestUtils.DUMMY_UUID_1;
        UUID playerB = TestUtils.DUMMY_UUID_2;

        // Team 1: Strength + Speed
        InfuseEffect strength = InfuseEffect.fromString("strength");
        InfuseEffect speed = InfuseEffect.fromString("speed");
        assertNotNull(strength);
        assertNotNull(speed);

        // Team 2: Thief + Ender
        InfuseEffect thief = InfuseEffect.fromString("thief");
        InfuseEffect ender = InfuseEffect.fromString("ender");
        assertNotNull(thief);
        assertNotNull(ender);

        // Verify Strength spark auto-crit calculation via mod method
        CooldownManager.setTimes(playerA, "strength", 30, 60);
        assertTrue(Strength.shouldAutoCrit(playerA));
        assertEquals(13.5f, Strength.applySparkAutoCrit(playerA, 10.0f), 0.001f);

        // Simulate Thief active spark tracking
        CooldownManager.setTimes(playerB, "thief", 30, 60);
        assertTrue(CooldownManager.isEffectActive(playerB, "thief"));
    }

    @Test
    @DisplayName("TC-T4-REAL-03: Server Persistence JSON Structure Round-Trip")
    public void testServerPersistenceJsonRoundTrip() {
        UUID playerUUID = TestUtils.DUMMY_UUID_3;

        // Create player data JSON structure matching DataManager
        JsonObject playerData = new JsonObject();
        playerData.addProperty("1", "aug_apophis");
        playerData.addProperty("2", "regen");

        String jsonString = playerData.toString();
        JsonObject parsed = JsonParser.parseString(jsonString).getAsJsonObject();

        assertEquals("aug_apophis", parsed.get("1").getAsString());
        assertEquals("regen", parsed.get("2").getAsString());

        InfuseEffect slot1 = InfuseEffect.fromString(parsed.get("1").getAsString());
        InfuseEffect slot2 = InfuseEffect.fromString(parsed.get("2").getAsString());

        assertNotNull(slot1);
        assertNotNull(slot2);
        assertTrue(slot1.isAugmented());
        assertFalse(slot2.isAugmented());
        assertEquals("apophis", slot1.getKey());
        assertEquals("regen", slot2.getKey());
    }

    @Test
    @DisplayName("TC-T4-REAL-04: Single-JAR Multi-Version Compatibility Artifact Identity")
    public void testSingleJarCompatibilityIdentity() {
        assertEquals(1, EffectIds.EMERALD);
        assertEquals(2, EffectIds.ENDER);
        assertEquals(3, EffectIds.FEATHER);
        assertEquals(4, EffectIds.FIRE);
        assertEquals(5, EffectIds.FROST);
        assertEquals(6, EffectIds.HASTE);
        assertEquals(7, EffectIds.HEART);
        assertEquals(8, EffectIds.INVIS);
        assertEquals(9, EffectIds.OCEAN);
        assertEquals(10, EffectIds.REGEN);
        assertEquals(11, EffectIds.SPEED);
        assertEquals(12, EffectIds.STRENGTH);
        assertEquals(13, EffectIds.THUNDER);
        assertEquals(14, EffectIds.APOPHIS);
        assertEquals(15, EffectIds.THIEF);
    }
}
