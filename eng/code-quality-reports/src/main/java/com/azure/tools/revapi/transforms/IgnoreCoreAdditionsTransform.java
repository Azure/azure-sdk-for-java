package com.azure.tools.revapi.transforms;

import org.revapi.Archive;
import org.revapi.Difference;
import org.revapi.Element;
import org.revapi.TransformationResult;
import org.revapi.base.BaseDifferenceTransform;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

public class IgnoreCoreAdditionsTransform<E extends Element<E>> extends BaseDifferenceTransform<E> {
    private static final Pattern CORE_ARCHIVE = Pattern.compile("com\\.azure:azure-core:.*");
    private static final String SUPPLEMENTARY = Archive.Role.SUPPLEMENTARY.toString();

    @Override
    public Pattern[] getDifferenceCodePatterns() {
        // This indicates to RevApi that all differences should be inspected by this transform.
        return new Pattern[] { Pattern.compile(".*") };
    }

    @Override
    public String getExtensionId() {
        // Used to configure this transform in revapi.json
        return "ignore-dependency-core-api-changes";
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

        // The difference is from azure-core and azure-core is a dependency to this SDK, discard the difference
        // since it should be ignored.
        return TransformationResult.discard();
    }
}
