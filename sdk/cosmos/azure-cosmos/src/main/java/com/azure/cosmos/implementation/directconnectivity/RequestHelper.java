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

public class RequestHelper {
    public static ReadConsistencyStrategy getReadConsistencyStrategyToUse(
        GatewayServiceConfigurationReader serviceConfigReader,
        RxDocumentServiceRequest request) {

        ReadConsistencyStrategy requestLevelReadConsistencyStrategy = ReadConsistencyStrategy.DEFAULT;
        if (request != null
            && request.requestContext != null
            && request.requestContext.readConsistencyStrategy != null) {

            requestLevelReadConsistencyStrategy = request.requestContext.readConsistencyStrategy;
        }

        if (requestLevelReadConsistencyStrategy != ReadConsistencyStrategy.DEFAULT)
        {
            // Update the consistency level header - this is needed to ensure
            // service telemetry / Kusto have appropriate data
            switch (requestLevelReadConsistencyStrategy) {
                case EVENTUAL:
                    request.getHeaders().put(
                        HttpConstants.HttpHeaders.CONSISTENCY_LEVEL,
                        ConsistencyLevel.EVENTUAL.toString());
                    break;

                case SESSION:
                    request.getHeaders().put(
                        HttpConstants.HttpHeaders.CONSISTENCY_LEVEL,
                        ConsistencyLevel.SESSION.toString());
                    break;

                case LATEST_COMMITTED:
                    request.getHeaders().put(
                        HttpConstants.HttpHeaders.CONSISTENCY_LEVEL,
                        ConsistencyLevel.BOUNDED_STALENESS.toString());
                    break;

                case GLOBAL_STRONG:
                    request.getHeaders().put(
                        HttpConstants.HttpHeaders.CONSISTENCY_LEVEL,
                        ConsistencyLevel.STRONG.toString());
                    break;

                default:
                    throw new IllegalStateException(
                        "Unknown read consistency strategy '" + requestLevelReadConsistencyStrategy + "'.");
            }

            return requestLevelReadConsistencyStrategy;
        }

        ConsistencyLevel defaultConsistencySnapshot = serviceConfigReader.getDefaultConsistencyLevel();
        String requestReadConsistencyStrategyHeaderValue =
            request.getHeaders().get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY);

        if (!Strings.isNullOrEmpty(requestReadConsistencyStrategyHeaderValue)) {
            ReadConsistencyStrategy requestReadConsistencyStrategy =
                ImplementationBridgeHelpers
                    .ReadConsistencyStrategyHelper
                    .getReadConsistencyStrategyAccessor()
                    .createFromServiceSerializedFormat(requestReadConsistencyStrategyHeaderValue);

            if (requestReadConsistencyStrategy == null) {
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

            return requestReadConsistencyStrategy;
        }

        ConsistencyLevel consistencyLevelToUse = defaultConsistencySnapshot;
        String requestConsistencyLevelHeaderValue =
            request.getHeaders().get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

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
