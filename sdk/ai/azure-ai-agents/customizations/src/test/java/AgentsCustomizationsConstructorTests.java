// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentsCustomizationsConstructorTests {
    @Test
    void protectsRetainedAndReplacementRootsWithoutRequiringRemovedModels() throws Exception {
        Map<String, String> models = new HashMap<>();
        for (String name : new String[] {
            "AgentHarness",
            "TelephonyBinding",
            "TelephonyAddress",
            "TelephonyOutboundRetryPolicy",
            "UnrelatedModel" }) {
            models.put(path(name), model(name));
        }

        Map<String, String> result = constructorCustomization().run(models, NOPLogger.NOP_LOGGER);

        for (String name : new String[] {
            "AgentHarness",
            "TelephonyBinding",
            "TelephonyAddress",
            "TelephonyOutboundRetryPolicy" }) {
            assertTrue(result.get(path(name)).contains("protected " + name + "()"), name);
        }
        assertTrue(result.get(path("UnrelatedModel")).contains("public UnrelatedModel()"));
    }

    @Test
    void protectsLegacyRootsWhenTheyAreStillEmitted() throws Exception {
        Map<String, String> models = new HashMap<>();
        models.put(path("CreateTelephonyBindingInput"), model("CreateTelephonyBindingInput"));
        models.put(path("TelephonyTransferDestination"), model("TelephonyTransferDestination"));

        Map<String, String> result = constructorCustomization().run(models, NOPLogger.NOP_LOGGER);

        assertTrue(result.get(path("CreateTelephonyBindingInput")).contains("protected CreateTelephonyBindingInput()"));
        assertTrue(
            result.get(path("TelephonyTransferDestination")).contains("protected TelephonyTransferDestination()"));
    }

    private static Customization constructorCustomization() throws Exception {
        Method method = AgentsCustomizations.class.getDeclaredMethod("protectPolymorphicBaseConstructors",
            LibraryCustomization.class);
        method.setAccessible(true);
        return new Customization() {
            @Override
            public void customize(LibraryCustomization customization, Logger logger) {
                try {
                    method.invoke(new AgentsCustomizations(), customization);
                } catch (ReflectiveOperationException exception) {
                    throw new AssertionError(exception);
                }
            }
        };
    }

    private static String path(String name) {
        return "src/main/java/com/azure/ai/agents/models/" + name + ".java";
    }

    private static String model(String name) {
        return "package com.azure.ai.agents.models;\npublic class " + name + " { public " + name + "() {} }\n";
    }
}
