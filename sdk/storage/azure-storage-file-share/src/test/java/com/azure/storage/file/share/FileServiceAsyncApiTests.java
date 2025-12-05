// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.ListSharesOptions;
import com.azure.storage.file.share.models.ShareCorsRule;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareItem;
import com.azure.storage.file.share.models.ShareMetrics;
import com.azure.storage.file.share.models.ShareNfsSettings;
import com.azure.storage.file.share.models.ShareNfsSettingsEncryptionInTransit;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareProtocolSettings;
import com.azure.storage.file.share.models.ShareProtocols;
import com.azure.storage.file.share.models.ShareRetentionPolicy;
import com.azure.storage.file.share.models.ShareServiceProperties;
import com.azure.storage.file.share.models.UserDelegationKey;
import com.azure.storage.file.share.models.ShareSmbSettings;
import com.azure.storage.file.share.models.ShareSmbSettingsEncryptionInTransit;
import com.azure.storage.file.share.options.ShareCreateOptions;
import com.azure.storage.file.share.models.ShareTokenIntent;
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
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileServiceAsyncApiTests extends FileShareTestBase {
    private String shareName;

    private static final Map<String, String> TEST_METADATA = Collections.singletonMap("testmetadata", "value");
    private static final String REALLY_LONG_STRING
        = "thisisareallylongstringthatexceedsthe64characterlimitallowedoncertainproperties";
    private static final List<ShareCorsRule> TOO_MANY_RULES = new ArrayList<>();
    private static final List<ShareCorsRule> INVALID_ALLOWED_HEADER
        = Collections.singletonList(new ShareCorsRule().setAllowedHeaders(REALLY_LONG_STRING));
    private static final List<ShareCorsRule> INVALID_EXPOSED_HEADER
        = Collections.singletonList(new ShareCorsRule().setExposedHeaders(REALLY_LONG_STRING));
    private static final List<ShareCorsRule> INVALID_ALLOWED_ORIGIN
        = Collections.singletonList(new ShareCorsRule().setAllowedOrigins(REALLY_LONG_STRING));
    private static final List<ShareCorsRule> INVALID_ALLOWED_METHOD
        = Collections.singletonList(new ShareCorsRule().setAllowedMethods("NOTAREALHTTPMETHOD"));

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
        String accountName
            = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
                .getAccountName();
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
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createShareWithMetadataSupplier")
    public void createShareWithMetadata(Map<String, String> metadata, Integer quota) {
        StepVerifier.create(primaryFileServiceAsyncClient.createShareWithResponse(shareName, metadata, quota))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    private static Stream<Arguments> createShareWithMetadataSupplier() {
        return Stream.of(Arguments.of(null, null), Arguments.of(TEST_METADATA, null), Arguments.of(null, 1),
            Arguments.of(TEST_METADATA, 1));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileServiceShareWithInvalidArgsSupplier")
    public void createShareWithInvalidArgs(Map<String, String> metadata, Integer quota, int statusCode,
        ShareErrorCode errMsg) {
        StepVerifier.create(primaryFileServiceAsyncClient.createShareWithResponse(shareName, metadata, quota))
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg));
    }

    @Test
    public void deleteShare() {
        StepVerifier
            .create(primaryFileServiceAsyncClient.createShare(shareName)
                .then(primaryFileServiceAsyncClient.deleteShareWithResponse(shareName, null)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteShareDoesNotExist() {
        StepVerifier.create(primaryFileServiceAsyncClient.deleteShare(generateShareName()))
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @ParameterizedTest
    @MethodSource("listSharesWithFilterSupplier")
    public void listSharesWithFilter(ListSharesOptions options, int limits, boolean includeMetadata,
        boolean includeSnapshot) {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share
                = new ShareItem().setName(shareName + i).setProperties(new ShareProperties().setQuota(i + 1));
            if (i == 2) {
                share.setMetadata(TEST_METADATA);
            }
            testShares.add(share);
        }

        Flux<ShareItem> createShares = Flux.fromIterable(testShares)
            .flatMap(share -> primaryFileServiceAsyncClient.createShareWithResponse(share.getName(),
                share.getMetadata(), share.getProperties().getQuota()))
            .thenMany(primaryFileServiceAsyncClient.listShares(options.setPrefix(prefix)));

        StepVerifier.create(createShares)
            .thenConsumeWhile(
                it -> FileShareTestHelper.assertSharesAreEqual(testShares.pop(), it, includeMetadata, includeSnapshot))
            .verifyComplete();

        for (int i = 0; i < 3 - limits; i++) {
            testShares.pop();
        }
        assertTrue(testShares.isEmpty());
    }

    protected static Stream<Arguments> listSharesWithFilterSupplier() {
        return Stream.of(Arguments.of(new ListSharesOptions(), 3, false, true, false),
            Arguments.of(new ListSharesOptions().setIncludeMetadata(true), 3, true, true, false),
            Arguments.of(new ListSharesOptions().setIncludeMetadata(false), 3, false, true, false),
            Arguments.of(new ListSharesOptions().setMaxResultsPerPage(2), 3, false, true, false));
    }

    @ParameterizedTest
    @MethodSource("listSharesWithArgsSupplier")
    public void listSharesWithArgs(ListSharesOptions options, int limits, boolean includeMetadata,
        boolean includeSnapshot) {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        Flux<ShareItem> createSharesFlux = Flux.range(0, 3).flatMap(i -> {
            ShareItem share = new ShareItem().setName(shareName + i)
                .setProperties(new ShareProperties().setQuota(2))
                .setMetadata(TEST_METADATA);
            ShareAsyncClient shareAsyncClient = primaryFileServiceAsyncClient.getShareAsyncClient(share.getName());
            Mono<ShareItem> createShareMono
                = shareAsyncClient.createWithResponse(share.getMetadata(), share.getProperties().getQuota())
                    .then(Mono.just(share));
            if (i == 2) {
                createShareMono
                    = createShareMono.then(shareAsyncClient.createSnapshotWithResponse(null).map(snapshotResponse -> {
                        ShareItem snapshotShare = new ShareItem().setName(share.getName())
                            .setMetadata(share.getMetadata())
                            .setProperties(share.getProperties())
                            .setSnapshot(snapshotResponse.getValue().getSnapshot());
                        FileShareTestHelper.assertResponseStatusCode(snapshotResponse, 201);
                        return snapshotShare;
                    }));
            }
            return createShareMono.doOnNext(testShares::add);
        });

        StepVerifier
            .create(createSharesFlux.thenMany(primaryFileServiceAsyncClient.listShares(options.setPrefix(prefix))))
            .assertNext(
                it -> FileShareTestHelper.assertSharesAreEqual(testShares.pop(), it, includeMetadata, includeSnapshot))
            .expectNextCount(limits - 1)
            .verifyComplete();
    }

    private static Stream<Arguments> listSharesWithArgsSupplier() {
        return Stream.of(Arguments.of(new ListSharesOptions(), 3, false, false),
            Arguments.of(new ListSharesOptions().setIncludeMetadata(true), 3, true, false),
            Arguments.of(new ListSharesOptions().setIncludeMetadata(true).setIncludeSnapshots(true), 4, true, true));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-07-07")
    @Test
    public void listSharesWithPremiumShare() {
        String premiumShareName = generateShareName();
        Mono<ShareAsyncClient> createShareMono = premiumFileServiceAsyncClient.createShare(premiumShareName);

        Flux<ShareItem> shares = createShareMono.thenMany(premiumFileServiceAsyncClient.listShares()
            .filter(item -> Objects.equals(item.getName(), premiumShareName))
            .next()
            .flatMap(shareItem -> {
                ShareProperties shareProperty = shareItem.getProperties();
                assertNotNull(shareProperty.getETag());
                assertNotNull(shareProperty.getLastModified());
                return Mono.empty();
            }));

        StepVerifier.create(shares).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void listSharesOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));

        Flux<ShareItem> shares
            = oAuthServiceClient.listShares().filter(item -> Objects.equals(item.getName(), shareName));

        StepVerifier.create(oAuthServiceClient.createShare(shareName).thenMany(shares)).assertNext(r -> {
            assertNotNull(r.getProperties());
            assertNotNull(r.getProperties().getETag());
            assertNotNull(r.getProperties().getLastModified());
        }).verifyComplete();
    }

    @PlaybackOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-01-05")
    @Test
    public void listSharesProvisionedV2() {
        Flux<ShareItem> shares
            = primaryFileServiceAsyncClient.listShares().filter(item -> Objects.equals(item.getName(), shareName));

        StepVerifier.create(primaryFileServiceAsyncClient.createShare(shareName).thenMany(shares)).assertNext(r -> {
            assertNotNull(r.getProperties().getIncludedBurstIops());
            assertNotNull(r.getProperties().getMaxBurstCreditsForIops());
            assertNotNull(r.getProperties().getNextAllowedProvisionedIopsDowngradeTime());
            assertNotNull(r.getProperties().getNextAllowedProvisionedBandwidthDowngradeTime());
        }).verifyComplete();
    }

    @ResourceLock("ServiceProperties")
    @Test
    public void setAndGetProperties() {
        Mono<Tuple2<ShareServiceProperties, Response<ShareServiceProperties>>> originalProperties = Mono.zip(
            primaryFileServiceAsyncClient.getProperties(), primaryFileServiceAsyncClient.getPropertiesWithResponse());

        ShareRetentionPolicy retentionPolicy = new ShareRetentionPolicy().setEnabled(true).setDays(3);
        ShareMetrics metrics = new ShareMetrics().setEnabled(true)
            .setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy)
            .setVersion("1.0");
        ShareServiceProperties updatedProperties
            = new ShareServiceProperties().setHourMetrics(metrics).setMinuteMetrics(metrics).setCors(new ArrayList<>());

        StepVerifier.create(originalProperties).assertNext(tuple -> {
            FileShareTestHelper.assertResponseStatusCode(tuple.getT2(), 200);
            assertTrue(
                FileShareTestHelper.assertFileServicePropertiesAreEqual(tuple.getT1(), tuple.getT2().getValue()));
        }).verifyComplete();

        StepVerifier.create(primaryFileServiceAsyncClient.setPropertiesWithResponse(updatedProperties))
            .assertNext(response -> FileShareTestHelper.assertResponseStatusCode(response, 202))
            .verifyComplete();

        StepVerifier.create(primaryFileServiceAsyncClient.getPropertiesWithResponse()).assertNext(response -> {
            FileShareTestHelper.assertResponseStatusCode(response, 200);
            assertTrue(FileShareTestHelper.assertFileServicePropertiesAreEqual(updatedProperties, response.getValue()));
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("setAndGetPropertiesWithInvalidArgsSupplier")
    public void setAndGetPropertiesWithInvalidArgs(List<ShareCorsRule> coreList, int statusCode,
        ShareErrorCode errMsg) {
        ShareRetentionPolicy retentionPolicy = new ShareRetentionPolicy().setEnabled(true).setDays(3);
        ShareMetrics metrics = new ShareMetrics().setEnabled(true)
            .setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy)
            .setVersion("1.0");

        ShareServiceProperties updatedProperties
            = new ShareServiceProperties().setHourMetrics(metrics).setMinuteMetrics(metrics).setCors(coreList);
        StepVerifier.create(primaryFileServiceAsyncClient.setProperties(updatedProperties))
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg));
    }

    private static Stream<Arguments> setAndGetPropertiesWithInvalidArgsSupplier() {
        return Stream.of(Arguments.of(TOO_MANY_RULES, 400, ShareErrorCode.INVALID_XML_DOCUMENT),
            Arguments.of(INVALID_ALLOWED_HEADER, 400, ShareErrorCode.INVALID_XML_DOCUMENT),
            Arguments.of(INVALID_EXPOSED_HEADER, 400, ShareErrorCode.INVALID_XML_DOCUMENT),
            Arguments.of(INVALID_ALLOWED_ORIGIN, 400, ShareErrorCode.INVALID_XML_DOCUMENT),
            Arguments.of(INVALID_ALLOWED_METHOD, 400, ShareErrorCode.INVALID_XML_NODE_VALUE));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ResourceLock("ServiceProperties")
    @Test
    public void setAndGetPropertiesOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));

        Mono<Tuple2<ShareServiceProperties, Response<ShareServiceProperties>>> originalProperties
            = Mono.zip(oAuthServiceClient.getProperties(), oAuthServiceClient.getPropertiesWithResponse());

        ShareRetentionPolicy retentionPolicy = new ShareRetentionPolicy().setEnabled(true).setDays(3);
        ShareMetrics metrics = new ShareMetrics().setEnabled(true)
            .setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy)
            .setVersion("1.0");
        ShareServiceProperties updatedProperties
            = new ShareServiceProperties().setHourMetrics(metrics).setMinuteMetrics(metrics).setCors(new ArrayList<>());

        StepVerifier.create(originalProperties).assertNext(tuple -> {
            FileShareTestHelper.assertResponseStatusCode(tuple.getT2(), 200);
            assertTrue(
                FileShareTestHelper.assertFileServicePropertiesAreEqual(tuple.getT1(), tuple.getT2().getValue()));
        }).verifyComplete();

        StepVerifier.create(oAuthServiceClient.setPropertiesWithResponse(updatedProperties))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();

        StepVerifier.create(oAuthServiceClient.getPropertiesWithResponse()).assertNext(it -> {
            FileShareTestHelper.assertResponseStatusCode(it, 200);
            assertTrue(FileShareTestHelper.assertFileServicePropertiesAreEqual(updatedProperties, it.getValue()));
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreShareMin() {
        ShareAsyncClient shareClient = primaryFileServiceAsyncClient.getShareAsyncClient(generateShareName());
        String fileName = generatePathName();
        long delay = ENVIRONMENT.getTestMode() == TestMode.PLAYBACK ? 0L : 30000L;
        Mono<ShareItem> shareItemMono = shareClient.create()
            .then(shareClient.getFileClient(fileName).create(2))
            .then(shareClient.delete())
            .then(Mono.delay(Duration.ofMillis(delay)))
            .then(primaryFileServiceAsyncClient
                .listShares(new ListSharesOptions().setPrefix(shareClient.getShareName()).setIncludeDeleted(true))
                .next());

        StepVerifier.create(shareItemMono.flatMap(shareItem -> {
            assertNotNull(shareItem);
            return primaryFileServiceAsyncClient.undeleteShare(shareItem.getName(), shareItem.getVersion())
                .flatMap(restoredShareClient -> restoredShareClient.getFileClient(fileName).exists());
        })).assertNext(Assertions::assertTrue).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void restoreShareOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareAsyncClient shareClient = oAuthServiceClient.getShareAsyncClient(generateShareName());

        String fileName = generatePathName();
        Mono<ShareAsyncClient> restoredShareClientMono = shareClient.create()
            .then(shareClient.getFileClient(fileName).create(2))
            .then(shareClient.delete())
            .then(oAuthServiceClient
                .listShares(new ListSharesOptions().setPrefix(shareClient.getShareName()).setIncludeDeleted(true))
                .next())
            .flatMap(r -> Mono.zip(Mono.just(r), Mono.delay(Duration.ofSeconds(30))))
            .flatMap(tuple -> {
                assertNotNull(tuple.getT1());
                return oAuthServiceClient.undeleteShare(tuple.getT1().getName(), tuple.getT1().getVersion());
            });

        StepVerifier.create(restoredShareClientMono.flatMap(it -> it.getFileClient(fileName).exists()))
            .assertNext(Assertions::assertTrue)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreShareMax() {
        ShareAsyncClient shareClient = primaryFileServiceAsyncClient.getShareAsyncClient(generateShareName());
        String fileName = generatePathName();
        long delay = ENVIRONMENT.getTestMode() == TestMode.PLAYBACK ? 0L : 30000L;
        Mono<ShareItem> shareItemMono = shareClient.create()
            .then(shareClient.getFileClient(fileName).create(2))
            .then(shareClient.delete())
            .then(Mono.delay(Duration.ofMillis(delay)))
            .then(primaryFileServiceAsyncClient
                .listShares(new ListSharesOptions().setPrefix(shareClient.getShareName()).setIncludeDeleted(true))
                .next());

        StepVerifier.create(shareItemMono.flatMap(shareItem -> {
            assertNotNull(shareItem);
            return primaryFileServiceAsyncClient.undeleteShareWithResponse(shareItem.getName(), shareItem.getVersion())
                .map(Response::getValue)
                .flatMap(restoredShareClient -> restoredShareClient.getFileClient(fileName).exists());
        })).assertNext(Assertions::assertTrue).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreShareError() {
        StepVerifier.create(primaryFileServiceAsyncClient.undeleteShare(generateShareName(), "01D60F8BB59A4652"))
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
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

        Flux<ShareItem> response
            = shareClient.createWithResponse(options).thenMany(premiumFileServiceAsyncClient.listShares());

        List<ShareItem> shares = new ArrayList<>();

        StepVerifier.create(response).thenConsumeWhile(shares::add).verifyComplete();

        ShareItem share = shares.stream().filter(r -> r.getName().equals(shareName)).findFirst().get();

        ShareProperties properties = share.getProperties();
        assertEquals(protocols.toString(), properties.getProtocols().toString());
        assertTrue(properties.isSnapshotVirtualDirectoryAccessEnabled());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void listSharePaidBursting() {
        ShareCreateOptions options = new ShareCreateOptions().setPaidBurstingEnabled(true)
            .setPaidBurstingMaxIops(5000L)
            .setPaidBurstingMaxBandwidthMibps(1000L);

        String shareName = generateShareName();

        ShareAsyncClient shareClient = premiumFileServiceAsyncClient.getShareAsyncClient(shareName);

        Flux<ShareItem> response
            = shareClient.createWithResponse(options).thenMany(premiumFileServiceAsyncClient.listShares());

        List<ShareItem> shares = new ArrayList<>();

        StepVerifier.create(response).thenConsumeWhile(shares::add).verifyComplete();

        ShareItem share = shares.stream().filter(r -> r.getName().equals(shareName)).findFirst().get();

        assertTrue(share.getProperties().isPaidBurstingEnabled());
        assertEquals(5000L, share.getProperties().getPaidBurstingMaxIops());
        assertEquals(1000L, share.getProperties().getPaidBurstingMaxBandwidthMibps());
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void fileServiceGetUserDelegationKey() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));

        OffsetDateTime expiry = testResourceNamer.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS);
        Mono<Response<UserDelegationKey>> response
            = oAuthServiceClient.getUserDelegationKeyWithResponse(testResourceNamer.now(), expiry);

        StepVerifier.create(response)
            .assertNext(r -> assertEquals(expiry, r.getValue().getSignedExpiry()))
            .verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void fileServiceGetUserDelegationKeyAuthError() {
        OffsetDateTime expiry = testResourceNamer.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS);

        //not oauth client
        StepVerifier
            .create(primaryFileServiceAsyncClient.getUserDelegationKeyWithResponse(testResourceNamer.now(), expiry))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 403,
                ShareErrorCode.AUTHENTICATION_FAILED));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    @ResourceLock("ServiceProperties")
    @Test
    public void getSetServicePropertiesEncryptionInTransitSMB() {
        Mono<Response<ShareServiceProperties>> propertiesResponseMono
            = primaryFileServiceAsyncClient.getPropertiesWithResponse();

        StepVerifier.create(propertiesResponseMono.flatMap(propertiesResponse -> {
            ShareServiceProperties properties = propertiesResponse.getValue();

            if (properties.getProtocol() != null
                && properties.getProtocol().getSmb() != null
                && properties.getProtocol().getSmb().getEncryptionInTransit() != null
                && Boolean.TRUE.equals(properties.getProtocol().getSmb().getEncryptionInTransit().isRequired())) {

                properties.getProtocol().getSmb().setMultichannel(null);
                properties.getProtocol().getSmb().getEncryptionInTransit().setRequired(false);

                return primaryFileServiceAsyncClient.setPropertiesWithResponse(properties)
                    .then(primaryFileServiceAsyncClient.getPropertiesWithResponse())
                    .doOnNext(updatedResponse -> assertFalse(
                        updatedResponse.getValue().getProtocol().getSmb().getEncryptionInTransit().isRequired()))
                    .then();
            } else {
                properties.setProtocol(new ShareProtocolSettings());
                properties.getProtocol()
                    .setSmb(new ShareSmbSettings()
                        .setEncryptionInTransit(new ShareSmbSettingsEncryptionInTransit().setRequired(true))
                        .setMultichannel(null));

                return primaryFileServiceAsyncClient.setPropertiesWithResponse(properties)
                    .then(primaryFileServiceAsyncClient.getPropertiesWithResponse())
                    .doOnNext(updatedResponse -> assertTrue(
                        updatedResponse.getValue().getProtocol().getSmb().getEncryptionInTransit().isRequired()))
                    .then();
            }
        })).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    @ResourceLock("ServiceProperties")
    @Test
    public void getSetServicePropertiesEncryptionInTransitNFS() {
        ShareServiceAsyncClient service = premiumFileServiceAsyncClient;

        Mono<Response<ShareServiceProperties>> propertiesResponseMono = service.getPropertiesWithResponse();

        StepVerifier.create(propertiesResponseMono.flatMap(propertiesResponse -> {
            ShareServiceProperties properties = propertiesResponse.getValue();

            if (properties.getProtocol() != null
                && properties.getProtocol().getNfs() != null
                && properties.getProtocol().getNfs().getEncryptionInTransit() != null
                && Boolean.TRUE.equals(properties.getProtocol().getNfs().getEncryptionInTransit().isRequired())) {

                properties.getProtocol().getNfs().getEncryptionInTransit().setRequired(false);
                return service.setPropertiesWithResponse(properties)
                    .then(service.getPropertiesWithResponse())
                    .doOnNext(updatedResponse -> assertFalse(
                        updatedResponse.getValue().getProtocol().getNfs().getEncryptionInTransit().isRequired()))
                    .then(service.getPropertiesWithResponse())
                    .flatMap(updatedResponse -> {
                        properties.getProtocol().getNfs().getEncryptionInTransit().setRequired(true);
                        return service.setPropertiesWithResponse(properties);
                    })
                    .then();
            } else {
                properties.setProtocol(new ShareProtocolSettings());
                properties.getProtocol()
                    .setNfs(new ShareNfsSettings()
                        .setEncryptionInTransit(new ShareNfsSettingsEncryptionInTransit().setRequired(true)));
                return service.setPropertiesWithResponse(properties)
                    .then(service.getPropertiesWithResponse())
                    .doOnNext(updatedResponse -> assertTrue(
                        updatedResponse.getValue().getProtocol().getNfs().getEncryptionInTransit().isRequired()))
                    .then(service.getPropertiesWithResponse())
                    .flatMap(updatedResponse -> {
                        properties.getProtocol().getNfs().getEncryptionInTransit().setRequired(false);
                        return service.setPropertiesWithResponse(properties);
                    })
                    .then();
            }
        })).verifyComplete();
    }
}
