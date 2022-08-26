// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for DocumentOperationStatus. */
@Immutable
public final class DocumentOperationStatus extends ExpandableStringEnum<DocumentOperationStatus> {
    /** Static value notStarted for DocumentOperationStatus. */
    public static final DocumentOperationStatus NOT_STARTED = fromString("notStarted");

    /** Static value running for DocumentOperationStatus. */
    public static final DocumentOperationStatus RUNNING = fromString("running");

    /** Static value failed for DocumentOperationStatus. */
    public static final DocumentOperationStatus FAILED = fromString("failed");

    /** Static value succeeded for DocumentOperationStatus. */
    public static final DocumentOperationStatus SUCCEEDED = fromString("succeeded");

    /** Static value canceled for DocumentOperationStatus. */
    public static final DocumentOperationStatus CANCELED = fromString("canceled");

    /**
     * Creates or finds a DocumentOperationStatus from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DocumentOperationStatus.
     */
    public static DocumentOperationStatus fromString(String name) {
        return fromString(name, DocumentOperationStatus.class);
    }

    /** @return known DocumentOperationStatus values. */
    public static Collection<DocumentOperationStatus> values() {
        return values(DocumentOperationStatus.class);
    }
}
