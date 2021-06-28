// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.AuthorizationTokenType;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class BarrierRequestHelperTest {
    @Test(groups = "direct")
    public void barrierBasic() {
        IAuthorizationTokenProvider authTokenProvider = getIAuthorizationTokenProvider();

        for (ResourceType resourceType : ResourceType.values()) {

            for (OperationType operationType : OperationType.values()) {
                Document randomResource = new Document();
                randomResource.setId(UUID.randomUUID().toString());
                RxDocumentServiceRequest request =
                        RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, resourceType, "/dbs/7mVFAA==/colls/7mVFAP1jpeU=", randomResource, (Map<String, String>) null);

                BarrierRequestHelper.createAsync(mockDiagnosticsClientContext(), request, authTokenProvider, 10l, 10l).block();
                request =
                        RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, resourceType, "/dbs/7mVFAA==", randomResource, null);

                request.setResourceId("3");
                try {
                    BarrierRequestHelper.createAsync(mockDiagnosticsClientContext(), request, authTokenProvider, 10l, 10l).block();
                } catch (Exception e) {
                    if (!BarrierRequestHelper.isCollectionHeadBarrierRequest(resourceType, operationType)) {
                        fail("Should not fail for non-collection head combinations");
                    }
                }
            }
        }
    }

    @Test(groups = "direct")
    public void barrierDBFeed() {
        IAuthorizationTokenProvider authTokenProvider = getIAuthorizationTokenProvider();

        ResourceType resourceType = ResourceType.DocumentCollection;
        OperationType operationType = OperationType.Query;

        Document randomResource = new Document();
        randomResource.setId(UUID.randomUUID().toString());
        RxDocumentServiceRequest request =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, resourceType, "/dbs/7mVFAA==/colls/7mVFAP1jpeU=", randomResource, (Map<String, String>) null);

        RxDocumentServiceRequest barrierRequest = BarrierRequestHelper.createAsync(mockDiagnosticsClientContext(), request, authTokenProvider, 11l, 10l).block();

        assertThat(barrierRequest.getOperationType()).isEqualTo(OperationType.HeadFeed);
        assertThat(barrierRequest.getResourceType()).isEqualTo(ResourceType.Database);


        assertThat(getTargetGlobalLsn(barrierRequest)).isEqualTo(10l);
        assertThat(getTargetLsn(barrierRequest)).isEqualTo(11l);
    }

    @Test(groups = "direct")
    public void barrierDocumentQueryNameBasedRequest() {
        IAuthorizationTokenProvider authTokenProvider = getIAuthorizationTokenProvider();

        ResourceType resourceType = ResourceType.Document;
        OperationType operationType = OperationType.Query;

        Document randomResource = new Document();
        randomResource.setId(UUID.randomUUID().toString());
        RxDocumentServiceRequest request =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, resourceType, "/dbs/dbname/colls/collname", randomResource, (Map<String, String>) null);

        RxDocumentServiceRequest barrierRequest = BarrierRequestHelper.createAsync(mockDiagnosticsClientContext(), request, authTokenProvider, 11l, 10l).block();

        assertThat(barrierRequest.getOperationType()).isEqualTo(OperationType.Head);
        assertThat(barrierRequest.getResourceType()).isEqualTo(ResourceType.DocumentCollection);
        assertThat(barrierRequest.getResourceAddress()).isEqualTo("dbs/dbname/colls/collname");

        assertThat(getTargetGlobalLsn(barrierRequest)).isEqualTo(10l);
        assertThat(getTargetLsn(barrierRequest)).isEqualTo(11l);
    }

    @Test(groups = "direct")
    public void barrierDocumentReadNameBasedRequest() {
        IAuthorizationTokenProvider authTokenProvider = getIAuthorizationTokenProvider();

        ResourceType resourceType = ResourceType.Document;
        OperationType operationType = OperationType.Read;

        Document randomResource = new Document();
        randomResource.setId(UUID.randomUUID().toString());
        RxDocumentServiceRequest request =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, resourceType, "/dbs/dbname/colls/collname", randomResource, (Map<String, String>) null);

        RxDocumentServiceRequest barrierRequest = BarrierRequestHelper.createAsync(mockDiagnosticsClientContext(), request, authTokenProvider, 11l, 10l).block();

        assertThat(barrierRequest.getOperationType()).isEqualTo(OperationType.Head);
        assertThat(barrierRequest.getResourceType()).isEqualTo(ResourceType.DocumentCollection);
        assertThat(barrierRequest.getResourceAddress()).isEqualTo("dbs/dbname/colls/collname");

        assertThat(getTargetGlobalLsn(barrierRequest)).isEqualTo(10l);
        assertThat(getTargetLsn(barrierRequest)).isEqualTo(11l);
        assertThat(barrierRequest.getIsNameBased()).isEqualTo(true);

    }

    @Test(groups = "direct")
    public void barrierDocumentReadRidBasedRequest() {
        IAuthorizationTokenProvider authTokenProvider = getIAuthorizationTokenProvider();

        ResourceType resourceType = ResourceType.Document;
        OperationType operationType = OperationType.Read;

        Document randomResource = new Document();
        randomResource.setId(UUID.randomUUID().toString());
        RxDocumentServiceRequest request =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, "7mVFAA==", resourceType, (Map<String, String>) null);

        RxDocumentServiceRequest barrierRequest = BarrierRequestHelper.createAsync(mockDiagnosticsClientContext(), request, authTokenProvider, 11l, 10l).block();

        assertThat(barrierRequest.getOperationType()).isEqualTo(OperationType.Head);
        assertThat(barrierRequest.getResourceType()).isEqualTo(ResourceType.DocumentCollection);
        assertThat(barrierRequest.getResourceAddress()).isEqualTo("7mVFAA==");

        assertThat(getTargetGlobalLsn(barrierRequest)).isEqualTo(10l);
        assertThat(getTargetLsn(barrierRequest)).isEqualTo(11l);
        assertThat(barrierRequest.getIsNameBased()).isEqualTo(false);
    }

    @Test(groups = "direct")
    public void barrierWithAadAuthorizationTokenProviderType() throws URISyntaxException {

        TokenCredential tokenCredential = new AadSimpleTokenCredential(TestConfigurations.MASTER_KEY);

        IAuthorizationTokenProvider authTokenProvider = new RxDocumentClientImpl(new URI(TestConfigurations.HOST),
                null,
                null,
                null,
                null,
                new Configs(),
                null,
                null,
                tokenCredential,
                false,
                false,
                false,
                null);

        ResourceType resourceType = ResourceType.DocumentCollection;
        OperationType operationType = OperationType.Read;

        Document randomResource = new Document();
        randomResource.setId(UUID.randomUUID().toString());
        RxDocumentServiceRequest request =
            RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, resourceType, "/dbs/7mVFAA==/colls/7mVFAP1jpeU=", randomResource, (Map<String, String>) null);

        RxDocumentServiceRequest barrierRequest = BarrierRequestHelper.createAsync(mockDiagnosticsClientContext(), request, authTokenProvider, 11l, 10l).block();

        assertThat(authTokenProvider.getAuthorizationTokenType()).isEqualTo(AuthorizationTokenType.AadToken);
        assertThat(barrierRequest.authorizationTokenType).isEqualTo(AuthorizationTokenType.AadToken);
        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.PrimaryMasterKey);
    }


    @DataProvider(name = "isCollectionHeadBarrierRequestArgProvider")
    public Object[][] isCollectionHeadBarrierRequestArgProvider() {
        return new Object[][]{
                // resourceType, operationType, isCollectionHeadBarrierRequest

                {ResourceType.Attachment, null, true},
                {ResourceType.Document, null, true},
                {ResourceType.Conflict, null, true},
                {ResourceType.StoredProcedure, null, true},
                {ResourceType.Attachment, null, true},
                {ResourceType.Trigger, null, true},

                {ResourceType.DocumentCollection, OperationType.ReadFeed, false},
                {ResourceType.DocumentCollection, OperationType.Query, false},
                {ResourceType.DocumentCollection, OperationType.SqlQuery, false},

                {ResourceType.DocumentCollection, OperationType.Create, true},
                {ResourceType.DocumentCollection, OperationType.Read, true},
                {ResourceType.DocumentCollection, OperationType.Replace, true},
                {ResourceType.DocumentCollection, OperationType.ExecuteJavaScript, true},

                {ResourceType.PartitionKeyRange, null, false},
        };
    }

    @Test(groups = "direct", dataProvider = "isCollectionHeadBarrierRequestArgProvider")
    public void isCollectionHeadBarrierRequest(ResourceType resourceType,
                                               OperationType operationType,
                                               boolean expectedResult) {
        if (operationType != null) {
            boolean actual = BarrierRequestHelper.isCollectionHeadBarrierRequest(resourceType, operationType);
            assertThat(actual).isEqualTo(expectedResult);
        } else {
            for (OperationType type : OperationType.values()) {
                boolean actual = BarrierRequestHelper.isCollectionHeadBarrierRequest(resourceType, type);
                assertThat(actual).isEqualTo(expectedResult);
            }
        }
    }

    private IAuthorizationTokenProvider getIAuthorizationTokenProvider() {
        return (RxDocumentClientImpl)
                new AsyncDocumentClient.Builder()
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .build();
    }

    private String getHeaderValue(RxDocumentServiceRequest req, String name) {
        return req.getHeaders().get(name);
    }

    private String getPartitionKey(RxDocumentServiceRequest req) {
        return getHeaderValue(req, HttpConstants.HttpHeaders.PARTITION_KEY);
    }

    private String getCollectionRid(RxDocumentServiceRequest req) {
        return getHeaderValue(req, WFConstants.BackendHeaders.COLLECTION_RID);
    }

    private PartitionKeyRangeIdentity getPartitionKeyRangeIdentity(RxDocumentServiceRequest req) {
        return req.getPartitionKeyRangeIdentity();
    }

    private Long getTargetLsn(RxDocumentServiceRequest req) {
        return Long.parseLong(getHeaderValue(req, HttpConstants.HttpHeaders.TARGET_LSN));
    }

    private Long getTargetGlobalLsn(RxDocumentServiceRequest req) {
        return Long.parseLong(getHeaderValue(req, HttpConstants.HttpHeaders.TARGET_GLOBAL_COMMITTED_LSN));
    }

    class AadSimpleTokenCredential implements TokenCredential {
        private final String keyEncoded;
        private final String AAD_HEADER_COSMOS_EMULATOR = "{\"typ\":\"JWT\",\"alg\":\"RS256\",\"x5t\":\"CosmosEmulatorPrimaryMaster\",\"kid\":\"CosmosEmulatorPrimaryMaster\"}";
        private final String AAD_CLAIM_COSMOS_EMULATOR_FORMAT = "{\"aud\":\"https://localhost.localhost\",\"iss\":\"https://sts.fake-issuer.net/7b1999a1-dfd7-440e-8204-00170979b984\",\"iat\":%d,\"nbf\":%d,\"exp\":%d,\"aio\":\"\",\"appid\":\"localhost\",\"appidacr\":\"1\",\"idp\":\"https://localhost:8081/\",\"oid\":\"96313034-4739-43cb-93cd-74193adbe5b6\",\"rh\":\"\",\"sub\":\"localhost\",\"tid\":\"EmulatorFederation\",\"uti\":\"\",\"ver\":\"1.0\",\"scp\":\"user_impersonation\",\"groups\":[\"7ce1d003-4cb3-4879-b7c5-74062a35c66e\",\"e99ff30c-c229-4c67-ab29-30a6aebc3e58\",\"5549bb62-c77b-4305-bda9-9ec66b85d9e4\",\"c44fd685-5c58-452c-aaf7-13ce75184f65\",\"be895215-eab5-43b7-9536-9ef8fe130330\"]}";

        public AadSimpleTokenCredential(String emulatorKey) {
            if (emulatorKey == null || emulatorKey.isEmpty()) {
                throw new IllegalArgumentException("emulatorKey");
            }

            this.keyEncoded = Utils.encodeUrlBase64String(emulatorKey.getBytes());
        }

        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            String aadToken = emulatorKey_based_AAD_String();
            return Mono.just(new AccessToken(aadToken, OffsetDateTime.now().plusHours(2)));
        }

        String emulatorKey_based_AAD_String() {
            ZonedDateTime currentTime = ZonedDateTime.now();
            String part1Encoded = Utils.encodeUrlBase64String(AAD_HEADER_COSMOS_EMULATOR.getBytes());
            String part2 = String.format(AAD_CLAIM_COSMOS_EMULATOR_FORMAT,
                currentTime.toEpochSecond(),
                currentTime.toEpochSecond(),
                currentTime.plusHours(2).toEpochSecond());
            String part2Encoded = Utils.encodeUrlBase64String(part2.getBytes());
            return part1Encoded + "." + part2Encoded + "." + this.keyEncoded;
        }
    }
}

