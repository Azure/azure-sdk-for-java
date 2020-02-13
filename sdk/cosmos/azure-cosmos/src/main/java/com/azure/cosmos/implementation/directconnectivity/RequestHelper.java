// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BadRequestException;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import reactor.core.publisher.Mono;

public class RequestHelper {
    public static Mono<ConsistencyLevel> GetConsistencyLevelToUse(GatewayServiceConfigurationReader serviceConfigReader,
                                                                  RxDocumentServiceRequest request) throws CosmosClientException {
        return serviceConfigReader.getDefaultConsistencyLevel().map(consistencyLevelToUse -> {

            String requestConsistencyLevelHeaderValue = request.getHeaders().get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        if (!Strings.isNullOrEmpty(requestConsistencyLevelHeaderValue)) {
            ConsistencyLevel requestConsistencyLevel = ConsistencyLevel.fromServiceSerializedFormat(requestConsistencyLevelHeaderValue);
            if (requestConsistencyLevel == null) {
                throw new BadRequestException(
                        String.format(
                            RMResources.InvalidHeaderValue,
                            requestConsistencyLevelHeaderValue,
                            HttpConstants.HttpHeaders.CONSISTENCY_LEVEL));
                }

                consistencyLevelToUse = requestConsistencyLevel;
            }

            return consistencyLevelToUse;
        });
    }
}
