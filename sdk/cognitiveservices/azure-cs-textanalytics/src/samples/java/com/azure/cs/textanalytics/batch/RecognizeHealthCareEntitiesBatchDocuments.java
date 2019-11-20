//// Copyright (c) Microsoft Corporation. All rights reserved.
//// Licensed under the MIT License.
//
//package com.azure.cs.textanalytics.batch;
//
//import com.azure.core.util.Context;
//import com.azure.cs.textanalytics.TextAnalyticsClient;
//import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
//import com.azure.cs.textanalytics.implementation.models.DocumentEntities;
//import com.azure.cs.textanalytics.implementation.models.EntitiesResult;
//import com.azure.cs.textanalytics.models.NamedEntity;
//import com.azure.cs.textanalytics.models.MultiLanguageBatchInput;
//import com.azure.cs.textanalytics.models.MultiLanguageInput;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class RecognizeHealthCareEntitiesBatchDocuments {
//
//    public static void main(String[] args) {
//
//        // TODO: user AAD token to do the authentication
//        // Instantiate a client that will be used to call the service.
//        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
//            .buildClient();
//
//        // The texts that need be analysed.
//        List<MultiLanguageInput> documents = new ArrayList<>();
//        MultiLanguageInput input = new MultiLanguageInput();
//        input.setId("1").setText("Patient should take 40mg ibuprofen twice a week.").setLanguage("US");
//        MultiLanguageInput input2 = new MultiLanguageInput();
//        input2.setId("2").setText("Patient has a fever and sinus infection.").setLanguage("US");
//        documents.add(input);
//        documents.add(input2);
//        MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput();
//        batchInput.setDocuments(documents);
//
//
//        // Detecting language from a batch of documents
//        EntitiesResult detectedResult = client.getHealthCareEntitiesWithResponse(batchInput, false, Context.NONE).getValue();
//        List<DocumentEntities> documentEntities = detectedResult.getDocuments();
//        for (DocumentEntities documentEntitie : documentEntities) {
//            List<NamedEntity> entities = documentEntitie.getEntities();
//            for (NamedEntity entity : entities) {
//                System.out.printf("Recognized Health Care NamedEntity: %s, NamedEntity Type: %s, NamedEntity Subtype: %s, Offset: %s, Length: %s, Score: %s",
//                    entity.getText(), entity.getType(), entity.getSubType(), entity.getOffset(), entity.getLength(), entity.getScore()));
//            }
//        }
//    }
//}
