// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.search.documents.models.IndexingParameters;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexingParameterExtensionTests {
    private static final String INDEXED_FILENAME_EXTENSIONS_KEY = "indexedFileNameExtensions";
    private static final String EXCLUDE_FILENAME_EXTENSIONS_KEY = "excludedFileNameExtensions";
    private static final String BLOB_EXTRACTION_MODE_KEY = "dataToExtract";
    private static final String PARSING_MODE_KEY = "parsingMode";
    private static final String PARSING_JSON = "json";
    private static final String PARSING_JSON_LINES = "jsonLines";
    private static final String PARSING_JSON_ARRAYS = "jsonArray";
    private static final String DOCUMENT_ROOT_KEY = "documentRoot";
    private static final String DELIMITED_TEXT_HEADERS_KEY = "delimitedTextHeaders";
    private static final String PARSE_TEXT = "text";
    private static final String ENCODING_KEY = "encoding";
    private static final String FIRST_LINE_CONTAINS_HEADERS_KEY = "firstLineContainsHeaders";
    private static final String EMPTY_EXTENSIONS_MESSAGE = "Extension cannot be null or empty string.";
    private static final String INVALID_EXTENSIONS_MESSAGE = "Extension cannot contain the wildcard character '*'.";

    @Test
    public void includeFileNameExtension() {
        String[] extensions = new String[] {
            "pdf", "csv"
        };

        IndexingParameters indexingParameters = new IndexingParameters();
        assertNull(indexingParameters.getConfiguration());
        IndexingParameterExtension.indexFileNameExtensions(
            indexingParameters, extensions);
        assertEquals(".pdf,.csv", indexingParameters.getConfiguration().get(INDEXED_FILENAME_EXTENSIONS_KEY));
    }

    @Test
    public void includeFileNameExtensionEmptyExtensions() {
        String[] extensions = new String[] {
            "", null
        };

        IndexingParameters indexingParameters = new IndexingParameters();
        assertNull(indexingParameters.getConfiguration());
        assertThrows(IllegalArgumentException.class, () -> IndexingParameterExtension.indexFileNameExtensions(
            indexingParameters, extensions), EMPTY_EXTENSIONS_MESSAGE);
    }

    @Test
    public void includeFileNameExtensionWildCardExtensions() {
        String[] extensions = new String[] {
            ".*"
        };

        IndexingParameters indexingParameters = new IndexingParameters();
        assertNull(indexingParameters.getConfiguration());
        assertThrows(IllegalArgumentException.class, () -> IndexingParameterExtension.indexFileNameExtensions(
            indexingParameters, extensions), INVALID_EXTENSIONS_MESSAGE);
    }

    @Test
    public void excludeFileNameExtension() {
        String[] extensions = new String[] {
            "pdf", "csv"
        };

        IndexingParameters indexingParameters = new IndexingParameters();
        assertNull(indexingParameters.getConfiguration());
        IndexingParameterExtension.excludeFileNameExtensions(
            indexingParameters, extensions);
        assertEquals(".pdf,.csv", indexingParameters.getConfiguration().get(EXCLUDE_FILENAME_EXTENSIONS_KEY));
    }

    @Test
    public void excludeFileNameExtensionEmptyExtensions() {
        String[] extensions = new String[] {
            "", null
        };

        IndexingParameters indexingParameters = new IndexingParameters();
        assertNull(indexingParameters.getConfiguration());
        assertThrows(IllegalArgumentException.class, () -> IndexingParameterExtension.excludeFileNameExtensions(
            indexingParameters, extensions), EMPTY_EXTENSIONS_MESSAGE);
    }

    @Test
    public void excludeFileNameExtensionWildCardExtensions() {
        String[] extensions = new String[] {
            ".*"
        };

        IndexingParameters indexingParameters = new IndexingParameters();
        assertNull(indexingParameters.getConfiguration());
        assertThrows(IllegalArgumentException.class, () -> IndexingParameterExtension.excludeFileNameExtensions(
            indexingParameters, extensions), INVALID_EXTENSIONS_MESSAGE);
    }

    @Test
    public void setBlobExtractionMode() {
        assertEquals(BlobExtractionMode.ALL_METADATA.toString(),
            IndexingParameterExtension.setBlobExtractionMode(new IndexingParameters(), BlobExtractionMode.ALL_METADATA)
                .getConfiguration().get(BLOB_EXTRACTION_MODE_KEY));
    }

    @Test
    public void setBlobExtractionModeInvalidMode() {
        assertThrows(NullPointerException.class, () ->
            IndexingParameterExtension.setBlobExtractionMode(new IndexingParameters(), null));
    }

    @Test
    public void parseJson() {
        assertEquals(PARSING_JSON, IndexingParameterExtension.parseJson(new IndexingParameters())
                .getConfiguration().get(PARSING_MODE_KEY));
    }

    @Test
    public void parseJsonLines() {
        assertEquals(PARSING_JSON_LINES, IndexingParameterExtension.parseJsonLines(new IndexingParameters())
            .getConfiguration().get(PARSING_MODE_KEY));
    }

    @Test
    public void parseJsonArrays() {
        String documentRoot = "the root";
        IndexingParameters indexingParameter =
            IndexingParameterExtension.parseJsonArrays(new IndexingParameters(), documentRoot);
        assertEquals(PARSING_JSON_ARRAYS, indexingParameter.getConfiguration().get(PARSING_MODE_KEY));
        assertEquals(documentRoot, indexingParameter.getConfiguration().get(DOCUMENT_ROOT_KEY));
    }

    @Test
    public void parseDelimitedTextFiles() {
        String[] headers = new String[] {
            "hotelName", "hotelId", "address"
        };

        IndexingParameters indexingParameters =
            IndexingParameterExtension.parseDelimitedTextFiles(new IndexingParameters(), headers);
        assertEquals("hotelName,hotelId,address",
            indexingParameters.getConfiguration().get(DELIMITED_TEXT_HEADERS_KEY));
    }

    @Test
    public void parseDelimitedTextFilesDefaultFirstLines() {
        IndexingParameters indexingParameters =
            IndexingParameterExtension.parseDelimitedTextFiles(new IndexingParameters(), null);
        assertTrue((boolean) indexingParameters.getConfiguration().get(FIRST_LINE_CONTAINS_HEADERS_KEY));
    }

    @Test
    public void parseText() {
        IndexingParameters indexingParameter =
            IndexingParameterExtension.parseText(new IndexingParameters());
        assertEquals(PARSE_TEXT, indexingParameter.getConfiguration().get(PARSING_MODE_KEY));
        assertEquals("UTF-8", indexingParameter.getConfiguration().get(ENCODING_KEY));
    }

    @Test
    public void parseTextWithEncoding() {
        IndexingParameters indexingParameter =
            IndexingParameterExtension.parseText(new IndexingParameters(), StandardCharsets.US_ASCII);
        assertEquals(PARSE_TEXT, indexingParameter.getConfiguration().get(PARSING_MODE_KEY));
        assertEquals("US-ASCII", indexingParameter.getConfiguration().get(ENCODING_KEY));
    }

}
