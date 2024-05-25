/*
 * The MIT License
 *
 * Copyright 2024 Microsoft Corporation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.FileFormat;
import com.azure.ai.translation.document.models.FileFormatType;
import com.azure.ai.translation.document.models.SupportedFileFormats;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        
        for (FileFormat fileFormat : responseValue){
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
        
        for (FileFormat fileFormat : responseValue){
            assertNotNull(fileFormat.getFormat());
            assertNotNull(fileFormat.getFileExtensions());
            assertNotNull(fileFormat.getContentTypes());
            assertNotNull(fileFormat.getType());  
        }
    }
}
