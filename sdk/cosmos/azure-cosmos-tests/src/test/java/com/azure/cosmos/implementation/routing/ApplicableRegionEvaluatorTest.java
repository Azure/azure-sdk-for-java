// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.AvailabilityStrategyContext;
import com.azure.cosmos.implementation.CrossRegionAvailabilityContextForRxDocumentServiceRequest;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PointOperationContextForCircuitBreaker;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApplicableRegionEvaluatorTest {
    private final static URI DefaultEndpoint = createUrl("https://default.documents.azure.com");
    private final static URI DefaultRegionalEndpoint = createUrl("https://location1.documents.azure.com");
    private final static URI Location1Endpoint = createUrl("https://location1.documents.azure.com");
    private final static URI Location2Endpoint = createUrl("https://location2.documents.azure.com");
    private final static URI Location3Endpoint = createUrl("https://location3.documents.azure.com");
    private final static URI Location4Endpoint = createUrl("https://location4.documents.azure.com");

    @DataProvider(name = "highAvailabilityConfigs")
    public Object[][] highAvailabilityConfigs() {

        HighAvailabilitySetting[] accountTypes = new HighAvailabilitySetting[] {HighAvailabilitySetting.SINGLE_WRITE_ACCOUNT, HighAvailabilitySetting.MULTI_WRITE_ACCOUNT};
        HighAvailabilitySetting[] opTypes = new HighAvailabilitySetting[] {HighAvailabilitySetting.IS_WRITE, HighAvailabilitySetting.IS_READ};
        HighAvailabilitySetting[] availabilityStrategySettings = new HighAvailabilitySetting[] {HighAvailabilitySetting.WITH_AVAILABILITY_STRATEGY, HighAvailabilitySetting.WITHOUT_AVAILABILITY_STRATEGY};
        HighAvailabilitySetting[] perPartitionAutomaticFailoverSettings = new HighAvailabilitySetting[] {HighAvailabilitySetting.PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED, HighAvailabilitySetting.PER_PARTITION_AUTOMATIC_FAILOVER_DISABLED};
        HighAvailabilitySetting[] perPartitionCircuitBreakerSettings = new HighAvailabilitySetting[] {HighAvailabilitySetting.PER_PARTITION_CIRCUIT_BREAKER_ENABLED, HighAvailabilitySetting.PER_PARTITION_CIRCUIT_BREAKER_DISABLED};

        for (HighAvailabilitySetting accountType : accountTypes) {

            List<Object> testArgs = new ArrayList<>();

            GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);

            if (accountType == HighAvailabilitySetting.SINGLE_WRITE_ACCOUNT) {
                Mockito.when(globalEndpointManager.canUseMultipleWriteLocations()).thenReturn(false);
                Mockito.when(globalEndpointManager.canUseMultipleWriteLocations(Mockito.any())).thenReturn(false);
            }

            if (accountType == HighAvailabilitySetting.MULTI_WRITE_ACCOUNT) {
                Mockito.when(globalEndpointManager.canUseMultipleWriteLocations()).thenReturn(true);
                Mockito.when(globalEndpointManager.canUseMultipleWriteLocations(Mockito.any())).thenReturn(true);
            }

            testArgs.add(globalEndpointManager);

            for (HighAvailabilitySetting availabilityStrategySetting : availabilityStrategySettings) {

                for (HighAvailabilitySetting opType : opTypes) {

                    RxDocumentServiceRequest request;

                    if (opType == HighAvailabilitySetting.IS_READ) {
                        request = createRequest(
                            OperationType.Read,
                            availabilityStrategySetting == HighAvailabilitySetting.WITH_AVAILABILITY_STRATEGY);
                    } else {
                        request = createRequest(
                            OperationType.Create,
                            availabilityStrategySetting == HighAvailabilitySetting.WITH_AVAILABILITY_STRATEGY);
                    }
                }
            }
        }

        return new Object[][]{};
    }

    @Test(groups = {"unit"}, enabled = false)
    public void validateApplicableRegions(
        GlobalEndpointManager globalEndpointManager,
        RxDocumentServiceRequest request) {

    }

    private static URI createUrl(String url) {
        try {
            return new URI(url);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static RxDocumentServiceRequest createRequest(
        OperationType operationType,
        boolean isAvailabilityStrategyEnabled) {

        String collectionResourceId = "coll1";
        String partitionKeyRangeId = "0";
        String minEPK = "";
        String maxEPK = "-FF";

        RxDocumentServiceRequest request;

        if (operationType.isWriteOperation()) {
            request = RxDocumentServiceRequest.create(
                null,
                operationType,
                ResourceType.Document);
        } else {
            request = RxDocumentServiceRequest.create(
                null,
                operationType,
                ResourceType.Document);
        }

        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange(partitionKeyRangeId, minEPK, maxEPK);
        request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover = request.requestContext.resolvedPartitionKeyRange;
        request.requestContext.resolvedCollectionRid = collectionResourceId;
        request.requestContext.setExcludeRegions(Collections.emptyList());

        if (isAvailabilityStrategyEnabled) {
            request.requestContext.setCrossRegionAvailabilityContext(
                new CrossRegionAvailabilityContextForRxDocumentServiceRequest(
                    null,
                    new PointOperationContextForCircuitBreaker(
                        new AtomicBoolean(false),
                        true,
                        collectionResourceId,
                        new SerializationDiagnosticsContext()),
                    new AvailabilityStrategyContext(true, false)
                ));
        } else {
            request.requestContext.setCrossRegionAvailabilityContext(
                new CrossRegionAvailabilityContextForRxDocumentServiceRequest(
                    null,
                    new PointOperationContextForCircuitBreaker(
                        new AtomicBoolean(false),
                        false,
                        collectionResourceId,
                        new SerializationDiagnosticsContext()),
                    new AvailabilityStrategyContext(false, false)
                ));
        }

        return request;
    }

    private static void handlePerPartitionCircuitBreakerEnforcement(
        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManagerForPerPartitionCircuitBreaker,
        RxDocumentServiceRequest request,
        HighAvailabilitySetting highAvailabilitySetting) {
    }

    private static void handlePerPartitionAutomaticFailoverEnforcement(
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover,
        RxDocumentServiceRequest request,
        HighAvailabilitySetting highAvailabilitySetting) {

    }

    private static void handleUserEnforcedExcludeRegionSetting(
        RxDocumentServiceRequest request,
        HighAvailabilitySetting highAvailabilitySetting) {

    }

    private enum HighAvailabilitySetting {
        MULTI_WRITE_ACCOUNT,
        SINGLE_WRITE_ACCOUNT,
        PER_PARTITION_CIRCUIT_BREAKER_ENABLED,
        PER_PARTITION_CIRCUIT_BREAKER_DISABLED,
        PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED,
        PER_PARTITION_AUTOMATIC_FAILOVER_DISABLED,
        WITH_AVAILABILITY_STRATEGY,
        WITHOUT_AVAILABILITY_STRATEGY,
        IS_READ,
        IS_WRITE,
        ACCOUNT_ONE_REGION_SCENARIO,
        ACCOUNT_TWO_REGION_SCENARIO,
        ACCOUNT_THREE_REGION_SCENARIO,
        PER_PARTITION_CIRCUIT_BREAKER_ONE_REGION_UNAVAILABLE,
        PER_PARTITION_CIRCUIT_BREAKER_TWO_REGION_UNAVAILABLE,
        PER_PARTITION_CIRCUIT_BREAKER_THREE_REGION_UNAVAILABLE,
        PER_PARTITION_AUTOMATIC_FAILOVER_WRITE_REGION_AVAILABLE,
        PER_PARTITION_AUTOMATIC_FAILOVER_WRITE_REGION_UNAVAILABLE,
        USER_ENFORCED_EXCLUDE_REGION_NONE,
        USER_ENFORCED_EXCLUDE_REGION_ONE,
        USER_ENFORCED_EXCLUDE_REGION_TWO,
        USER_ENFORCED_EXCLUDE_REGION_THREE,
    }
}
