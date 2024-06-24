// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.FileFormat;
import com.azure.ai.translation.document.models.FileFormatType;
import com.azure.ai.translation.document.models.SupportedFileFormats;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class SupportedFormatsTests extends DocumentTranslationClientTestBase {
    @Test    
    public void testGetDocumentsFormats() {        
        // method invocation
        SupportedFileFormats response = getDocumentTranslationClient().getSupportedFormats(FileFormatType.DOCUMENT);
        
        // response assertion
        assertNotNull(response);
        
        List<FileFormat> responseValue = response.getValue();
        assertTrue(!responseValue.isEmpty(), "The supported documents should be greater than 0");            
        
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
        SupportedFileFormats response = getDocumentTranslationClient().getSupportedFormats(FileFormatType.GLOSSARY);
        
        // response assertion
        assertNotNull(response);
        
        List<FileFormat> responseValue = response.getValue();
        assertTrue(!responseValue.isEmpty(), "The supported documents should be greater than 0");            
        
        for (FileFormat fileFormat : responseValue) {
            assertNotNull(fileFormat.getFormat());
            assertNotNull(fileFormat.getFileExtensions());
            assertNotNull(fileFormat.getContentTypes());
            assertNotNull(fileFormat.getType());  
        }
    }
}
