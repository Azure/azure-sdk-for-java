// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.linting.extensions.revapi.transforms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.revapi.AnalysisContext;

import javax.lang.model.element.Element;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility class containing methods to work with RevApi.
 */
final class RevApiUtils {
    private static final String CONFIGURATION_ERROR_MESSAGE = "Configuration '%s' must be an object where each key is "
        + "an initial prefix to be matched and the value is an array of strings representing sub-prefixes to match, "
        + "where an empty array of sub-prefixes indicates only the initial prefix needs to be matched.";

    /**
     * Finds the outermost class {@link TypeElement} for the given {@link Element}.
     *
     * @param el The element to find the outermost class for.
     * @return The outermost class for the given element, may return null.
     */
    static TypeElement findOuterMostClass(Element el) {
        while (el != null && !(el instanceof TypeElement)) {
            el = el.getEnclosingElement();
        }

        if (el == null) {
            return null;
        }

        return ((TypeElement) el).getNestingKind() == NestingKind.TOP_LEVEL
            ? (TypeElement) el
            : findOuterMostClass(el.getEnclosingElement());
    }

    static List<PrefixMatcher> createPrefixMatchersFromConfiguration(AnalysisContext analysisContext,
        String propertyName) {
        JsonNode configuration = analysisContext.getConfigurationNode().get(propertyName);
        if (configuration == null) {
            return Collections.emptyList();
        }

        if (!configuration.isObject()) {
            throw new IllegalArgumentException(String.format(CONFIGURATION_ERROR_MESSAGE, propertyName));
        }

        List<PrefixMatcher> prefixMatchers = new ArrayList<>();
        ObjectNode prefixesObject = (ObjectNode) configuration;
        for (Iterator<Map.Entry<String, JsonNode>> it = prefixesObject.fields(); it.hasNext();) {
            Map.Entry<String, JsonNode> allowedPrefixKvp = it.next();
            String initialPrefix = allowedPrefixKvp.getKey();
            if (initialPrefix.isEmpty()) {
                throw new IllegalArgumentException(String.format(CONFIGURATION_ERROR_MESSAGE, propertyName));
            }

            JsonNode allowedPrefix = allowedPrefixKvp.getValue();
            if (!allowedPrefix.isArray()) {
                throw new IllegalArgumentException(String.format(CONFIGURATION_ERROR_MESSAGE, propertyName));
            }

            ArrayNode subPrefixes = (ArrayNode) allowedPrefix;
            List<String> subPrefixList = new ArrayList<>();

            for (JsonNode subPrefix : subPrefixes) {
                if (!subPrefix.isTextual()) {
                    throw new IllegalArgumentException(String.format(CONFIGURATION_ERROR_MESSAGE, propertyName));
                }

                subPrefixList.add(subPrefix.asText());
            }

            prefixMatchers.add(new PrefixMatcher(initialPrefix, subPrefixList));
        }

        return prefixMatchers;
    }

    private RevApiUtils() {
        // Private constructor to prevent instantiation.
    }
}
