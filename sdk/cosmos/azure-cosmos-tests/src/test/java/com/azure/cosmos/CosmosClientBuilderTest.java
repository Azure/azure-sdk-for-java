// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.ApiType;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RegionScopedSessionContainer;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.SessionContainer;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosClientBuilderTest {
    String hostName = "https://sample-account.documents.azure.com:443/";

    @DataProvider(name = "regionScopedSessionContainerConfigs")
    public Object[] regionScopedSessionContainerConfigs() {
        return new Object[] {false, true};
    }

    @Test(groups = "unit")
    public void validateBadPreferredRegions1() {
        try {
            CosmosAsyncClient client = new CosmosClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(hostName)
                .preferredRegions(Arrays.asList("westus1,eastus1"))
                .buildAsyncClient();
            client.close();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(Exception.class);
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e).hasCauseExactlyInstanceOf(URISyntaxException.class);
            assertThat(e.getMessage()).isEqualTo("invalid location [westus1,eastus1] or serviceEndpoint [https://sample-account.documents.azure.com:443/]");
        }
    }

    @Test(groups = "unit")
    public void validateBadPreferredRegions2() {
        try {
            CosmosAsyncClient client = new CosmosClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(hostName)
                .preferredRegions(Arrays.asList(" "))
                .buildAsyncClient();
            client.close();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("preferredRegion can't be empty");
        }
    }

    @Test(groups = "emulator")
    public void validateApiTypePresent() {
        ApiType apiType = ApiType.TABLE;
        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();

        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(directConnectionConfig)
            .userAgentSuffix("custom-direct-client")
            .multipleWriteRegionsEnabled(false)
            .endpointDiscoveryEnabled(false)
            .readRequestsFallbackEnabled(true);

         ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor accessor =
            ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();
         accessor.setCosmosClientApiType(cosmosClientBuilder, apiType);

        RxDocumentClientImpl documentClient =
            (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(cosmosClientBuilder.buildAsyncClient());
        assertThat(ReflectionUtils.getApiType(documentClient)).isEqualTo(apiType);
    }

    @Test(groups = "emulator", dataProvider = "regionScopedSessionContainerConfigs")
    public void validateSessionTokenCapturingForAccountDefaultConsistency(boolean shouldRegionScopedSessionContainerEnabled) {

        try {

            if (shouldRegionScopedSessionContainerEnabled) {
                System.setProperty("COSMOS.SESSION_CAPTURING_TYPE", "REGION_SCOPED");
            }

            CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .userAgentSuffix("custom-direct-client");

            CosmosAsyncClient client = cosmosClientBuilder.buildAsyncClient();
            RxDocumentClientImpl documentClient =
                (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);

            if (documentClient.getDefaultConsistencyLevelOfAccount() != ConsistencyLevel.SESSION) {
                throw new SkipException("This test is only applicable when default account-level consistency is Session.");
            }

            ISessionContainer sessionContainer = documentClient.getSession();

            if (System.getProperty("COSMOS.SESSION_CAPTURING_TYPE") != null && System.getProperty("COSMOS.SESSION_CAPTURING_TYPE").equals("REGION_SCOPED")) {
                assertThat(sessionContainer instanceof RegionScopedSessionContainer).isTrue();
            } else {
                assertThat(sessionContainer instanceof SessionContainer).isTrue();
            }

            assertThat(sessionContainer.getDisableSessionCapturing()).isEqualTo(false);
        } finally {
            System.clearProperty("COSMOS.SESSION_CAPTURING_TYPE");
        }
    }

    // set env variable to COSMOS.SESSION_CAPTURING_TYPE to REGION_SCOPED to test all possible assertions
    @Test(groups = "unit", enabled = false)
    public void validateSessionTokenCapturingForAccountDefaultConsistencyWithEnvVariable() {

        try {

            CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .userAgentSuffix("custom-direct-client");

            CosmosAsyncClient client = cosmosClientBuilder.buildAsyncClient();
            RxDocumentClientImpl documentClient =
                (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);

            if (documentClient.getDefaultConsistencyLevelOfAccount() != ConsistencyLevel.SESSION) {
                throw new SkipException("This test is only applicable when default account-level consistency is Session.");
            }

            ISessionContainer sessionContainer = documentClient.getSession();

            if (System.getenv("COSMOS.SESSION_CAPTURING_TYPE") != null && System.getenv("COSMOS.SESSION_CAPTURING_TYPE").equals("REGION_SCOPED")) {
                assertThat(sessionContainer instanceof RegionScopedSessionContainer).isTrue();
            } else {
                assertThat(sessionContainer instanceof SessionContainer).isTrue();
            }

            assertThat(sessionContainer.getDisableSessionCapturing()).isEqualTo(false);
        } finally {
            System.clearProperty("COSMOS.SESSION_CAPTURING_TYPE");
        }
    }

    @Test(groups = "emulator")
    public void validateContainerCreationInterceptor() {
        CosmosClient clientWithoutInterceptor = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .userAgentSuffix("noInterceptor")
            .buildClient();

        ConcurrentMap<CacheKey, List<?>> queryCache = new ConcurrentHashMap<>();

        CosmosClient clientWithInterceptor = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .userAgentSuffix("withInterceptor")
            .containerCreationInterceptor(originalContainer -> new CacheAndValidateQueriesContainer(originalContainer, queryCache))
            .buildClient();

        CosmosAsyncClient asyncClientWithInterceptor = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .userAgentSuffix("withInterceptor")
            .containerCreationInterceptor(originalContainer -> new CacheAndValidateQueriesContainer(originalContainer, queryCache))
            .buildAsyncClient();

        CosmosContainer normalContainer = clientWithoutInterceptor
            .getDatabase("TestDB")
            .getContainer("TestContainer");
        assertThat(normalContainer).isNotNull();
        assertThat(normalContainer.getClass()).isEqualTo(CosmosContainer.class);
        assertThat(normalContainer.asyncContainer.getClass()).isEqualTo(CosmosAsyncContainer.class);

        CosmosContainer customSyncContainer = clientWithInterceptor
            .getDatabase("TestDB")
            .getContainer("TestContainer");
        assertThat(customSyncContainer).isNotNull();
        assertThat(customSyncContainer.getClass()).isEqualTo(CosmosContainer.class);
        assertThat(customSyncContainer.asyncContainer.getClass()).isEqualTo(CacheAndValidateQueriesContainer.class);

        CosmosAsyncContainer customAsyncContainer = asyncClientWithInterceptor
            .getDatabase("TestDB")
            .getContainer("TestContainer");
        assertThat(customAsyncContainer).isNotNull();
        assertThat(customAsyncContainer.getClass()).isEqualTo(CacheAndValidateQueriesContainer.class);

        try {
            customSyncContainer.queryItems("SELECT * from c", null, ObjectNode.class);
            fail("Unparameterized query should throw");
        } catch (IllegalStateException expectedError) {}

        try {
            customAsyncContainer.queryItems("SELECT * from c", null, ObjectNode.class);
            fail("Unparameterized query should throw");
        } catch (IllegalStateException expectedError) {}

        try {
            customAsyncContainer.queryItems("SELECT * from c", ObjectNode.class);
            fail("Unparameterized query should throw");
        } catch (IllegalStateException expectedError) {}

        SqlQuerySpec querySpec = new SqlQuerySpec().setQueryText("SELECT * from c");
        assertThat(queryCache).size().isEqualTo(0);

        try {
            List<ObjectNode> items = customSyncContainer
                .queryItems(querySpec, null, ObjectNode.class)
                .stream().collect(Collectors.toList());
            fail("Not yet cached - the query above should always throw");
        } catch (CosmosException cosmosException) {
            // Container does not exist - when not cached should fail
            assertThat(cosmosException.getStatusCode()).isEqualTo(404);
            assertThat(cosmosException.getSubStatusCode()).isEqualTo(1003);
        }

        queryCache.putIfAbsent(new CacheKey(ObjectNode.class.getCanonicalName(), querySpec), new ArrayList<>());
        assertThat(queryCache).size().isEqualTo(1);

        // Validate that CacheKey equality check works
        queryCache.putIfAbsent(new CacheKey(ObjectNode.class.getCanonicalName(), querySpec), new ArrayList<>());
        assertThat(queryCache).size().isEqualTo(1);

        // Validate that form cache the results can be served
        List<ObjectNode> items = customSyncContainer
            .queryItems(querySpec, null, ObjectNode.class)
            .stream().collect(Collectors.toList());

        querySpec = new SqlQuerySpec().setQueryText("SELECT * from c");
        CosmosPagedFlux<ObjectNode> cachedPagedFlux = customAsyncContainer
            .queryItems(querySpec, null, ObjectNode.class);
        assertThat(cachedPagedFlux.getClass().getName()).startsWith("com.azure.cosmos.util.CosmosPagedFluxStaticListImpl");

        // Validate that uncached query form async Container also fails with 404 due to non-existing Container
        querySpec = new SqlQuerySpec().setQueryText("SELECT * from r");
        try {
            CosmosPagedFlux<ObjectNode> uncachedPagedFlux = customAsyncContainer
                .queryItems(querySpec, null, ObjectNode.class);
        } catch (CosmosException cosmosException) {
            assertThat(cosmosException.getStatusCode()).isEqualTo(404);
            assertThat(cosmosException.getSubStatusCode()).isEqualTo(1003);
        }
    }

    // Test to validate that the default value of TCP NRTO (5s) is not overridden to a lower value
    // when the user sets a lower value in DirectConnectionConfig (which is not allowed).
    // The user can only override the TCP NRTO to a higher value.
    // This is to ensure that the TCP NRTO is not set to a very low value which can cause
    // operations such as queries, change feed reads, stored procedure executions to fail
    // due to the BELatency taking > 5s in some cases for the aforementioned operation types
    @Test(groups = "emulator")
    public void validateTcpNrtoOverride() {

        DirectConnectionConfig directConnectionConfig = new DirectConnectionConfig();
        directConnectionConfig.setNetworkRequestTimeout(Duration.ofSeconds(1));

        try (CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(directConnectionConfig)
            .buildClient()) {

            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);
            ConnectionPolicy connectionPolicy = rxDocumentClient.getConnectionPolicy();

            assertThat(connectionPolicy).isNotNull();
            assertThat(connectionPolicy.getTcpNetworkRequestTimeout().toMillis()).isEqualTo(5_000L);
            assertThat(connectionPolicy.getHttpNetworkRequestTimeout().toMillis()).isEqualTo(60_000L);
        } catch (Exception e) {
            fail("CosmosClientBuilder should implement AutoCloseable");
        }

        directConnectionConfig = new DirectConnectionConfig();
        directConnectionConfig.setNetworkRequestTimeout(Duration.ofSeconds(7));

        try (CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(directConnectionConfig)
            .buildClient()) {

            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);
            ConnectionPolicy connectionPolicy = rxDocumentClient.getConnectionPolicy();

            assertThat(connectionPolicy).isNotNull();
            assertThat(connectionPolicy.getTcpNetworkRequestTimeout().toMillis()).isEqualTo(7_000L);
        } catch (Exception e) {
            fail("CosmosClientBuilder should implement AutoCloseable");
        }
    }

    private static class CacheKey {
        private final String className;
        private final String queryText;

        private final List<SqlParameter> parameters;
        public CacheKey(String className, SqlQuerySpec querySpec) {
            this.className = className;
            this.queryText = querySpec.getQueryText();
            List<SqlParameter> tempParameters = querySpec.getParameters();
            if (tempParameters != null) {
                tempParameters.sort(Comparator.comparing(SqlParameter::getName));
                this.parameters = tempParameters;
            } else {
                this.parameters = new ArrayList<>();
            }
        }

        @Override
        public int hashCode() {
            Object[] temp = new Object[2 + this.parameters.size()];
            temp[0] = this.className;
            temp[1] = this.queryText;
            for (int i = 0; i < this.parameters.size(); i++) {
                temp[2 + i] = this.parameters.get(i).getValue(Object.class);
            }

            return Objects.hash(temp);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (!(obj instanceof CacheKey)) {
                return false;
            }

            CacheKey other = (CacheKey)obj;
            if (!this.className.equals(other.className)) {
                return false;
            }

            if (!this.queryText.equals(other.queryText)) {
                return false;
            }

            if (this.parameters.size() != other.parameters.size()) {
                return false;
            }

            for (int i = 0; i < this.parameters.size(); i++) {
                if (!this.parameters.get(i).getName().equals(other.parameters.get(i).getName())) {
                    return false;
                }

                if (!this.parameters.get(i).getValue(Object.class).equals(other.parameters.get(i).getValue(Object.class))) {
                    return false;
                }
            }

            return true;
        }
    }

    private static class CacheAndValidateQueriesContainer extends CosmosAsyncContainer {
        private final ConcurrentMap<CacheKey, List<?>> queryCache;

        protected CacheAndValidateQueriesContainer(
            CosmosAsyncContainer toBeWrappedContainer,
            ConcurrentMap<CacheKey, List<?>> queryCache) {

            super(toBeWrappedContainer);
            this.queryCache = queryCache;
        }

        @Override
        public <T> CosmosPagedFlux<T> queryItems(String query, CosmosQueryRequestOptions options, Class<T> classType) {
            throw new IllegalStateException("No unparameterized queries allowed. Use parameterized query instead.");
        }

        @Override
        public <T> CosmosPagedFlux<T> queryItems(SqlQuerySpec querySpec, Class<T> classType) {
            return this.queryItems(querySpec, null, classType);
        }

        @Override
        public <T> CosmosPagedFlux<T> queryItems(String query, Class<T> classType) {
            throw new IllegalStateException("No unparameterized queries allowed. Use parameterized query instead.");
        }

        @Override
        public <T> CosmosPagedFlux<T> queryItems(SqlQuerySpec querySpec, CosmosQueryRequestOptions options, Class<T> classType) {
            CacheKey key = new CacheKey(classType.getCanonicalName(), querySpec);
            List<?> cachedResult = this.queryCache.get(key);
            if (cachedResult != null) {
                return CosmosPagedFlux.fromList((List<T>)cachedResult, false);
            }

            return super
                .queryItems(querySpec, options, classType);
        }
    }
}
