// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;

import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RequestHelper {
    public static ReadConsistencyStrategy getReadConsistencyStrategyToUse(
        GatewayServiceConfigurationReader serviceConfigReader,
        RxDocumentServiceRequest request) {

        checkNotNull(serviceConfigReader, "Argument 'serviceConfigReader' must not be null.");
        checkNotNull(request, "Argument 'request' must not be null.");

        ReadConsistencyStrategy requestLevelReadConsistencyStrategy = null;
        if (request.requestContext != null
            && request.requestContext.readConsistencyStrategy != null) {

            requestLevelReadConsistencyStrategy = request.requestContext.readConsistencyStrategy;
        }

        return getReadConsistencyStrategyToUse(
            request.getHeaders(),
            requestLevelReadConsistencyStrategy,
            serviceConfigReader.getDefaultConsistencyLevel());
    }

    public static ReadConsistencyStrategy getReadConsistencyStrategyToUse(
        Map<String, String> headers,
        ReadConsistencyStrategy requestLevelReadConsistencyStrategy,
        ConsistencyLevel defaultConsistencySnapshot) {

        if (requestLevelReadConsistencyStrategy == null
            || requestLevelReadConsistencyStrategy == ReadConsistencyStrategy.DEFAULT) {

            String requestReadConsistencyStrategyHeaderValue =
                headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY);

            if (!Strings.isNullOrEmpty(requestReadConsistencyStrategyHeaderValue)) {
                requestLevelReadConsistencyStrategy =
                    ImplementationBridgeHelpers
                        .ReadConsistencyStrategyHelper
                        .getReadConsistencyStrategyAccessor()
                        .createFromServiceSerializedFormat(requestReadConsistencyStrategyHeaderValue);

                if (requestLevelReadConsistencyStrategy == null) {
                    throw new BadRequestException(
                        String.format(
                            RMResources.InvalidHeaderValue,
                            requestReadConsistencyStrategyHeaderValue,
                            HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY));
                }

                if (!validateReadConsistencyStrategy(defaultConsistencySnapshot, requestLevelReadConsistencyStrategy)) {
                    throw new BadRequestException(
                        String.format(
                            RMResources.ReadConsistencyStrategyGlobalStrongOnlyAllowedForGlobalStrongAccount,
                            requestReadConsistencyStrategyHeaderValue,
                            HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY,
                            ConsistencyLevel.STRONG,
                            defaultConsistencySnapshot));
                }
            }
        } else {
            headers.put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, requestLevelReadConsistencyStrategy.toString());
        }

        if (requestLevelReadConsistencyStrategy != null
            && requestLevelReadConsistencyStrategy != ReadConsistencyStrategy.DEFAULT)
        {
            // Update the consistency level header - this is needed to ensure
            // service telemetry / Kusto have appropriate data
            switch (requestLevelReadConsistencyStrategy) {
                case EVENTUAL:
                    headers.put(
                        HttpConstants.HttpHeaders.CONSISTENCY_LEVEL,
                        ConsistencyLevel.EVENTUAL.toString());
                    break;

                case SESSION:
                    headers.put(
                        HttpConstants.HttpHeaders.CONSISTENCY_LEVEL,
                        ConsistencyLevel.SESSION.toString());
                    break;

                case LATEST_COMMITTED:
                    headers.put(
                        HttpConstants.HttpHeaders.CONSISTENCY_LEVEL,
                        ConsistencyLevel.BOUNDED_STALENESS.toString());
                    break;

                case GLOBAL_STRONG:
                    headers.put(
                        HttpConstants.HttpHeaders.CONSISTENCY_LEVEL,
                        ConsistencyLevel.STRONG.toString());
                    break;

                default:
                    throw new IllegalStateException(
                        "Unknown read consistency strategy '" + requestLevelReadConsistencyStrategy + "'.");
            }

            return requestLevelReadConsistencyStrategy;
        }

        ConsistencyLevel consistencyLevelToUse = defaultConsistencySnapshot;
        String requestConsistencyLevelHeaderValue =
            headers.get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        if (!Strings.isNullOrEmpty(requestConsistencyLevelHeaderValue)) {
            ConsistencyLevel requestConsistencyLevel = BridgeInternal.fromServiceSerializedFormat(requestConsistencyLevelHeaderValue);
            if (requestConsistencyLevel == null) {
                throw new BadRequestException(
                    String.format(
                        RMResources.InvalidHeaderValue,
                        requestConsistencyLevelHeaderValue,
                        HttpConstants.HttpHeaders.CONSISTENCY_LEVEL));
            }

            consistencyLevelToUse = requestConsistencyLevel;
        }

        switch (consistencyLevelToUse) {
            case EVENTUAL:
            case CONSISTENT_PREFIX:
                return ReadConsistencyStrategy.EVENTUAL;

            case SESSION:
                return ReadConsistencyStrategy.SESSION;

            case BOUNDED_STALENESS:
                return ReadConsistencyStrategy.LATEST_COMMITTED;

            case STRONG:
                return ReadConsistencyStrategy.GLOBAL_STRONG;

            default:
                throw new IllegalStateException(
                    "Unknown consistency level '" + consistencyLevelToUse + "'.");
        }
    }

    private static boolean validateReadConsistencyStrategy(
        ConsistencyLevel backendConsistency,
        ReadConsistencyStrategy readConsistencyStrategy) {

        if (readConsistencyStrategy == ReadConsistencyStrategy.GLOBAL_STRONG)
        {
            return backendConsistency == ConsistencyLevel.STRONG;
        }

        return true;
    }
}
