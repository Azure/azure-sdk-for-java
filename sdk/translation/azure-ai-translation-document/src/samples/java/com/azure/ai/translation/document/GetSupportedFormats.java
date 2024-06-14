// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.translation.document.models.FileFormat;
import com.azure.ai.translation.document.models.FileFormatType;
import com.azure.ai.translation.document.models.SupportedFileFormats;
import java.util.List;

/**
 * Sample for getting supported document formats.
 */
public class GetSupportedFormats {
    public static void main(final String[] args) {
        String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");
        String apiKey = System.getenv("DOCUMENT_TRANSLATION_API_KEY");
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        DocumentTranslationClient documentTranslationClient = new DocumentTranslationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

        // BEGIN: getSupportedFormats
        SupportedFileFormats documentResponse = documentTranslationClient.getSupportedFormats(FileFormatType.DOCUMENT);
        List<FileFormat> documentFileFormats = documentResponse.getValue();
        for (FileFormat fileFormat : documentFileFormats) {
            System.out.println("FileFormat:" + fileFormat.getFormat());
            System.out.println("FileExtensions:" + fileFormat.getFileExtensions());
            System.out.println("ContentTypes:" + fileFormat.getContentTypes());
            System.out.println("Type:" + fileFormat.getType());
        }

        SupportedFileFormats glossaryResponse = documentTranslationClient.getSupportedFormats(FileFormatType.GLOSSARY);
        List<FileFormat> glossaryFileFormats = glossaryResponse.getValue();
        for (FileFormat fileFormat : glossaryFileFormats) {
            System.out.println("FileFormat:" + fileFormat.getFormat());
            System.out.println("FileExtensions:" + fileFormat.getFileExtensions());
            System.out.println("ContentTypes:" + fileFormat.getContentTypes());
            System.out.println("Type:" + fileFormat.getType());
        }
        // END: getSupportedFormats
    }
}
