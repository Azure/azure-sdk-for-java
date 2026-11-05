// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Offline tests that document exactly how {@code azure-storage-file-datalake} treats special characters in path names
 * for {@code getFileClient}, {@code getDirectoryClient} and {@code renameWithResponse}.
 *
 * <p>These tests do not talk to the service. They capture the {@link HttpRequest} that the SDK would have put on the
 * wire so the encoding contract can be asserted deterministically.</p>
 *
 * <p>The contract being verified is:</p>
 * <ul>
 *     <li>Path names passed to the client factory methods are stored verbatim - the SDK never URL-decodes them
 *     (see {@code DataLakePathAsyncClient}'s constructor which assigns {@code this.pathName = pathName}).</li>
 *     <li>The SDK percent-encodes the path itself when building the request URL. This is done by
 *     {@code UrlEscapers.PATH_ESCAPER} in {@code azure-core} because the generated {@code PathsService} declares
 *     {@code @PathParam("path")} without {@code encoded = true}.</li>
 *     <li>The rename source header is encoded by {@code Utility.urlEncode} in
 *     {@code DataLakePathAsyncClient.renameWithResponse}.</li>
 * </ul>
 */
public class DataLakePathNameEncodingTests {
    private static final String ENDPOINT = "https://account.dfs.core.windows.net";
    private static final String FILE_SYSTEM_NAME = "filesystem";

    /**
     * Characters that {@code UrlEscapers.PATH_ESCAPER} treats as safe, meaning they travel to the service as-is and
     * must therefore NOT be percent-encoded by the caller.
     */
    private static final String UNRESERVED_AND_SUB_DELIMS = "-._~!$&'()*+,;=:@";

    private static class RequestCapturingHttpClient implements HttpClient {
        private final HttpHeaders responseHeaders;
        private final Integer forcedStatus;
        private volatile HttpRequest request;

        RequestCapturingHttpClient() {
            this(new HttpHeaders(), null);
        }

        RequestCapturingHttpClient(HttpHeaders responseHeaders, Integer forcedStatus) {
            this.responseHeaders = responseHeaders;
            this.forcedStatus = forcedStatus;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            this.request = request;
            int status = forcedStatus != null ? forcedStatus : (request.getHttpMethod() == HttpMethod.PUT ? 201 : 200);
            return Mono.just(new MockHttpResponse(request, status, responseHeaders));
        }

        HttpRequest getRequest() {
            return request;
        }

        String getUrlPath() {
            return request.getUrl().getPath();
        }
    }

    private static DataLakeFileSystemClient fileSystemClient(HttpClient httpClient) {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient).build();

        return new DataLakeFileSystemClientBuilder().endpoint(ENDPOINT)
            .fileSystemName(FILE_SYSTEM_NAME)
            .credential(new MockTokenCredential())
            .pipeline(pipeline)
            .buildClient();
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * 1. The name handed to getFileClient/getDirectoryClient is stored verbatim - it is neither decoded nor encoded.
     * ------------------------------------------------------------------------------------------------------------
     */

    @ParameterizedTest
    @ValueSource(
        strings = {
            "file",
            "100%done",
            "a#b",
            "a+b",
            "a b",
            "a&b",
            "a?b",
            "a@b",
            "a=b",
            "a,b",
            "a;b",
            "a'b",
            "a\"b",
            "a<b>c",
            "a|b",
            "a*b",
            "a:b",
            "a\\b",
            "a[b]c",
            "a{b}c",
            "a^b",
            "a`b",
            "斑點",
            "%E6%96%91%E9%BB%9E",
            "path/to]a file" })
    public void pathNameIsStoredVerbatim(String name) {
        DataLakeFileSystemClient fileSystemClient = fileSystemClient(new RequestCapturingHttpClient());

        assertEquals(name, fileSystemClient.getFileClient(name).getFilePath());
        assertEquals(name, fileSystemClient.getDirectoryClient(name).getDirectoryPath());
    }

    /**
     * Raw {@code %} and {@code #} used to throw {@code IllegalArgumentException} ("Illegal hex characters in escape
     * (%) pattern") because the SDK called {@code Utility.urlDecode} on the name. Since 12.19.0 the name is stored
     * verbatim, so raw names never throw.
     */
    @ParameterizedTest
    @ValueSource(strings = { "100%done", "50%", "%", "%zz", "a%2", "report#1", "a%b#c" })
    public void rawPercentOrHashNeverThrows(String name) {
        DataLakeFileSystemClient fileSystemClient = fileSystemClient(new RequestCapturingHttpClient());

        assertDoesNotThrow(() -> fileSystemClient.getFileClient(name));
        assertDoesNotThrow(() -> fileSystemClient.getDirectoryClient(name));
        assertDoesNotThrow(() -> fileSystemClient.getDirectoryClient("dir").getFileClient(name));
        assertDoesNotThrow(() -> fileSystemClient.getDirectoryClient("dir").getSubdirectoryClient(name));
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * 2. The SDK percent-encodes the path when it builds the request URL.
     * ------------------------------------------------------------------------------------------------------------
     */

    @ParameterizedTest
    @MethodSource("wireEncodingSupplier")
    public void specialCharactersAreEncodedByTheSdkOnTheWire(String rawName, String expectedEncodedPath) {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileSystemClient(httpClient).getFileClient(rawName).create(true);

        assertEquals("/" + FILE_SYSTEM_NAME + "/" + expectedEncodedPath, httpClient.getUrlPath());
    }

    @ParameterizedTest
    @MethodSource("wireEncodingSupplier")
    public void specialCharactersAreEncodedByTheSdkOnTheWireForDirectories(String rawName, String expectedEncodedPath) {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileSystemClient(httpClient).getDirectoryClient(rawName).create(true);

        assertEquals("/" + FILE_SYSTEM_NAME + "/" + expectedEncodedPath, httpClient.getUrlPath());
    }

    private static Stream<Arguments> wireEncodingSupplier() {
        return Stream.of(
            // raw name passed to the SDK | path that appears on the wire
            Arguments.of("file.txt", "file.txt"),
            // Characters that are reserved/unsafe in a URL path are percent-encoded by the SDK.
            Arguments.of("a b", "a%20b"), Arguments.of("a\"b", "a%22b"), Arguments.of("report#1", "report%231"),
            Arguments.of("100%done", "100%25done"), Arguments.of("a<b", "a%3Cb"), Arguments.of("a>b", "a%3Eb"),
            Arguments.of("a?b", "a%3Fb"), Arguments.of("a[b", "a%5Bb"), Arguments.of("a\\b", "a%5Cb"),
            Arguments.of("a]b", "a%5Db"), Arguments.of("a^b", "a%5Eb"), Arguments.of("a`b", "a%60b"),
            Arguments.of("a{b", "a%7Bb"), Arguments.of("a|b", "a%7Cb"), Arguments.of("a}b", "a%7Db"),
            // '/' is a path separator in the name; it is encoded so the whole name is addressed as one path parameter.
            Arguments.of("dir/file.txt", "dir%2Ffile.txt"),
            // Non-ASCII is UTF-8 percent-encoded.
            Arguments.of("斑點", "%E6%96%91%E9%BB%9E"),
            // Unreserved + sub-delims are safe and are NOT encoded.
            Arguments.of("a+b", "a+b"), Arguments.of("a&b", "a&b"), Arguments.of("a=b", "a=b"),
            Arguments.of("a,b", "a,b"), Arguments.of("a;b", "a;b"), Arguments.of("a'b", "a'b"),
            Arguments.of("a!b", "a!b"), Arguments.of("a$b", "a$b"), Arguments.of("a(b)c", "a(b)c"),
            Arguments.of("a*b", "a*b"), Arguments.of("a:b", "a:b"), Arguments.of("a@b", "a@b"),
            Arguments.of("a-b_c.d~e", "a-b_c.d~e"));
    }

    /**
     * Every unreserved character and sub-delimiter is placed on the wire as-is. Callers must not encode these.
     */
    @Test
    public void unreservedAndSubDelimitersAreNotEncoded() {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();
        String name = "file" + UNRESERVED_AND_SUB_DELIMS + "txt";

        fileSystemClient(httpClient).getFileClient(name).create(true);

        assertEquals("/" + FILE_SYSTEM_NAME + "/" + name, httpClient.getUrlPath());
    }

    /**
     * The characters the issue asks about - {@code " \ / : | < > * ?} - are all accepted by the client. The client
     * performs no name validation at all; only the service enforces naming rules.
     */
    @ParameterizedTest
    @MethodSource("windowsReservedCharacterSupplier")
    public void windowsReservedCharactersAreNotRejectedByTheClient(String rawName, String expectedEncodedPath) {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        assertDoesNotThrow(() -> fileSystemClient(httpClient).getFileClient(rawName).create(true));

        assertEquals("/" + FILE_SYSTEM_NAME + "/" + expectedEncodedPath, httpClient.getUrlPath());
    }

    private static Stream<Arguments> windowsReservedCharacterSupplier() {
        return Stream.of(Arguments.of("a\"b", "a%22b"), Arguments.of("a\\b", "a%5Cb"), Arguments.of("a/b", "a%2Fb"),
            Arguments.of("a:b", "a:b"), Arguments.of("a|b", "a%7Cb"), Arguments.of("a<b", "a%3Cb"),
            Arguments.of("a>b", "a%3Eb"), Arguments.of("a*b", "a*b"), Arguments.of("a?b", "a%3Fb"));
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * 3. Pre-encoding by the caller results in double encoding - i.e. a different resource.
     * ------------------------------------------------------------------------------------------------------------
     */

    @ParameterizedTest
    @CsvSource({
        "my%20file,my%2520file",
        "my%2Ffile,my%252Ffile",
        "test%25test,test%2525test",
        "%E6%96%91%E9%BB%9E,%25E6%2596%2591%25E9%25BB%259E" })
    public void callerSuppliedEncodingIsDoubleEncoded(String preEncodedName, String expectedEncodedPath) {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileSystemClient(httpClient).getFileClient(preEncodedName).create(true);

        // The '%' of the caller's escape sequence is itself escaped to %25, so a differently named resource is
        // addressed. Callers must pass the raw, unencoded name.
        assertEquals("/" + FILE_SYSTEM_NAME + "/" + expectedEncodedPath, httpClient.getUrlPath());
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * 4. Sub-paths: DataLakeDirectoryClient.getFileClient / getSubdirectoryClient concatenate with '/' and then the
     *    whole path is encoded as a single path parameter.
     * ------------------------------------------------------------------------------------------------------------
     */

    @Test
    public void directoryChildPathsAreConcatenatedRawAndEncodedOnce() {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();
        DataLakeDirectoryClient directoryClient = fileSystemClient(httpClient).getDirectoryClient("my dir");

        DataLakeFileClient fileClient = directoryClient.getFileClient("100%done.txt");
        assertEquals("my dir/100%done.txt", fileClient.getFilePath());

        fileClient.create(true);
        assertEquals("/" + FILE_SYSTEM_NAME + "/my%20dir%2F100%25done.txt", httpClient.getUrlPath());

        DataLakeDirectoryClient subdirectoryClient = directoryClient.getSubdirectoryClient("sub+dir");
        assertEquals("my dir/sub+dir", subdirectoryClient.getDirectoryPath());

        subdirectoryClient.create(true);
        assertEquals("/" + FILE_SYSTEM_NAME + "/my%20dir%2Fsub+dir", httpClient.getUrlPath());
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * 5. renameWithResponse: the destination is a raw path too, and the SDK builds the x-ms-rename-source header.
     * ------------------------------------------------------------------------------------------------------------
     */

    @ParameterizedTest
    @MethodSource("renameSupplier")
    public void renameEncodesSourceHeaderAndDestinationUrl(String sourceName, String destinationName,
        String expectedRenameSource, String expectedDestinationPath) {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();
        DataLakeFileClient fileClient = fileSystemClient(httpClient).getFileClient(sourceName);

        DataLakeFileClient renamed
            = fileClient.renameWithResponse(null, destinationName, null, null, null, null).getValue();

        HttpRequest request = httpClient.getRequest();
        assertNotNull(request);

        // Source is sent in the x-ms-rename-source header, percent-encoded by the SDK.
        assertEquals("/" + FILE_SYSTEM_NAME + "/" + expectedRenameSource,
            request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-rename-source")));

        // Destination is the request URL, percent-encoded by the SDK.
        assertEquals("/" + FILE_SYSTEM_NAME + "/" + expectedDestinationPath, request.getUrl().getPath());

        // The returned client reports the raw destination name, exactly as it was passed in.
        assertEquals(destinationName, renamed.getFilePath());
    }

    private static Stream<Arguments> renameSupplier() {
        return Stream.of(
            // source | destination | expected x-ms-rename-source | expected destination path
            Arguments.of("file.txt", "renamed.txt", "file.txt", "renamed.txt"),
            Arguments.of("100%done.txt", "100%25done.txt", "100%25done.txt", "100%2525done.txt"),
            Arguments.of("a b.txt", "c d.txt", "a%20b.txt", "c%20d.txt"),
            Arguments.of("a+b.txt", "c+d.txt", "a%2Bb.txt", "c+d.txt"),
            Arguments.of("dir/file.txt", "dir/renamed.txt", "dir%2Ffile.txt", "dir%2Frenamed.txt"),
            Arguments.of("斑點.txt", "點斑.txt", "%E6%96%91%E9%BB%9E.txt", "%E9%BB%9E%E6%96%91.txt"));
    }

    /**
     * Regression test. The client returned by {@code rename}/{@code renameWithResponse} used to be constructed with
     * {@code Utility.urlEncode(pathName)}, which left it internally inconsistent: blob-backed operations (such as
     * {@code getProperties}) addressed the correct path while Data Lake endpoint operations (such as {@code create})
     * double-encoded it. The returned client must address exactly the path that the rename created.
     */
    @Test
    public void clientReturnedByRenameAddressesTheRenamedPath() {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();
        DataLakeFileSystemClient fileSystemClient = fileSystemClient(httpClient);

        DataLakeFileClient renamed = fileSystemClient.getFileClient("source.txt").rename(null, "my file.txt");
        String renameUrl = httpClient.getUrlPath();

        assertEquals("/" + FILE_SYSTEM_NAME + "/my%20file.txt", renameUrl);
        assertEquals("my file.txt", renamed.getFilePath());
        assertEquals(ENDPOINT + "/" + FILE_SYSTEM_NAME + "/my%20file.txt", renamed.getFileUrl());

        // A Data Lake endpoint operation addresses the path that was just created.
        renamed.createIfNotExists();
        assertEquals(renameUrl, httpClient.getUrlPath());

        // A blob endpoint operation addresses the same path.
        renamed.getProperties();
        assertEquals(renameUrl, httpClient.getUrlPath());

        // And it agrees with a freshly acquired client.
        fileSystemClient.getFileClient("my file.txt").createIfNotExists();
        assertEquals(renameUrl, httpClient.getUrlPath());
    }

    /**
     * The same consistency requirement for directories, and for a name containing a literal {@code %}.
     */
    @Test
    public void directoryClientReturnedByRenameAddressesTheRenamedPath() {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();
        DataLakeFileSystemClient fileSystemClient = fileSystemClient(httpClient);

        DataLakeDirectoryClient renamed = fileSystemClient.getDirectoryClient("source").rename(null, "100%done dir");
        String renameUrl = httpClient.getUrlPath();

        assertEquals("/" + FILE_SYSTEM_NAME + "/100%25done%20dir", renameUrl);
        assertEquals("100%done dir", renamed.getDirectoryPath());

        renamed.createIfNotExists();
        assertEquals(renameUrl, httpClient.getUrlPath());

        renamed.getProperties();
        assertEquals(renameUrl, httpClient.getUrlPath());
    }

    /**
     * Regression test. {@code DataLakeFileClient} delegates {@code append}/{@code flush}/{@code upload} to its internal
     * async client. That async client must be rooted at the renamed (destination) path; otherwise these operations
     * would address the original source path even though {@code create}/{@code getProperties} address the destination.
     */
    @Test
    public void fileClientReturnedByRenameDelegatesToTheRenamedPath() {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();
        DataLakeFileSystemClient fileSystemClient = fileSystemClient(httpClient);

        DataLakeFileClient renamed = fileSystemClient.getFileClient("source.txt").rename(null, "my file.txt");
        String renameUrl = httpClient.getUrlPath();
        assertEquals("/" + FILE_SYSTEM_NAME + "/my%20file.txt", renameUrl);
        HttpRequest renameRequest = httpClient.getRequest();

        // append is delegated to the internal async client - it must address the renamed path. The mock cannot return
        // a valid append response, but the request is captured when it is put on the wire, which is all this routing
        // regression cares about.
        byte[] data = new byte[] { 1, 2, 3 };
        try {
            renamed.append(new ByteArrayInputStream(data), 0, data.length);
        } catch (RuntimeException ignored) {
            // The offline mock does not satisfy the append response contract; only the request routing matters here.
        }

        HttpRequest appendRequest = httpClient.getRequest();
        // Guard against a false pass: the append must actually have reached the HTTP client (a distinct request was
        // captured) rather than failing earlier and leaving the rename request in place.
        assertNotSame(renameRequest, appendRequest);
        assertEquals(HttpMethod.PATCH, appendRequest.getHttpMethod());
        assertEquals("/" + FILE_SYSTEM_NAME + "/my%20file.txt", appendRequest.getUrl().getPath());
    }

    /**
     * Regression test. {@code DataLakeDirectoryClient} delegates operations such as {@code setAccessControlRecursive}
     * to its internal (base) async client. That async client must be rooted at the renamed (destination) path so that
     * a recursive ACL update targets the renamed directory rather than the original source path.
     */
    @Test
    public void directoryClientReturnedByRenameDelegatesToTheRenamedPath() {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();
        DataLakeFileSystemClient fileSystemClient = fileSystemClient(httpClient);

        DataLakeDirectoryClient renamed = fileSystemClient.getDirectoryClient("source").rename(null, "dest dir");
        HttpRequest renameRequest = httpClient.getRequest();

        // setAccessControlRecursive is delegated to the internal async client - it must address the renamed path. The
        // mock cannot return a valid response body, but the request is captured when it is put on the wire, which is
        // all this routing regression cares about.
        try {
            renamed.setAccessControlRecursive(PathAccessControlEntry.parseList("user::rwx"));
        } catch (RuntimeException ignored) {
            // The offline mock does not satisfy the response contract; only the request routing matters here.
        }

        HttpRequest aclRequest = httpClient.getRequest();
        // Guard against a false pass: the recursive ACL update must actually have reached the HTTP client (a distinct
        // request was captured) rather than failing earlier and leaving the rename request in place.
        assertNotSame(renameRequest, aclRequest);
        assertEquals(HttpMethod.PATCH, aclRequest.getHttpMethod());
        assertEquals("/" + FILE_SYSTEM_NAME + "/dest%20dir", aclRequest.getUrl().getPath());
    }

    /**
     * {@code undeletePath} builds its result client through the same copy constructors, so it must round-trip the
     * deleted path name unchanged too.
     */
    @Test
    public void undeletedClientReportsRawPathName() {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient(
            new HttpHeaders().set(HttpHeaderName.fromString("x-ms-resource-type"), "file"), 200);

        DataLakePathClient undeleted = fileSystemClient(httpClient).undeletePath("my file.txt", "deletionId");

        assertEquals("my file.txt", undeleted.getObjectPath());
        assertEquals(ENDPOINT + "/" + FILE_SYSTEM_NAME + "/my%20file.txt", undeleted.getPathUrl());
    }

    /**
     * The rename source header and the request URL use two different (but both valid) percent-encoding sets. Anything
     * a caller pre-encodes is escaped again in both, so pre-encoding is never correct.
     */
    @Test
    public void renameOfPreEncodedNameIsDoubleEncoded() {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileSystemClient(httpClient).getFileClient("my%20file.txt")
            .renameWithResponse(null, "my%20renamed.txt", null, null, null, null);

        HttpRequest request = httpClient.getRequest();
        assertEquals("/" + FILE_SYSTEM_NAME + "/my%2520file.txt",
            request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-rename-source")));
        assertEquals("/" + FILE_SYSTEM_NAME + "/my%2520renamed.txt", request.getUrl().getPath());
    }
}
