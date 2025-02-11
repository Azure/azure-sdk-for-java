// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.revapi.transforms;

import com.fasterxml.jackson.databind.JsonNode;
import org.revapi.AnalysisContext;
import org.revapi.Difference;
import org.revapi.Element;
import org.revapi.TransformationResult;
import org.revapi.base.BaseDifferenceTransform;
import org.revapi.java.spi.JavaTypeElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static com.azure.tools.revapi.transforms.RevApiUtils.createPrefixMatchersFromConfiguration;
import static com.azure.tools.revapi.transforms.RevApiUtils.findOuterMostClass;

/**
 * RevApi transformation that allows for ignoring external APIs that are allowed to be exposed.
 *
 * @param <E> Type of element to transform.
 */
public final class AllowedExternalApis<E extends Element<E>> extends BaseDifferenceTransform<E> {
    private static final Pattern DIFFERENCE_CODE_PATTERN = Pattern.compile("java.class.externalClassExposedInAPI",
        Pattern.LITERAL);

    private boolean enabled = false;
    private List<PrefixMatcher> allowedPrefixes = Collections.emptyList();

    @Override
    public Pattern[] getDifferenceCodePatterns() {
        return new Pattern[] { DIFFERENCE_CODE_PATTERN };
    }

    @Override
    public TransformationResult tryTransform(@Nullable E oldElement, @Nullable E newElement, Difference difference) {
        if (!enabled || newElement == null || allowedPrefixes.isEmpty()) {
            // Missing element to compare.
            return TransformationResult.keep();
        }

        if (!(newElement instanceof JavaTypeElement)) {
            // Unknown element type.
            return TransformationResult.keep();
        }

        TypeElement outermostElement = findOuterMostClass(((JavaTypeElement) newElement).getDeclaringElement());

        if (outermostElement == null) {
            return TransformationResult.keep();
        }

        String className = outermostElement.getQualifiedName().toString();

        for (PrefixMatcher prefixMatcher : allowedPrefixes) {
            if (prefixMatcher.test(className)) {
                return TransformationResult.discard();
            }
        }

        return TransformationResult.keep();
    }

    @Override
    public String getExtensionId() {
        return "allowed-external-apis";
    }

    @Override
    public void initialize(@Nonnull AnalysisContext analysisContext) {
        JsonNode enabledNode = analysisContext.getConfigurationNode().get("enabled");
        this.enabled = enabledNode != null && enabledNode.isBoolean() && enabledNode.booleanValue();
        this.allowedPrefixes = createPrefixMatchersFromConfiguration(analysisContext, "allowedPrefixes");
    }
}
