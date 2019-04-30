/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyRangeIdentity;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.TestConfigurations;
import com.microsoft.azure.cosmosdb.rx.internal.IAuthorizationTokenProvider;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentClientImpl;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.UUID;

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
                        RxDocumentServiceRequest.create(operationType, resourceType, "/dbs/7mVFAA==/colls/7mVFAP1jpeU=", randomResource, (Map<String, String>) null);

                BarrierRequestHelper.createAsync(request, authTokenProvider, 10l, 10l).toCompletable().await();
                request =
                        RxDocumentServiceRequest.create(operationType, resourceType, "/dbs/7mVFAA==", randomResource, null);

                request.setResourceId("3");
                try {
                    BarrierRequestHelper.createAsync(request, authTokenProvider, 10l, 10l).toCompletable().await();
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
                RxDocumentServiceRequest.create(operationType, resourceType, "/dbs/7mVFAA==/colls/7mVFAP1jpeU=", randomResource, (Map<String, String>) null);

        RxDocumentServiceRequest barrierRequest = BarrierRequestHelper.createAsync(request, authTokenProvider, 11l, 10l).toBlocking().value();

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
                RxDocumentServiceRequest.create(operationType, resourceType, "/dbs/dbname/colls/collname", randomResource, (Map<String, String>) null);

        RxDocumentServiceRequest barrierRequest = BarrierRequestHelper.createAsync(request, authTokenProvider, 11l, 10l).toBlocking().value();

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
                RxDocumentServiceRequest.create(operationType, resourceType, "/dbs/dbname/colls/collname", randomResource, (Map<String, String>) null);

        RxDocumentServiceRequest barrierRequest = BarrierRequestHelper.createAsync(request, authTokenProvider, 11l, 10l).toBlocking().value();

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
                RxDocumentServiceRequest.create(operationType, "7mVFAA==", resourceType, (Map<String, String>) null);

        RxDocumentServiceRequest barrierRequest = BarrierRequestHelper.createAsync(request, authTokenProvider, 11l, 10l).toBlocking().value();

        assertThat(barrierRequest.getOperationType()).isEqualTo(OperationType.Head);
        assertThat(barrierRequest.getResourceType()).isEqualTo(ResourceType.DocumentCollection);
        assertThat(barrierRequest.getResourceAddress()).isEqualTo("7mVFAA==");

        assertThat(getTargetGlobalLsn(barrierRequest)).isEqualTo(10l);
        assertThat(getTargetLsn(barrierRequest)).isEqualTo(11l);
        assertThat(barrierRequest.getIsNameBased()).isEqualTo(false);
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
}

