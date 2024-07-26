// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;

import java.util.ArrayList;
import java.util.List;

public class CosmosContainerUtils {

    public static void validateContainers(List<String> containerNames, CosmosAsyncClient cosmosAsyncClient, String databaseName) {
        StringBuilder queryStringBuilder = new StringBuilder();
        List<SqlParameter> parameters = new ArrayList<>();

        queryStringBuilder.append("SELECT * FROM c WHERE c.id IN ( ");
        for (int i = 0; i < containerNames.size(); i++) {
            String idValue = containerNames.get(i);
            String idParamName = "@param" + i;

            parameters.add(new SqlParameter(idParamName, idValue));
            queryStringBuilder.append(idParamName);

            if (i < containerNames.size() - 1) {
                queryStringBuilder.append(", ");
            }
        }
        queryStringBuilder.append(" )");
        List<CosmosContainerProperties> cosmosContainerProperties = cosmosAsyncClient.getDatabase(databaseName)
            .queryContainers(new SqlQuerySpec(queryStringBuilder.toString(), parameters))
            .byPage()
            .flatMapIterable(response -> response.getResults())
            .collectList()
            .onErrorMap(throwable -> KafkaCosmosExceptionsHelper.convertToConnectException(throwable, "validateContainers failed.")).block();
        if (cosmosContainerProperties.isEmpty() || cosmosContainerProperties.size() != containerNames.size()) {
            throw new IllegalStateException("Containers specified in the config do not exist in the CosmosDB account.");
        }
    }
}
