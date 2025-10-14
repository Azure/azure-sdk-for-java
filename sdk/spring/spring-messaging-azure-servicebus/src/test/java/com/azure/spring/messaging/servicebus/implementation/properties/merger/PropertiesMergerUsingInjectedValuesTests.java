// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger;

import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProducerProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.azure.spring.messaging.servicebus.implementation.properties.merger.util.TestPropertiesComparer.isMergedPropertiesCorrect;
import static com.azure.spring.messaging.servicebus.implementation.properties.merger.util.TestPropertiesValueInjectHelper.injectPseudoPropertyValues;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertiesMergerUsingInjectedValuesTests {

    @Test
    void allParentPropertiesWillBeMergedBySenderMerger() {
        // Arrange
        ProducerProperties child = new ProducerProperties();

        NamespaceProperties parent = new NamespaceProperties();
        injectPseudoPropertyValues(parent, List.of("cloudType"), "FullyQualifiedNamespace");

        // Action
        SenderPropertiesParentMerger merger = new SenderPropertiesParentMerger();
        ProducerProperties result = merger.merge(child, parent);

        // Assertion
        assertTrue(isMergedPropertiesCorrect(parent, child, result));
    }

    @Test
    void allChildPropertiesWillBeMergedBySenderMerger() {
        // Arrange
        ProducerProperties child = new ProducerProperties();
        injectPseudoPropertyValues(child, List.of("cloudType"), "FullyQualifiedNamespace");

        NamespaceProperties parent = new NamespaceProperties();

        // Action
        SenderPropertiesParentMerger merger = new SenderPropertiesParentMerger();
        ProducerProperties result = merger.merge(child, parent);

        // Assertion
        assertTrue(isMergedPropertiesCorrect(child, child, result));
    }

    @Test
    void allParentPropertiesWillBeMergedByProcessorMerger() {
        // Arrange
        ProcessorProperties child = new ProcessorProperties();

        NamespaceProperties parent = new NamespaceProperties();
        injectPseudoPropertyValues(parent, List.of("cloudType"), "FullyQualifiedNamespace");

        // Action
        ProcessorPropertiesParentMerger merger = new ProcessorPropertiesParentMerger();
        ProcessorProperties result = merger.merge(child, parent);

        // Assertion
        assertTrue(isMergedPropertiesCorrect(parent, child, result));
    }

    @Test
    void allChildPropertiesWillBeMergedByProcessorMerger() {
        // Arrange
        ProcessorProperties child = new ProcessorProperties();
        injectPseudoPropertyValues(child, List.of("cloudType"), "FullyQualifiedNamespace");

        NamespaceProperties parent = new NamespaceProperties();

        // Action
        ProcessorPropertiesParentMerger merger = new ProcessorPropertiesParentMerger();
        ProcessorProperties result = merger.merge(child, parent);

        // Assertion
        assertTrue(isMergedPropertiesCorrect(child, child, result));
    }
}
