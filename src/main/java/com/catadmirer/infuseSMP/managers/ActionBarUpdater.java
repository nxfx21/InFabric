package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.util.MessageUtil;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class ActionBarUpdater {
    private int ticks = 0;

    public ActionBarUpdater() {
    }

    public void start() {
        ServerTickEvents.START_SERVER_TICK.register(this::onTick);
    }

    private void onTick(MinecraftServer server) {
        ticks++;
        if (ticks % 20 != 0) return; // Run every 20 ticks

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            EffectMapping effect;

            String placeholder = Infuse.getInstance().getMainConfig().emptyEffectIcon() ? "\ue901\ue904" : "";

            String leftPad = "";
            String leftTime = "";
            String leftEmoji = placeholder;
            String rightEmoji = placeholder;
            String rightTime = "";
            String rightPad = "";

            UUID uuid = player.getUuid();
            effect = Infuse.getInstance().getDataManager().getEffect(uuid, "1");
            
            if (effect != null) {
                leftEmoji = effect.getIcon() + "\ue904";
                String key = effect.regular().getKey();
                if (CooldownManager.isEffectActive(uuid, key)) {
                    leftEmoji = String.valueOf(effect.getActiveIcon());
                    long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                    leftTime = "<#" + Integer.toHexString(effect.getColor().getRGB() & 0xFFFFFF) + ">" + MessageUtil.formatTime(timeLeft);
                    rightPad = getSpaceTimeStr(stripTags(leftTime));
                } else if (CooldownManager.isOnCooldown(uuid, key)) {
                    long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                    leftTime = MessageUtil.formatTime(timeLeft);
                    rightPad = getSpaceTimeStr(stripTags(leftTime));
                }
            }

            effect = Infuse.getInstance().getDataManager().getEffect(uuid, "2");
            if (effect != null) {
                rightEmoji = effect.getIcon() + "\ue904";
                String key = effect.regular().getKey();
                if (CooldownManager.isEffectActive(uuid, key)) {
                    rightEmoji = String.valueOf(effect.getActiveIcon());
                    long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                    rightTime = "<#" + Integer.toHexString(effect.getColor().getRGB() & 0xFFFFFF) + ">" + MessageUtil.formatTime(timeLeft);
                    leftPad = getSpaceTimeStr(stripTags(rightTime));
                } else if (CooldownManager.isOnCooldown(uuid, key)) {
                    long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                    rightTime = MessageUtil.formatTime(timeLeft);
                    leftPad = getSpaceTimeStr(stripTags(rightTime));
                }
            }


            String actionBarRaw = String.format("<b>%s%s</b> <white>%s %s <b>%s%s</b>", leftPad, leftTime, leftEmoji, rightEmoji, rightTime, rightPad);
            player.sendMessage(TagParser.DEFAULT.parseText(actionBarRaw, ParserContext.of()), true);
        }
    }

    public String getSpaceTimeStr(String timeStr) {
        return "\ue905".repeat(Math.max(0, timeStr.length() - 1)) + (timeStr.contains(":") ? "\ue904" : "\ue905");
    }

    private String stripTags(String text) {
        return text.replaceAll("<[^>]+>", "");
    }
}
