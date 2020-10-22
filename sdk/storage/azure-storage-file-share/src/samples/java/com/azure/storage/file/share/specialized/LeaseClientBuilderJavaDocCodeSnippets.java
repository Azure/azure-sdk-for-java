// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.specialized;

import com.azure.storage.file.share.ShareAsyncClient;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareClientBuilder;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareFileClientBuilder;

public class LeaseClientBuilderJavaDocCodeSnippets {
    private ShareFileAsyncClient shareFileAsyncClient = new ShareFileClientBuilder()
        .resourcePath("file")
        .buildFileAsyncClient();

    private ShareFileClient shareFileClient = new ShareFileClientBuilder()
        .resourcePath("file")
        .buildFileClient();

    private ShareAsyncClient shareAsyncClient = new ShareClientBuilder()
        .endpoint("file")
        .buildAsyncClient();

    private ShareClient shareClient = new ShareClientBuilder()
        .endpoint("file")
        .buildClient();

    private String leaseId = "leaseId";

    /**
     * Code snippets for {@link ShareLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiation() {
        // BEGIN: com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.syncInstantiation
        ShareLeaseClient fileLeaseClient = new ShareLeaseClientBuilder()
            .fileClient(shareFileClient)
            .buildClient();
        // END: com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.syncInstantiation
    }

    /**
     * Code snippets for {@link ShareLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithLeaseId() {
        // BEGIN: com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.syncInstantiationWithFileAndLeaseId
        ShareLeaseClient fileLeaseClient = new ShareLeaseClientBuilder()
            .fileClient(shareFileClient)
            .leaseId(leaseId)
            .buildClient();
        // END: com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.syncInstantiationWithFileAndLeaseId
    }

    /**
     * Code snippets for {@link ShareLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithShareAndLeaseId() {
        // BEGIN: com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.syncInstantiationWithShareAndLeaseId
        ShareLeaseClient fileLeaseClient = new ShareLeaseClientBuilder()
            .shareClient(shareClient)
            .leaseId(leaseId)
            .buildClient();
        // END: com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.syncInstantiationWithShareAndLeaseId
    }

    /**
     * Code snippets for {@link ShareLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiation() {
        // BEGIN: com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.asyncInstantiation
        ShareLeaseAsyncClient fileLeaseAsyncClient = new ShareLeaseClientBuilder()
            .fileAsyncClient(shareFileAsyncClient)
            .buildAsyncClient();
        // END: com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.asyncInstantiation
    }

    /**
     * Code snippets for {@link ShareLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithLeaseId() {
        // BEGIN: com.azure.storage.file.specialized.ShareLeaseClientBuilder.asyncInstantiationWithFileAndLeaseId
        ShareLeaseAsyncClient fileLeaseAsyncClient = new ShareLeaseClientBuilder()
            .fileAsyncClient(shareFileAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        // END: com.azure.storage.file.specialized.ShareLeaseClientBuilder.asyncInstantiationWithFileAndLeaseId
    }

    /**
     * Code snippets for {@link ShareLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithShareAndLeaseId() {
        // BEGIN: com.azure.storage.file.specialized.ShareLeaseClientBuilder.asyncInstantiationWithShareAndLeaseId
        ShareLeaseAsyncClient fileLeaseAsyncClient = new ShareLeaseClientBuilder()
            .shareAsyncClient(shareAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        // END: com.azure.storage.file.specialized.ShareLeaseClientBuilder.asyncInstantiationWithShareAndLeaseId
    }
}
