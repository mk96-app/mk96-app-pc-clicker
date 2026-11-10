package com.markes96.pcclicker.bootstrap;

import com.markes96.pcclicker.adapter.in.ui.ApplicationInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Reads the build facts that Maven writes into {@code application.properties}. */
final class ApplicationInfoLoader {

    private static final String RESOURCE = "application.properties";

    private ApplicationInfoLoader() {
    }

    static ApplicationInfo load() {
        try (InputStream in = ApplicationInfoLoader.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + RESOURCE + " next to " + ApplicationInfoLoader.class);
            }
            final Properties properties = new Properties();
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return new ApplicationInfo(
                    required(properties, "app.name"),
                    required(properties, "app.version"),
                    required(properties, "app.author"),
                    required(properties, "app.buildDate"),
                    required(properties, "app.repository"));
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private static String required(Properties properties, String key) {
        final String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing application property " + key);
        }
        return value.strip();
    }
}
