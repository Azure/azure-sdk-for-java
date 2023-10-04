// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/** Type of content source. */
public final class ContentSourceKind extends ExpandableStringEnum<ContentSourceKind> {

    /**
     * Creates or finds a ContentSourceKind from its string representation.
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ContentSourceKind() {
    }

    /** Enum value azureBlob. */
    public static final ContentSourceKind AZURE_BLOB = fromString("azureBlob");

    /** Enum value azureBlobFileList. */
    public static final ContentSourceKind AZURE_BLOB_FILE_LIST = fromString("azureBlobFileList");

    /**
     * Creates or finds a ContentSourceKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ContentSourceKind.
     */
    @JsonCreator
    public static ContentSourceKind fromString(String name) {
        return fromString(name, ContentSourceKind.class);
    }

    /**
     * Gets known ContentSourceKind values.
     *
     * @return known ContentSourceKind values.
     */
    public static Collection<ContentSourceKind> values() {
        return values(ContentSourceKind.class);
    }
}
