// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.redis.models.RedisConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RedisConfigurationTests {

    @Test
    @Disabled
    public void generateConfigurationUtils() {
        // Update class ConfigurationUtils
        System.out.println(generateToMap());
        System.out.println(generatePutConfiguration());
        System.out.println(generateRemoveConfiguration());
    }

    @Test
    public void testConfigurationUtils() {
        RedisConfiguration configuration = new RedisConfiguration()
            .withRdbBackupEnabled("true")
            .withAofStorageConnectionString0("connection");

        Map<String, String> map = ConfigurationUtils.toMap(configuration);
        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals("true", map.get("rdb-backup-enabled"));
        Assertions.assertEquals("connection", map.get("aof-storage-connection-string-0"));

        RedisConfiguration configuration1 = ConfigurationUtils.toConfiguration(map);
        Assertions.assertEquals("true", configuration1.rdbBackupEnabled());
        Assertions.assertEquals("connection", configuration1.aofStorageConnectionString0());
        Assertions.assertTrue(CoreUtils.isNullOrEmpty(configuration1.additionalProperties()));

        configuration
            .withAdditionalProperties(Collections.singletonMap("key1", "value1"));
        map = ConfigurationUtils.toMap(configuration);
        Assertions.assertEquals(3, map.size());
        Assertions.assertEquals("value1", map.get("key1"));

        configuration1 = ConfigurationUtils.toConfiguration(map);
        Assertions.assertEquals("true", configuration1.rdbBackupEnabled());
        Assertions.assertEquals("connection", configuration1.aofStorageConnectionString0());
        Assertions.assertEquals(1, configuration1.additionalProperties().size());
        Assertions.assertEquals("value1", configuration1.additionalProperties().get("key1"));

        configuration = new RedisConfiguration();
        ConfigurationUtils.putConfiguration(configuration, "rdb-backup-enabled", "true");
        Assertions.assertEquals("true", configuration.rdbBackupEnabled());
        ConfigurationUtils.putConfiguration(configuration, "key1", "value1");
        Assertions.assertEquals("value1", configuration1.additionalProperties().get("key1"));

        ConfigurationUtils.removeConfiguration(configuration, "rdb-backup-enabled");
        Assertions.assertNull(configuration.rdbBackupEnabled());
        ConfigurationUtils.removeConfiguration(configuration, "key2");
        Assertions.assertEquals("value1", configuration1.additionalProperties().get("key1"));
        ConfigurationUtils.removeConfiguration(configuration, "key1");
        Assertions.assertTrue(CoreUtils.isNullOrEmpty(configuration.additionalProperties()));
    }

    private static String generateToMap() {
        StringBuilder sb = new StringBuilder();

        sb.append("static Map<String, String> toMap(RedisConfiguration configuration) {\n");
        sb.append("    Map<String, String> map = new HashMap<>();\n");
        sb.append("    if (configuration != null) {\n");

        getFieldsWithJsonProperty(false).forEach((key, value) -> {
            sb.append("        if (configuration.").append(value.getName()).append("() != null) {\n");
            sb.append("            map.put(\"").append(key).append("\", configuration.").append(value.getName()).append("());\n");
            sb.append("        }\n");
        });

        sb.append("        if (configuration.additionalProperties() != null) {\n");
        sb.append("            configuration.additionalProperties().forEach((key1, value) -> map.put(key1, value.toString()));\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    return map;\n");

        sb.append("}\n");

        return sb.toString();
    }

    private static String generatePutConfiguration() {
        StringBuilder sb = new StringBuilder();

        sb.append("static void putConfiguration(RedisConfiguration configuration,\n");
        sb.append("                             String key, String value) {\n");

        sb.append("    if (configuration == null) {\n");
        sb.append("        return;\n");
        sb.append("    }\n");

        sb.append("    switch (key) {\n");
        getFieldsWithJsonProperty(true).forEach((key, value) -> sb.append(writeSwitchCase(key, value,
            "configuration." + setterMethodName(value) + "(value)")));
        sb.append("        default:\n");
        sb.append("            if (configuration.additionalProperties() == null) {\n");
        sb.append("                configuration.withAdditionalProperties(new HashMap<>());\n");
        sb.append("            }\n");
        sb.append("            configuration.additionalProperties().put(key, value);\n");
        sb.append("            break;\n");

        sb.append("    }\n");

        sb.append("}\n");

        return sb.toString();
    }

    private static String generateRemoveConfiguration() {
        StringBuilder sb = new StringBuilder();

        sb.append("static void removeConfiguration(RedisConfiguration configuration, String key) {\n");

        sb.append("    if (configuration == null) {\n");
        sb.append("        return;\n");
        sb.append("    }\n");
        sb.append("    if (configuration.additionalProperties() != null) {\n");
        sb.append("         configuration.additionalProperties().remove(key);\n");
        sb.append("    }\n");

        sb.append("    switch (key) {\n");
        getFieldsWithJsonProperty(true).forEach((key, value) -> sb.append(writeSwitchCase(key, value,
            "configuration." + setterMethodName(value) + "(null)")));
        sb.append("        default:\n");
        sb.append("            break;\n");
        sb.append("    }\n");

        sb.append("}\n");

        return sb.toString();
    }

    private static String writeSwitchCase(String property, Field field, String code) {
        StringBuilder sb = new StringBuilder();
        sb.append("        case \"").append(property).append("\":\n");
        sb.append("            ").append(code).append(";\n");
        sb.append("            break;\n");
        return sb.toString();
    }

    private static String setterMethodName(Field field) {
        String name = field.getName();
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        return "with" + name;
    }

    private static Map<String, Field> getFieldsWithJsonProperty(boolean modifiable) {
        Class<?> aClass = RedisConfiguration.class;
        Stream<Field> fields = Stream.of(aClass.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(JsonProperty.class));
        if (modifiable) {
            fields = fields
                .filter(f -> f.getDeclaredAnnotation(JsonProperty.class).access() != JsonProperty.Access.WRITE_ONLY);
        }
        return fields.collect(Collectors.toMap(
            f -> f.getDeclaredAnnotation(JsonProperty.class).value(),
            Function.identity()));
    }
}
