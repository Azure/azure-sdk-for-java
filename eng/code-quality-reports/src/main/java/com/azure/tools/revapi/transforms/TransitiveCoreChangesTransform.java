// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.revapi.transforms;

import org.revapi.Archive;
import org.revapi.Criticality;
import org.revapi.Difference;
import org.revapi.Element;
import org.revapi.TransformationResult;
import org.revapi.base.BaseDifferenceTransform;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * Transform that runs after RevApi generates API differences that removes transitive azure-core changes from the
 * flagged differences set.
 *
 * @param <E> Type of element to transform.
 */
public final class TransitiveCoreChangesTransform<E extends Element<E>> extends BaseDifferenceTransform<E> {
    private static final Pattern CORE_ARCHIVE = Pattern.compile("com\\.azure:azure-core:.*");
    private static final String SUPPLEMENTARY = Archive.Role.SUPPLEMENTARY.toString();

    @Override
    public Pattern[] getDifferenceCodePatterns() {
        // This indicates to RevApi that all differences should be inspected by this transform.
        return new Pattern[] { Pattern.compile(".*") };
    }

    @Override
    public String getExtensionId() {
        // Used to configure this transform in the RevApi pipeline.
        return "transitive-core-changes";
    }

    @Override
    public TransformationResult tryTransform(@Nullable E oldElement, @Nullable E newElement, Difference difference) {
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

        if (!CORE_ARCHIVE.matcher(newArchive).matches()) {
            // The difference isn't from the azure-core SDK, keep the current result.
            return TransformationResult.keep();
        }

        // Only transform the Difference if the criticality is an error. If this isn't guarded it results in an
        // infinite transformation loop as RevApi will keep running the transformation pipeline until there are no
        // transformations applied in the pipeline run.
        if (difference.criticality == Criticality.ERROR) {
            // The difference is from azure-core and azure-core is a dependency to this SDK, discard it for now.
            // In the future this could retain it with a lower criticality level for informational reasons.
            return TransformationResult.discard();
        } else {
            return TransformationResult.keep();
        }
    }
}
