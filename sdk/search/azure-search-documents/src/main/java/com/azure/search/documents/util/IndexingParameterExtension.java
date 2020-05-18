// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.IndexingParameters;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Defines extension methods for the IndexingParameters class.
 */
public class IndexingParameterExtension {
    private static final ClientLogger LOGGER = new ClientLogger(IndexingParameterExtension.class);
    private static final String PARSING_MODE_KEY = "parsingMode";
    private static final String DELIMITER = ",";

    /**
     * Specifies that the indexer will index only the blobs with the file name extensions you specify. Each
     * string is a file extensions with a leading dot. For example, ".pdf", ".docx", etc.
     * If you pass the same file extension to this method and excludeFileNameExtensions,
     * blobs with that extension will be excluded from indexing (that is, excludeFileNameExtensions takes precedence).
     * @see <a href="https://docs.microsoft.com/azure/search/search-howto-indexing-azure-blob-storage"/> for details.
     *
     * This option only applies to indexers that index Azure Blob Storage.
     *
     * @param parameters IndexingParameters to configure.
     * @param extensions File extensions to include in indexing.
     * @return The {@link IndexingParameters} instance.
     */
    public static IndexingParameters indexFileNameExtensions(IndexingParameters parameters, String[] extensions) {
        if (extensions.length > 0) {
            String validExtensionString = Arrays.stream(extensions).map(IndexingParameterExtension::validateExtension)
                .map(IndexingParameterExtension::fixUpExtension).collect(Collectors.joining(DELIMITER));
            configure(parameters, "indexedFileNameExtensions", validExtensionString);
        }

        return parameters;
    }

    /**
     * Specifies that the indexer will index only the blobs without the file name extensions you specify. Each
     * string is a file extensions with a leading dot. For example, ".pdf", ".docx", etc.
     * If you pass the same file extension to this method and indexFileNameExtensions,
     * blobs with that extension will be excluded from indexing (that is, excludeFileNameExtensions takes precedence).
     * @see <a href="https://docs.microsoft.com/azure/search/search-howto-indexing-azure-blob-storage"/> for details.
     *
     * This option only applies to indexers that index Azure Blob Storage.
     *
     * @param parameters IndexingParameters to configure.
     * @param extensions File extensions to exclude from indexing.
     * @return The {@link IndexingParameters} instance.
     */
    public static IndexingParameters excludeFileNameExtensions(IndexingParameters parameters, String[] extensions) {
        if (extensions.length > 0) {
            String validExtensionString = Arrays.stream(extensions).map(IndexingParameterExtension::validateExtension)
                .map(IndexingParameterExtension::fixUpExtension).collect(Collectors.joining(DELIMITER));
            configure(parameters, "excludedFileNameExtensions", validExtensionString);
        }

        return parameters;
    }

    /**
     * Specifies which parts of a blob will be indexed by the blob storage indexer.
     * @see <a href="https://docs.microsoft.com/azure/search/search-howto-indexing-azure-blob-storage">
     *
     * This option only applies to indexers that index Azure Blob Storage.
     *
     * @param parameters IndexingParameters to configure.
     * @param extractionMode A {@link BlobExtractionMode} value specifying what to index
     * @return The {@link IndexingParameters} instance.
     */
    public static IndexingParameters setBlobExtractionMode(IndexingParameters parameters,
        BlobExtractionMode extractionMode) {
        configure(parameters, "dataToExtract", extractionMode.toString());
        return parameters;
    }


    /**
     * Tells the indexer to assume that all blobs contain JSON, which it will then parse such that each blob's JSON
     * will map to a single document in the search index.
     * @see <a href="https://docs.microsoft.com/azure/search/search-howto-index-json-blobs/"/> for details.
     *
     * This option only applies to indexers that index Azure Blob Storage.
     *
     * @param parameters IndexingParameters to configure.
     * @return The {@link IndexingParameters} instance.
     */
    public static IndexingParameters parseJson(IndexingParameters parameters) {
        configure(parameters, PARSING_MODE_KEY, "json");
        return parameters;
    }

    /**
     * Tells the indexer to assume that all blobs contain new-line separated JSON, which it will then parse such that
     * individual JSON entities in each blob will map to a single document in the search index.
     * @see <a href="https://docs.microsoft.com/azure/search/search-howto-index-json-blobs/" /> for details.
     *
     * This option only applies to indexers that index Azure Blob Storage.
     *
     * @param parameters IndexingParameters to configure.
     * @return The {@link IndexingParameters} instance.
     */
    public static IndexingParameters parseJsonLines(IndexingParameters parameters) {
        configure(parameters, PARSING_MODE_KEY, "jsonLines");
        return parameters;
    }

    /**
     * Tells the indexer to assume that all blobs contain JSON arrays, which it will then parse such that
     * each JSON object in each array will map to a single document in the search index.
     * @see <a href="https://docs.microsoft.com/azure/search/search-howto-index-json-blobs" /> for details.
     *
     * This option only applies to indexers that index Azure Blob Storage.
     *
     * @param parameters IndexingParameters to configure.
     * @param documentRoot An optional JSON Pointer that tells the indexer how to find the JSON array if it's not the
     * top-level JSON property of each blob. If this parameter is null or empty, the indexer will assume that
     * the JSON array can be found in the top-level JSON property of each blob. Default is null.
     * @return The {@link IndexingParameters} instance.
     */
    public static IndexingParameters parseJsonArrays(IndexingParameters parameters, String documentRoot) {
        configure(parameters, PARSING_MODE_KEY, "jsonArray");
        if (!CoreUtils.isNullOrEmpty(documentRoot)) {
            configure(parameters, "documentRoot", documentRoot);
        }
        return parameters;
    }

    /**
     * Tells the indexer to assume that all blobs are delimited text files. Currently only comma-separated value (CSV)
     * text files are supported.
     * @see <a href="https://docs.microsoft.com/azure/search/search-howto-index-csv-blobs" /> for details.
     *
     * This option only applies to indexers that index Azure Blob Storage.
     *
     * @param parameters IndexingParameters to configure.
     * @param headers Specifies column headers that the indexer will use to map values to specific fields
     * in the search index. If you don't specify any headers, the indexer assumes that the first non-blank line of
     * each blob contains comma-separated headers.
     * @return The {@link IndexingParameters} instance.
     */
    public static IndexingParameters parseDelimitedTextFiles(IndexingParameters parameters, String[] headers) {
        configure(parameters, PARSING_MODE_KEY, "delimitedText");

        if (headers == null || headers.length == 0) {
            configure(parameters, "firstLineContainsHeaders", true);
        } else {
            configure(parameters, "delimitedTextHeaders", String.join(DELIMITER, headers));
        }
        return parameters;
    }

    /**
     * Tells the indexer to assume that blobs should be parsed as text files in UTF-8 encoding.
     * @see
     * <a href="https://docs.microsoft.com/azure/search/search-howto-indexing-azure-blob-storage#indexing-plain-text"/>
     *
     * @param parameters IndexingParameters to configure.
     * @return The {@link IndexingParameters} instance.
     */
    public static IndexingParameters parseText(IndexingParameters parameters) {
        parseText(parameters, StandardCharsets.UTF_8);
        return parameters;
    }

    /**
     * Tells the indexer to assume that blobs should be parsed as text files in the desired encoding.
     * @see
     * <a href="https://docs.microsoft.com/azure/search/search-howto-indexing-azure-blob-storage#indexing-plain-text"/>
     *
     * @param parameters IndexingParameters to configure.
     * @param encoding Encoding used to read the text stored in blobs.
     * @return The {@link IndexingParameters} instance.
     */
    public static IndexingParameters parseText(IndexingParameters parameters, Charset encoding) {
        Objects.requireNonNull(encoding, "'Encoding' cannot be null");
        configure(parameters, PARSING_MODE_KEY, "text");
        configure(parameters, "encoding", encoding.name());
        return parameters;
    }

    private static void configure(IndexingParameters parameters, String key, Object value) {
        Objects.requireNonNull(parameters, "'Parameters' cannot be null.");
        if (parameters.getConfiguration() == null) {
            parameters.setConfiguration(new HashMap<>());
        }
        parameters.getConfiguration().put(key, value);
    }

    private static String validateExtension(String extension) {
        if (CoreUtils.isNullOrEmpty(extension)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Extension cannot be null or empty string."));
        }
        if (extension.contains("*")) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Extension cannot contain the wildcard character '*'."));
        }
        return extension;
    }


    private static String fixUpExtension(String extension) {
        if (extension.startsWith(".")) {
            return extension;
        }
        return "." + extension;
    }
}
