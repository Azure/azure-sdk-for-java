// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.revapi.transforms;

import com.fasterxml.jackson.databind.JsonNode;
import org.revapi.AnalysisContext;
import org.revapi.Archive;
import org.revapi.Criticality;
import org.revapi.Difference;
import org.revapi.Element;
import org.revapi.TransformationResult;
import org.revapi.base.BaseDifferenceTransform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Transform that runs after RevApi generates API differences that removes transitive changes from the flagged
 * differences set.
 *
 * @param <E> Type of element to transform.
 */
public final class IgnoredTransitiveChangesTransform<E extends Element<E>> extends BaseDifferenceTransform<E> {
    private static final String CONFIGURATION_ERROR_MESSAGE = "Configuration 'ignoredNewArchives' must be an array "
        + "of strings representing a Maven groupId:artifactId pair to ignore transitive changes from.";

    private static final String SUPPLEMENTARY = Archive.Role.SUPPLEMENTARY.toString();

    private boolean enabled = false;
    private List<String> ignoredNewArchives = Collections.emptyList();

    /**
     * Creates a new instance of {@link IgnoredTransitiveChangesTransform}.
     */
    public IgnoredTransitiveChangesTransform() {
    }

    @Override
    public List<Predicate<String>> getDifferenceCodePredicates() {
        return Collections.singletonList(ignored -> true);
    }

    @Override
    public String getExtensionId() {
        // Used to configure this transform in the RevApi pipeline.
        return "ignored-transitive-changes";
    }

    @Override
    public void initialize(AnalysisContext analysisContext) {
        JsonNode enabledNode = analysisContext.getConfigurationNode().get("enabled");
        this.enabled = enabledNode != null && enabledNode.isBoolean() && enabledNode.booleanValue();
        JsonNode configuration = analysisContext.getConfigurationNode().get("ignoredNewArchives");
        if (configuration != null) {
            if (!configuration.isArray()) {
                throw new IllegalArgumentException(CONFIGURATION_ERROR_MESSAGE);
            }

            List<String> ignoredNewArchives = new ArrayList<>();
            for (JsonNode node : configuration) {
                if (!node.isTextual()) {
                    throw new IllegalArgumentException(CONFIGURATION_ERROR_MESSAGE);
                }

                ignoredNewArchives.add(node.asText());
            }

            this.ignoredNewArchives = ignoredNewArchives;
        }
    }

    @Override
    public TransformationResult tryTransform(E oldElement, E newElement, Difference difference) {
        if (!enabled) {
            // If this transform isn't enabled, keep the current result.
            return TransformationResult.keep();
        }

        // RevApi will always add newArchive and newArchiveRole together.
        String newArchive = difference.attachments.get("newArchive");
        String newArchiveRole = difference.attachments.get("newArchiveRole");

        if (newArchive == null) {
            // No new archive, keep the current result.
            return TransformationResult.keep();
        }

        if (!SUPPLEMENTARY.equalsIgnoreCase(newArchiveRole)) {
            // The difference isn't from a dependency, keep the current result.
            return TransformationResult.keep();
        }

        boolean shouldKeep = true;
        for (String ignoredNewArchive : ignoredNewArchives) {
            if (newArchive.startsWith(ignoredNewArchive)) {
                shouldKeep = false;
                break;
            }
        }

        if (shouldKeep) {
            // The difference didn't match any 'ignoredNewArchives', keep the current result.
            return TransformationResult.keep();
        }

        // Only transform the Difference if the criticality is an error. If this isn't guarded it results in an
        // infinite transformation loop as RevApi will keep running the transformation pipeline until there are no
        // transformations applied in the pipeline run.
        if (difference.criticality == Criticality.ERROR) {
            // The difference is from an ignored new archive, discard it.
            // In the future this could retain it with a lower criticality level for informational reasons.
            return TransformationResult.discard();
        } else {
            return TransformationResult.keep();
        }
    }
}
