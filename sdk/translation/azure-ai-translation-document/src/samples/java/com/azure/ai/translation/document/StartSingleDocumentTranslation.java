// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.core.util.BinaryData;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.translation.document.models.DocumentFileDetails;
import com.azure.ai.translation.document.models.DocumentTranslateContent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sample for starting a single document translation
 */
public class StartSingleDocumentTranslation {
    private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
    private static final Path DOCUMENT_FILE_PATH = Paths.get(CURRENT_DIRECTORY, "src", "test", "java", "com", "azure", "ai", "translation", "document", "TestData", "test-input.txt");
    
    public static void main(final String[] args) {
        SingleDocumentTranslationClient singleDocumentTranslationClient = new SingleDocumentTranslationClientBuilder()
            .endpoint("{endpoint}")
            .credential(new AzureKeyCredential("{key}"))
            .buildClient();
        
        // BEGIN:SingleDocumentTranslation
        DocumentFileDetails document = createDocumentContent();
        DocumentTranslateContent documentTranslateContent = new DocumentTranslateContent(document);
        String targetLanguage = "hi";    
        
        BinaryData response = singleDocumentTranslationClient.documentTranslate(targetLanguage, documentTranslateContent);        
        String translatedResponse = response.toString();
        System.out.println("Translated Response: " + translatedResponse);
        // END:SingleDocumentTranslation
    }
    
    private static DocumentFileDetails createDocumentContent() {
        DocumentFileDetails document = null;
        try {
            byte[] fileData = Files.readAllBytes(DOCUMENT_FILE_PATH);
            BinaryData documentContent = BinaryData.fromBytes(fileData);
            
            String documentFilename = DOCUMENT_FILE_PATH.getFileName().toString();
            String documentContentType = "text/html";
            
            document = new DocumentFileDetails(documentContent)
                    .setFilename(documentFilename)
                    .setContentType(documentContentType);            
            
        } catch (IOException ex) {
            Logger.getLogger(SingleDocumentTranslationTests.class.getName()).log(Level.SEVERE, null, ex);
        }
        return document;
    }
}
