// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.assertj.core.api.AssertionsForClassTypes;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SessionNotAvailableRetryTest extends TestSuiteBase {
    private static final int TIMEOUT = 60000;
    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private DatabaseAccount databaseAccount;

    @BeforeClass(groups = {"multi-region", "multi-master"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode()
            .buildAsyncClient();
        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(client);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
        GlobalEndpointManager globalEndpointManager =
            ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
        this.databaseAccount = databaseAccount;

    }

    @AfterClass(groups = {"multi-region", "multi-master"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @DataProvider(name = "preferredRegions")
    private Object[][] preferredRegions() {
        List<String> preferredLocations1 = new ArrayList<>();
        List<String> preferredLocations2 = new ArrayList<>();
        Iterator<DatabaseAccountLocation> locationIterator = this.databaseAccount.getReadableLocations().iterator();
        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            preferredLocations1.add(accountLocation.getName());
        }

        //putting preferences in opposite direction than what came from database account api
        for (int i = preferredLocations1.size() - 1; i >= 0; i--) {
            preferredLocations2.add(preferredLocations1.get(i));
        }

        return new Object[][]{
            new Object[]{preferredLocations1, OperationType.Read, false},
            new Object[]{preferredLocations2, OperationType.Read, false},
            new Object[]{Collections.emptyList(), OperationType.Read, false},
            new Object[]{preferredLocations1, OperationType.Query, false},
            new Object[]{preferredLocations2, OperationType.Query, false},
            new Object[]{Collections.emptyList(), OperationType.Query, false},
            new Object[]{preferredLocations1, OperationType.Create, true},
            new Object[]{preferredLocations2, OperationType.Create, true},
            new Object[]{Collections.emptyList(), OperationType.Create, true}
        };
    }

    @DataProvider(name = "operations")
    private Object[][] operations() {
        return new Object[][]{
            new Object[]{OperationType.Read},
            new Object[]{OperationType.Query},
            new Object[]{OperationType.Create},
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "preferredRegions", timeOut = TIMEOUT)
    public void sessionNotAvailableRetryMultiMaster(
        List<String> preferredLocations,
        OperationType operationType,
        boolean isWriteOperation) {

        List<String> preferredLocationsWithLowerCase =
            preferredLocations.stream().map(location -> location.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
        CosmosAsyncClient preferredListClient = null;
        // inject 404/1002 into all regions
        FaultInjectionRule sessionNotAvailableRule = new FaultInjectionRuleBuilder("sessionNotAvailableRuleMultiMaster-" + UUID.randomUUID())
            .condition(new FaultInjectionConditionBuilder().build())
            .result(
                FaultInjectionResultBuilders
                    .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    .build())
            .build();

        AccountLevelLocationContext accountLevelWriteableLocationContext = this.getAccountLevelLocationContext(databaseAccount, true);
        validate(accountLevelWriteableLocationContext, true);

        List<String> writeRegionList = accountLevelWriteableLocationContext.serviceOrderedWriteableRegions
            .stream()
            .map(regionName -> regionName.toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());

        AccountLevelLocationContext accountLevelReadableLocationContext = this.getAccountLevelLocationContext(databaseAccount, false);
        validate(accountLevelReadableLocationContext, false);

        List<String> readRegionList = accountLevelReadableLocationContext.serviceOrderedReadableRegions
            .stream()
            .map(regionName -> regionName.toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());

        try {
            preferredListClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .preferredRegions(preferredLocations)
                .buildAsyncClient();

            cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(preferredListClient);
            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(sessionNotAvailableRule)).block();

            try {
                PartitionKey partitionKey = new PartitionKey("Test");
                if (operationType.equals(OperationType.Read)) {
                    cosmosAsyncContainer.readItem("Test", partitionKey, TestItem.class).block();
                } else if (operationType.equals(OperationType.Query)) {
                    String query = "Select * from C";
                    CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
                    requestOptions.setPartitionKey(partitionKey);
                    cosmosAsyncContainer.queryItems(query, requestOptions, TestItem.class).byPage().blockFirst();
                } else if (operationType.equals(OperationType.Create)) {
                    TestItem item = new TestItem();
                    item.setId("Test");
                    item.setMypk("Test");
                    cosmosAsyncContainer.createItem(item, partitionKey, new CosmosItemRequestOptions()).block();
                }

                fail("Request should fail with 404/1002 error");
            } catch (CosmosException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
                assertThat(ex.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);

                if (preferredLocations == null || preferredLocations.isEmpty()) {

                    if (isWriteOperation) {
                        assertThat(ex.getDiagnostics().getContactedRegionNames().size()).isEqualTo(writeRegionList.size());
                        assertThat(ex.getDiagnostics().getContactedRegionNames().containsAll(writeRegionList)).isTrue();
                    } else {
                        assertThat(ex.getDiagnostics().getContactedRegionNames().size()).isEqualTo(readRegionList.size());
                        assertThat(ex.getDiagnostics().getContactedRegionNames().containsAll(readRegionList)).isTrue();
                    }
                } else {
                    assertThat(ex.getDiagnostics().getContactedRegionNames().size()).isEqualTo(preferredLocations.size());
                    assertThat(ex.getDiagnostics().getContactedRegionNames().containsAll(preferredLocationsWithLowerCase)).isTrue();
                }

                // validate the contacted regions follow the preferredRegion sequence
                List<String> contactedRegions = new ArrayList<>();
                String previousContactedRegion = StringUtils.EMPTY;
                ClientSideRequestStatistics clientSideRequestStatistics = BridgeInternal.getClientSideRequestStatics(ex.getDiagnostics());
                for (ClientSideRequestStatistics.StoreResponseStatistics storeResponseStatistics : clientSideRequestStatistics.getResponseStatisticsList()) {
                    if (!storeResponseStatistics.getRegionName().equalsIgnoreCase(previousContactedRegion)) {
                        contactedRegions.add(storeResponseStatistics.getRegionName().toLowerCase(Locale.ROOT));
                        previousContactedRegion = storeResponseStatistics.getRegionName().toLowerCase(Locale.ROOT);
                    }
                }
                List<String> expectedContactedRegions = new ArrayList<>();

                if (preferredLocations == null || preferredLocations.isEmpty()) {
                    // SDK will do one more round retry in first preferred region due to RenameCollectionAwareClientRetryPolicy
                    if (isWriteOperation) {
                        expectedContactedRegions.addAll(writeRegionList);
                        expectedContactedRegions.add(writeRegionList.get(0));
                    } else {
                        expectedContactedRegions.addAll(readRegionList);
                        expectedContactedRegions.add(readRegionList.get(0));
                    }

                    assertThat(contactedRegions.size()).isEqualTo(expectedContactedRegions.size());
                    assertThat(contactedRegions.containsAll(expectedContactedRegions)).isTrue();
                } else {
                    expectedContactedRegions.addAll(preferredLocationsWithLowerCase);
                    // SDK will do one more round retry in first preferred region due to RenameCollectionAwareClientRetryPolicy
                    expectedContactedRegions.add(preferredLocationsWithLowerCase.get(0));
                    assertThat(contactedRegions.size()).isEqualTo(expectedContactedRegions.size());
                    assertThat(contactedRegions.containsAll(expectedContactedRegions)).isTrue();
                }
            }
        } finally {
            sessionNotAvailableRule.disable();
            safeClose(preferredListClient);
        }
    }

    @Test(groups = {"multi-region"}, dataProvider = "preferredRegions", timeOut = TIMEOUT)
    public void sessionNotAvailableRetrySingleMaster(
        List<String> preferredLocations,
        OperationType operationType,
        boolean isWriteOperation) {

        CosmosAsyncClient preferredListClient = null;

        List<String> preferredLocationsWithLowerCase =
            preferredLocations.stream().map(location -> location.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
        // inject 404/1002 into all regions
        FaultInjectionRule sessionNotAvailableRule = new FaultInjectionRuleBuilder("sessionNotAvailableRuleSingleMaster-" + UUID.randomUUID())
            .condition(new FaultInjectionConditionBuilder().build())
            .result(
                FaultInjectionResultBuilders
                    .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    .build())
            .build();

        try {
            preferredListClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .preferredRegions(preferredLocations)
                .buildAsyncClient();

            cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(preferredListClient);
            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(sessionNotAvailableRule)).block();

            PartitionKey partitionKey = new PartitionKey("Test");
            try {
                if (operationType.equals(OperationType.Read)) {
                    cosmosAsyncContainer.readItem("TestId", partitionKey, TestItem.class).block();
                } else if (operationType.equals(OperationType.Query)) {
                    String query = "Select * from C";
                    CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
                    requestOptions.setPartitionKey(new PartitionKey("Test"));
                    cosmosAsyncContainer.queryItems(query, requestOptions, TestItem.class).byPage().blockFirst();
                } else if (operationType.equals(OperationType.Create)) {
                    TestItem item = new TestItem();
                    item.setId("Test");
                    item.setMypk("Test");
                    cosmosAsyncContainer.createItem(item, partitionKey, new CosmosItemRequestOptions()).block();
                }

                fail("Request should fail with 404/1002 error");
            } catch (CosmosException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
                assertThat(ex.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);

                AccountLevelLocationContext accountLevelWriteableLocationContext = this.getAccountLevelLocationContext(databaseAccount, true);
                validate(accountLevelWriteableLocationContext, true);

                List<String> writeRegionList = accountLevelWriteableLocationContext.serviceOrderedWriteableRegions
                    .stream()
                    .map(regionName -> regionName.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toList());

                // for single master, when retrying 404/1002, it will retry on the write region
                // so for write operation or if the first preferred region is the same as write region, the contracted region count should 1
                if (operationType.isWriteOperation()
                    || !preferredLocationsWithLowerCase.isEmpty() && preferredLocationsWithLowerCase.get(0).equalsIgnoreCase(writeRegionList.get(0))) {
                    assertThat(ex.getDiagnostics().getContactedRegionNames().size()).isEqualTo(1);
                } else if (preferredLocations.isEmpty()) {
                    assertThat(ex.getDiagnostics().getContactedRegionNames().size()).isEqualTo(1);

                    // validate the contacted region sequence
                    List<String> contactedRegions = new ArrayList<>();
                    String previousContactedRegion = StringUtils.EMPTY;
                    ClientSideRequestStatistics clientSideRequestStatistics = BridgeInternal.getClientSideRequestStatics(ex.getDiagnostics());
                    for (ClientSideRequestStatistics.StoreResponseStatistics storeResponseStatistics : clientSideRequestStatistics.getResponseStatisticsList()) {
                        if (!storeResponseStatistics.getRegionName().equalsIgnoreCase(previousContactedRegion)) {
                            contactedRegions.add(storeResponseStatistics.getRegionName().toLowerCase(Locale.ROOT));
                            previousContactedRegion = storeResponseStatistics.getRegionName().toLowerCase(Locale.ROOT);
                        }
                    }

                    List<String> expectedContactedRegions = new ArrayList<>();

                    // retries stick to sole write region in single-write accounts for read-semantic operations
                    // where all preferred regions is not set on client
                    expectedContactedRegions.addAll(writeRegionList);
                    assertThat(contactedRegions.size()).isEqualTo(expectedContactedRegions.size());
                    assertThat(contactedRegions.containsAll(expectedContactedRegions)).isTrue();
                }
                else {

                    assertThat(ex.getDiagnostics().getContactedRegionNames().size()).isEqualTo(2);

                    // validate the contacted region sequence
                    List<String> contactedRegions = new ArrayList<>();
                    String previousContactedRegion = StringUtils.EMPTY;
                    ClientSideRequestStatistics clientSideRequestStatistics = BridgeInternal.getClientSideRequestStatics(ex.getDiagnostics());
                    for (ClientSideRequestStatistics.StoreResponseStatistics storeResponseStatistics : clientSideRequestStatistics.getResponseStatisticsList()) {
                        if (!storeResponseStatistics.getRegionName().equalsIgnoreCase(previousContactedRegion)) {
                            contactedRegions.add(storeResponseStatistics.getRegionName().toLowerCase(Locale.ROOT));
                            previousContactedRegion = storeResponseStatistics.getRegionName().toLowerCase(Locale.ROOT);
                        }
                    }

                    List<String> expectedContactedRegions = new ArrayList<>();
                    expectedContactedRegions.add(preferredLocationsWithLowerCase.get(0));
                    expectedContactedRegions.addAll(writeRegionList);
                    // SDK will do one more round retry in first preferred region due to RenameCollectionAwareClientRetryPolicy
                    expectedContactedRegions.add(preferredLocationsWithLowerCase.get(0));
                    assertThat(contactedRegions.size()).isEqualTo(expectedContactedRegions.size());
                    assertThat(contactedRegions.containsAll(expectedContactedRegions)).isTrue();
                }
            }
        } finally {
            sessionNotAvailableRule.disable();
            safeClose(preferredListClient);
        }
    }

    private AccountLevelLocationContext getAccountLevelLocationContext(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();

        List<String> serviceOrderedReadableRegions = new ArrayList<>();
        List<String> serviceOrderedWriteableRegions = new ArrayList<>();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());

            if (writeOnly) {
                serviceOrderedWriteableRegions.add(accountLocation.getName());
            } else {
                serviceOrderedReadableRegions.add(accountLocation.getName());
            }
        }

        return new AccountLevelLocationContext(
            serviceOrderedReadableRegions,
            serviceOrderedWriteableRegions,
            regionMap);
    }

    private static void validate(AccountLevelLocationContext accountLevelLocationContext, boolean isWriteOnly) {

        AssertionsForClassTypes.assertThat(accountLevelLocationContext).isNotNull();

        if (isWriteOnly) {
            AssertionsForClassTypes.assertThat(accountLevelLocationContext.serviceOrderedWriteableRegions).isNotNull();
            AssertionsForClassTypes.assertThat(accountLevelLocationContext.serviceOrderedWriteableRegions.size()).isGreaterThanOrEqualTo(1);
        } else {
            AssertionsForClassTypes.assertThat(accountLevelLocationContext.serviceOrderedReadableRegions).isNotNull();
            AssertionsForClassTypes.assertThat(accountLevelLocationContext.serviceOrderedReadableRegions.size()).isGreaterThanOrEqualTo(1);
        }
    }

    private class TestItem {
        private String id;
        private String mypk;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }
    }

    private static class AccountLevelLocationContext {
        private final List<String> serviceOrderedReadableRegions;
        private final List<String> serviceOrderedWriteableRegions;
        private final Map<String, String> regionNameToEndpoint;

        public AccountLevelLocationContext(
            List<String> serviceOrderedReadableRegions,
            List<String> serviceOrderedWriteableRegions,
            Map<String, String> regionNameToEndpoint) {

            this.serviceOrderedReadableRegions = serviceOrderedReadableRegions;
            this.serviceOrderedWriteableRegions = serviceOrderedWriteableRegions;
            this.regionNameToEndpoint = regionNameToEndpoint;
        }
    }
}
