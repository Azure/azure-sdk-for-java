// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.share.models.CopyableFileSmbPropertiesList;
import com.azure.storage.file.share.models.PermissionCopyModeType;
import com.azure.storage.file.share.models.ShareFileCopyInfo;
import com.azure.storage.file.share.options.ShareFileCopyOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies how {@link ShareFileClient#beginCopy} and {@link ShareFileAsyncClient#beginCopy} handle their options: the
 * async client reports invalid input through the returned {@link PollerFlux} rather than throwing, neither client sends
 * a request for invalid input, and valid input starts the copy. The validation rules themselves are unit tested in
 * {@link CopyOptionsModelHelperTests}.
 */
public class BeginCopyValidationTests {
    private static final String SOURCE_URL = FileIdTestHelper.ENDPOINT + "/" + FileIdTestHelper.SHARE_NAME + "/source";
    private static final HttpHeaderName PERMISSION_COPY_MODE
        = HttpHeaderName.fromString("x-ms-file-permission-copy-mode");
    private static final HttpHeaderName PERMISSION = HttpHeaderName.fromString("x-ms-file-permission");
    private static final HttpHeaderName PERMISSION_KEY = HttpHeaderName.fromString("x-ms-file-permission-key");

    private final List<HttpRequest> requests = new ArrayList<>();

    @Test
    public void fileIdAddressedClient() {
        ShareFileAsyncClient client = builder().fileId(FileIdTestHelper.FILE_ID).buildFileAsyncClient();

        verifyError(() -> client.beginCopy(SOURCE_URL, Collections.emptyMap(), null), IllegalStateException.class);
        verifyError(() -> client.beginCopy(SOURCE_URL, new ShareFileCopyOptions(), null), IllegalStateException.class);
    }

    @Test
    public void nullOptions() {
        ShareFileAsyncClient client = builder().resourcePath("destination").buildFileAsyncClient();

        PollerFlux<ShareFileCopyInfo, Void> poller
            = assertDoesNotThrow(() -> client.beginCopy(SOURCE_URL, (ShareFileCopyOptions) null, null));

        StepVerifier.create(poller).verifyErrorSatisfies(error -> {
            assertInstanceOf(NullPointerException.class, error);
            assertTrue(error.getMessage().contains("options"), error.getMessage());
        });
        assertEquals(0, requests.size());
    }

    @Test
    public void nullOptionsSync() {
        ShareFileClient client = builder().resourcePath("destination").buildFileClient();

        NullPointerException error = assertThrows(NullPointerException.class,
            () -> client.beginCopy(SOURCE_URL, (ShareFileCopyOptions) null, null));

        assertTrue(error.getMessage().contains("options"), error.getMessage());
        assertEquals(0, requests.size());
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.CopyOptionsModelHelperTests#copyFlagConflictSupplier")
    public void copyFlagAndSmbPropertyConflict(
        Function<CopyableFileSmbPropertiesList, CopyableFileSmbPropertiesList> flag,
        Function<FileSmbProperties, FileSmbProperties> property, String message) {
        ShareFileAsyncClient client = builder().resourcePath("destination").buildFileAsyncClient();
        ShareFileCopyOptions options
            = new ShareFileCopyOptions().setSmbPropertiesToCopy(flag.apply(new CopyableFileSmbPropertiesList()))
                .setSmbProperties(property.apply(new FileSmbProperties()));

        verifyError(() -> client.beginCopy(SOURCE_URL, options, null), IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.CopyOptionsModelHelperTests#permissionCopyModeSupplier")
    public void permissionCopyMode(PermissionCopyModeType mode, boolean setPermission, boolean setKey,
        boolean rejected) {
        ShareFileAsyncClient client = builder().resourcePath("destination").buildFileAsyncClient();
        ShareFileCopyOptions options = CopyOptionsModelHelperTests.permissionOptions(mode, setPermission, setKey);

        if (rejected) {
            verifyError(() -> client.beginCopy(SOURCE_URL, options, null), IllegalArgumentException.class);
            return;
        }

        StepVerifier.create(client.beginCopy(SOURCE_URL, options, Duration.ofMillis(1)).take(1))
            .expectNextCount(1)
            .verifyComplete();
        assertCopyRequest(mode, setPermission, setKey);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.CopyOptionsModelHelperTests#permissionCopyModeSupplier")
    public void permissionCopyModeSync(PermissionCopyModeType mode, boolean setPermission, boolean setKey,
        boolean rejected) {
        ShareFileClient client = builder().resourcePath("destination").buildFileClient();
        ShareFileCopyOptions options = CopyOptionsModelHelperTests.permissionOptions(mode, setPermission, setKey);

        if (rejected) {
            assertThrows(IllegalArgumentException.class, () -> client.beginCopy(SOURCE_URL, options, null));
            assertEquals(0, requests.size());
            return;
        }

        client.beginCopy(SOURCE_URL, options, Duration.ofMillis(1)).waitForCompletion();
        assertCopyRequest(mode, setPermission, setKey);
    }

    private void assertCopyRequest(PermissionCopyModeType mode, boolean setPermission, boolean setKey) {
        HttpRequest copyRequest = requests.get(0);
        assertEquals(HttpMethod.PUT, copyRequest.getHttpMethod());
        assertEquals(mode == null ? null : mode.toString(), copyRequest.getHeaders().getValue(PERMISSION_COPY_MODE));
        assertEquals(setPermission ? "permission" : null, copyRequest.getHeaders().getValue(PERMISSION));
        assertEquals(setKey ? "permission-key" : null, copyRequest.getHeaders().getValue(PERMISSION_KEY));
    }

    private void verifyError(Supplier<PollerFlux<ShareFileCopyInfo, Void>> beginCopy,
        Class<? extends Throwable> expected) {
        PollerFlux<ShareFileCopyInfo, Void> poller = assertDoesNotThrow(beginCopy::get);
        StepVerifier.create(poller).verifyError(expected);
        assertEquals(0, requests.size());
    }

    private ShareFileClientBuilder builder() {
        HttpClient httpClient = request -> {
            requests.add(request);
            HttpHeaders headers = request.getHttpMethod() == HttpMethod.PUT
                ? new HttpHeaders().set(HttpHeaderName.ETAG, "\"0x8DEAF1479E1C087\"")
                    .set(HttpHeaderName.LAST_MODIFIED, "Wed, 21 Oct 2015 07:28:00 GMT")
                : FileIdTestHelper.fileHeaders("destination");
            headers.set(HttpHeaderName.fromString("x-ms-copy-id"), "copy-id")
                .set(HttpHeaderName.fromString("x-ms-copy-status"), "success");
            return Mono
                .just(new MockHttpResponse(request, request.getHttpMethod() == HttpMethod.PUT ? 202 : 200, headers));
        };
        return new ShareFileClientBuilder().endpoint(FileIdTestHelper.ENDPOINT)
            .shareName(FileIdTestHelper.SHARE_NAME)
            .credential(new StorageSharedKeyCredential("account", "a2V5"))
            .httpClient(httpClient);
    }
}
