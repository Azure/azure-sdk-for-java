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

class OnAnyPropertyCondition extends PropertyCondition {

    @Override
    protected String getAnnotationName() {
        return ConditionalOnAnyProperty.class.getName();
    }

    protected ConditionOutcome determineOutcome(AnnotationAttributes annotationAttributes, PropertyResolver resolver) {
        OnAnyPropertyCondition.Spec spec = new OnAnyPropertyCondition.Spec(annotationAttributes);
        List<String> missingProperties = new ArrayList<>();
        List<String> nonMatchingProperties = new ArrayList<>();
        List<String> matchingProperties = new ArrayList<>();
        spec.collectProperties(resolver, missingProperties, nonMatchingProperties, matchingProperties);

        if (matchingProperties.isEmpty()) {
            if (!missingProperties.isEmpty()) {
                return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnAnyProperty.class, spec)
                                                                .didNotFind("property", "properties")
                                                                .items(ConditionMessage.Style.QUOTE, missingProperties));
            }
            if (!nonMatchingProperties.isEmpty()) {
                return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnAnyProperty.class, spec)
                                                                .found("different value in property", "different "
                                                                    + "value in properties")
                                                                .items(ConditionMessage.Style.QUOTE,
                                                                    nonMatchingProperties));
            }
        }

        return ConditionOutcome
            .match(ConditionMessage.forCondition(ConditionalOnAnyProperty.class, spec).because("matched"));
    }

    private static class Spec {

        private final String prefix;

        private final String[] prefixes;

        private final String havingValue;

        private final String[] names;

        private final boolean matchIfMissing;

        Spec(AnnotationAttributes annotationAttributes) {
            String prefixAttr = annotationAttributes.getString("prefix");
            prefix = AzureStringUtils.ensureEndsWithSuffix(prefixAttr.trim(), PROPERTY_SUFFIX);
            this.prefixes = getPrefixes(annotationAttributes);
            this.havingValue = annotationAttributes.getString("havingValue");
            this.names = getNames(annotationAttributes);
            this.matchIfMissing = annotationAttributes.getBoolean("matchIfMissing");
        }

        private String[] getNames(Map<String, Object> annotationAttributes) {
            String[] value = (String[]) annotationAttributes.get("value");
            String[] name = (String[]) annotationAttributes.get("name");
            Assert.state(value.length > 0 || name.length > 0,
                "The name or value attribute of @ConditionalOnAnyProperty must be specified");
            Assert.state(value.length == 0 || name.length == 0,
                "The name and value attributes of @ConditionalOnAnyProperty are exclusive");
            return (value.length > 0) ? value : name;
        }

        private String[] getPrefixes(Map<String, Object> annotationAttributes) {
            String[] prefixesAttr = (String[]) annotationAttributes.get("prefixes");
            for(int i = 0; i < prefixesAttr.length; i++){
                prefixesAttr[i] = AzureStringUtils.ensureEndsWithSuffix(prefixesAttr[i].trim(), PROPERTY_SUFFIX);
            }
            return prefixesAttr;
        }

        private void collectProperties(PropertyResolver resolver, List<String> missing, List<String> nonMatching,
                                       List<String> matching) {
            for (String name : this.names) {
                String key = this.prefix + name;
                if (resolver.containsProperty(key)) {
                    if (!isMatch(resolver.getProperty(key), this.havingValue)) {
                        nonMatching.add(name);
                    } else {
                        matching.add(name);
                    }
                } else {
                    if (!this.matchIfMissing) {
                        missing.add(name);
                    } else {
                        matching.add(name);
                    }
                }
            }
            for (String prefix : this.prefixes) {
                for (String name : this.names) {
                    String key = prefix + name;
                    if (resolver.containsProperty(key)) {
                        if (!isMatch(resolver.getProperty(key), this.havingValue)) {
                            nonMatching.add(name);
                        } else {
                            matching.add(name);
                        }
                    } else {
                        if (!this.matchIfMissing) {
                            missing.add(name);
                        } else {
                            matching.add(name);
                        }
                    }
                }
            }
        }

        private boolean isMatch(String value, String requiredValue) {
            if (StringUtils.hasLength(requiredValue)) {
                return requiredValue.equalsIgnoreCase(value);
            }
            return !"false".equalsIgnoreCase(value);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("(");
            if (this.prefix.length() > 1) {
                result.append(this.prefix);
            } else if (this.prefixes.length > 0) {
                if (this.prefixes.length == 1) {
                    result.append(this.prefixes[0]);
                } else {
                    result.append("[");
                    result.append(StringUtils.arrayToCommaDelimitedString(this.prefixes));
                    result.append("]");
                }
            }
            if (this.names.length == 1) {
                result.append(this.names[0]);
            } else {
                result.append("[");
                result.append(StringUtils.arrayToCommaDelimitedString(this.names));
                result.append("]");
            }
            if (StringUtils.hasLength(this.havingValue)) {
                result.append("=").append(this.havingValue);
            }
            result.append(")");
            return result.toString();
        }

    }

}
