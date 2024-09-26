// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.redis.models.RedisConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RedisConfigurationTests {

    @Test
    @Disabled
    public void generateConfigurationUtils() throws IOException {
        // Update class ConfigurationUtils
        System.out.println(generateToMap());
        System.out.println(generatePutConfiguration());
        System.out.println(generateRemoveConfiguration());
    }

    @Test
    public void testConfigurationUtils() {
        RedisConfiguration configuration = new RedisConfiguration()
            .withRdbBackupEnabled("true")
            .withAofBackupEnabled("true")
            .withAofStorageConnectionString0("connection");

        Map<String, String> map = ConfigurationUtils.toMap(configuration);
        Assertions.assertEquals(3, map.size());
        Assertions.assertEquals("true", map.get("rdb-backup-enabled"));
        Assertions.assertEquals("connection", map.get("aof-storage-connection-string-0"));
        Assertions.assertEquals("true", map.get("aof-backup-enabled"));

        RedisConfiguration configuration1 = ConfigurationUtils.toConfiguration(map);
        Assertions.assertEquals("true", configuration1.rdbBackupEnabled());
        Assertions.assertEquals("connection", configuration1.aofStorageConnectionString0());
        Assertions.assertTrue(CoreUtils.isNullOrEmpty(configuration1.additionalProperties()));

        configuration
            .withAdditionalProperties(Collections.singletonMap("key1", "value1"));
        map = ConfigurationUtils.toMap(configuration);
        Assertions.assertEquals(4, map.size());
        Assertions.assertEquals("value1", map.get("key1"));

        configuration1 = ConfigurationUtils.toConfiguration(map);
        Assertions.assertEquals("true", configuration1.rdbBackupEnabled());
        Assertions.assertEquals("connection", configuration1.aofStorageConnectionString0());
        Assertions.assertEquals(1, configuration1.additionalProperties().size());
        Assertions.assertEquals("value1", configuration1.additionalProperties().get("key1"));
        Assertions.assertEquals("true", configuration1.aofBackupEnabled());

        configuration = new RedisConfiguration();
        ConfigurationUtils.putConfiguration(configuration, "rdb-backup-enabled", "true");
        Assertions.assertEquals("true", configuration.rdbBackupEnabled());
        ConfigurationUtils.putConfiguration(configuration, "key1", "value1");
        Assertions.assertEquals("value1", configuration1.additionalProperties().get("key1"));
        ConfigurationUtils.putConfiguration(configuration, "aof-backup-enabled", "true");
        Assertions.assertEquals("true", configuration.aofBackupEnabled());

        ConfigurationUtils.removeConfiguration(configuration, "rdb-backup-enabled");
        Assertions.assertNull(configuration.rdbBackupEnabled());
        ConfigurationUtils.removeConfiguration(configuration, "key2");
        Assertions.assertEquals("value1", configuration1.additionalProperties().get("key1"));
        ConfigurationUtils.removeConfiguration(configuration, "key1");
        Assertions.assertTrue(CoreUtils.isNullOrEmpty(configuration.additionalProperties()));
        ConfigurationUtils.removeConfiguration(configuration, "aof-backup-enabled");
        Assertions.assertNull(configuration.aofBackupEnabled());
    }

    @Test
    public void testConfig() {
        System.out.println(new File(".").getAbsolutePath());
    }

    private static String generateToMap() throws IOException {
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
        sb.append("            configuration.additionalProperties().forEach((key1, value) -> map.put(key1, value));\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    return map;\n");

        sb.append("}\n");

        return sb.toString();
    }

    private static String generatePutConfiguration() throws IOException {
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

    private static String generateRemoveConfiguration() throws IOException {
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

    private static Map<String, Field> getFieldsWithJsonProperty(boolean modifiable) throws IOException {
        Class<?> aClass = RedisConfiguration.class;
        String content = new String(Files.readAllBytes(Paths.get("../../resourcemanager/azure-resourcemanager-redis/src/main/java/com/azure/resourcemanager/redis/models/RedisConfiguration.java")), Charset.defaultCharset());
        Stream<Field> fields = Stream.of(aClass.getDeclaredFields())
            .filter(f -> toJsonField(f, content).exists || fromJsonField(f, content).exists);
        if (modifiable) {
            fields = fields
                // Appearance in `toJson` method means the field is modifiable.
                .filter(f -> toJsonField(f, content).exists);
        }
        return fields.collect(Collectors.toMap(
            f -> {
                FieldInfo fromJsonField = fromJsonField(f, content);
                if (fromJsonField.exists) {
                    return fromJsonField.serializedName;
                }
                FieldInfo toJsonField = toJsonField(f, content);
                if (toJsonField.exists) {
                    return toJsonField.serializedName;
                }
                throw new IllegalStateException("Shouldn't reach here, as we've already made sure that either fromJsonField or toJsonField exists.");
            },
            Function.identity()));
    }

    private static FieldInfo fromJsonField(Field f, String content) {
        // e.g. if ("rdb-backup-enabled".equals(fieldName)) {
        //        deserializedRedisConfiguration.rdbBackupEnabled = reader.getString();
        //      }
        Pattern fromJsonFieldPattern = Pattern.compile("if \\(\"([\\w|-]+)\"\\.equals\\(fieldName\\)\\) \\{\\W*[\\w]+\\." + f.getName() + " = reader.getString\\(\\);");
        Matcher matcher = fromJsonFieldPattern.matcher(content);
        FieldInfo fieldInfo = new FieldInfo();
        if (matcher.find()) {
            fieldInfo.exists = true;
            fieldInfo.serializedName = matcher.group(1);
        }
        return fieldInfo;
    }

    private static FieldInfo toJsonField(Field f, String content) {
        // e.g. jsonWriter.writeStringField("rdb-backup-enabled", this.rdbBackupEnabled);
        Pattern toJsonFieldPattern = Pattern.compile("\\.writeStringField\\(\"([\\w|-]+)\", this\\." + f.getName());
        Matcher matcher = toJsonFieldPattern.matcher(content);
        FieldInfo fieldInfo = new FieldInfo();
        if (matcher.find()) {
            fieldInfo.exists = true;
            fieldInfo.serializedName = matcher.group(1);
        }
        return fieldInfo;
    }

    private static class FieldInfo {
        boolean exists;
        String serializedName;
    }
}
