// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.models.ListSharesOptions;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.models.ShareItem;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureStorageResourcePatternResolverTests {

    private AzureStorageBlobProtocolResolver blobProtocolResolver;
    private AzureStorageFileProtocolResolver fileProtocolResolver;
    private ConfigurableListableBeanFactory beanFactory;
    private BlobServiceClient blobServiceClient;
    private ShareServiceClient shareServiceClient;

    @BeforeEach
    public void setup() {
        blobServiceClient = getBlobServiceClient();
        shareServiceClient = getShareServiceClient();

        beanFactory = mock(ConfigurableListableBeanFactory.class);
        when(beanFactory.getBean(BlobServiceClient.class)).thenReturn(blobServiceClient);
        when(beanFactory.getBean(ShareServiceClient.class)).thenReturn(shareServiceClient);

        blobProtocolResolver = new AzureStorageBlobProtocolResolver();
        fileProtocolResolver = new AzureStorageFileProtocolResolver();
        blobProtocolResolver.postProcessBeanFactory(beanFactory);
        fileProtocolResolver.postProcessBeanFactory(beanFactory);


    }

    public BlobServiceClient getBlobServiceClient() {
        BlobServiceClient client = mock(BlobServiceClient.class);
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        BlockBlobClient blockBlobClient = mock(BlockBlobClient.class);

        BlobContainerItem containerItem1 = new BlobContainerItem();
        containerItem1.setName("mycontainer");
        ArrayList<BlobContainerItem> containerList = new ArrayList<>();
        containerList.add(containerItem1);
        OnePageResponse<BlobContainerItem> blobContainersResponse = new OnePageResponse<>(containerList);

        BlobItem blobItem1 = new BlobItem();
        blobItem1.setName("myblob");
        ArrayList<BlobItem> blobList = new ArrayList<>();
        blobList.add(blobItem1);
        OnePageResponse<BlobItem> blobItemsResponse = new OnePageResponse<>(blobList);

        when(client.listBlobContainers(any(ListBlobContainersOptions.class), isNull()))
            .thenReturn(new PagedIterable<>(new PagedFlux<>(() -> Mono.just(blobContainersResponse))));

        when(client.getBlobContainerClient("mycontainer")).thenReturn(containerClient);
        when(containerClient.listBlobs(any(ListBlobsOptions.class), isNull()))
            .thenReturn(new PagedIterable<>(new PagedFlux<>(() -> Mono.just(blobItemsResponse))));
        when(containerClient.getBlobClient("myblob")).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        return client;
    }

    @SuppressWarnings("unchecked")
    public ShareServiceClient getShareServiceClient() {
        ShareServiceClient client = mock(ShareServiceClient.class);
        PagedIterable<ShareItem> shareItems = mock(PagedIterable.class);
        ShareClient shareClient = mock(ShareClient.class);
        ShareDirectoryClient shareDirectoryClient = mock(ShareDirectoryClient.class);
        PagedIterable<ShareFileItem> shareFileItems = mock(PagedIterable.class);

        ShareItem shareItem1 = new ShareItem();
        shareItem1.setName("mycontainer");
        ArrayList<ShareItem> shareList = new ArrayList<>();
        shareList.add(shareItem1);
        OnePageResponse<ShareItem> sharesResponse = new OnePageResponse<>(shareList);

        ShareFileItem shareFileItem1 = new ShareFileItem("myblob", false, 0L);
        ArrayList<ShareFileItem> shareFileList = new ArrayList<>();
        shareFileList.add(shareFileItem1);
        OnePageResponse<ShareFileItem> shareFilesResponse = new OnePageResponse<>(shareFileList);


        when(client.listShares(any(ListSharesOptions.class), isNull(), isNull()))
            .thenReturn(new PagedIterable<>(new PagedFlux<>(() -> Mono.just(sharesResponse))));

        when(client.getShareClient("mycontainer")).thenReturn(shareClient);
        when(shareClient.getRootDirectoryClient()).thenReturn(shareDirectoryClient);
        when(shareDirectoryClient.listFilesAndDirectories(any(String.class), isNull(), isNull(), isNull()))
            .thenReturn(
                new PagedIterable<>(new PagedFlux<>(() -> Mono.just(shareFilesResponse))));

        return client;
    }

    @ParameterizedTest
    @MethodSource("locationsProvider")
    public void resolveBlobResources(String location) throws Exception {

        resolveStorageResources(location, blobProtocolResolver);

    }

    @ParameterizedTest
    @MethodSource("locationsProvider")
    public void resolveFileResources(String location) throws Exception {

        resolveStorageResources(location, fileProtocolResolver);
    }

    private void resolveStorageResources(String location, AbstractAzureStorageProtocolResolver resolver)
        throws Exception {
        String protocol = AzureStorageUtils.getStorageProtocolPrefix(resolver.getStorageType());
        Resource[] resources = resolver.getResources(protocol + location);
        Assertions.assertNotNull(resources);
    }

    static String[] locationsProvider() {
        return new String[] {
            "mycontainer/myblob",
            "mycon*/mybl*",
            "mycontaine?/myblo?",
            "mycontainer/*",
            "*/myblob",
            "*/*" };
    }
}
