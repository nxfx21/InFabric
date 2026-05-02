package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;

public class ApophisManager {
    private final Infuse plugin;
    
    private final Property APOPHIS_SKIN = new Property(
            "textures",
            "ewogICJ0aW1lc3RhbXAiIDogMTcxNzg4NTA2MDQwNywKICAicHJvZmlsZUlkIiA6ICJlZGUyYzdhMGFjNjM0MTNiYjA5ZDNmMGJlZTllYzhlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVEZXZKYWRlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2MwOTBmY2NjMjBmMWM3ZWMyMDBkNGVkMDUxMjQwNjM3ZmRmNjE5ZDg1Nzg0NWZhNWRmNWJkMzM1MWJiMjBkOCIKICAgIH0KICB9Cn0=",
            "mBgGwS28lqNz7rJCysD9SElJpA5q+34uTZK68JFXIFzuoN31KQg2VHjVDz+/nAr0yXdRwOrgL5rnRb2NbKBPyKSWdcB8A1nVHeNMpoJ5c5CzEERyOROUiTRxge/MIhYL7Fkj67fkh7Sc/l7BwDAf7/7OIgiAIleUTLZ9COnIN15gylTBldOo3JOka8TTNrI1i4QmnMsbgT0luQZzrUMRtZxIHNwx+26IevzCE+hpNdwiYqnDVZdayDLPVy1vv+i3C7AJGd9b7/2/qv0YmWxvT3uKrPR8+9fbSWltGx9ikrdXO17FrGc5u0gqmPWAaSSWw/NJmMhPenILh7/MvXA8mO2m7JeuhnM/EYzdOMB3qzvkUEVddFIngPl6LNE8XG1R+APFBsbpnpybB7dQphSud5DNfuZijqLDd735kykYlRMzw5VVGf7fONheLzSV42XRsIU+5IazHvmAZ4pxr72+r9bbS9vRW38ZgQIy6p8r4tLv9jfmqmcS9lEn1CAgDLAqZWGzIWeIgOdDsrWH4ia/1gj6oZVefRCr2dAS84NsOQUdoJDbS8G0+ArN+CWgnlcwOJCS6MB5kBmQl2FPvwLcSnnRcS66XKfH28Bu2/J3Hu5zRWbONuOLQTbYFxwftUtvS1IORKBCfWvlJTx5G/mz1KOGW89iOCpW8jdx8EmzpRI="
    );

    public ApophisManager() {
        this.plugin = Infuse.getInstance();
    }

    public void initDisguise(ServerPlayerEntity target) {
        UUID uuid = target.getUuid();

        File disguiseFile = new File(plugin.getDataFolder(), "data/ApophisPlayers/" + uuid + ".txt");
        disguiseFile.getParentFile().mkdirs();

        if (disguiseFile.exists()) return;
        
        try {
            FileWriter writer = new FileWriter(disguiseFile);
            GameProfile profile = target.getGameProfile();
            Optional<Property> textures = profile.getProperties().get("textures").stream().findFirst();

            writer.write(target.getName().getString() + "\n");
            if (textures.isEmpty()) {
                writer.write("null\nnull");
            } else {
                writer.write(textures.get().value() + "\n");
                writer.write(String.valueOf(textures.get().signature()));
            }

            writer.flush();
            writer.close();
        } catch (IOException err) {
            Infuse.LOGGER.error("Failed to write to {}.", disguiseFile.getPath());
        }
    }

    public void equipApophis(ServerPlayerEntity target) {
        initDisguise(target);

        GameProfile profile = target.getGameProfile();
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", APOPHIS_SKIN);
        
        // Hiding the player's name (stub)
    }

    public void unequipApophis(ServerPlayerEntity target) {
        UUID uuid = target.getUuid();

        File disguiseFile = new File(plugin.getDataFolder(), "data/ApophisPlayers/" + uuid + ".txt");

        try (Scanner scanner = new Scanner(disguiseFile)) {
            GameProfile profile = target.getGameProfile();
            String value = "";
            String signature = null;
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // name
            }

            if (scanner.hasNextLine()) {
                value = scanner.nextLine();
            }

            if (scanner.hasNextLine()) {
                signature = scanner.nextLine();
                if (signature.equals("null")) {
                    signature = null;
                }
            }

            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", new Property("textures", value, signature));

        } catch (FileNotFoundException err) {}

        if (disguiseFile.exists()) {
            disguiseFile.delete();
        }
    }
}
