// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.ai.agents.BetaVoiceAgentsTelephonyClient;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PolymorphicModelConstructorTests {
    @ParameterizedTest
    @ValueSource(classes = { AgentHarness.class, TelephonyBinding.class, TelephonyOutboundRetryPolicy.class })
    public void retainedRootsHaveProtectedConstructors(Class<?> modelClass) {
        assertProtectedConstructors(modelClass);
    }

    @Test
    public void bindingInputRootHasProtectedConstructors() {
        Method createBinding = Arrays.stream(BetaVoiceAgentsTelephonyClient.class.getMethods())
            .filter(method -> method.getName().equals("createTelephonyBinding")
                && method.getParameterCount() == 2
                && method.getParameterTypes()[0] == String.class)
            .findFirst()
            .orElseThrow(() -> new AssertionError("The typed createTelephonyBinding method must be present"));

        assertEquals(TelephonyBinding.class, createBinding.getReturnType());
        // Follow the generated API so both the legacy request root and its shared-model replacement are tested.
        Class<?> inputClass = createBinding.getParameterTypes()[1];
        assertTrue(
            inputClass.getSimpleName().equals("CreateTelephonyBindingInput") || inputClass == TelephonyBinding.class,
            inputClass.getName());
        assertProtectedConstructors(inputClass);
    }

    @Test
    public void transferDestinationRootHasProtectedConstructors() throws NoSuchMethodException {
        // This is TelephonyTransferDestination before regeneration and TelephonyAddress afterwards.
        Class<?> destinationClass = TelephonyTransferTarget.class.getMethod("getDestination").getReturnType();
        assertTrue(destinationClass.getSimpleName().equals("TelephonyTransferDestination")
            || destinationClass.getSimpleName().equals("TelephonyAddress"), destinationClass.getName());
        assertProtectedConstructors(destinationClass);
    }

    @Test
    public void unrelatedModelKeepsPublicConstructors() {
        Constructor<?>[] constructors = TelephonyTransferTarget.class.getDeclaredConstructors();
        assertTrue(constructors.length > 0);
        for (Constructor<?> constructor : constructors) {
            assertTrue(Modifier.isPublic(constructor.getModifiers()), constructor.toString());
        }
    }

    private static void assertProtectedConstructors(Class<?> modelClass) {
        assertTrue(Modifier.isPublic(modelClass.getModifiers()), modelClass.getName());
        assertFalse(Modifier.isAbstract(modelClass.getModifiers()), modelClass.getName());
        Constructor<?>[] constructors = modelClass.getDeclaredConstructors();
        assertTrue(constructors.length > 0, modelClass.getName());
        for (Constructor<?> constructor : constructors) {
            assertTrue(Modifier.isProtected(constructor.getModifiers()), constructor.toString());
        }
    }
}
