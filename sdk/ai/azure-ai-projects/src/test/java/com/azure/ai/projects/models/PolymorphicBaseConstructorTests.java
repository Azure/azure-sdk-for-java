// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.models;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonSerializable;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolymorphicBaseConstructorTests {
    @ParameterizedTest
    @MethodSource("polymorphicModels")
    void protectedBaseConstructorsPreservePolymorphicDeserialization(Class<?> baseType, JsonSerializable<?> model)
        throws NoSuchMethodException {
        assertTrue(Modifier.isProtected(baseType.getDeclaredConstructor().getModifiers()));
        assertEquals(0, baseType.getConstructors().length);
        assertFalse(Modifier.isAbstract(baseType.getModifiers()));

        BinaryData serialized = BinaryData.fromObject(model);
        Object deserialized = serialized.toObject(baseType);
        assertEquals(model.getClass(), deserialized.getClass());
        assertEquals(serialized.toString(), BinaryData.fromObject(deserialized).toString());

        String unknownJson = "{\"type\":\"future_variant\"}";
        Object unknown = BinaryData.fromString(unknownJson).toObject(baseType);
        assertEquals(baseType, unknown.getClass());
        assertEquals(unknownJson, BinaryData.fromObject(unknown).toString());
    }

    @Test
    void evaluatorWithoutGeneratedSubtypeDeserializesAsBase() {
        String json = "{\"type\":\"service\"}";
        EvaluatorDefinition definition = BinaryData.fromString(json).toObject(EvaluatorDefinition.class);

        assertEquals(EvaluatorDefinition.class, definition.getClass());
        assertEquals(EvaluatorDefinitionType.SERVICE, definition.getType());
        assertEquals(json, BinaryData.fromObject(definition).toString());
    }

    private static Stream<Arguments> polymorphicModels() {
        return Stream.of(
            Arguments.of(DataGenerationJobOptions.class, new SimpleQnADataGenerationJobOptions(3).setTrainSplit(0.75)),
            Arguments.of(DataGenerationJobSource.class,
                new PromptDataGenerationJobSource("Generate questions.").setDescription("Example source")),
            Arguments.of(EvaluatorDefinition.class, new PromptBasedEvaluatorDefinition("Score the response.")),
            Arguments.of(EvaluatorGenerationJobSource.class,
                new PromptEvaluatorGenerationJobSource("Evaluate response quality.").setDescription("Example source")));
    }
}
