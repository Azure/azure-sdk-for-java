// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger;

import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class TestPropertiesComparer {

    // we need a way to compare values using reflection
    static <T, S> boolean mergeValueCorrect(T parent, S child, S result, String... ignoredVariableNames) {
        Set<String> ignoredMemberVariableNames = new HashSet<>(Arrays.asList(ignoredVariableNames));

        // compare value retrieved from parent, child and result

        // todo
        BeanUtils.copyProperties();

    }

}
