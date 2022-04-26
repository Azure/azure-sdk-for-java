// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.condition;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class PropertyCondition extends SpringBootCondition {

    public static final String PROPERTY_SUFFIX = ".";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        List<AnnotationAttributes> allAnnotationAttributes = annotationAttributesFromMultiValueMap(
            metadata.getAllAnnotationAttributes(getAnnotationName()));
        List<ConditionMessage> noMatch = new ArrayList<>();
        List<ConditionMessage> match = new ArrayList<>();
        for (AnnotationAttributes annotationAttributes : allAnnotationAttributes) {
            ConditionOutcome outcome = determineOutcome(annotationAttributes, context.getEnvironment());
            (outcome.isMatch() ? match : noMatch).add(outcome.getConditionMessage());
        }
        if (!noMatch.isEmpty()) {
            return ConditionOutcome.noMatch(ConditionMessage.of(noMatch));
        }
        return ConditionOutcome.match(ConditionMessage.of(match));
    }

    protected abstract ConditionOutcome determineOutcome(AnnotationAttributes annotationAttributes,
                                                         PropertyResolver resolver);

    protected abstract String getAnnotationName();

    protected List<AnnotationAttributes> annotationAttributesFromMultiValueMap(
        MultiValueMap<String, Object> multiValueMap) {
        List<Map<String, Object>> maps = new ArrayList<>();

        if (multiValueMap != null) {
            multiValueMap.forEach((key, value) -> {
                for (int i = 0; i < value.size(); i++) {
                    Map<String, Object> map;
                    if (i < maps.size()) {
                        map = maps.get(i);
                    } else {
                        map = new HashMap<>();
                        maps.add(map);
                    }
                    map.put(key, value.get(i));
                }
            });
        }

        List<AnnotationAttributes> annotationAttributes = new ArrayList<>(maps.size());
        for (Map<String, Object> map : maps) {
            annotationAttributes.add(AnnotationAttributes.fromMap(map));
        }
        return annotationAttributes;
    }
}
