// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.sharing;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.azure.analytics.purview.sharing.models.BlobStorageArtifact;
import com.azure.analytics.purview.sharing.models.InPlaceSentShare;
import com.azure.analytics.purview.sharing.models.ReferenceNameType;
import com.azure.analytics.purview.sharing.models.SentShare;
import com.azure.analytics.purview.sharing.models.ServiceInvitation;
import com.azure.analytics.purview.sharing.models.StorageAccountPath;
import com.azure.analytics.purview.sharing.models.StoreReference;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import reactor.core.publisher.Mono;

class PurviewShareTestBase extends TestBase {

    protected ReceivedSharesClient receivedSharesClient;

    protected SentSharesClient sentSharesClient;

    protected String clientId;

    protected String targetActiveDirectoryId;

    protected String targetObjectId;

    protected String providerStorageAccountResourceId;

    protected String consumerStorageAccountResourceId;

    protected String consumerEmail;

    @Override
    protected void beforeTest() {

        this.initializeSentShareClient();
        this.initializeReceivedShareClient();

        clientId = Configuration.getGlobalConfiguration().get("AZURE_CLIENT_ID", "6a2919d0-880a-4ed8-B50d-7abe4d74291c");
        targetActiveDirectoryId = Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID", "4653a7b2-02ff-4155-8e55-2d0c7f3178a1");
        targetObjectId = Configuration.getGlobalConfiguration().get("TARGET_OBJECT_ID", "68700464-b46c-4ec0-88ff-6061da36da69");
        providerStorageAccountResourceId = Configuration.getGlobalConfiguration().get("PROVIDER_STORAGE_RESOURCE_ID", "/subscriptions/8af54e97-8629-48cd-A92e-24753982bf92/resourceGroups/my-resource-group/providers/Microsoft.Storage/storageAccounts/providerstorage");
        consumerStorageAccountResourceId = Configuration.getGlobalConfiguration().get("CONSUMER_STORAGE_RESOURCE_ID", "/subscriptions/8af54e97-8629-48cd-A92e-24753982bf92/resourceGroups/my-resource-group/providers/Microsoft.Storage/storageAccounts/consumerstorage");
        consumerEmail = Configuration.getGlobalConfiguration().get("CONSUMER_EMAIL", "consumer@contoso.com");
    }

    protected SentShare createSentShare(UUID uuid) {

        String sentShareId = uuid.toString();

        InPlaceSentShare sentShare = new InPlaceSentShare()
                .setDisplayName(testResourceNamer.randomName("sentshare", 26)).setDescription("A sample share");

        StoreReference storeReference = new StoreReference().setReferenceName(this.providerStorageAccountResourceId)
                .setType(ReferenceNameType.ARM_RESOURCE_REFERENCE);

        StorageAccountPath storageAccountPath = new StorageAccountPath().setContainerName("test-files")
                .setReceiverPath("graph.png").setSenderPath("graph.png");

        List<StorageAccountPath> paths = new ArrayList<>();
        paths.add(storageAccountPath);

        BlobStorageArtifact artifact = new BlobStorageArtifact().setStoreReference(storeReference).setPaths(paths);

        sentShare.setArtifact(artifact);

        RequestOptions requestOptions = new RequestOptions();
        SyncPoller<BinaryData, BinaryData> response = sentSharesClient.beginCreateOrReplaceSentShare(sentShareId,
                BinaryData.fromObject(sentShare), requestOptions);

        response.waitForCompletion();

        return response.getFinalResult().toObject(SentShare.class);
    }

    protected Response<BinaryData> createSentShareAndServiceInvitation() {
        return this.createSentShareAndServiceInvitation(UUID.randomUUID(), UUID.randomUUID());
    }

    protected Response<BinaryData> createSentShareAndServiceInvitation(UUID sentShareId, UUID sentShareInvitationId) {
        this.createSentShare(sentShareId);

        String invitationId = sentShareInvitationId.toString();

        ServiceInvitation sentShareInvitation = new ServiceInvitation()
                .setTargetActiveDirectoryId(UUID.fromString(this.targetActiveDirectoryId))
                .setTargetObjectId(UUID.fromString(this.targetObjectId));

        return sentSharesClient.createSentShareInvitationWithResponse(sentShareId.toString(), invitationId,
                BinaryData.fromObject(sentShareInvitation), new RequestOptions());
    }

    private void initializeReceivedShareClient() {
        ReceivedSharesClientBuilder receivedSharesClientbuilder = new ReceivedSharesClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "https://account.purview.azure.com/share"))
                .httpClient(HttpClient.createDefault())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (getTestMode() == TestMode.PLAYBACK) {
            receivedSharesClientbuilder.httpClient(interceptorManager.getPlaybackClient())
                    .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else if (getTestMode() == TestMode.RECORD) {
            receivedSharesClientbuilder.addPolicy(interceptorManager.getRecordPolicy())
                    .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            receivedSharesClientbuilder.credential(new DefaultAzureCredentialBuilder().build());
        }

        receivedSharesClient = receivedSharesClientbuilder.buildClient();
    }

    private void initializeSentShareClient() {
        SentSharesClientBuilder sentSharesClientbuilder = new SentSharesClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "https://account.purview.azure.com/share"))
                .httpClient(HttpClient.createDefault())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (getTestMode() == TestMode.PLAYBACK) {
            sentSharesClientbuilder.httpClient(interceptorManager.getPlaybackClient())
                    .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else if (getTestMode() == TestMode.RECORD) {
            sentSharesClientbuilder.addPolicy(interceptorManager.getRecordPolicy())
                    .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            sentSharesClientbuilder.credential(new DefaultAzureCredentialBuilder().build());
        }

        sentSharesClient = sentSharesClientbuilder.buildClient();
    }
}
