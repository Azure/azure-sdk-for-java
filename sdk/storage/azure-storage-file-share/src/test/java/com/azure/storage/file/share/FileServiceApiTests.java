// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.ListSharesOptions;
import com.azure.storage.file.share.models.ShareAccessTier;
import com.azure.storage.file.share.models.ShareCorsRule;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareItem;
import com.azure.storage.file.share.models.ShareMetrics;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareProtocolSettings;
import com.azure.storage.file.share.models.ShareProtocols;
import com.azure.storage.file.share.models.ShareRetentionPolicy;
import com.azure.storage.file.share.models.ShareServiceProperties;
import com.azure.storage.file.share.models.ShareSmbSettings;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.file.share.models.SmbMultichannel;
import com.azure.storage.file.share.options.ShareCreateOptions;
import com.azure.storage.file.share.options.ShareSetPropertiesOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileServiceApiTests extends FileShareTestBase {
    private String shareName;

    private static final Map<String, String> TEST_METADATA = Collections.singletonMap("testmetadata", "value");
    private static final String REALLY_LONG_STRING = "thisisareallylongstringthatexceedsthe64characterlimitallowedoncertainproperties";
    private static List<ShareCorsRule> tooManyRules = new ArrayList<>();
    private static final List<ShareCorsRule> INVALID_ALLOWED_HEADER = Collections.singletonList(new ShareCorsRule()
        .setAllowedHeaders(REALLY_LONG_STRING));
    private static final List<ShareCorsRule> INVALID_EXPOSED_HEADER = Collections.singletonList(new ShareCorsRule()
        .setExposedHeaders(REALLY_LONG_STRING));
    private static final List<ShareCorsRule> INVALID_ALLOWED_ORIGIN = Collections.singletonList(new ShareCorsRule()
        .setAllowedOrigins(REALLY_LONG_STRING));
    private static final List<ShareCorsRule> INVALID_ALLOWED_METHOD = Collections.singletonList(new ShareCorsRule()
        .setAllowedMethods("NOTAREALHTTPMETHOD"));

    @BeforeAll
    public static void setupSpec() {
        for (int i = 0; i < 6; i++) {
            tooManyRules.add(new ShareCorsRule());
        }
        tooManyRules = Collections.unmodifiableList(tooManyRules);
    }

    @BeforeEach
    public void setup() {
        shareName = generateShareName();
        primaryFileServiceClient = fileServiceBuilderHelper().buildClient();
    }

    @Test
    public void getFileServiceURL() {
        String accountName = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount()
            .getConnectionString()).getAccountName();
        String expectURL = String.format("https://%s.file.core.windows.net", accountName);
        String fileServiceURL = primaryFileServiceClient.getFileServiceUrl();
        assertEquals(expectURL, fileServiceURL);
    }

    @Test
    public void getShareDoesNotCreateAShare() {
        ShareClient shareClient = primaryFileServiceClient.getShareClient(shareName);
        assertInstanceOf(ShareClient.class, shareClient);
    }

    @Test
    public void createShare() {
        Response<ShareClient> createShareResponse = primaryFileServiceClient.createShareWithResponse(shareName, null,
            null, null, null);
        FileShareTestHelper.assertResponseStatusCode(createShareResponse, 201);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void createShareMaxOverloads() {
        Response<ShareClient> createShareResponse = primaryFileServiceClient.createShareWithResponse(shareName,
            new ShareCreateOptions().setQuotaInGb(1).setMetadata(TEST_METADATA).setAccessTier(ShareAccessTier.HOT),
            null, null);
        FileShareTestHelper.assertResponseStatusCode(createShareResponse, 201);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileServiceShareWithInvalidArgsSupplier")
    public void createShareWithInvalidArgs(Map<String, String> metadata, Integer quota, int statusCode,
        ShareErrorCode errMsg) {
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryFileServiceClient.createShareWithResponse(shareName, metadata, quota, null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg);
    }

    @Test
    public void deleteShare() {
        primaryFileServiceClient.createShare(shareName);
        Response<Void> deleteShareResponse = primaryFileServiceClient.deleteShareWithResponse(shareName, null, null,
            null);
        FileShareTestHelper.assertResponseStatusCode(deleteShareResponse, 202);
    }

    @Test
    public void deleteShareDoesNotExist() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryFileServiceClient.deleteShare(generateShareName()));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("listSharesWithFilterSupplier")
    public void listSharesWithFilter(ListSharesOptions options, int limits, boolean includeMetadata,
        boolean includeSnapshot, boolean includeDeleted) {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        options.setPrefix(shareName);
        for (int i = 0; i < 4; i++) {
            ShareItem share = new ShareItem().setProperties(new ShareProperties().setQuota(i + 1))
                .setName(shareName + i);
            if (i == 2) {
                share.setMetadata(TEST_METADATA);
            }

            testShares.add(share);
            primaryFileServiceClient.createShareWithResponse(share.getName(), share.getMetadata(),
                share.getProperties().getQuota(), null, null);

            if (i == 3) {
                share.getProperties().setDeletedTime(OffsetDateTime.now());
                primaryFileServiceClient.deleteShare(share.getName());
            }
        }

        Iterator<ShareItem> shares = primaryFileServiceClient.listShares(options, null, null).iterator();

        for (int i = 0; i < limits; i++) {
            FileShareTestHelper.assertSharesAreEqual(testShares.pop(), shares.next(), includeMetadata, includeSnapshot,
                includeDeleted);
        }
        assertTrue(includeDeleted || !shares.hasNext());
    }

    private static Stream<Arguments> listSharesWithFilterSupplier() {
        return Stream.of(
            Arguments.of(new ListSharesOptions(), 3, false, true, false),
            Arguments.of(new ListSharesOptions().setIncludeMetadata(true), 3, true, true, false),
            Arguments.of(new ListSharesOptions().setIncludeMetadata(false), 3, false, true, false),
            Arguments.of(new ListSharesOptions().setMaxResultsPerPage(2), 3, false, true, false),
            Arguments.of(new ListSharesOptions().setIncludeDeleted(true), 4, false, true, true)
        );
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("listSharesWithArgsSupplier")
    public void listSharesWithArgs(ListSharesOptions options, int limits, boolean includeMetadata,
        boolean includeSnapshot, boolean includeDeleted) {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        options.setPrefix(shareName);
        for (int i = 0; i < 4; i++) {
            ShareItem share = new ShareItem().setName(shareName + i).setProperties(new ShareProperties().setQuota(2))
                .setMetadata(TEST_METADATA);
            ShareClient shareClient = primaryFileServiceClient.getShareClient(share.getName());
            shareClient.createWithResponse(share.getMetadata(), share.getProperties().getQuota(), null, null);
            if (i == 2) {
                String snapshot = shareClient.createSnapshot().getSnapshot();
                testShares.add(new ShareItem().setName(share.getName()).setMetadata(share.getMetadata())
                    .setProperties(share.getProperties()).setSnapshot(snapshot));
            }
            if (i == 3) {
                share.getProperties().setDeletedTime(OffsetDateTime.now());
                primaryFileServiceClient.deleteShare(share.getName());
            }
            testShares.add(share);
        }

        Iterator<ShareItem> shares = primaryFileServiceClient.listShares(options, null, null).iterator();

        for (int i = 0; i < limits; i++) {
            assertTrue(FileShareTestHelper.assertSharesAreEqual(testShares.pop(), shares.next(), includeMetadata,
                includeSnapshot, includeDeleted));
        }
        assertFalse(shares.hasNext());
    }

    private static Stream<Arguments> listSharesWithArgsSupplier() {
        return Stream.of(
            Arguments.of(new ListSharesOptions(), 3, false, false, false),
            Arguments.of(new ListSharesOptions().setIncludeMetadata(true), 3, true, false, false),
            Arguments.of(new ListSharesOptions().setIncludeMetadata(true).setIncludeSnapshots(true), 4, true, true,
                false),
            Arguments.of(new ListSharesOptions().setIncludeMetadata(true).setIncludeSnapshots(true)
                .setIncludeDeleted(true), 5, true, true, true)
        );
    }

    @Test
    public void listSharesMaxResultsByPage() {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        ListSharesOptions options = new ListSharesOptions().setPrefix(shareName);
        for (int i = 0; i < 4; i++) {
            ShareItem share = new ShareItem().setName(shareName + i);
            ShareClient shareClient = primaryFileServiceClient.getShareClient(share.getName());
            shareClient.create();
            testShares.add(share);
        }

        for (PagedResponse<ShareItem> page : primaryFileServiceClient.listShares(options, null, null)
            .iterableByPage(2)) {
            assertTrue(page.getValue().size() <= 2);
        }
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void listSharesGetAccessTier() {
        String shareName = generateShareName();
        ShareClient share = primaryFileServiceClient.createShareWithResponse(shareName,
            new ShareCreateOptions().setAccessTier(ShareAccessTier.HOT), null, null).getValue();

        OffsetDateTime time = testResourceNamer.now().truncatedTo(ChronoUnit.SECONDS);
        time = time.minusSeconds(1); // account for time skew on the other side.
        share.setProperties(new ShareSetPropertiesOptions().setAccessTier(ShareAccessTier.TRANSACTION_OPTIMIZED));

        Iterator<ShareItem> shares = primaryFileServiceClient.listShares(new ListSharesOptions().setPrefix(prefix),
            null, null).iterator();

        ShareItem item = shares.next();
        assertEquals(shareName, item.getName());
        assertEquals(ShareAccessTier.TRANSACTION_OPTIMIZED.toString(), item.getProperties().getAccessTier());
        assertNotNull(item.getProperties().getAccessTierChangeTime());
        assertTrue(item.getProperties().getAccessTierChangeTime().isEqual(time)
            || item.getProperties().getAccessTierChangeTime().isAfter(time));
        assertTrue(item.getProperties().getAccessTierChangeTime().isBefore(time.plusMinutes(1)));
        assertEquals("pending-from-hot", item.getProperties().getAccessTierTransitionState());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-02-12")
    @Test
    public void listSharesWithPremiumShare() {
        String premiumShareName = generateShareName();
        premiumFileServiceClient.createShare(premiumShareName);
        for (ShareItem shareItem : premiumFileServiceClient.listShares()) {
            if (Objects.equals(shareItem.getName(), premiumShareName)) {
                assertNotNull(shareItem.getProperties().getETag());
                assertNotNull(shareItem.getProperties().getLastModified());
            }
        }
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void listSharesOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        oAuthServiceClient.createShare(shareName);

        for (ShareItem shareItem : oAuthServiceClient.listShares()) {
            if (Objects.equals(shareItem.getName(), shareName)) {
                assertNotNull(shareItem.getProperties().getETag());
                assertNotNull(shareItem.getProperties().getLastModified());
            }
            assertNull(shareItem.getProperties().getMetadata());
        }
    }

    @ResourceLock("ServiceProperties")
    @Test
    public void setAndGetProperties() {
        ShareServiceProperties originalProperties = primaryFileServiceClient.getProperties();
        ShareRetentionPolicy retentionPolicy = new ShareRetentionPolicy().setEnabled(true).setDays(3);
        ShareMetrics metrics = new ShareMetrics().setEnabled(true).setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy).setVersion("1.0");
        ShareServiceProperties updatedProperties = new ShareServiceProperties().setHourMetrics(metrics)
            .setMinuteMetrics(metrics).setCors(new ArrayList<>());

        Response<ShareServiceProperties> getPropertiesBeforeResponse =
            primaryFileServiceClient.getPropertiesWithResponse(null, null);
        Response<Void> setPropertiesResponse =
            primaryFileServiceClient.setPropertiesWithResponse(updatedProperties, null, null);
        Response<ShareServiceProperties> getPropertiesAfterResponse =
            primaryFileServiceClient.getPropertiesWithResponse(null, null);

        FileShareTestHelper.assertResponseStatusCode(getPropertiesBeforeResponse, 200);
        FileShareTestHelper.assertFileServicePropertiesAreEqual(originalProperties,
            getPropertiesBeforeResponse.getValue());
        FileShareTestHelper.assertResponseStatusCode(setPropertiesResponse, 202);
        FileShareTestHelper.assertResponseStatusCode(getPropertiesAfterResponse, 200);
        FileShareTestHelper.assertFileServicePropertiesAreEqual(updatedProperties,
            getPropertiesAfterResponse.getValue());
    }

    @PlaybackOnly
    @ResourceLock("ServiceProperties")
    @Test
    public void setAndGetPropertiesPremium() {
        ShareServiceProperties originalProperties = premiumFileServiceClient.getProperties();
        ShareRetentionPolicy retentionPolicy = new ShareRetentionPolicy().setEnabled(true).setDays(3);
        ShareMetrics metrics = new ShareMetrics().setEnabled(true).setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy).setVersion("1.0");
        ShareProtocolSettings protocolSettings = new ShareProtocolSettings().setSmb(
            new ShareSmbSettings().setMultichannel(new SmbMultichannel().setEnabled(true)));
        ShareServiceProperties updatedProperties = new ShareServiceProperties().setHourMetrics(metrics)
            .setMinuteMetrics(metrics).setCors(new ArrayList<>())
            .setProtocol(protocolSettings);

        Response<ShareServiceProperties> getPropertiesBeforeResponse =
            premiumFileServiceClient.getPropertiesWithResponse(null, null);
        Response<Void> setPropertiesResponse =
            premiumFileServiceClient.setPropertiesWithResponse(updatedProperties, null, null);
        Response<ShareServiceProperties> getPropertiesAfterResponse =
            premiumFileServiceClient.getPropertiesWithResponse(null, null);

        FileShareTestHelper.assertResponseStatusCode(getPropertiesBeforeResponse, 200);
        FileShareTestHelper.assertFileServicePropertiesAreEqual(originalProperties,
            getPropertiesBeforeResponse.getValue());
        FileShareTestHelper.assertResponseStatusCode(setPropertiesResponse, 202);
        FileShareTestHelper.assertResponseStatusCode(getPropertiesAfterResponse, 200);
        FileShareTestHelper.assertFileServicePropertiesAreEqual(updatedProperties,
            getPropertiesAfterResponse.getValue());
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
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryFileServiceClient.setProperties(updatedProperties));

        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg);
    }

    private static Stream<Arguments> setAndGetPropertiesWithInvalidArgsSupplier() {
        return Stream.of(Arguments.of(tooManyRules, 400, ShareErrorCode.INVALID_XML_DOCUMENT),
            Arguments.of(INVALID_ALLOWED_HEADER, 400, ShareErrorCode.INVALID_XML_DOCUMENT),
            Arguments.of(INVALID_EXPOSED_HEADER, 400, ShareErrorCode.INVALID_XML_DOCUMENT),
            Arguments.of(INVALID_ALLOWED_ORIGIN, 400, ShareErrorCode.INVALID_XML_DOCUMENT),
            Arguments.of(INVALID_ALLOWED_METHOD, 400, ShareErrorCode.INVALID_XML_NODE_VALUE));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @ResourceLock("ServiceProperties")
    @Test
    public void setAndGetPropertiesOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));

        ShareServiceProperties originalProperties = oAuthServiceClient.getProperties();
        ShareRetentionPolicy retentionPolicy = new ShareRetentionPolicy().setEnabled(true).setDays(3);
        ShareMetrics metrics = new ShareMetrics().setEnabled(true).setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy).setVersion("1.0");
        ShareServiceProperties updatedProperties = new ShareServiceProperties().setHourMetrics(metrics)
            .setMinuteMetrics(metrics).setCors(new ArrayList<>());

        Response<ShareServiceProperties> getPropertiesBeforeResponse =
            oAuthServiceClient.getPropertiesWithResponse(null, null);
        Response<Void> setPropertiesResponse =
            oAuthServiceClient.setPropertiesWithResponse(updatedProperties, null, null);
        Response<ShareServiceProperties> getPropertiesAfterResponse =
            oAuthServiceClient.getPropertiesWithResponse(null, null);

        FileShareTestHelper.assertResponseStatusCode(getPropertiesBeforeResponse, 200);
        FileShareTestHelper.assertFileServicePropertiesAreEqual(originalProperties,
            getPropertiesBeforeResponse.getValue());
        FileShareTestHelper.assertResponseStatusCode(setPropertiesResponse, 202);
        FileShareTestHelper.assertResponseStatusCode(getPropertiesAfterResponse, 200);
        FileShareTestHelper.assertFileServicePropertiesAreEqual(updatedProperties,
            getPropertiesAfterResponse.getValue());

    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreShareMin() {
        ShareClient shareClient = primaryFileServiceClient.getShareClient(generateShareName());
        shareClient.create();
        String fileName = generatePathName();
        shareClient.getFileClient(fileName).create(2);
        shareClient.delete();
        sleepIfRunningAgainstService(30000);
        ShareItem shareItem = primaryFileServiceClient.listShares(
            new ListSharesOptions()
                .setPrefix(shareClient.getShareName())
                .setIncludeDeleted(true),
            null, Context.NONE).iterator().next();

        ShareClient restoredShareClient = primaryFileServiceClient.undeleteShare(shareItem.getName(),
            shareItem.getVersion());

        restoredShareClient.getFileClient(fileName).exists();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void restoreShareOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareClient shareClient = oAuthServiceClient.getShareClient(shareName);

        shareClient.create();
        String fileName = generatePathName();
        shareClient.getFileClient(fileName).create(2);
        shareClient.delete();
        sleepIfRunningAgainstService(30000);
        ShareItem shareItem = oAuthServiceClient.listShares(
            new ListSharesOptions()
                .setPrefix(shareClient.getShareName())
                .setIncludeDeleted(true),
            null, Context.NONE).iterator().next();

        ShareClient restoredShareClient = oAuthServiceClient.undeleteShare(shareItem.getName(),
            shareItem.getVersion());

        restoredShareClient.getFileClient(fileName).exists();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreShareMax() {
        ShareClient shareClient = primaryFileServiceClient.getShareClient(generateShareName());
        shareClient.create();
        String fileName = generatePathName();
        shareClient.getFileClient(fileName).create(2);
        shareClient.delete();
        sleepIfRunningAgainstService(30000);
        ShareItem shareItem = primaryFileServiceClient.listShares(
            new ListSharesOptions()
                .setPrefix(shareClient.getShareName())
                .setIncludeDeleted(true),
            null, Context.NONE).iterator().next();

        ShareClient restoredShareClient = primaryFileServiceClient.undeleteShareWithResponse(
            shareItem.getName(), shareItem.getVersion(), null, Context.NONE).getValue();

        assertTrue(restoredShareClient.getFileClient(fileName).exists());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreShareError() {
        assertThrows(ShareStorageException.class, () -> primaryFileServiceClient.undeleteShare(generateShareName(),
            "01D60F8BB59A4652"));
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#isServiceVersionSpecified")
    @Test
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    public void perCallPolicy() {
        ShareServiceClient serviceClient = getServiceClientBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryFileServiceClient.getFileServiceUrl(), getPerCallVersionPolicy()).buildClient();
        Response<ShareServiceProperties> response = serviceClient.getPropertiesWithResponse(null, null);
        assertEquals(response.getHeaders().getValue(X_MS_VERSION), "2017-11-09");
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void listSharesEnableSnapshotVirtualDirectoryAccess() {
        ShareCreateOptions options = new ShareCreateOptions();
        ShareProtocols protocols = ModelHelper.parseShareProtocols(Constants.HeaderConstants.NFS_PROTOCOL);
        options.setProtocols(protocols);
        options.setSnapshotVirtualDirectoryAccessEnabled(true);

        ShareClient shareClient = premiumFileServiceClient.getShareClient(generateShareName());
        shareClient.createWithResponse(options, null, null);

        ShareItem share = premiumFileServiceClient.listShares().iterator().next();
        assertEquals(protocols.toString(), share.getProperties().getProtocols().toString());
        assertTrue(share.getProperties().isSnapshotVirtualDirectoryAccessEnabled());
    }
}
