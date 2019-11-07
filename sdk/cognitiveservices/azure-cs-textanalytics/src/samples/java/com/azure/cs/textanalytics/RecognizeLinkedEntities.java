// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import textanalytics.models.DocumentLinkedEntities;
import textanalytics.models.LinkedEntity;

import java.util.List;

public class RecognizeLinkedEntities {

    public static void main(String[] args) {
        // The connection string value can be obtained by going to your Text Analytics instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};name={secret_value}";

        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // The text that need be analysed.
        String text = "Old Faithful is a geyser at Yellowstone Park";

        DocumentLinkedEntities detectedResult = client.detectLinkedEntities(text, "US", false);
        List<LinkedEntity> linkedEntities = detectedResult.getEntities();
        for (LinkedEntity linkedEntity : linkedEntities) {
            System.out.println(String.format("Recognized Linked Entity: %s, URL: %s, Data Source: %s",
                linkedEntity.getName(), linkedEntity.getUrl(), linkedEntity.getDataSource()));
        }
    }
}
