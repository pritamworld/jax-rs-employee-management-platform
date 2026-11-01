package com.moxdroid.employee.config;

import java.io.InputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public final class EnvironmentConfig {
    private static final Properties P = new Properties();
    private static final Set<String> ALLOWED = Set.of("local", "test", "uat", "prod");
    private static final Map<String, String> ENV = Map.ofEntries(
            Map.entry("db.url", "DB_URL"), Map.entry("db.username", "DB_USERNAME"),
            Map.entry("db.password", "DB_PASSWORD"), Map.entry("jwt.secret", "JWT_SECRET"),
            Map.entry("jwt.issuer", "JWT_ISSUER"), Map.entry("jwt.ttlMinutes", "JWT_TTL_MINUTES"));
    static {
        try {
            loadFile("application.properties");
            String profile = System.getenv().getOrDefault("APP_ENV", "local").toLowerCase();
            if (!ALLOWED.contains(profile))
                throw new IllegalStateException("Unsupported APP_ENV: " + profile);
            loadFile("application-" + profile + ".properties");
            ENV.forEach((key, env) -> {
                String value = System.getenv(env);
                if (value != null && !value.isBlank())
                    P.setProperty(key, value);
            });
            // Resolve simple ${ENV:default} placeholders in common properties.
            for (String key : P.stringPropertyNames()) {
                String value = P.getProperty(key);
                if (value.startsWith("${") && value.endsWith("}")) {
                    String inner = value.substring(2, value.length() - 1);
                    String[] parts = inner.split(":", 2);
                    P.setProperty(key, System.getenv().getOrDefault(parts[0],
                            parts.length == 2 ? parts[1] : ""));
                }
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private EnvironmentConfig() {
    }

    private static void loadFile(String name) throws IOException {
        try (InputStream in = EnvironmentConfig.class.getClassLoader().getResourceAsStream(name)) {
            if (in == null)
                throw new IllegalStateException("Missing config: " + name);
            P.load(in);
        }
    }

    public static String get(String key) {
        String v = P.getProperty(key);
        if (v == null || v.isBlank())
            throw new IllegalStateException("Missing property: " + key);
        return v;
    }

    public static String getOrDefault(String key, String fallback) {
        String v = P.getProperty(key);
        return v == null || v.isBlank() ? fallback : v;
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }
}
