package com.nxfx21.infabric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public class MessageTranslator {
    private static final Logger LOGGER = LoggerFactory.getLogger("Infuse-Translator");
    public static final Set<String> SUPPORTED_LOCALES = Set.of("en_US", "es", "ru", "tr");

    private final Map<String, Map<String, Object>> loadedLocales = new HashMap<>();

    public MessageTranslator() {
        loadAll();
    }

    public void loadAll() {
        loadedLocales.clear();
        for (String locale : SUPPORTED_LOCALES) {
            loadLocale(locale);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadLocale(String locale) {
        Map<String, Object> translations = new HashMap<>();
        Yaml yaml = new Yaml();

        // 1. Load bundled base locale
        try (InputStream in = getClass().getResourceAsStream("/lang/base/" + locale + ".yml")) {
            if (in != null) {
                Object data = yaml.load(in);
                if (data instanceof Map<?, ?> map) {
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        translations.put(String.valueOf(entry.getKey()).toLowerCase(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load base locale for {}", locale, e);
        }

        // 2. Load custom override from data folder if present
        File customFile = new File(Infuse.getInstance().getDataFolder(), "lang/" + locale + ".yml");
        if (customFile.exists()) {
            try (InputStream in = new FileInputStream(customFile)) {
                Object data = yaml.load(in);
                if (data instanceof Map<?, ?> map) {
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        translations.put(String.valueOf(entry.getKey()).toLowerCase(), entry.getValue());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load custom locale overrides from {}", customFile.getPath(), e);
            }
        }

        loadedLocales.put(locale, translations);
    }

    public String translate(String key) {
        String locale = "en_US";
        if (Infuse.getInstance() != null && Infuse.getInstance().getMainConfig() != null) {
            locale = Infuse.getInstance().getMainConfig().lang();
        }

        if (!SUPPORTED_LOCALES.contains(locale)) {
            LOGGER.warn("Locale \"{}\" not recognized. Falling back to en_US.", locale);
            locale = "en_US";
        }

        Map<String, Object> translations = loadedLocales.get(locale);
        if (translations == null) {
            loadLocale(locale);
            translations = loadedLocales.get(locale);
        }

        String searchKey = key.toLowerCase();
        Object val = (translations != null) ? translations.get(searchKey) : null;

        // Fallback to en_US if missing in active locale
        if (val == null && !locale.equals("en_US")) {
            Map<String, Object> fallback = loadedLocales.get("en_US");
            if (fallback != null) {
                val = fallback.get(searchKey);
            }
        }

        if (val instanceof List<?> list) {
            List<String> strList = new ArrayList<>();
            for (Object item : list) {
                strList.add(String.valueOf(item));
            }
            return String.join("\n", strList);
        } else if (val != null) {
            return String.valueOf(val);
        }

        return null;
    }
}
