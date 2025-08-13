// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.linting.extensions.revapi.transforms;

import com.fasterxml.jackson.databind.JsonNode;
import org.revapi.AnalysisContext;
import org.revapi.Criticality;
import org.revapi.Difference;
import org.revapi.Element;
import org.revapi.TransformationResult;
import org.revapi.base.BaseDifferenceTransform;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static io.clientcore.linting.extensions.revapi.transforms.RevApiUtils.createPrefixMatchersFromConfiguration;

/**
 * Transform that runs after RevApi generates API differences that removes Jackson Databind changes from the flagged
 * differences set.
 *
 * @param <E> Type of element to transform.
 */
public final class IgnoredJacksonDatabindRemovalTransform<E extends Element<E>> extends BaseDifferenceTransform<E> {
    private boolean enabled = false;
    private List<PrefixMatcher> ignoredPackages = Collections.emptyList();

    /**
     * Creates a new instance of {@link IgnoredJacksonDatabindRemovalTransform}.
     */
    public IgnoredJacksonDatabindRemovalTransform() {
    }

    @Override
    public List<Predicate<String>> getDifferenceCodePredicates() {
        return Collections.singletonList("java.annotation.removed"::equals);
    }

    @Override
    public String getExtensionId() {
        // Used to configure this transform in the RevApi pipeline.
        return "ignored-jackson-databind-removal";
    }

    @Override
    public void initialize(AnalysisContext analysisContext) {
        JsonNode enabledNode = analysisContext.getConfigurationNode().get("enabled");
        this.enabled = enabledNode != null && enabledNode.isBoolean() && enabledNode.booleanValue();
        this.ignoredPackages = createPrefixMatchersFromConfiguration(analysisContext, "ignoredPackages");
    }

    @Override
    public TransformationResult tryTransform(E oldElement, E newElement, Difference difference) {
        if (!enabled) {
            // If this transform isn't enabled, keep the current result.
            return TransformationResult.keep();
        }

        // RevApi should add 'annotationType' as an attachment for 'java.annotation.removed' differences.
        String annotationType = difference.attachments.get("annotationType");

        if (difference.criticality != Criticality.ERROR) {
            // Only transform the Difference if the criticality is an error. If this isn't guarded it results in an
            // infinite transformation loop as RevApi will keep running the transformation pipeline until there are no
            // transformations applied in the pipeline run.
            return TransformationResult.keep();
        }

        if (annotationType == null || annotationType.isEmpty()) {
            // But if the annotationType wasn't included keep the current result as we can't determine if this is a
            // Jackson Databind change.
            return TransformationResult.keep();
        }

        if (!annotationType.contains("fasterxml.jackson") || !annotationType.contains("annotation")) {
            // The annotation isn't from Jackson Databind, keep the current result.
            return TransformationResult.keep();
        }

        // Now verify that this change is in a package that is allowed to make the change.
        String packageName = difference.attachments.get("package");

        if (packageName == null || packageName.isEmpty()) {
            // But if the package wasn't included keep the current result as we can't determine if this package is
            // allowed to remove Jackson Databind annotations.
            return TransformationResult.keep();
        }

        return shouldDiscard(packageName) ? TransformationResult.discard() : TransformationResult.keep();
    }

    private boolean shouldDiscard(String packageName) {
        for (PrefixMatcher prefixMatcher : ignoredPackages) {
            if (prefixMatcher.test(packageName)) {
                // The package matches one of the ignored packages, discard the change.
                return true;
            }
        }

        return false;
    }
}
