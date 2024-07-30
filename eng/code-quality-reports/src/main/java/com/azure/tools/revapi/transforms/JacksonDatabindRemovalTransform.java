// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.tools.revapi.transforms;

import org.revapi.Criticality;
import org.revapi.Difference;
import org.revapi.Element;
import org.revapi.TransformationResult;
import org.revapi.base.BaseDifferenceTransform;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * Transform that runs after RevApi generates API differences that removes Jackson Databind changes from the flagged
 * differences set.
 *
 * @param <E> Type of element to transform.
 */
public final class JacksonDatabindRemovalTransform<E extends Element<E>> extends BaseDifferenceTransform<E> {
    private static final Pattern DIFFERENCE_CODE_PATTERN = Pattern.compile("java\\.annotation\\.removed");

    @Override
    public Pattern[] getDifferenceCodePatterns() {
        // This indicates to RevApi that all differences should be inspected by this transform.
        return new Pattern[] { DIFFERENCE_CODE_PATTERN };
    }

    @Override
    public String getExtensionId() {
        // Used to configure this transform in the RevApi pipeline.
        return "jackson-databind-removal";
    }

    @Override
    public TransformationResult tryTransform(@Nullable E oldElement, @Nullable E newElement, Difference difference) {
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

    private static boolean shouldDiscard(String packageName) {
        if (!packageName.startsWith("com.azure.")) {
            // The package isn't from the Azure SDK, keep the current result.
            return false;
        }

        if (packageName.regionMatches(10, "containers.containerregistry.models", 0, 35)) {
            // Container Registry
            return true;
        } else if (packageName.regionMatches(10, "search.documents", 0, 16)) {
            // Search Documents
            return packageName.regionMatches(26, ".models", 0, 7)
                || packageName.regionMatches(26, ".indexes.models", 0, 13);
        } else if (packageName.regionMatches(10, "security.", 0, 9)) {
            if (packageName.regionMatches(19, "attestation.models", 0, 17)) {
                // Attestation
                return true;
            } else if (packageName.regionMatches(19, "keyvault.", 0, 9)) {
                // KeyVault
                return packageName.regionMatches(28, "administration.models", 0, 21)
                    || packageName.regionMatches(28, "certificates.models", 0, 19)
                    || packageName.regionMatches(28, "keys.models", 0, 11)
                    || packageName.regionMatches(28, "keys.cryptography.models", 0, 24);
            }
        } else if (packageName.regionMatches(10, "ai.", 0, 3)) {
            if (packageName.regionMatches(13, "textanalytics.models", 0, 20)) {
                // Text Analytics
                return true;
            } else if (packageName.regionMatches(13, "formrecognizer.", 0, 15)) {
                // Form Recognizer
                return packageName.regionMatches(28, "models", 0, 6)
                    || packageName.regionMatches(28, "training.models", 0, 15)
                    || packageName.regionMatches(28, "documentanalysis.models", 0, 23)
                    || packageName.regionMatches(28, "documentanalysis.administration.models", 0, 38);
            } else if (packageName.regionMatches(13, "metricsadvisor.", 0, 15)) {
                // Metrics Advisor
                return packageName.regionMatches(28, "models", 0, 6)
                    || packageName.regionMatches(28, "administration.models", 0, 21);
            } else if (packageName.regionMatches(13, "contentsafety.models", 0, 20)) {
                // Content Safety
                return true;
            }
        } else if (packageName.regionMatches(10, "messaging.", 0, 10)) {
            // Service Bus
            if (packageName.regionMatches(20, "servicebus.", 0, 11)) {
                return packageName.regionMatches(31, "models", 0, 6)
                    || packageName.regionMatches(31, "administration.models", 0, 21);
            } else if (packageName.regionMatches(20, "eventgrid.systemevents", 0, 22)) {
                // Event Grid
                return true;
            }
        } else if (packageName.regionMatches(10, "monitor.query.models", 0, 20)) {
            // Monitor Query
            return true;
        } else if (packageName.regionMatches(10, "data.tables.models", 0, 18)) {
            // Tables
            return true;
        } else if (packageName.regionMatches(10, "storage.", 0, 8)) {
            if (packageName.regionMatches(18, "file.datalake.models", 0, 20)) {
                // DataLake
                return true;
            } else if (packageName.regionMatches(18, "file.share.models", 0, 17)) {
                // Shares
                return true;
            } else if (packageName.regionMatches(18, "queue.models", 0, 12)) {
                // Queue
                return true;
            } else if (packageName.regionMatches(18, "blob.", 0, 5)) {
                // Blob
                return packageName.regionMatches(23, "models", 0, 6)
                    || packageName.regionMatches(23, "options", 0, 7);
            }
        } else if (packageName.regionMatches(10, "communication.", 0, 14)) {
            if (packageName.regionMatches(24, "jobrouter.models", 0, 16)) {
                // Communication Job Router
                return true;
            }
        }

        // The package is from the Azure SDK, but not in the allowed list, keep the current result.
        return false;
    }
}
