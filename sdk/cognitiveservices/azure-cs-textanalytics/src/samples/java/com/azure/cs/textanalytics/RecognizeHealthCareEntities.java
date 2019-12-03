//// Copyright (c) Microsoft Corporation. All rights reserved.
//// Licensed under the MIT License.
//
//package com.azure.cs.textanalytics;
//
//
//import com.azure.cs.textanalytics.implementation.models.DocumentEntities;
//import com.azure.cs.textanalytics.models.NamedEntity;
//
//import java.util.List;
//
//public class RecognizeHealthCareEntities {
//
////    public static void main(String[] args) {
////        // The connection string value can be obtained by going to your Text Analytics instance in the Azure portal
////        // and navigating to "Access Keys" page under the "Settings" section.
////        String connectionString = "endpoint={endpoint_value};id={id_value};name={secret_value}";
////
////        // Instantiate a client that will be used to call the service.
////        TextAnalyticsClient client = new TextAnalyticsClientImplBuilder()
////            .connectionString(connectionString)
////            .buildClient();
////
////        // The text that need be analysed.
////        String text = "Patient should take 40mg ibuprofen twice a week.";
////
////        final DocumentEntities documentEntitie = client.getHealthCareEntities(text, "US", false);
////
////        List<NamedEntity> entities = documentEntitie.getEntities();
////        for (NamedEntity entity : entities) {
////            System.out.printf(
////                "Recognized Health Care NamedEntity: %s, NamedEntity Type: %s, NamedEntity Subtype: %s, Offset: %s, Length: %s, Score: %s",
////                entity.getText(), entity.getType(), entity.getSubType(), entity.getOffset(), entity.getLength(), entity.getScore()));
////        }
////    }
//}
