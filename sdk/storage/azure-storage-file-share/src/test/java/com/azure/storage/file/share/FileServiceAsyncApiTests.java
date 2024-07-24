// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.ListSharesOptions;
import com.azure.storage.file.share.models.ShareCorsRule;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareItem;
import com.azure.storage.file.share.models.ShareMetrics;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareProtocols;
import com.azure.storage.file.share.models.ShareRetentionPolicy;
import com.azure.storage.file.share.models.ShareServiceProperties;
import com.azure.storage.file.share.options.ShareCreateOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileServiceAsyncApiTests extends FileShareTestBase {
    private String shareName;

    private static final Map<String, String> TEST_METADATA = Collections.singletonMap("testmetadata", "value");
    private static final String REALLY_LONG_STRING = "thisisareallylongstringthatexceedsthe64characterlimitallowedoncertainproperties";
    private static final List<ShareCorsRule> TOO_MANY_RULES = new ArrayList<ShareCorsRule>();
    private static final List<ShareCorsRule> INVALID_ALLOWED_HEADER = Collections.singletonList(new ShareCorsRule().setAllowedHeaders(REALLY_LONG_STRING));
    private static final List<ShareCorsRule> INVALID_EXPOSED_HEADER = Collections.singletonList(new ShareCorsRule().setExposedHeaders(REALLY_LONG_STRING));
    private static final List<ShareCorsRule> INVALID_ALLOWED_ORIGIN = Collections.singletonList(new ShareCorsRule().setAllowedOrigins(REALLY_LONG_STRING));
    private static final List<ShareCorsRule> INVALID_ALLOWED_METHOD = Collections.singletonList(new ShareCorsRule().setAllowedMethods("NOTAREALHTTPMETHOD"));

    @BeforeEach
    public void setup() {
        shareName = generateShareName();
        primaryFileServiceAsyncClient = fileServiceBuilderHelper().buildAsyncClient();
        for (int i = 0; i < 6; i++) {
            TOO_MANY_RULES.add(new ShareCorsRule());
        }
    }

    @Test
    public void getFileServiceURL() {
        String accountName = StorageSharedKeyCredential.fromConnectionString(
            ENVIRONMENT.getPrimaryAccount().getConnectionString()).getAccountName();
        String expectURL = String.format("https://%s.file.core.windows.net", accountName);
        String fileServiceURL = primaryFileServiceAsyncClient.getFileServiceUrl();
        assertEquals(expectURL, fileServiceURL);
    }

    @Test
    public void getShareDoesNotCreateAShare() {
        ShareAsyncClient shareAsyncClient = primaryFileServiceAsyncClient.getShareAsyncClient(shareName);
        assertInstanceOf(ShareAsyncClient.class, shareAsyncClient);
    }

    @Test
    public void createShare() {
        StepVerifier.create(primaryFileServiceAsyncClient.createShareWithResponse(shareName, null, (Context) null))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createShareWithMetadataSupplier")
    public void createShareWithMetadata(Map<String, String> metadata, Integer quota) {
        StepVerifier.create(primaryFileServiceAsyncClient.createShareWithResponse(shareName, metadata, quota))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    private static Stream<Arguments> createShareWithMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(TEST_METADATA, null),
            Arguments.of(null, 1),
            Arguments.of(TEST_METADATA, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileServiceShareWithInvalidArgsSupplier")
    public void createShareWithInvalidArgs(Map<String, String> metadata, Integer quota, int statusCode,
        ShareErrorCode errMsg) {
        StepVerifier.create(primaryFileServiceAsyncClient.createShareWithResponse(shareName, metadata, quota))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode,
                errMsg));
    }

    @Test
    public void deleteShare() {
        primaryFileServiceAsyncClient.createShare(shareName).block();
        StepVerifier.create(primaryFileServiceAsyncClient.deleteShareWithResponse(shareName, null))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();
    }

    @Test
    public void deleteShareDoesNotExist() {
        StepVerifier.create(primaryFileServiceAsyncClient.deleteShare(generateShareName()))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.SHARE_NOT_FOUND));
    }

    @ParameterizedTest
    @MethodSource("listSharesWithFilterSupplier")
    public void listSharesWithFilter(ListSharesOptions options, int limits, boolean includeMetadata,
        boolean includeSnapshot) {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().setName(shareName + i)
                .setProperties(new ShareProperties().setQuota(i + 1));
            if (i == 2) {
                share.setMetadata(TEST_METADATA);
            }
            testShares.add(share);
            primaryFileServiceAsyncClient.createShareWithResponse(share.getName(), share.getMetadata(),
                share.getProperties().getQuota()).block();
        }

        StepVerifier.create(primaryFileServiceAsyncClient.listShares(options.setPrefix(prefix)))
            .thenConsumeWhile(it -> FileShareTestHelper.assertSharesAreEqual(testShares.pop(), it, includeMetadata,
                includeSnapshot)
        ).verifyComplete();

        for (int i = 0; i < 3 - limits; i++) {
            testShares.pop();
        }
        assertTrue(testShares.isEmpty());
    }

    protected static Stream<Arguments> listSharesWithFilterSupplier() {
        return Stream.of(
            Arguments.of(new ListSharesOptions(), 3, false, true, false),
            Arguments.of(new ListSharesOptions().setIncludeMetadata(true), 3, true, true, false),
            Arguments.of(new ListSharesOptions().setIncludeMetadata(false), 3, false, true, false),
            Arguments.of(new ListSharesOptions().setMaxResultsPerPage(2), 3, false, true, false)
        );
    }

    @ParameterizedTest
    @MethodSource("listSharesWithArgsSupplier")
    public void listSharesWithArgs(ListSharesOptions options, int limits, boolean includeMetadata,
        boolean includeSnapshot) {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().setName(shareName + i).setProperties(new ShareProperties().setQuota(2))
                .setMetadata(TEST_METADATA);
            ShareAsyncClient shareAsyncClient = primaryFileServiceAsyncClient.getShareAsyncClient(share.getName());
            shareAsyncClient.createWithResponse(share.getMetadata(), share.getProperties().getQuota()).block();
            if (i == 2) {
                StepVerifier.create(shareAsyncClient.createSnapshotWithResponse(null))
                    .assertNext(it -> {
                        testShares.add(new ShareItem().setName(share.getName()).setMetadata(share.getMetadata())
                            .setProperties(share.getProperties()).setSnapshot(it.getValue().getSnapshot()));
                        FileShareTestHelper.assertResponseStatusCode(it, 201);
                    }).verifyComplete();
            }
            testShares.add(share);
        }

        StepVerifier.create(primaryFileServiceAsyncClient.listShares(options.setPrefix(prefix)))
            .assertNext(it -> FileShareTestHelper.assertSharesAreEqual(testShares.pop(), it, includeMetadata,
                includeSnapshot))
            .expectNextCount(limits - 1).verifyComplete();

    }

    private static Stream<Arguments> listSharesWithArgsSupplier() {
        return Stream.of(
            Arguments.of(new ListSharesOptions(), 3, false, false),
            Arguments.of(new ListSharesOptions().setIncludeMetadata(true), 3, true, false),
            Arguments.of(new ListSharesOptions().setIncludeMetadata(true).setIncludeSnapshots(true), 4, true, true));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-07-07")
    @Test
    public void listSharesWithPremiumShare() {
        String premiumShareName = generateShareName();
        premiumFileServiceAsyncClient.createShare(premiumShareName).block();

        Flux<ShareItem> shares = premiumFileServiceAsyncClient.listShares().filter(item ->
            Objects.equals(item.getName(), premiumShareName));
        ShareProperties shareProperty = Objects.requireNonNull(shares.blockFirst()).getProperties();
        assertNotNull(shareProperty.getETag());
        assertNotNull(shareProperty.getLastModified());

    }

    @ResourceLock("ServiceProperties")
    @Test
    public void setAndGetProperties() {
        ShareServiceProperties originalProperties = primaryFileServiceAsyncClient.getProperties().block();
        ShareRetentionPolicy retentionPolicy = new ShareRetentionPolicy().setEnabled(true).setDays(3);
        ShareMetrics metrics = new ShareMetrics().setEnabled(true).setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy).setVersion("1.0");
        ShareServiceProperties updatedProperties = new ShareServiceProperties().setHourMetrics(metrics)
            .setMinuteMetrics(metrics).setCors(new ArrayList<>());

        StepVerifier.create(primaryFileServiceAsyncClient.getPropertiesWithResponse()).assertNext(it -> {
            FileShareTestHelper.assertResponseStatusCode(it, 200);
            assertTrue(FileShareTestHelper.assertFileServicePropertiesAreEqual(originalProperties, it.getValue()));
        }).verifyComplete();

        StepVerifier.create(primaryFileServiceAsyncClient.setPropertiesWithResponse(updatedProperties))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();

        StepVerifier.create(primaryFileServiceAsyncClient.getPropertiesWithResponse()).assertNext(it -> {
            FileShareTestHelper.assertResponseStatusCode(it, 200);
            assertTrue(FileShareTestHelper.assertFileServicePropertiesAreEqual(updatedProperties, it.getValue()));
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("setAndGetPropertiesWithInvalidArgsSupplier")
    public void setAndGetPropertiesWithInvalidArgs(List<ShareCorsRule> coreList, int statusCode,
        ShareErrorCode errMsg) {
        ShareRetentionPolicy retentionPolicy = new ShareRetentionPolicy().setEnabled(true).setDays(3);
        ShareMetrics metrics = new ShareMetrics().setEnabled(true).setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy).setVersion("1.0");

        ShareServiceProperties updatedProperties = new ShareServiceProperties().setHourMetrics(metrics)
            .setMinuteMetrics(metrics).setCors(coreList);
        StepVerifier.create(primaryFileServiceAsyncClient.setProperties(updatedProperties))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode,
                errMsg));
    }

    private static Stream<Arguments> setAndGetPropertiesWithInvalidArgsSupplier() {
        return Stream.of(Arguments.of(TOO_MANY_RULES, 400, ShareErrorCode.INVALID_XML_DOCUMENT),
            Arguments.of(INVALID_ALLOWED_HEADER, 400, ShareErrorCode.INVALID_XML_DOCUMENT),
            Arguments.of(INVALID_EXPOSED_HEADER, 400, ShareErrorCode.INVALID_XML_DOCUMENT),
            Arguments.of(INVALID_ALLOWED_ORIGIN, 400, ShareErrorCode.INVALID_XML_DOCUMENT),
            Arguments.of(INVALID_ALLOWED_METHOD, 400, ShareErrorCode.INVALID_XML_NODE_VALUE));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreShareMin() {
        ShareAsyncClient shareClient = primaryFileServiceAsyncClient.getShareAsyncClient(generateShareName());
        String fileName = generatePathName();
        ShareItem shareItem = shareClient.create()
            .then(shareClient.getFileClient(fileName).create(2))
            .then(shareClient.delete())
            .then(primaryFileServiceAsyncClient.listShares(
                new ListSharesOptions()
                    .setPrefix(shareClient.getShareName())
                    .setIncludeDeleted(true)).next()).block();
        sleepIfRunningAgainstService(30000);
        assertNotNull(shareItem);
        Mono<ShareAsyncClient> restoredShareClientMono = primaryFileServiceAsyncClient.undeleteShare(
            shareItem.getName(), shareItem.getVersion());
        StepVerifier.create(restoredShareClientMono.flatMap(it -> it.getFileClient(fileName).exists()))
            .assertNext(Assertions::assertTrue).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreShareMax() {
        ShareAsyncClient shareClient = primaryFileServiceAsyncClient.getShareAsyncClient(generateShareName());
        String fileName = generatePathName();
        ShareItem shareItem = shareClient.create()
            .then(shareClient.getFileClient(fileName).create(2))
            .then(shareClient.delete())
            .then(primaryFileServiceAsyncClient.listShares(
                new ListSharesOptions()
                    .setPrefix(shareClient.getShareName())
                    .setIncludeDeleted(true)).next()).block();
        sleepIfRunningAgainstService(30000);

        assertNotNull(shareItem);
        Mono<ShareAsyncClient> restoredShareClientMono = primaryFileServiceAsyncClient.undeleteShareWithResponse(
            shareItem.getName(), shareItem.getVersion()).map(Response::getValue);

        StepVerifier.create(restoredShareClientMono.flatMap(it -> it.getFileClient(fileName).exists()))
            .assertNext(Assertions::assertTrue).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreShareError() {
        StepVerifier.create(primaryFileServiceAsyncClient.undeleteShare(generateShareName(), "01D60F8BB59A4652"))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.SHARE_NOT_FOUND));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void listSharesEnableSnapshotVirtualDirectoryAccess() {
        ShareCreateOptions options = new ShareCreateOptions();
        ShareProtocols protocols = ModelHelper.parseShareProtocols(Constants.HeaderConstants.NFS_PROTOCOL);
        options.setProtocols(protocols);
        options.setSnapshotVirtualDirectoryAccessEnabled(true);

        String shareName = generateShareName();

        ShareAsyncClient shareClient = premiumFileServiceAsyncClient.getShareAsyncClient(shareName);

        Flux<ShareItem> response = shareClient.createWithResponse(options)
            .thenMany(premiumFileServiceAsyncClient.listShares());

        List<ShareItem> shares = new ArrayList<>();

        StepVerifier.create(response)
            .thenConsumeWhile(shares::add)
            .verifyComplete();

        ShareItem share = shares.stream().filter(r -> r.getName().equals(shareName)).findFirst().get();

        ShareProperties properties = share.getProperties();
        assertEquals(protocols.toString(), properties.getProtocols().toString());
        assertTrue(properties.isSnapshotVirtualDirectoryAccessEnabled());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void listSharePaidBursting() {
        ShareCreateOptions options = new ShareCreateOptions()
            .setEnablePaidBursting(true)
            .setPaidBurstingMaxIops(5000L)
            .setPaidBurstingMaxBandwidthMibps(1000L);

        String shareName = generateShareName();

        ShareAsyncClient shareClient = premiumFileServiceAsyncClient.getShareAsyncClient(shareName);

        Flux<ShareItem> response = shareClient.createWithResponse(options)
            .thenMany(premiumFileServiceAsyncClient.listShares());

        List<ShareItem> shares = new ArrayList<>();

        StepVerifier.create(response)
            .thenConsumeWhile(shares::add)
            .verifyComplete();

        ShareItem share = shares.stream().filter(r -> r.getName().equals(shareName)).findFirst().get();

        assertTrue(share.getProperties().getEnablePaidBursting());
        assertEquals(5000L, share.getProperties().getPaidBurstingMaxIops());
        assertEquals(1000L, share.getProperties().getPaidBurstingMaxBandwidthMibps());
    }
}
