// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.share.options.ShareFileRenameOptions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that the client returned by rename addresses the destination path the same way as a client built for it.
 */
public class RenameDestinationClientTests {
    private static final String DESTINATION = "dest dir/dest file.txt";

    private final List<HttpRequest> requests = new ArrayList<>();

    @Test
    public void asyncFileRenameReturnsClientForDestinationPath() {
        ShareFileAsyncClient source = builder().resourcePath("source.txt").buildFileAsyncClient();

        ShareFileAsyncClient destination
            = source.renameWithResponse(new ShareFileRenameOptions(DESTINATION)).block().getValue();
        destination.delete().block();
        String actualUrl = lastRequestUrl();

        assertEquals(expectedFileDeleteUrl(), actualUrl);
        assertEquals(DESTINATION, destination.getFilePath());
    }

    @Test
    public void syncFileRenameReturnsClientForDestinationPath() {
        ShareFileClient source = builder().resourcePath("source.txt").buildFileClient();

        ShareFileClient destination = source.rename(DESTINATION);
        destination.delete();
        String actualUrl = lastRequestUrl();

        assertEquals(expectedFileDeleteUrl(), actualUrl);
        assertEquals(DESTINATION, destination.getFilePath());
    }

    @Test
    public void asyncDirectoryRenameReturnsClientForDestinationPath() {
        ShareDirectoryAsyncClient source = builder().resourcePath("source").buildDirectoryAsyncClient();

        ShareDirectoryAsyncClient destination
            = source.renameWithResponse(new ShareFileRenameOptions(DESTINATION)).block().getValue();
        destination.delete().block();
        String actualUrl = lastRequestUrl();

        assertEquals(expectedDirectoryDeleteUrl(), actualUrl);
        assertEquals(DESTINATION, destination.getDirectoryPath());
    }

    @Test
    public void syncDirectoryRenameReturnsClientForDestinationPath() {
        ShareDirectoryClient source = builder().resourcePath("source").buildDirectoryClient();

        ShareDirectoryClient destination = source.rename(DESTINATION);
        destination.delete();
        String actualUrl = lastRequestUrl();

        assertEquals(expectedDirectoryDeleteUrl(), actualUrl);
        assertEquals(DESTINATION, destination.getDirectoryPath());
    }

    private ShareFileClientBuilder builder() {
        HttpClient httpClient = request -> {
            requests.add(request);
            int statusCode = request.getHttpMethod() == HttpMethod.DELETE ? 202 : 200;
            return Mono.just(new MockHttpResponse(request, statusCode, new HttpHeaders()));
        };
        return new ShareFileClientBuilder().endpoint(FileIdTestHelper.ENDPOINT)
            .shareName(FileIdTestHelper.SHARE_NAME)
            .credential(new StorageSharedKeyCredential("account", "a2V5"))
            .httpClient(httpClient);
    }

    // The URL a client built directly for the destination path sends; the renamed client must match it.
    private String expectedFileDeleteUrl() {
        builder().resourcePath(DESTINATION).buildFileClient().delete();
        return lastRequestUrl();
    }

    private String expectedDirectoryDeleteUrl() {
        builder().resourcePath(DESTINATION).buildDirectoryClient().delete();
        return lastRequestUrl();
    }

    private String lastRequestUrl() {
        return requests.get(requests.size() - 1).getUrl().toString();
    }
}
