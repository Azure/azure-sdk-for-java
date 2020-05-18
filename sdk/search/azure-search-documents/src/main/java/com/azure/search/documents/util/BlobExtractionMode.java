// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Defines which parts of a blob will be indexed by the blob storage indexer.
 * @see <a href="https://docs.microsoft.com/azure/search/search-howto-indexing-azure-blob-storage" />
 */
public class BlobExtractionMode extends ExpandableStringEnum<BlobExtractionMode> {

    /**
     * Specifies that only the standard blob properties and user-specified metadata will be indexed.
     * @see <a href="https://docs.microsoft.com/azure/storage/storage-properties-metadata" />
     */
    public static final BlobExtractionMode STORAGE_METADATA = fromString("storageMetadata");

    /**
     * Specifies that storage metadata and the content-type specific metadata extracted from the blob content will be
     * indexed.
     * @see
     * <a href="https://docs.microsoft.com/azure/search/search-howto-indexing-azure-blob-storage#content-type-specific-metadata-properties"/>
     */
    public static final BlobExtractionMode ALL_METADATA = fromString("allMetadata");

    /**
     * Specifies that all metadata and textual content extracted from the blob will be indexed.
     * This is the default value.
     * @see
     * <a href="https://docs.microsoft.com/azure/search/search-howto-indexing-azure-blob-storage#document-extraction-process"/>
     */
    public static final BlobExtractionMode CONTENT_AND_METADATA = fromString("contentAndMetadata");

    /**
     * Creates or finds a BlobExtractionMode from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding BlobExtractionMode.
     */
    @JsonCreator
    private static BlobExtractionMode fromString(String name) {
        return fromString(name, BlobExtractionMode.class);
    }

    /**
     * @return known BlobExtractionMode values.
     */
    public static Collection<BlobExtractionMode> values() {
        return values(BlobExtractionMode.class);
    }
}
