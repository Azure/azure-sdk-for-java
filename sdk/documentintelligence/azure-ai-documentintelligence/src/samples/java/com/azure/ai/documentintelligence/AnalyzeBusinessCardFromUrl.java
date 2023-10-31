// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.Document;
import com.azure.ai.documentintelligence.models.DocumentField;
import com.azure.ai.documentintelligence.models.DocumentFieldType;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sample for analyzing business card information from a URL.
 */
public class AnalyzeBusinessCardFromUrl {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{apikey}"))
            .endpoint("https:{endpoint}.cognitiveservices.azure.com")
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS).setAllowedHeaderNames(Set.of("Ocp-Apim-Subscription-Key")))
            .buildClient();

        String businessCardUrl =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer"
                + "/azure-ai-formrecognizer/src/samples/resources/sample-forms/businessCards/businessCard.jpg";

        SyncPoller<AnalyzeResultOperation, AnalyzeResult> analyzeBusinessCardPoller =
            client.beginAnalyzeDocument("prebuilt-businessCard", null, null, null, null, null, null,
                new AnalyzeDocumentRequest().setUrlSource(businessCardUrl));

        AnalyzeResult businessCardPageResults = analyzeBusinessCardPoller.getFinalResult();

        for (int i = 0; i < businessCardPageResults.getDocuments().size(); i++) {
            System.out.printf("--------Analyzing business card %d%n--------", i);
            Document analyzedBusinessCard = businessCardPageResults.getDocuments().get(i);
            Map<String, DocumentField> businessCardFields = analyzedBusinessCard.getFields();
            DocumentField contactNamesDocumentField = businessCardFields.get("ContactNames");
            if (contactNamesDocumentField != null) {
                if (DocumentFieldType.ARRAY == contactNamesDocumentField.getType()) {
                    List<DocumentField> contactNamesList = contactNamesDocumentField.getValueArray();
                    contactNamesList.stream()
                        .filter(contactName -> DocumentFieldType.OBJECT == contactName.getType())
                        .map(contactName -> {
                            System.out.printf("Contact name: %s%n", contactName.getContent());
                            return contactName.getValueObject();
                        })
                        .forEach(contactNamesMap -> contactNamesMap.forEach((key, contactName) -> {
                            if ("FirstName".equals(key)) {
                                if (DocumentFieldType.STRING == contactName.getType()) {
                                    String firstName = contactName.getValueString();
                                    System.out.printf("\tFirst Name: %s, confidence: %.2f%n",
                                        firstName, contactName.getConfidence());
                                }
                            }
                        }));
                }
            }
        }
    }
}
