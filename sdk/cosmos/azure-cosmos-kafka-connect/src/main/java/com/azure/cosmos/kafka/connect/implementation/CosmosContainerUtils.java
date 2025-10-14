// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CosmosContainerUtils {
    public static List<String> validateDatabaseAndContainers(
        List<String> includedContainers,
        CosmosAsyncClient cosmosAsyncClient,
        String databaseName) {

        StringBuilder queryStringBuilder = new StringBuilder();
        List<SqlParameter> parameters = new ArrayList<>();

        if (includedContainers == null || includedContainers.isEmpty()) {
            queryStringBuilder.append("SELECT * FROM c");
        } else {
            queryStringBuilder.append("SELECT * FROM c WHERE c.id IN ( ");
            for (int i = 0; i < includedContainers.size(); i++) {
                String idValue = includedContainers.get(i);
                String idParamName = "@param" + i;

                parameters.add(new SqlParameter(idParamName, idValue));
                queryStringBuilder.append(idParamName);

                if (i < includedContainers.size() - 1) {
                    queryStringBuilder.append(", ");
                }
            }
            queryStringBuilder.append(" )");
        }

        List<CosmosContainerProperties> cosmosContainerProperties = cosmosAsyncClient.getDatabase(databaseName)
            .queryContainers(new SqlQuerySpec(queryStringBuilder.toString(), parameters))
            .byPage()
            .flatMapIterable(response -> response.getResults())
            .collectList()
            .onErrorMap(throwable -> {
                if (KafkaCosmosExceptionsHelper.isOwnerResourceNotExistsException(throwable)) {
                    return KafkaCosmosExceptionsHelper.convertToConnectException(
                        throwable,
                        "Database specified in the config does not exist in the CosmosDB account.");
                }

                return KafkaCosmosExceptionsHelper.convertToConnectException(throwable, "validateDatabaseAndContainers failed.");
            })
            .block();

        List<String> containersFromDatabase = cosmosContainerProperties
            .stream()
            .map(CosmosContainerProperties::getId)
            .collect(Collectors.toList());

        if (containersFromDatabase.isEmpty()
            || (includedContainers != null && !containersFromDatabase.containsAll(includedContainers))) {
            throw new IllegalStateException("Containers specified in the config do not exist in the CosmosDB account.");
        }

        return containersFromDatabase;
    }
}
