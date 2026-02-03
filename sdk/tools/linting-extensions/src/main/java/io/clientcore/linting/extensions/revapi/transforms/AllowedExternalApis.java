// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.revapi.transforms;

import com.fasterxml.jackson.databind.JsonNode;
import org.revapi.AnalysisContext;
import org.revapi.Difference;
import org.revapi.Element;
import org.revapi.TransformationResult;
import org.revapi.base.BaseDifferenceTransform;
import org.revapi.java.spi.JavaTypeElement;

import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static io.clientcore.linting.extensions.revapi.transforms.RevApiUtils.createPrefixMatchersFromConfiguration;
import static io.clientcore.linting.extensions.revapi.transforms.RevApiUtils.findOuterMostClass;

/**
 * RevApi transformation that allows for ignoring external APIs that are allowed to be exposed.
 *
 * @param <E> Type of element to transform.
 */
public final class AllowedExternalApis<E extends Element<E>> extends BaseDifferenceTransform<E> {
    private boolean enabled = false;
    private List<PrefixMatcher> allowedPrefixes = Collections.emptyList();

    /**
     * Creates a new instance of {@link AllowedExternalApis}.
     */
    public AllowedExternalApis() {
    }

    @Override
    public List<Predicate<String>> getDifferenceCodePredicates() {
        return Collections.singletonList("java.class.externalClassExposedInAPI"::equals);
    }

    @Override
    public TransformationResult tryTransform(E oldElement, E newElement, Difference difference) {
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
    public void initialize(AnalysisContext analysisContext) {
        JsonNode enabledNode = analysisContext.getConfigurationNode().get("enabled");
        this.enabled = enabledNode != null && enabledNode.isBoolean() && enabledNode.booleanValue();
        this.allowedPrefixes = createPrefixMatchersFromConfiguration(analysisContext, "allowedPrefixes");
    }
}
