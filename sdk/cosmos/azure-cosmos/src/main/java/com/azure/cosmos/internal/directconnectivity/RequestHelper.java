// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.internal.directconnectivity;

import com.azure.cosmos.BadRequestException;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.internal.HttpConstants;
import com.azure.cosmos.internal.RMResources;
import com.azure.cosmos.internal.RxDocumentServiceRequest;
import com.azure.cosmos.internal.Strings;
import org.apache.commons.lang3.EnumUtils;

public class RequestHelper {
    public static ConsistencyLevel GetConsistencyLevelToUse(GatewayServiceConfigurationReader serviceConfigReader,
                                                            RxDocumentServiceRequest request) throws CosmosClientException {
        ConsistencyLevel consistencyLevelToUse = serviceConfigReader.getDefaultConsistencyLevel();

        String requestConsistencyLevelHeaderValue = request.getHeaders().get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        if (!Strings.isNullOrEmpty(requestConsistencyLevelHeaderValue)) {
            ConsistencyLevel requestConsistencyLevel = EnumUtils.getEnum(ConsistencyLevel.class, Strings.fromCamelCaseToUpperCase(requestConsistencyLevelHeaderValue));
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
    }
}
