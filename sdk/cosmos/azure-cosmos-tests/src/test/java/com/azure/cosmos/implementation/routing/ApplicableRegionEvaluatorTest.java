// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.AvailabilityStrategyContext;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.CrossRegionAvailabilityContextForRxDocumentServiceRequest;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.DatabaseAccountManagerInternal;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PointOperationContextForCircuitBreaker;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApplicableRegionEvaluatorTest {

    private static final Logger logger = LoggerFactory.getLogger(ApplicableRegionEvaluatorTest.class);

    private static final URI TestAccountEndpoint = createUrl("https://testaccount.documents.azure.com");
    private static final URI TestAccountEastUsEndpoint = createUrl("https://testaccount-eastus.documents.azure.com");
    private static final URI TestAccountWestUsEndpoint = createUrl("https://testaccount-westus.documents.azure.com");
    private static final URI TestAccountCentralUsEndpoint = createUrl("https://testaccount-centralus.documents.azure.com");

    private static final String AccountId = "testaccount";

    private static final String EastUsLocation = "East US";
    private static final String WestUsLocation = "West US";
    private static final String CentralUsLocation = "Central US";

    @DataProvider(name = "highAvailabilityConfigs")
    public Object[][] highAvailabilityConfigs() {
       List<List<Object>> testScenarioMatrix = generateTestScenarioMatrix();
        List<List<Object>> testArgsMatrix = new ArrayList<>();

        // MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS,WITH_AVAILABILITY_STRATEGY,IS_READ,USER_ENFORCED_EXCLUDE_REGION_TWO,PER_PARTITION_CIRCUIT_BREAKER_LAST_REGION_UNAVAILABLE,PER_PARTITION_AUTOMATIC_FAILOVER_DISABLED

//        List<List<Object>> testScenarioMatrix = new ArrayList<>();
//        testScenarioMatrix.add(Arrays.asList(
//            DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS,
//            AvailabilityStrategyScenarios.WITH_AVAILABILITY_STRATEGY,
//            OpTypeScenarios.IS_READ,
//            UserEnforcedExcludeRegionScenarios.USER_ENFORCED_EXCLUDE_FIRST_TWO_PREFERRED_REGIONS,
//            PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_REGION_UNAVAILABLE,
//            PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_DISABLED
//        ));

        for (List<Object> row : testScenarioMatrix) {
            Assertions.assertThat(row.size()).isEqualTo(6);

            Assertions.assertThat(row.get(0) instanceof DatabaseAccountTypes).isTrue();
            Assertions.assertThat(row.get(1) instanceof AvailabilityStrategyScenarios).isTrue();
            Assertions.assertThat(row.get(2) instanceof OpTypeScenarios).isTrue();
            Assertions.assertThat(row.get(3) instanceof UserEnforcedExcludeRegionScenarios).isTrue();
            Assertions.assertThat(row.get(4) instanceof PerPartitionCircuitBreakerScenarios).isTrue();
            Assertions.assertThat(row.get(5) instanceof PerPartitionAutomaticFailoverScenarios).isTrue();

            StringBuilder testScenarioId = new StringBuilder();

            DatabaseAccountTypes databaseAccountType = (DatabaseAccountTypes) row.get(0);

            testScenarioId.append(databaseAccountType.name());
            testScenarioId.append(",");

            AvailabilityStrategyScenarios availabilityStrategyScenario = (AvailabilityStrategyScenarios) row.get(1);

            testScenarioId.append(availabilityStrategyScenario.name());
            testScenarioId.append(",");

            OpTypeScenarios opTypeScenario = (OpTypeScenarios) row.get(2);

            testScenarioId.append(opTypeScenario.name());
            testScenarioId.append(",");

            UserEnforcedExcludeRegionScenarios userEnforcedExcludeRegionScenario = (UserEnforcedExcludeRegionScenarios) row.get(3);

            testScenarioId.append(userEnforcedExcludeRegionScenario.name());
            testScenarioId.append(",");

            PerPartitionCircuitBreakerScenarios perPartitionCircuitBreakerScenario = (PerPartitionCircuitBreakerScenarios) row.get(4);

            testScenarioId.append(perPartitionCircuitBreakerScenario.name());
            testScenarioId.append(",");

            PerPartitionAutomaticFailoverScenarios perPartitionAutomaticFailoverScenario = (PerPartitionAutomaticFailoverScenarios) row.get(5);

            testScenarioId.append(perPartitionAutomaticFailoverScenario.name());

            List<Object> testArgs = new ArrayList<>();

            testArgs.add(testScenarioId.toString());

            GlobalEndpointManager globalEndpointManager = handleDatabaseAccountType(databaseAccountType);

            testArgs.add(globalEndpointManager);

            RxDocumentServiceRequest request = createRequest(
                opTypeScenario == OpTypeScenarios.IS_READ ? OperationType.Read : OperationType.Create,
                availabilityStrategyScenario == AvailabilityStrategyScenarios.WITH_AVAILABILITY_STRATEGY);

            testArgs.add(request);

            boolean isValidScenario = true;

            isValidScenario &= handleUserEnforcedExcludeRegionSetting(
                request,
                userEnforcedExcludeRegionScenario);

            isValidScenario &= handlePerPartitionCircuitBreakerEnforcement(
                request,
                databaseAccountType,
                perPartitionCircuitBreakerScenario);

            GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
                = new GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover(
                globalEndpointManager,
                perPartitionAutomaticFailoverScenario != PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_DISABLED);

            isValidScenario &= handlePerPartitionAutomaticFailoverEnforcement(
                globalPartitionEndpointManagerForPerPartitionAutomaticFailover,
                request,
                databaseAccountType,
                perPartitionAutomaticFailoverScenario);

            if (isValidScenario) {
                testArgs.add(generateExpectedApplicableEndpoints(
                    databaseAccountType,
                    opTypeScenario,
                    userEnforcedExcludeRegionScenario,
                    perPartitionCircuitBreakerScenario));

                testArgsMatrix.add(testArgs);
            }
        }

        return testArgsMatrix.stream()
            .map(l -> l.stream().toArray(Object[]::new))
            .toArray(Object[][]::new);
    }

    @Test(groups = {"unit"}, dataProvider = "highAvailabilityConfigs")
    public void validateApplicableRegions(
        String testScenario,
        GlobalEndpointManager globalEndpointManager,
        RxDocumentServiceRequest request,
        List<URI> expectedApplicableEndpoints) {

        logger.info("SCENARIO : {}", testScenario);

        try {
            List<URI> actualApplicableEndpoints = request.isReadOnly() ?
                globalEndpointManager.getApplicableReadEndpoints(request) :
                globalEndpointManager.getApplicableWriteEndpoints(request);

            Assertions.assertThat(actualApplicableEndpoints).hasSize(expectedApplicableEndpoints.size());

            for (int i = 0; i < expectedApplicableEndpoints.size(); i++) {
                Assertions.assertThat(actualApplicableEndpoints.get(i)).isEqualTo(expectedApplicableEndpoints.get(i));
            }
        } finally {
            globalEndpointManager.close();
        }
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

    private static GlobalEndpointManager handleDatabaseAccountType(DatabaseAccountTypes databaseAccountType) {

        GlobalEndpointManager globalEndpointManager;
        DatabaseAccountManagerInternal databaseAccountManagerInternal;
        ConnectionPolicy connectionPolicy;
        DatabaseAccount databaseAccount;
        List<DatabaseAccountLocation> readableDatabaseAccountLocations;
        List<DatabaseAccountLocation> writeableDatabaseAccountLocations;

        switch (databaseAccountType) {
            case ACCOUNT_WITH_ONE_REGION:
                databaseAccountManagerInternal = Mockito.mock(DatabaseAccountManagerInternal.class);

                readableDatabaseAccountLocations = new ArrayList<>();
                readableDatabaseAccountLocations.add(createDatabaseAccountLocation(EastUsLocation, TestAccountEastUsEndpoint.toString()));

                writeableDatabaseAccountLocations = new ArrayList<>();
                writeableDatabaseAccountLocations.add(createDatabaseAccountLocation(EastUsLocation, TestAccountEastUsEndpoint.toString()));

                databaseAccount = new DatabaseAccount();
                databaseAccount.setWritableLocations(writeableDatabaseAccountLocations);
                databaseAccount.setReadableLocations(readableDatabaseAccountLocations);
                databaseAccount.setEnableMultipleWriteLocations(writeableDatabaseAccountLocations.size() > 1);
                databaseAccount.setId(AccountId);

                connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
                connectionPolicy.setEndpointDiscoveryEnabled(true);
                connectionPolicy.setMultipleWriteRegionsEnabled(true);
                connectionPolicy.setPreferredRegions(Arrays.asList(EastUsLocation));

                Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
                Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(TestAccountEndpoint);
                Mockito.when(databaseAccountManagerInternal.getConnectionPolicy()).thenReturn(connectionPolicy);

                globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
                globalEndpointManager.init();

                return globalEndpointManager;
            case SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS:
                databaseAccountManagerInternal = Mockito.mock(DatabaseAccountManagerInternal.class);

                readableDatabaseAccountLocations = new ArrayList<>();
                readableDatabaseAccountLocations.add(createDatabaseAccountLocation(EastUsLocation, TestAccountEastUsEndpoint.toString()));
                readableDatabaseAccountLocations.add(createDatabaseAccountLocation(WestUsLocation, TestAccountWestUsEndpoint.toString()));

                writeableDatabaseAccountLocations = new ArrayList<>();
                writeableDatabaseAccountLocations.add(createDatabaseAccountLocation(EastUsLocation, TestAccountEastUsEndpoint.toString()));

                databaseAccount = new DatabaseAccount();
                databaseAccount.setWritableLocations(writeableDatabaseAccountLocations);
                databaseAccount.setReadableLocations(readableDatabaseAccountLocations);
                databaseAccount.setEnableMultipleWriteLocations(writeableDatabaseAccountLocations.size() > 1);
                databaseAccount.setId(AccountId);

                connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
                connectionPolicy.setEndpointDiscoveryEnabled(true);
                connectionPolicy.setMultipleWriteRegionsEnabled(true);
                connectionPolicy.setPreferredRegions(Arrays.asList(EastUsLocation, WestUsLocation));

                Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
                Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(TestAccountEndpoint);
                Mockito.when(databaseAccountManagerInternal.getConnectionPolicy()).thenReturn(connectionPolicy);

                globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
                globalEndpointManager.init();

                return globalEndpointManager;
            case SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS:
                databaseAccountManagerInternal = Mockito.mock(DatabaseAccountManagerInternal.class);

                readableDatabaseAccountLocations = new ArrayList<>();
                readableDatabaseAccountLocations.add(createDatabaseAccountLocation(EastUsLocation, TestAccountEastUsEndpoint.toString()));
                readableDatabaseAccountLocations.add(createDatabaseAccountLocation(WestUsLocation, TestAccountWestUsEndpoint.toString()));
                readableDatabaseAccountLocations.add(createDatabaseAccountLocation(CentralUsLocation, TestAccountCentralUsEndpoint.toString()));

                writeableDatabaseAccountLocations = new ArrayList<>();
                writeableDatabaseAccountLocations.add(createDatabaseAccountLocation(EastUsLocation, TestAccountEastUsEndpoint.toString()));

                databaseAccount = new DatabaseAccount();
                databaseAccount.setWritableLocations(writeableDatabaseAccountLocations);
                databaseAccount.setReadableLocations(readableDatabaseAccountLocations);
                databaseAccount.setEnableMultipleWriteLocations(writeableDatabaseAccountLocations.size() > 1);
                databaseAccount.setId(AccountId);

                connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
                connectionPolicy.setEndpointDiscoveryEnabled(true);
                connectionPolicy.setMultipleWriteRegionsEnabled(true);
                connectionPolicy.setPreferredRegions(Arrays.asList(EastUsLocation, WestUsLocation, CentralUsLocation));

                Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
                Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(TestAccountEndpoint);
                Mockito.when(databaseAccountManagerInternal.getConnectionPolicy()).thenReturn(connectionPolicy);

                globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
                globalEndpointManager.init();

                return globalEndpointManager;
            case MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS:
                databaseAccountManagerInternal = Mockito.mock(DatabaseAccountManagerInternal.class);

                readableDatabaseAccountLocations = new ArrayList<>();
                readableDatabaseAccountLocations.add(createDatabaseAccountLocation(EastUsLocation, TestAccountEastUsEndpoint.toString()));
                readableDatabaseAccountLocations.add(createDatabaseAccountLocation(WestUsLocation, TestAccountWestUsEndpoint.toString()));

                writeableDatabaseAccountLocations = new ArrayList<>();
                writeableDatabaseAccountLocations.add(createDatabaseAccountLocation(EastUsLocation, TestAccountEastUsEndpoint.toString()));
                writeableDatabaseAccountLocations.add(createDatabaseAccountLocation(WestUsLocation, TestAccountWestUsEndpoint.toString()));

                databaseAccount = new DatabaseAccount();
                databaseAccount.setWritableLocations(writeableDatabaseAccountLocations);
                databaseAccount.setReadableLocations(readableDatabaseAccountLocations);
                databaseAccount.setEnableMultipleWriteLocations(writeableDatabaseAccountLocations.size() > 1);
                databaseAccount.setId(AccountId);

                connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
                connectionPolicy.setEndpointDiscoveryEnabled(true);
                connectionPolicy.setMultipleWriteRegionsEnabled(true);
                connectionPolicy.setPreferredRegions(Arrays.asList(EastUsLocation, WestUsLocation));

                Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
                Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(TestAccountEndpoint);
                Mockito.when(databaseAccountManagerInternal.getConnectionPolicy()).thenReturn(connectionPolicy);

                globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
                globalEndpointManager.init();

                return globalEndpointManager;
            case MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS:
                databaseAccountManagerInternal = Mockito.mock(DatabaseAccountManagerInternal.class);

                readableDatabaseAccountLocations = new ArrayList<>();
                readableDatabaseAccountLocations.add(createDatabaseAccountLocation(EastUsLocation, TestAccountEastUsEndpoint.toString()));
                readableDatabaseAccountLocations.add(createDatabaseAccountLocation(WestUsLocation, TestAccountWestUsEndpoint.toString()));
                readableDatabaseAccountLocations.add(createDatabaseAccountLocation(CentralUsLocation, TestAccountCentralUsEndpoint.toString()));

                writeableDatabaseAccountLocations = new ArrayList<>();
                writeableDatabaseAccountLocations.add(createDatabaseAccountLocation(EastUsLocation, TestAccountEastUsEndpoint.toString()));
                writeableDatabaseAccountLocations.add(createDatabaseAccountLocation(WestUsLocation, TestAccountWestUsEndpoint.toString()));
                writeableDatabaseAccountLocations.add(createDatabaseAccountLocation(CentralUsLocation, TestAccountCentralUsEndpoint.toString()));

                databaseAccount = new DatabaseAccount();
                databaseAccount.setWritableLocations(writeableDatabaseAccountLocations);
                databaseAccount.setReadableLocations(readableDatabaseAccountLocations);
                databaseAccount.setEnableMultipleWriteLocations(writeableDatabaseAccountLocations.size() > 1);
                databaseAccount.setId(AccountId);

                connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
                connectionPolicy.setEndpointDiscoveryEnabled(true);
                connectionPolicy.setMultipleWriteRegionsEnabled(true);
                connectionPolicy.setPreferredRegions(Arrays.asList(EastUsLocation, WestUsLocation, CentralUsLocation));

                Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
                Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(TestAccountEndpoint);
                Mockito.when(databaseAccountManagerInternal.getConnectionPolicy()).thenReturn(connectionPolicy);

                globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
                globalEndpointManager.init();

                return globalEndpointManager;
            default:
                throw new IllegalArgumentException(String.format("Unknown database account type: %s", databaseAccountType));
        }
    }

    private static boolean handlePerPartitionCircuitBreakerEnforcement(
        RxDocumentServiceRequest request,
        DatabaseAccountTypes databaseAccountType,
        PerPartitionCircuitBreakerScenarios perPartitionCircuitBreakerScenarios) {

        if (!request.isReadOnly() && (
            databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS)) {

            return false;
        }

        switch (perPartitionCircuitBreakerScenarios) {
            case PER_PARTITION_CIRCUIT_BREAKER_DISABLED:
                return true;
            case PER_PARTITION_CIRCUIT_BREAKER_ONE_REGION_UNAVAILABLE:

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {
                    return false;
                }

                request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(Arrays.asList(EastUsLocation));
                return true;
            case PER_PARTITION_CIRCUIT_BREAKER_TWO_REGION_UNAVAILABLE:

                if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS || databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    return false;
                }

                request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(Arrays.asList(EastUsLocation, WestUsLocation));
                return true;
            case PER_PARTITION_CIRCUIT_BREAKER_LAST_REGION_UNAVAILABLE:
                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {
                    return false;
                }

                if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {

                    request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(Arrays.asList(CentralUsLocation));
                    return true;
                }

                request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(Arrays.asList(WestUsLocation));
                return true;
            case PER_PARTITION_CIRCUIT_BREAKER_LAST_BUT_ONE_REGION_UNAVAILABLE:
                if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {

                    request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(Arrays.asList(WestUsLocation));
                    return true;
                }

                return false;
            case PER_PARTITION_CIRCUIT_BREAKER_THREE_REGION_UNAVAILABLE:
                 return false;
            default:
                throw new IllegalArgumentException(String.format("Unknown per-partition circuit breaker scenario: %s", perPartitionCircuitBreakerScenarios));
        }

    }

    private static boolean handlePerPartitionAutomaticFailoverEnforcement(
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover,
        RxDocumentServiceRequest request,
        DatabaseAccountTypes databaseAccountType,
        PerPartitionAutomaticFailoverScenarios perPartitionAutomaticFailoverScenarios) {

        if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
            databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
            databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
            return false;
        }

        switch (perPartitionAutomaticFailoverScenarios) {
            case PER_PARTITION_AUTOMATIC_FAILOVER_DISABLED:
            case PER_PARTITION_AUTOMATIC_FAILOVER_WRITE_REGION_AVAILABLE:
                return true;
            case PER_PARTITION_AUTOMATIC_FAILOVER_WRITE_REGION_UNAVAILABLE:
                globalPartitionEndpointManagerForPerPartitionAutomaticFailover.tryMarkEndpointAsUnavailableForPartitionKeyRange(request);
                return true;
            default:
                throw new IllegalArgumentException(String.format("Unknown per-partition circuit breaker scenario: %s", perPartitionAutomaticFailoverScenarios));
        }
    }

    private static List<URI> generateExpectedApplicableEndpoints(
        DatabaseAccountTypes databaseAccountType,
        OpTypeScenarios opTypeScenario,
        UserEnforcedExcludeRegionScenarios userEnforcedExcludeRegionScenario,
        PerPartitionCircuitBreakerScenarios perPartitionCircuitBreakerScenario) {

        if (userEnforcedExcludeRegionScenario == UserEnforcedExcludeRegionScenarios.USER_ENFORCED_EXCLUDE_REGION_NONE) {
            if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_DISABLED) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountEastUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_TWO_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint, TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountCentralUsEndpoint, TestAccountEastUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint, TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_THREE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_BUT_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountEastUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                }
            }
        }
        else if (userEnforcedExcludeRegionScenario == UserEnforcedExcludeRegionScenarios.USER_ENFORCED_EXCLUDE_FIRST_PREFERRED_REGION) {
            if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_DISABLED) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountWestUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_TWO_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountWestUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountCentralUsEndpoint, TestAccountWestUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_THREE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountWestUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_BUT_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountWestUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountCentralUsEndpoint, TestAccountWestUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint, TestAccountWestUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
        } else if (userEnforcedExcludeRegionScenario == UserEnforcedExcludeRegionScenarios.USER_ENFORCED_EXCLUDE_FIRST_TWO_PREFERRED_REGIONS) {
            if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_DISABLED) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_TWO_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_THREE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint, TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint, TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_BUT_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountCentralUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }

        }  else if (userEnforcedExcludeRegionScenario == UserEnforcedExcludeRegionScenarios.USER_ENFORCED_EXCLUDE_FIRST_THREE_PREFERRED_REGIONS) {
            if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_DISABLED) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_TWO_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_THREE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
            else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_BUT_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return Arrays.asList(TestAccountEastUsEndpoint);
                    } else {
                        return Arrays.asList(TestAccountEndpoint);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private static boolean handleUserEnforcedExcludeRegionSetting(
        RxDocumentServiceRequest request,
        UserEnforcedExcludeRegionScenarios enforcedExcludeRegionScenarios) {

        switch (enforcedExcludeRegionScenarios) {
            case USER_ENFORCED_EXCLUDE_REGION_NONE:
                return true;
            case USER_ENFORCED_EXCLUDE_FIRST_PREFERRED_REGION:
                request.requestContext.setExcludeRegions(Arrays.asList(EastUsLocation));
                return true;
            case USER_ENFORCED_EXCLUDE_FIRST_TWO_PREFERRED_REGIONS:
                request.requestContext.setExcludeRegions(Arrays.asList(EastUsLocation, WestUsLocation));
                return true;
            case USER_ENFORCED_EXCLUDE_FIRST_THREE_PREFERRED_REGIONS:
                request.requestContext.setExcludeRegions(Arrays.asList(EastUsLocation, WestUsLocation, CentralUsLocation));
                return true;
            default:
                throw new IllegalArgumentException(String.format("Unknown enforced exclude region scenario: %s", enforcedExcludeRegionScenarios));
        }
    }

    private enum DatabaseAccountTypes {
        MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS,
        MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS,
        SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS,
        SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS,
        ACCOUNT_WITH_ONE_REGION
    }


    private enum OpTypeScenarios {
        IS_READ,
        IS_WRITE,
    }

    private enum UserEnforcedExcludeRegionScenarios {
        USER_ENFORCED_EXCLUDE_REGION_NONE,
        USER_ENFORCED_EXCLUDE_FIRST_PREFERRED_REGION,
        USER_ENFORCED_EXCLUDE_FIRST_TWO_PREFERRED_REGIONS,
        USER_ENFORCED_EXCLUDE_FIRST_THREE_PREFERRED_REGIONS
    }

    private enum AvailabilityStrategyScenarios {
        WITH_AVAILABILITY_STRATEGY,
        WITHOUT_AVAILABILITY_STRATEGY,
    }

    private enum PerPartitionCircuitBreakerScenarios {
        PER_PARTITION_CIRCUIT_BREAKER_DISABLED,
        PER_PARTITION_CIRCUIT_BREAKER_ONE_REGION_UNAVAILABLE,
        PER_PARTITION_CIRCUIT_BREAKER_LAST_REGION_UNAVAILABLE,
        PER_PARTITION_CIRCUIT_BREAKER_LAST_BUT_ONE_REGION_UNAVAILABLE,
        PER_PARTITION_CIRCUIT_BREAKER_TWO_REGION_UNAVAILABLE,
        PER_PARTITION_CIRCUIT_BREAKER_THREE_REGION_UNAVAILABLE
    }

    private enum PerPartitionAutomaticFailoverScenarios {
        PER_PARTITION_AUTOMATIC_FAILOVER_DISABLED,
        PER_PARTITION_AUTOMATIC_FAILOVER_WRITE_REGION_AVAILABLE,
        PER_PARTITION_AUTOMATIC_FAILOVER_WRITE_REGION_UNAVAILABLE
    }

    private static List<List<Object>> generateTestScenarioMatrix() {

        List<List<Object>> testScenarioMatrix = new ArrayList<>();

        for (DatabaseAccountTypes databaseAccountType : DatabaseAccountTypes.values()) {

            List<Object> testArgs = new ArrayList<>();

            testArgs.add(databaseAccountType);

            for (AvailabilityStrategyScenarios availabilityStrategyScenario : AvailabilityStrategyScenarios.values()) {

                testArgs.add(availabilityStrategyScenario);

                for (OpTypeScenarios opTypeScenario : OpTypeScenarios.values()) {
                    testArgs.add(opTypeScenario);

                    for (UserEnforcedExcludeRegionScenarios userEnforcedExcludeRegionScenario : UserEnforcedExcludeRegionScenarios.values()) {
                        testArgs.add(userEnforcedExcludeRegionScenario);
                        for (PerPartitionCircuitBreakerScenarios perPartitionCircuitBreakerScenario : PerPartitionCircuitBreakerScenarios.values()) {

                            testArgs.add(perPartitionCircuitBreakerScenario);
                            for (PerPartitionAutomaticFailoverScenarios perPartitionAutomaticFailoverScenario : PerPartitionAutomaticFailoverScenarios.values()) {

                                testArgs.add(perPartitionAutomaticFailoverScenario);
                                List<Object> newTestArgs = new ArrayList<>(testArgs.size());

                                newTestArgs.addAll(testArgs);

                                testScenarioMatrix.add(newTestArgs);
                                testArgs.remove(testArgs.size() - 1);
                            }
                            testArgs.remove(testArgs.size() - 1);
                        }
                        testArgs.remove(testArgs.size() - 1);
                    }
                    testArgs.remove(testArgs.size() - 1);
                }
                testArgs.remove(testArgs.size() - 1);
            }
            testArgs.remove(testArgs.size() - 1);
        }

        return testScenarioMatrix;
    }

    private static DatabaseAccountLocation createDatabaseAccountLocation(String name, String endpoint) {
        DatabaseAccountLocation dal = new DatabaseAccountLocation();
        dal.setName(name);
        dal.setEndpoint(endpoint);

        return dal;
    }
}
