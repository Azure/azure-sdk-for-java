// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.union.codesnippets;

import io.clientcore.core.util.GenericParameterizedType;
import io.clientcore.core.util.Union;
import org.junit.jupiter.api.Test;

import java.util.List;

public class UnionJavaDocCodeSnippets {

    @Test
    public void unionCreation() {
        // BEGIN: io.clientcore.core.util.union.UnionJavaDocCodeSnippetsBasic
        Union union = Union.ofTypes(String.class, Integer.class);
        // END: io.clientcore.core.util.union.UnionJavaDocCodeSnippetsBasic

        // BEGIN: io.clientcore.core.util.union.UnionJavaDocCodeSnippetsPrimitiveType
        Union unionPrimitives = Union.ofTypes(int.class, double.class);
        // END: io.clientcore.core.util.union.UnionJavaDocCodeSnippetsPrimitiveType

        // BEGIN: io.clientcore.core.util.union.UnionJavaDocCodeSnippetsCollectionType
        Union unionCollections = Union.ofTypes(
            new GenericParameterizedType(List.class, String.class),
            new GenericParameterizedType(List.class, Integer.class));
        // END: io.clientcore.core.util.union.UnionJavaDocCodeSnippetsCollectionType
    }

    @Test
    public void unionConsumeSwitch() {
        // BEGIN: io.clientcore.core.util.union.UnionJavaDocCodeSnippetsSwitch
        Union union = Union.ofTypes(String.class, Integer.class);
        union.setValue("Hello");
        switch (union.getValue()) {
            case String s -> System.out.println("String value: " + s);
            case Integer i -> System.out.println("Integer value: " + i);
            default -> throw new IllegalArgumentException(
                "Unknown type: " + union.getCurrentType().getTypeName());
        }
        // END: io.clientcore.core.util.union.UnionJavaDocCodeSnippetsSwitch
    }

    @Test
    public void unionConsumeLambda() {
        // BEGIN: io.clientcore.core.util.union.UnionJavaDocCodeSnippetsLambda
        Union union = Union.ofTypes(String.class, Integer.class);
        union.setValue("Hello");
        union.tryConsume(
            v -> System.out.println("String value: " + v), String.class);
        union.tryConsume(
            v -> System.out.println("Integer value: " + v), Integer.class);
        // END: io.clientcore.core.util.union.UnionJavaDocCodeSnippetsLambda
    }

}
