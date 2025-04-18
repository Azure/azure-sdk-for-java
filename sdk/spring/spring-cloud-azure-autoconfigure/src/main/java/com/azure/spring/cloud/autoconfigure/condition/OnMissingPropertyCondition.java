// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.condition;

import com.azure.spring.cloud.core.implementation.util.AzureStringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class OnMissingPropertyCondition extends PropertyCondition {

    @Override
    protected String getAnnotationName() {
        return ConditionalOnMissingProperty.class.getName();
    }

    protected ConditionOutcome determineOutcome(AnnotationAttributes annotationAttributes, PropertyResolver resolver) {
        OnMissingPropertyCondition.Spec spec = new OnMissingPropertyCondition.Spec(annotationAttributes);
        List<String> foundProperties = new ArrayList<>();
        spec.collectProperties(resolver, foundProperties);

        if (!foundProperties.isEmpty()) {
            return ConditionOutcome.noMatch(ConditionMessage
                .forCondition(ConditionalOnMissingProperty.class, spec)
                .found("property", "properties")
                .items(ConditionMessage.Style.QUOTE, foundProperties));
        }

        return ConditionOutcome
            .match(ConditionMessage.forCondition(ConditionalOnAnyProperty.class, spec).because("matched"));
    }

    private static class Spec {

        private final String prefix;

        private final String[] names;

        Spec(AnnotationAttributes annotationAttributes) {
            String prefixAttr = annotationAttributes.getString("prefix");
            this.prefix = AzureStringUtils.ensureEndsWithSuffix(prefixAttr.trim(), PROPERTY_SUFFIX);
            this.names = getNames(annotationAttributes);
        }

        private String[] getNames(Map<String, Object> annotationAttributes) {
            String[] value = (String[]) annotationAttributes.get("value");
            String[] name = (String[]) annotationAttributes.get("name");
            Assert.state(value.length > 0 || name.length > 0,
                "The name or value attribute of @ConditionalOnMissingProperty must be specified");
            Assert.state(value.length == 0 || name.length == 0,
                "The name and value attributes of @ConditionalOnMissingProperty are exclusive");
            return (value.length > 0) ? value : name;
        }

        private void collectProperties(PropertyResolver resolver, List<String> nonMatching) {
            for (String name : this.names) {
                String key = this.prefix + name;
                if (resolver.containsProperty(key)) {
                    nonMatching.add(name);
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("(");
            result.append(this.prefix);
            if (this.names.length == 1) {
                result.append(this.names[0]);
            } else {
                result.append("[");
                result.append(StringUtils.arrayToCommaDelimitedString(this.names));
                result.append("]");
            }
            result.append(")");
            return result.toString();
        }

    }

}
