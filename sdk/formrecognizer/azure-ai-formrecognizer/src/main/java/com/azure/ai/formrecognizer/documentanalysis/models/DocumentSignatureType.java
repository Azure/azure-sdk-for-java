// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for DocumentSignatureType. */
@Immutable
public final class DocumentSignatureType extends ExpandableStringEnum<DocumentSignatureType> {

    /**
     * Creates a DocumentSignatureType object.
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public DocumentSignatureType() {
    }

    /** Static value signed for DocumentSignatureType. */
    public static final DocumentSignatureType SIGNED = fromString("signed");

    /** Static value unsigned for DocumentSignatureType. */
    public static final DocumentSignatureType UNSIGNED = fromString("unsigned");

    /**
     * Creates or finds a DocumentSignatureType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DocumentSignatureType.
     */
    public static DocumentSignatureType fromString(String name) {
        return fromString(name, DocumentSignatureType.class);
    }

    /**
     * Returns  known DocumentSignatureType values.
     * @return known DocumentSignatureType values.
     */
    public static Collection<DocumentSignatureType> values() {
        return values(DocumentSignatureType.class);
    }
}
