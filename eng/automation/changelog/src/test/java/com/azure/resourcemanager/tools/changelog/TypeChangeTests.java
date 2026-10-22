// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.tools.changelog;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.Modifier;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeChangeTests {
    private static final String CLASS_NAME = "com.azure.test.ResourceOperation";
    private static final int CLASS = Modifier.PUBLIC;
    private static final int INTERFACE = Modifier.PUBLIC | Modifier.INTERFACE | Modifier.ABSTRACT;

    @TempDir
    Path tempDir;

    @Test
    public void testClassToInterface() throws Exception {
        JSONObject result = compare(CLASS | Modifier.FINAL, INTERFACE,
            new String[] { "public String errorCode() { return null; }" },
            new String[] { "public abstract String errorCode();" });

        assertOnlyTypeChange(result, "a class", "an interface");
    }

    @Test
    public void testInterfaceToClass() throws Exception {
        JSONObject result = compare(INTERFACE, CLASS,
            new String[] { "public abstract String errorCode();" },
            new String[] { "public String errorCode() { return null; }" });

        assertOnlyTypeChange(result, "an interface", "a class");
    }

    @Test
    public void testInterfaceToAbstractClass() throws Exception {
        JSONObject result = compare(INTERFACE, CLASS | Modifier.ABSTRACT,
            new String[] { "public abstract String errorCode();" },
            new String[] { "public abstract String errorCode();" });

        assertOnlyTypeChange(result, "an interface", "a class");
    }

    @Test
    public void testTypeChangesWithoutMethods() throws Exception {
        assertOnlyTypeChange(compare(CLASS, INTERFACE, new String[0], new String[0]), "a class", "an interface");
        assertOnlyTypeChange(compare(INTERFACE, CLASS, new String[0], new String[0]), "an interface", "a class");
    }

    @Test
    public void testReturnTypeChangesAreRetained() throws Exception {
        for (boolean toInterface : new boolean[] { true, false }) {
            JSONObject result = compare(toInterface ? CLASS : INTERFACE, toInterface ? INTERFACE : CLASS,
                new String[] { toInterface
                    ? "public String errorCode() { return null; }"
                    : "public abstract String errorCode();" },
                new String[] { toInterface
                    ? "public abstract Object errorCode();"
                    : "public Object errorCode() { return null; }" });

            assertChange(result, "`java.lang.String errorCode()` -> `java.lang.Object errorCode()`");
            assertEquals(2, result.getJSONArray("breakingChanges").length());
        }
    }

    @Test
    public void testParameterChangesAreRetained() throws Exception {
        for (boolean toInterface : new boolean[] { true, false }) {
            JSONObject result = compare(toInterface ? CLASS : INTERFACE, toInterface ? INTERFACE : CLASS,
                new String[] { toInterface
                    ? "public String errorCode(String code) { return code; }"
                    : "public abstract String errorCode(String code);" },
                new String[] { toInterface
                    ? "public abstract String errorCode(int code);"
                    : "public String errorCode(int code) { return null; }" });

            assertChange(result, "`errorCode(java.lang.String)` was removed");
            assertTrue(result.getString("changelog").contains("`errorCode(int)` was added"));
        }
    }

    @Test
    public void testAddedAndRemovedMethodsAreRetained() throws Exception {
        JSONObject result = compare(CLASS, INTERFACE,
            new String[] {
                "public String errorCode() { return null; }",
                "public static Object fromJson(Object reader) { return null; }",
                "public Object toJson(Object writer) { return null; }"
            },
            new String[] {
                "public abstract String errorCode();",
                "public abstract Object innerModel();"
            });

        assertChange(result, "`fromJson(java.lang.Object)` was removed");
        assertChange(result, "`toJson(java.lang.Object)` was removed");
        assertTrue(result.getString("changelog").contains("### Features Added"));
        assertTrue(result.getString("changelog").contains("`innerModel()` was added"));
        assertFalse(result.getString("changelog").contains("errorCode()"));
        assertEquals(3, result.getJSONArray("breakingChanges").length());

        JSONObject reversed = compare(INTERFACE, CLASS,
            new String[] { "public abstract Object innerModel();" },
            new String[] { "public Object toJson(Object writer) { return null; }" });
        assertChange(reversed, "`innerModel()` was removed");
        assertTrue(reversed.getString("changelog").contains("`toJson(java.lang.Object)` was added"));
    }

    @Test
    public void testIndependentModifiersAreRetained() throws Exception {
        JSONObject result = compare(CLASS, INTERFACE,
            new String[] { "public static String errorCode() { return null; }" },
            new String[] { "public abstract String errorCode();" });
        assertChange(result, "`java.lang.String errorCode()` -> `java.lang.String errorCode()`");
        assertEquals(2, result.getJSONArray("breakingChanges").length());

        JSONObject reversed = compare(INTERFACE, CLASS,
            new String[] { "public abstract String errorCode();" },
            new String[] { "public static String errorCode() { return null; }" });
        assertChange(reversed, "`java.lang.String errorCode()` -> `java.lang.String errorCode()`");
        assertEquals(2, reversed.getJSONArray("breakingChanges").length());
    }

    @Test
    public void testAbstractnessChangeWithoutTypeChangeIsRetained() throws Exception {
        JSONObject result = compare(CLASS | Modifier.ABSTRACT, CLASS | Modifier.ABSTRACT,
            new String[] { "public String errorCode() { return null; }" },
            new String[] { "public abstract String errorCode();" });

        assertChange(result, "`java.lang.String errorCode()` -> `java.lang.String errorCode()`");
        assertEquals(1, result.getJSONArray("breakingChanges").length());
        assertFalse(result.getString("changelog").contains("Changed from"));
    }

    @Test
    public void testExceptionChangeAlongsideTypeChangeIsRetained() throws Exception {
        JSONObject result = compare(CLASS, INTERFACE,
            new String[] { "public String errorCode() { return null; }" },
            new String[] { "public abstract String errorCode() throws java.io.IOException;" });

        assertChange(result, "`java.lang.String errorCode()` -> `java.lang.String errorCode()`");
        assertEquals(2, result.getJSONArray("breakingChanges").length());
    }

    private static void assertOnlyTypeChange(JSONObject result, String oldType, String newType) {
        String change = "Changed from " + oldType + " to " + newType;
        assertEquals("### Breaking Changes\n\n#### `ResourceOperation` was modified\n\n* " + change + ".\n\n",
            result.getString("changelog"));
        assertEquals(1, result.getJSONArray("breakingChanges").length());
        assertEquals(change + " in class `" + CLASS_NAME + "`.", result.getJSONArray("breakingChanges").getString(0));
    }

    private static void assertChange(JSONObject result, String change) {
        assertTrue(result.getString("changelog").contains(change), result.toString());
        assertTrue(result.getJSONArray("breakingChanges").toList()
            .contains("Method " + change + " in class `" + CLASS_NAME + "`."), result.toString());
    }

    private JSONObject compare(int oldModifiers, int newModifiers, String[] oldMethods, String[] newMethods)
        throws Exception {
        Path oldJar = createJar(tempDir.resolve("old.jar"), oldModifiers, oldMethods);
        Path newJar = createJar(tempDir.resolve("new.jar"), newModifiers, newMethods);
        String previousOldJar = System.getProperty("OLD_JAR");
        String previousNewJar = System.getProperty("NEW_JAR");
        try {
            System.setProperty("OLD_JAR", oldJar.toString());
            System.setProperty("NEW_JAR", newJar.toString());
            return Main.getChangelog();
        } finally {
            restoreProperty("OLD_JAR", previousOldJar);
            restoreProperty("NEW_JAR", previousNewJar);
        }
    }

    private static Path createJar(Path path, int modifiers, String[] methods) throws Exception {
        ClassPool pool = new ClassPool(true);
        CtClass type = Modifier.isInterface(modifiers) ? pool.makeInterface(CLASS_NAME) : pool.makeClass(CLASS_NAME);
        try {
            type.setModifiers(modifiers);
            if (!type.isInterface()) {
                CtConstructor constructor = CtNewConstructor.defaultConstructor(type);
                constructor.setModifiers(Modifier.PRIVATE);
                type.addConstructor(constructor);
            }
            for (String method : methods) {
                type.addMethod(CtNewMethod.make(method, type));
            }
            try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(path))) {
                jar.putNextEntry(new JarEntry(CLASS_NAME.replace('.', '/') + ".class"));
                jar.write(type.toBytecode());
                jar.closeEntry();
            }
        } finally {
            type.detach();
        }
        return path;
    }

    private static void restoreProperty(String name, String previousValue) {
        if (previousValue == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, previousValue);
        }
    }
}
