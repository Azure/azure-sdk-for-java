// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.core.util.Context;
import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import com.azure.cs.textanalytics.implementation.models.DocumentEntities;
import com.azure.cs.textanalytics.implementation.models.EntitiesResult;
import com.azure.cs.textanalytics.models.Entity;
import com.azure.cs.textanalytics.models.MultiLanguageBatchInput;
import com.azure.cs.textanalytics.models.MultiLanguageInput;

import java.util.ArrayList;
import java.util.List;

public class RecognizePIIBatchDocuments {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The texts that need be analysed.
        List<MultiLanguageInput> documents = new ArrayList<>();
        MultiLanguageInput input = new MultiLanguageInput();
        input.setId("1").setText("My SSN is 555-55-5555").setLanguage("US");
        MultiLanguageInput input2 = new MultiLanguageInput();
        input2.setId("2").setText("Visa card 4147999933330000").setLanguage("US");
        documents.add(input);
        documents.add(input2);
        MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput();
        batchInput.setDocuments(documents);


        // Detecting language from a batch of documents
        EntitiesResult detectedResult = client.recognizePiiEntitiesWithResponse(batchInput, false, Context.NONE).getValue();
        List<DocumentEntities> documentEntities = detectedResult.getDocuments();
        for (DocumentEntities documentEntitie : documentEntities) {
            List<Entity> entities = documentEntitie.getEntities();
            for (Entity entity : entities) {
                System.out.println(String.format(
                    "Recognized Personal Idenfiable Info Entity: %s, Entity Type: %s, Entity Subtype: %s, Offset: %s, Length: %s, Score: %s",
                    entity.getText(), entity.getType(), entity.getSubType(), entity.getOffset(), entity.getLength(), entity.getScore()));
            }
        }
    }

}
