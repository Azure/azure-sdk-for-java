// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.core.util.Context;
import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import textanalytics.models.DocumentLinkedEntities;
import textanalytics.models.EntityLinkingResult;
import textanalytics.models.LinkedEntity;
import textanalytics.models.MultiLanguageBatchInput;
import textanalytics.models.MultiLanguageInput;

import java.util.ArrayList;
import java.util.List;

public class RecognizeLinkedEntitiesBatchDocuments {

    public static void main(String[] args) {
        // The connection string value can be obtained by going to your Text Analytics instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};name={secret_value}";

        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // The texts that need be analysed.
        List<MultiLanguageInput> documents = new ArrayList<>();
        MultiLanguageInput input = new MultiLanguageInput();

        input.setId("1").setText("Old Faithful is a geyser at Yellowstone Park").setLanguage("US");
        MultiLanguageInput input2 = new MultiLanguageInput();
        input2.setId("2").setText("Mount Shasta has lenticular clouds.").setLanguage("US");

        documents.add(input);
        documents.add(input2);

        MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput();
        batchInput.setDocuments(documents);

        // Detecting language from a batch of documents
        EntityLinkingResult detectedResult = client.detectLinkedEntitiesBatchWithResponse(batchInput, false, Context.NONE).getValue();
        List<DocumentLinkedEntities> documentLinkedEntities = detectedResult.getDocuments();
        for (DocumentLinkedEntities documentLinkedEntitie : documentLinkedEntities) {
            List<LinkedEntity> linkedEntities = documentLinkedEntitie.getEntities();
            for (LinkedEntity linkedEntity : linkedEntities) {
                System.out.println(String.format("Recognized Linked Entity: %s, URL: %s, Data Source: %s",
                    linkedEntity.getName(), linkedEntity.getUrl(), linkedEntity.getDataSource()));
            }
        }
    }
}
