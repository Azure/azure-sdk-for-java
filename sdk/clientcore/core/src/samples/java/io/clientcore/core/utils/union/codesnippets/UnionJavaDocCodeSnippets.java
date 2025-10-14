// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils.union.codesnippets;

import io.clientcore.core.implementation.GenericParameterizedType;
import io.clientcore.core.utils.Union;
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
        // GenericParameterizedType is a non-public helper class that allows us to specify a generic type with
        // a class and a type. User can define any similar class to achieve the same functionality.
        Union unionCollections = Union.ofTypes(
            new GenericParameterizedType(List.class, String.class),
            new GenericParameterizedType(List.class, Integer.class));
        // END: io.clientcore.core.util.union.UnionJavaDocCodeSnippetsCollectionType
    }

    @Test
    public void unionConsumeIfElseStatement() {
        // BEGIN: io.clientcore.core.util.union.UnionJavaDocCodeSnippetsIfElseStatement
        Union union = Union.ofTypes(String.class, Integer.class);
        union.setValue("Hello");
        Object value = union.getValue();
        // we can write an if-else block to consume the value in Java 8+, or switch pattern match in Java 17+
        if (value instanceof String) {
            String s = (String) value;
            System.out.println("String value: " + s);
        } else if (value instanceof Integer) {
            Integer i = (Integer) value;
            System.out.println("Integer value: " + i);
        } else {
            throw new IllegalArgumentException("Unknown type: " + union.getCurrentType().getTypeName());
        }
        // END: io.clientcore.core.util.union.UnionJavaDocCodeSnippetsIfElseStatement
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
