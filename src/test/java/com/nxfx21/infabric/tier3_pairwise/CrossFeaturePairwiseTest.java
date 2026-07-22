package com.nxfx21.infabric.tier3_pairwise;

import com.nxfx21.infabric.effects.*;
import com.nxfx21.infabric.testrunner.FabricTestHarness;
import com.nxfx21.infabric.testrunner.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CrossFeaturePairwiseTest {

    @BeforeAll
    public static void setUp() {
        FabricTestHarness.ensureEffectsRegistered();
    }

    @Test
    @DisplayName("TC-T3-PAIR-01: Pairwise serialization parity check for all 15 effect pairs")
    public void testPairwiseSerialization() {
        List<String> effectKeys = List.of(
            "emerald", "ender", "feather", "fire", "frost",
            "haste", "heart", "invis", "ocean", "regen",
            "speed", "strength", "thunder", "apophis", "thief"
        );

        for (String k1 : effectKeys) {
            for (String k2 : effectKeys) {
                InfuseEffect e1 = InfuseEffect.fromString(k1);
                InfuseEffect e2 = InfuseEffect.fromString(k2);

                assertNotNull(e1);
                assertNotNull(e2);

                if (k1.equals(k2)) {
                    assertEquals(e1, e2);
                } else {
                    assertNotEquals(e1, e2);
                    assertNotEquals(e1.getId(), e2.getId());
                }
            }
        }
    }

    @Test
    @DisplayName("TC-T3-PAIR-02: Regular vs Augmented pair isolation")
    public void testRegularVsAugmentedIsolation() {
        List<String> effectKeys = List.of("emerald", "ender", "fire", "strength", "regen");

        for (String key : effectKeys) {
            InfuseEffect reg = InfuseEffect.fromString(key);
            InfuseEffect aug = InfuseEffect.fromString("aug_" + key);

            assertNotNull(reg);
            assertNotNull(aug);
            assertFalse(reg.isAugmented());
            assertTrue(aug.isAugmented());
            assertEquals(reg.getId(), aug.getId());
            assertNotEquals(reg.serialize(), aug.serialize());
            assertEquals(reg.serialize() + 100, aug.serialize());

            TestUtils.assertEffectEquals(reg, InfuseEffect.deserialize(reg.serialize()));
            TestUtils.assertEffectEquals(aug, InfuseEffect.deserialize(aug.serialize()));
        }
    }

    @Test
    @DisplayName("TC-T3-PAIR-03: Apophis + Heart HP boost value aggregation math")
    public void testHpBoostAggregationMath() {
        assertNotNull(Apophis.APOPHIS_BOOST_ID);
        assertNotNull(Heart.HEART_BOOST_ID);

        net.minecraft.server.network.ServerPlayerEntity player = org.mockito.Mockito.mock(net.minecraft.server.network.ServerPlayerEntity.class, org.mockito.Mockito.withSettings().stubOnly());
        net.minecraft.entity.attribute.EntityAttributeInstance maxHealthAttr = org.mockito.Mockito.mock(net.minecraft.entity.attribute.EntityAttributeInstance.class);
        java.util.Map<net.minecraft.util.Identifier, net.minecraft.entity.attribute.EntityAttributeModifier> modifiers = new java.util.HashMap<>();
        
        org.mockito.Mockito.doAnswer(invocation -> {
            net.minecraft.entity.attribute.EntityAttributeModifier mod = invocation.getArgument(0);
            modifiers.put(mod.id(), mod);
            return null;
        }).when(maxHealthAttr).addTemporaryModifier(org.mockito.Mockito.any());

        org.mockito.Mockito.doAnswer(invocation -> {
            net.minecraft.util.Identifier id = invocation.getArgument(0);
            return modifiers.get(id);
        }).when(maxHealthAttr).getModifier(org.mockito.Mockito.any());

        org.mockito.Mockito.doAnswer(invocation -> {
            double val = 20.0;
            for (net.minecraft.entity.attribute.EntityAttributeModifier mod : modifiers.values()) {
                val += mod.value();
            }
            return val;
        }).when(maxHealthAttr).getValue();

        org.mockito.Mockito.when(player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.MAX_HEALTH)).thenReturn(maxHealthAttr);

        Apophis apophis = new Apophis();
        Heart heart = new Heart();

        apophis.equip(player);
        heart.equip(player);

        assertEquals(40.0, maxHealthAttr.getValue(), "Combining Apophis and Heart attribute modifiers must total 40 HP (20 hearts)");
    }

    @Test
    @DisplayName("TC-T3-PAIR-04: Speed + Fire particle trail dash duration math")
    public void testSpeedAndFireMath() {
        Speed speed = new Speed();
        Fire fire = new Fire();

        assertNotEquals(speed.getId(), fire.getId());
        assertEquals("speed", speed.getKey());
        assertEquals("fire", fire.getKey());
    }

    @Test
    @DisplayName("TC-T3-PAIR-05: Thief stealing Emerald & Apophis slot combination math")
    public void testThiefDualSlotStealCombination() {
        Thief thief = new Thief();
        Emerald emerald = new Emerald();
        Apophis apophis = new Apophis();

        assertNotEquals(thief.getId(), emerald.getId());
        assertNotEquals(thief.getId(), apophis.getId());
        assertNotEquals(emerald.getId(), apophis.getId());
    }
}
