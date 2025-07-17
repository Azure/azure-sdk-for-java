// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.AvailabilityStrategyContext;
import com.azure.cosmos.implementation.ClientRetryPolicy;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.CrossRegionAvailabilityContextForRxDocumentServiceRequest;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.DatabaseAccountManagerInternal;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PointOperationContextForCircuitBreaker;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.ServiceUnavailableException;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

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

    private static final ServiceUnavailableException SERVICE_UNAVAILABLE_EXCEPTION
        = new ServiceUnavailableException(null, 0L, null, new HashMap<>(), HttpConstants.SubStatusCodes.SERVER_GENERATED_503);

    @DataProvider(name = "highAvailabilityConfigs")
    public Object[][] highAvailabilityConfigs() {

        List<List<Object>> testScenarioMatrix = generateTestScenarioMatrix();
        List<List<Object>> testArgsMatrix = new ArrayList<>();

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

            testArgs.add(globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

            if (isValidScenario) {
                testArgs.add(generateExpectedApplicableEndpoints(
                    databaseAccountType,
                    opTypeScenario,
                    userEnforcedExcludeRegionScenario,
                    perPartitionCircuitBreakerScenario,
                    perPartitionAutomaticFailoverScenario));

                testArgsMatrix.add(testArgs);
            }
        }

        return testArgsMatrix.stream()
            .map(l -> l.stream().toArray(Object[]::new))
            .toArray(Object[][]::new);
    }

    // the test validateApplicableRegions runs through various scenarios such as:
    //      - what is the account type? multi-write [or] single-write? what regions does such an account have?
    //      - what regions are excluded by the customer?
    //      - what regions are treated as unavailable by per-partition circuit breaker (PPCB)?
    //      - what regions have been failed over by per-partition automatic failover (PPAF)?
    //      - has the customer configured the client with empty preferred regions?
    //      - is the operation read or write?
    // given a scenario, the goal is to assert against the applicable endpoints for a given operation type
    //  & what is the first endpoint a request should go to (if there is an override through PPAF)
    // for reads in single-write multi-region scenarios, it is also checked whether PPAF override
    //  can be applied (exclude all regions / empty preferred regions) but only for first call,
    //  second call onwards, request should go to account-level primary region
    @Test(groups = {"unit"}, dataProvider = "highAvailabilityConfigs")
    public void validateApplicableRegions(
        String testScenario,
        GlobalEndpointManager globalEndpointManager,
        RxDocumentServiceRequest request,
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover,
        ResolvedEndpointsContext expectedResolvedEndpointsContext) {

        logger.info("SCENARIO : {}", testScenario);

        try {

            GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManagerForPerPartitionCircuitBreaker
                = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class);

            List<RegionalRoutingContext> actualApplicableRegionalRoutingContexts = request.isReadOnly() ?
                globalEndpointManager.getApplicableReadRegionalRoutingContexts(request) :
                globalEndpointManager.getApplicableWriteRegionalRoutingContexts(request);

            List<RegionalRoutingContext> expectedApplicableRegionalRoutingContexts
                = expectedResolvedEndpointsContext.applicableRegionalRoutingContexts;
            RegionalRoutingContext expectedRegionalRoutingContextToRoute
                = expectedResolvedEndpointsContext.regionalRoutingContextToRoute;

            Assertions.assertThat(actualApplicableRegionalRoutingContexts).hasSize(expectedApplicableRegionalRoutingContexts.size());

            ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(
                mockDiagnosticsClientContext(),
                globalEndpointManager,
                true,
                new ThrottlingRetryOptions(),
                null,
                globalPartitionEndpointManagerForPerPartitionCircuitBreaker,
                globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

            for (int i = 0; i < expectedApplicableRegionalRoutingContexts.size(); i++) {
                Assertions.assertThat(actualApplicableRegionalRoutingContexts.get(i)).isEqualTo(expectedApplicableRegionalRoutingContexts.get(i));
            }

            clientRetryPolicy.onBeforeSendRequest(request);

            Assertions.assertThat(request.requestContext.regionalRoutingContextToRoute).isEqualTo(expectedRegionalRoutingContextToRoute);

            // if PPAF has applied override and applicable-region set size > 1, start with
            CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest
                = request.requestContext.getCrossRegionAvailabilityContext();

            Assertions.assertThat(crossRegionAvailabilityContextForRequest).isNotNull();

            if (crossRegionAvailabilityContextForRequest.hasPerPartitionAutomaticFailoverBeenAppliedForReads()) {
                Assertions.assertThat(request.isReadOnlyRequest()).isTrue();

                // test retry flow for such reads when there are more than 1 applicable regions
                // locationIndex is incremented in this flow
                clientRetryPolicy.shouldRetry(SERVICE_UNAVAILABLE_EXCEPTION).block();

                // re-evaluate applicable endpoints / regions and location endpoint to route
                clientRetryPolicy.onBeforeSendRequest(request);

                actualApplicableRegionalRoutingContexts = globalEndpointManager.getApplicableReadRegionalRoutingContexts(request);

                if (actualApplicableRegionalRoutingContexts.size() > 1) {
                    Assertions.assertThat(actualApplicableRegionalRoutingContexts.size()).isEqualTo(expectedApplicableRegionalRoutingContexts.size() + 1);
                    Assertions.assertThat(actualApplicableRegionalRoutingContexts.get(0)).isEqualTo(actualApplicableRegionalRoutingContexts.get(1));

                    for (int i = 1; i < actualApplicableRegionalRoutingContexts.size(); i++) {
                        Assertions.assertThat(actualApplicableRegionalRoutingContexts.get(i)).isEqualTo(expectedApplicableRegionalRoutingContexts.get(i - 1));
                    }

                    Assertions.assertThat(request.requestContext.regionalRoutingContextToRoute).isEqualTo(actualApplicableRegionalRoutingContexts.get(0));
                }
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
        LocationCache locationCache;
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

                locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);
                locationCache.onDatabaseAccountRead(databaseAccount);

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

                locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);
                locationCache.onDatabaseAccountRead(databaseAccount);

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

                locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);
                locationCache.onDatabaseAccountRead(databaseAccount);

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

                locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);
                locationCache.onDatabaseAccountRead(databaseAccount);

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
                locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);
                locationCache.onDatabaseAccountRead(databaseAccount);

                return globalEndpointManager;
            case ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION:
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
                connectionPolicy.setPreferredRegions(Collections.emptyList());

                Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
                Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(TestAccountEndpoint);
                Mockito.when(databaseAccountManagerInternal.getConnectionPolicy()).thenReturn(connectionPolicy);

                globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());

                locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);
                locationCache.onDatabaseAccountRead(databaseAccount);

                return globalEndpointManager;
            case SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION:
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
                connectionPolicy.setPreferredRegions(Collections.emptyList());

                Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
                Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(TestAccountEndpoint);
                Mockito.when(databaseAccountManagerInternal.getConnectionPolicy()).thenReturn(connectionPolicy);

                globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());

                locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);
                locationCache.onDatabaseAccountRead(databaseAccount);

                return globalEndpointManager;
            case SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION:
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
                connectionPolicy.setPreferredRegions(Collections.emptyList());

                Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
                Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(TestAccountEndpoint);
                Mockito.when(databaseAccountManagerInternal.getConnectionPolicy()).thenReturn(connectionPolicy);

                globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());

                locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);
                locationCache.onDatabaseAccountRead(databaseAccount);

                return globalEndpointManager;
            case MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION:
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
                connectionPolicy.setPreferredRegions(Collections.emptyList());

                Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
                Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(TestAccountEndpoint);
                Mockito.when(databaseAccountManagerInternal.getConnectionPolicy()).thenReturn(connectionPolicy);

                globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());

                locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);
                locationCache.onDatabaseAccountRead(databaseAccount);

                return globalEndpointManager;
            case MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION:
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
                connectionPolicy.setPreferredRegions(Collections.emptyList());

                Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
                Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(TestAccountEndpoint);
                Mockito.when(databaseAccountManagerInternal.getConnectionPolicy()).thenReturn(connectionPolicy);

                globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());

                locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);
                locationCache.onDatabaseAccountRead(databaseAccount);

                return globalEndpointManager;
            default:
                throw new IllegalArgumentException(String.format("Unknown database account type: %s", databaseAccountType));
        }
    }

    private static boolean handlePerPartitionCircuitBreakerEnforcement(
        RxDocumentServiceRequest request,
        DatabaseAccountTypes databaseAccountType,
        PerPartitionCircuitBreakerScenarios perPartitionCircuitBreakerScenarios) {

        switch (perPartitionCircuitBreakerScenarios) {
            case PER_PARTITION_CIRCUIT_BREAKER_DISABLED:
                return true;
            case PER_PARTITION_CIRCUIT_BREAKER_ONE_REGION_UNAVAILABLE:

                if (!request.isReadOnly() && (
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                        databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION)) {

                    return false;
                }

                request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(Arrays.asList(EastUsLocation));
                return true;
            case PER_PARTITION_CIRCUIT_BREAKER_TWO_REGION_UNAVAILABLE:

                if (!request.isReadOnly() && (
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                        databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION)) {

                    return false;
                }

                if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    return false;
                }

                request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(Arrays.asList(EastUsLocation, WestUsLocation));
                return true;
            case PER_PARTITION_CIRCUIT_BREAKER_LAST_REGION_UNAVAILABLE:
                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {
                    return false;
                }

                if (!request.isReadOnly() && (
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                        databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION)) {

                    return false;
                }

                if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {

                    request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(Arrays.asList(CentralUsLocation));
                    return true;
                }

                request.requestContext.setUnavailableRegionsForPerPartitionCircuitBreaker(Arrays.asList(WestUsLocation));
                return true;
            case PER_PARTITION_CIRCUIT_BREAKER_LAST_BUT_ONE_REGION_UNAVAILABLE:

                if (!request.isReadOnly() && (
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                        databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                        databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION)) {

                    return false;
                }

                if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {

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

        RxDocumentServiceRequest writeOpRequest;

        switch (perPartitionAutomaticFailoverScenarios) {
            case PER_PARTITION_AUTOMATIC_FAILOVER_DISABLED:
                return true;
            case PER_PARTITION_AUTOMATIC_FAILOVER_WRITE_REGION_AVAILABLE:

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    return false;
                }

                return true;
            case PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE:

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    return false;
                }

                writeOpRequest = createRequest(OperationType.Create, false);
                writeOpRequest.requestContext.routeToLocation(new RegionalRoutingContext(TestAccountEastUsEndpoint));

                request.requestContext.routeToLocation(new RegionalRoutingContext(TestAccountEastUsEndpoint));
                globalPartitionEndpointManagerForPerPartitionAutomaticFailover.tryMarkEndpointAsUnavailableForPartitionKeyRange(request.isReadOnlyRequest() ? writeOpRequest : request, false);
                return true;
            case PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE:
                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    return false;
                }

                writeOpRequest = createRequest(OperationType.Create, false);

                writeOpRequest.requestContext.routeToLocation(new RegionalRoutingContext(TestAccountEastUsEndpoint));
                request.requestContext.routeToLocation(new RegionalRoutingContext(TestAccountEastUsEndpoint));

                globalPartitionEndpointManagerForPerPartitionAutomaticFailover.tryMarkEndpointAsUnavailableForPartitionKeyRange(request.isReadOnly() ? writeOpRequest : request, false);

                request.requestContext.routeToLocation(new RegionalRoutingContext(TestAccountWestUsEndpoint));
                writeOpRequest.requestContext.routeToLocation(new RegionalRoutingContext(TestAccountWestUsEndpoint));

                globalPartitionEndpointManagerForPerPartitionAutomaticFailover.tryMarkEndpointAsUnavailableForPartitionKeyRange(request.isReadOnly() ? writeOpRequest : request, false);

                return true;
            default:
                throw new IllegalArgumentException(String.format("Unknown per-partition circuit breaker scenario: %s", perPartitionAutomaticFailoverScenarios));
        }
    }

    private static ResolvedEndpointsContext generateExpectedApplicableEndpoints(
        DatabaseAccountTypes databaseAccountType,
        OpTypeScenarios opTypeScenario,
        UserEnforcedExcludeRegionScenarios userEnforcedExcludeRegionScenario,
        PerPartitionCircuitBreakerScenarios perPartitionCircuitBreakerScenario,
        PerPartitionAutomaticFailoverScenarios perPartitionAutomaticFailoverScenario) {

        if (userEnforcedExcludeRegionScenario == UserEnforcedExcludeRegionScenarios.USER_ENFORCED_EXCLUDE_REGION_NONE) {
            if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_DISABLED) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario
                            == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario
                            == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario
                            == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario
                            == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario
                            == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario
                            == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountEastUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_TWO_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint, TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint, TestAccountEastUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint, TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_THREE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_BUT_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountEastUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                }
            }
        } else if (userEnforcedExcludeRegionScenario == UserEnforcedExcludeRegionScenarios.USER_ENFORCED_EXCLUDE_FIRST_PREFERRED_REGION) {
            if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_DISABLED) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_TWO_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint, TestAccountWestUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint, TestAccountWestUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint, TestAccountWestUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_THREE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint, TestAccountWestUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                            if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                                return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                            }
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountWestUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_BUT_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint, TestAccountWestUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint, TestAccountWestUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountWestUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint, TestAccountWestUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            }
        } else if (userEnforcedExcludeRegionScenario == UserEnforcedExcludeRegionScenarios.USER_ENFORCED_EXCLUDE_FIRST_TWO_PREFERRED_REGIONS) {
            if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_DISABLED) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    } else {
                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_TWO_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_THREE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint, TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                            if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                                return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountCentralUsEndpoint));
                            } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                                return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountCentralUsEndpoint));
                            }
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint, TestAccountCentralUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_BUT_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountCentralUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            }

        } else if (userEnforcedExcludeRegionScenario == UserEnforcedExcludeRegionScenarios.USER_ENFORCED_EXCLUDE_FIRST_THREE_PREFERRED_REGIONS) {
            if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_DISABLED) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_TWO_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_THREE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {


                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            } else if (perPartitionCircuitBreakerScenario == PerPartitionCircuitBreakerScenarios.PER_PARTITION_CIRCUIT_BREAKER_LAST_BUT_ONE_REGION_UNAVAILABLE) {

                if (databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION ||
                    databaseAccountType == DatabaseAccountTypes.ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION) {

                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }

                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {
                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {
                        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                } else if (databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS ||
                    databaseAccountType == DatabaseAccountTypes.SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION) {
                    if (opTypeScenario == OpTypeScenarios.IS_READ) {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEastUsEndpoint));
                    } else {

                        if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountWestUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        } else if (perPartitionAutomaticFailoverScenario == PerPartitionAutomaticFailoverScenarios.PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE) {
                            return new ResolvedEndpointsContext(TestAccountCentralUsEndpoint, Arrays.asList(TestAccountEndpoint));
                        }

                        return new ResolvedEndpointsContext(TestAccountEastUsEndpoint, Arrays.asList(TestAccountEndpoint));
                    }
                }
            }
        }

        return new ResolvedEndpointsContext(TestAccountEndpoint, Arrays.asList(TestAccountEndpoint));
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
        ACCOUNT_WITH_ONE_REGION,
        MULTI_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION,
        MULTI_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION,
        SINGLE_WRITE_ACCOUNT_WITH_THREE_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION,
        SINGLE_WRITE_ACCOUNT_WITH_TWO_REGIONS_CLIENT_WITH_NO_PREFERRED_REGION,
        ACCOUNT_WITH_ONE_REGION_CLIENT_WITH_NO_PREFERRED_REGION,
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
        PER_PARTITION_AUTOMATIC_FAILOVER_PRIMARY_REGION_UNAVAILABLE,
        PER_PARTITION_AUTOMATIC_FAILOVER_BOTH_PRIMARY_AND_SECONDARY_REGION_UNAVAILABLE,
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

    static class ResolvedEndpointsContext {

        private final URI locationEndpointToRoute;
        private final RegionalRoutingContext regionalRoutingContextToRoute;
        private final List<URI> applicableEndpoints;
        private final List<RegionalRoutingContext> applicableRegionalRoutingContexts;

        public ResolvedEndpointsContext(URI locationEndpointToRoute, List<URI> applicableGatewayRegionalEndpoint) {
            this.locationEndpointToRoute = locationEndpointToRoute;
            this.regionalRoutingContextToRoute = new RegionalRoutingContext(this.locationEndpointToRoute);
            this.applicableEndpoints = applicableGatewayRegionalEndpoint;
            this.applicableRegionalRoutingContexts = new ArrayList<>();

            for (URI applicableEndpoint : applicableGatewayRegionalEndpoint) {
                applicableRegionalRoutingContexts.add(new RegionalRoutingContext(applicableEndpoint));
            }
        }
    }
}
