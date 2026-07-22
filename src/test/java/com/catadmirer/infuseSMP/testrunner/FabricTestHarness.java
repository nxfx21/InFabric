package com.catadmirer.infuseSMP.testrunner;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.HitTracker;
import com.catadmirer.infuseSMP.MainConfig;
import com.catadmirer.infuseSMP.effects.*;
import com.catadmirer.infuseSMP.managers.DataManager;

import java.io.File;
import java.lang.reflect.Field;

public class FabricTestHarness {
    private static boolean initialized = false;

    public static synchronized void ensureEffectsRegistered() {
        if (initialized) return;

        try {
            net.minecraft.SharedConstants.createGameVersion();
        } catch (Throwable ignored) {
        }

        initInfuseInstance();

        registerSafely(new Emerald());
        registerSafely(new Ender());
        registerSafely(new Feather());
        registerSafely(new Fire());
        registerSafely(new Frost());
        registerSafely(new Haste());
        registerSafely(new Heart());
        registerSafely(new Invis());
        registerSafely(new Ocean());
        registerSafely(new Regen());
        registerSafely(new Speed());
        registerSafely(new Strength());
        registerSafely(new Thunder());
        registerSafely(new Apophis());
        registerSafely(new Thief());

        initialized = true;
    }

    private static void initInfuseInstance() {
        if (Infuse.getInstance() != null) return;
        try {
            Infuse plugin = new Infuse();
            setField(Infuse.class, null, "instance", plugin);

            File dataFolder = new File("build/tmp/test_config");
            if (!dataFolder.exists()) dataFolder.mkdirs();
            setField(Infuse.class, plugin, "dataFolder", dataFolder);

            MainConfig config = new MainConfig(dataFolder);
            config.load();
            setField(Infuse.class, plugin, "mainConfig", config);

            DataManager dm = new DataManager();
            setField(Infuse.class, plugin, "dataManager", dm);

            HitTracker ht = new HitTracker();
            setField(Infuse.class, plugin, "hitTracker", ht);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setField(Class<?> clazz, Object target, String fieldName, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void registerSafely(InfuseEffect effect) {
        if (!InfuseEffect.isRegistered(effect)) {
            InfuseEffect.register(effect);
        }
    }
}
