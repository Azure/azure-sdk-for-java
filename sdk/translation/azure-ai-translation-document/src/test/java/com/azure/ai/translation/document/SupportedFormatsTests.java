// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.FileFormat;
import com.azure.ai.translation.document.models.FileFormatType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class SupportedFormatsTests extends DocumentTranslationClientTestBase {
    @Test
    public void testGetDocumentsFormats() {
        // method invocation
        List<FileFormat> responseValue = getDocumentTranslationClient().getSupportedFormats(FileFormatType.DOCUMENT);

        // response assertion
        assertNotNull(responseValue);
        assertFalse(responseValue.isEmpty(), "The supported documents should be greater than 0");

        for (FileFormat fileFormat : responseValue) {
            assertNotNull(fileFormat.getFormat());
            assertNotNull(fileFormat.getFileExtensions());
            assertNotNull(fileFormat.getContentTypes());
            assertNotNull(fileFormat.getType());
        }
    }

    @Test
    public void testGetGlossariesFormats() {
        // method invocation
        List<FileFormat> responseValue = getDocumentTranslationClient().getSupportedFormats(FileFormatType.GLOSSARY);

        // response assertion
        assertNotNull(responseValue);
        assertFalse(responseValue.isEmpty(), "The supported documents should be greater than 0");

        for (FileFormat fileFormat : responseValue) {
            assertNotNull(fileFormat.getFormat());
            assertNotNull(fileFormat.getFileExtensions());
            assertNotNull(fileFormat.getContentTypes());
            assertNotNull(fileFormat.getType());
        }
    }
}
