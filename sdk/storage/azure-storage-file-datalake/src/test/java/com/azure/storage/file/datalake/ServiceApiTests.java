// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.datalake.models.DataLakeAnalyticsLogging;
import com.azure.storage.file.datalake.models.DataLakeAudience;
import com.azure.storage.file.datalake.models.DataLakeCorsRule;
import com.azure.storage.file.datalake.models.DataLakeMetrics;
import com.azure.storage.file.datalake.models.DataLakeRetentionPolicy;
import com.azure.storage.file.datalake.models.DataLakeServiceProperties;
import com.azure.storage.file.datalake.models.DataLakeStaticWebsite;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileSystemItem;
import com.azure.storage.file.datalake.models.FileSystemItemProperties;
import com.azure.storage.file.datalake.models.FileSystemListDetails;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.ListFileSystemsOptions;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.options.FileSystemEncryptionScopeOptions;
import com.azure.storage.file.datalake.options.FileSystemUndeleteOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceApiTests extends DataLakeTestBase {
    private static void validatePropsSet(DataLakeServiceProperties sent, DataLakeServiceProperties received) {
        assertEquals(sent.getLogging().isRead(), received.getLogging().isRead());
        assertEquals(sent.getLogging().isWrite(), received.getLogging().isWrite());
        assertEquals(sent.getLogging().isDelete(), received.getLogging().isDelete());
        assertEquals(sent.getLogging().getRetentionPolicy().isEnabled(), received.getLogging().getRetentionPolicy().isEnabled());
        assertEquals(sent.getLogging().getRetentionPolicy().getDays(), received.getLogging().getRetentionPolicy().getDays());
        assertEquals(sent.getLogging().getVersion(), received.getLogging().getVersion());

        assertEquals(sent.getCors().size(), received.getCors().size());
        assertEquals(sent.getCors().get(0).getAllowedMethods(), received.getCors().get(0).getAllowedMethods());
        assertEquals(sent.getCors().get(0).getAllowedHeaders(), received.getCors().get(0).getAllowedHeaders());
        assertEquals(sent.getCors().get(0).getAllowedOrigins(), received.getCors().get(0).getAllowedOrigins());
        assertEquals(sent.getCors().get(0).getExposedHeaders(), received.getCors().get(0).getExposedHeaders());
        assertEquals(sent.getCors().get(0).getMaxAgeInSeconds(), received.getCors().get(0).getMaxAgeInSeconds());

        assertEquals(sent.getDefaultServiceVersion(), received.getDefaultServiceVersion());

        assertEquals(sent.getHourMetrics().isEnabled(), received.getHourMetrics().isEnabled());
        assertEquals(sent.getHourMetrics().isIncludeApis(), received.getHourMetrics().isIncludeApis());
        assertEquals(sent.getHourMetrics().getRetentionPolicy().isEnabled(), received.getHourMetrics().getRetentionPolicy().isEnabled());
        assertEquals(sent.getHourMetrics().getRetentionPolicy().getDays(), received.getHourMetrics().getRetentionPolicy().getDays());
        assertEquals(sent.getHourMetrics().getVersion(), received.getHourMetrics().getVersion());

        assertEquals(sent.getMinuteMetrics().isEnabled(), received.getMinuteMetrics().isEnabled());
        assertEquals(sent.getMinuteMetrics().isIncludeApis(), received.getMinuteMetrics().isIncludeApis());
        assertEquals(sent.getMinuteMetrics().getRetentionPolicy().isEnabled(), received.getMinuteMetrics().getRetentionPolicy().isEnabled());
        assertEquals(sent.getMinuteMetrics().getRetentionPolicy().getDays(), received.getMinuteMetrics().getRetentionPolicy().getDays());
        assertEquals(sent.getMinuteMetrics().getVersion(), received.getMinuteMetrics().getVersion());

        assertEquals(sent.getDeleteRetentionPolicy().isEnabled(), received.getDeleteRetentionPolicy().isEnabled());
        assertEquals(sent.getDeleteRetentionPolicy().getDays(), received.getDeleteRetentionPolicy().getDays());

        assertEquals(sent.getStaticWebsite().isEnabled(), received.getStaticWebsite().isEnabled());
        assertEquals(sent.getStaticWebsite().getIndexDocument(), received.getStaticWebsite().getIndexDocument());
        assertEquals(sent.getStaticWebsite().getErrorDocument404Path(), received.getStaticWebsite().getErrorDocument404Path());
    }

    @ResourceLock("ServiceProperties")
    @Test
    public void setGetProperties() {
        int retry = 0;

        // Retry this test up to 5 times as the service properties have propagation lag.
        while (retry < 5 && !interceptorManager.isPlaybackMode()) {
            try {
                DataLakeRetentionPolicy retentionPolicy = new DataLakeRetentionPolicy().setDays(5).setEnabled(true);
                DataLakeAnalyticsLogging logging =
                    new DataLakeAnalyticsLogging().setRead(true).setVersion("1.0").setRetentionPolicy(retentionPolicy);
                List<DataLakeCorsRule> corsRules = Collections.singletonList(new DataLakeCorsRule()
                    .setAllowedMethods("GET,PUT,HEAD")
                    .setAllowedOrigins("*")
                    .setAllowedHeaders("x-ms-version")
                    .setExposedHeaders("x-ms-client-request-id")
                    .setMaxAgeInSeconds(10));
                DataLakeMetrics hourMetrics = new DataLakeMetrics()
                    .setEnabled(true)
                    .setVersion("1.0")
                    .setRetentionPolicy(retentionPolicy)
                    .setIncludeApis(true);
                DataLakeMetrics minuteMetrics = new DataLakeMetrics()
                    .setEnabled(true)
                    .setVersion("1.0")
                    .setRetentionPolicy(retentionPolicy)
                    .setIncludeApis(true);
                DataLakeStaticWebsite website = new DataLakeStaticWebsite()
                    .setEnabled(true)
                    .setIndexDocument("myIndex.html")
                    .setErrorDocument404Path("custom/error/path.html");

                DataLakeServiceProperties sentProperties = new DataLakeServiceProperties()
                    .setLogging(logging)
                    .setCors(corsRules)
                    .setDefaultServiceVersion("2016-05-31")
                    .setMinuteMetrics(minuteMetrics)
                    .setHourMetrics(hourMetrics)
                    .setDeleteRetentionPolicy(retentionPolicy)
                    .setStaticWebsite(website);

                HttpHeaders headers =
                    primaryDataLakeServiceClient.setPropertiesWithResponse(sentProperties, null, null).getHeaders();

                // Service properties may take up to 30s to take effect. If they weren't already in place, wait.
                sleepIfRunningAgainstService(30 * 1000);

                DataLakeServiceProperties receivedProperties = primaryDataLakeServiceClient.getProperties();

                assertNotNull(headers.getValue(X_MS_REQUEST_ID));
                assertNotNull(headers.getValue(X_MS_VERSION));
                validatePropsSet(sentProperties, receivedProperties);

                break;
            } catch (Exception ex) {
                // Retry delay
                sleepIfRunningAgainstService(30 * 1000);
            } finally {
                retry++;
            }
        }
    }

    // In java, we don't have support from the validator for checking the bounds on days. The service will catch these.
    @ResourceLock("ServiceProperties")
    @Test
    public void setPropsMin() {
        DataLakeRetentionPolicy retentionPolicy = new DataLakeRetentionPolicy().setDays(5).setEnabled(true);
        DataLakeAnalyticsLogging logging = new DataLakeAnalyticsLogging().setRead(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy);
        List<DataLakeCorsRule> corsRules = Collections.singletonList(new DataLakeCorsRule()
            .setAllowedMethods("GET,PUT,HEAD")
            .setAllowedOrigins("*")
            .setAllowedHeaders("x-ms-version")
            .setExposedHeaders("x-ms-client-request-id")
            .setMaxAgeInSeconds(10));
        DataLakeMetrics hourMetrics = new DataLakeMetrics().setEnabled(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy).setIncludeApis(true);
        DataLakeMetrics minuteMetrics = new DataLakeMetrics().setEnabled(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy).setIncludeApis(true);
        DataLakeStaticWebsite website = new DataLakeStaticWebsite().setEnabled(true)
            .setIndexDocument("myIndex.html")
            .setErrorDocument404Path("custom/error/path.html");

        DataLakeServiceProperties sentProperties = new DataLakeServiceProperties()
            .setLogging(logging).setCors(corsRules).setDefaultServiceVersion("2016-05-31")
            .setMinuteMetrics(minuteMetrics).setHourMetrics(hourMetrics)
            .setDeleteRetentionPolicy(retentionPolicy)
            .setStaticWebsite(website);

        assertEquals(202, primaryDataLakeServiceClient.setPropertiesWithResponse(sentProperties, null, null).getStatusCode());
    }

    @ResourceLock("ServiceProperties")
    @Test
    public void setPropsCorsCheck() {
        DataLakeServiceProperties serviceProperties = primaryDataLakeServiceClient.getProperties();

        // Some properties are not set and this test validates that they are not null when sent to the service
        serviceProperties.setCors(Collections.singletonList(new DataLakeCorsRule().setAllowedOrigins("microsoft.com")
            .setMaxAgeInSeconds(60)
            .setAllowedMethods("GET")
            .setAllowedHeaders("x-ms-version")));

        assertEquals(202, primaryDataLakeServiceClient.setPropertiesWithResponse(serviceProperties, null, null).getStatusCode());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @ResourceLock("ServiceProperties")
    @Test
    public void setPropsStaticWebsite() {
        DataLakeServiceProperties serviceProperties = primaryDataLakeServiceClient.getProperties();

        serviceProperties.setStaticWebsite(new DataLakeStaticWebsite()
            .setEnabled(true)
            .setErrorDocument404Path("error/404.html")
            .setDefaultIndexDocumentPath("index.html"));

        Response<Void> resp = primaryDataLakeServiceClient.setPropertiesWithResponse(serviceProperties, null, null);
        assertEquals(202, resp.getStatusCode());

        DataLakeStaticWebsite staticWebsite = primaryDataLakeServiceClient.getProperties().getStaticWebsite();
        assertTrue(staticWebsite.isEnabled());
        assertEquals(serviceProperties.getStaticWebsite().getErrorDocument404Path(), staticWebsite.getErrorDocument404Path());
        assertEquals(serviceProperties.getStaticWebsite().getDefaultIndexDocumentPath(), staticWebsite.getDefaultIndexDocumentPath());
    }

    @ResourceLock("ServiceProperties")
    @Test
    public void setPropsError() {
        assertThrows(Exception.class, () -> getServiceClient(getDataLakeCredential(),
            "https://error.blob.core.windows.net").setProperties(new DataLakeServiceProperties()));
    }

    @ResourceLock("ServiceProperties")
    @Test
    public void getPropsMin() {
        assertEquals(200, primaryDataLakeServiceClient.getPropertiesWithResponse(null, null).getStatusCode());
    }

    @ResourceLock("ServiceProperties")
    @Test
    public void getPropsError() {
        assertThrows(Exception.class, () -> getServiceClient(getDataLakeCredential(),
            "https://error.blob.core.windows.net").getProperties());
    }

    @Test
    public void createFileSystemEncryptionScope() {
        FileSystemEncryptionScopeOptions encryptionScope = new FileSystemEncryptionScopeOptions()
            .setDefaultEncryptionScope(ENCRYPTION_SCOPE_STRING)
            .setEncryptionScopeOverridePrevented(true);

        DataLakeFileSystemClient fsClient = getServiceClientBuilder(getDataLakeCredential(),
            primaryDataLakeServiceClient.getAccountUrl())
            .fileSystemEncryptionScopeOptions(encryptionScope)
            .buildClient()
            .getFileSystemClient(generateFileSystemName());

        fsClient.create();
        FileSystemProperties properties = fsClient.getProperties();

        assertEquals(ENCRYPTION_SCOPE_STRING, properties.getEncryptionScope());
        assertTrue(properties.isEncryptionScopeOverridePrevented());
    }

    @Test
    public void listFileSystems() {
        for (FileSystemItem c : primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions().setPrefix(prefix), null)) {
            assertTrue(c.getName().startsWith(prefix));
            assertNotNull(c.getProperties().getLastModified());
            assertNotNull(c.getProperties().getETag());
            assertNotNull(c.getProperties().getLeaseStatus());
            assertNotNull(c.getProperties().getLeaseState());
            assertNull(c.getProperties().getLeaseDuration());
            assertNull(c.getProperties().getPublicAccess());
            assertFalse(c.getProperties().hasLegalHold());
            assertFalse(c.getProperties().hasImmutabilityPolicy());
        }
    }

    @Test
    public void listFileSystemsMin() {
        assertDoesNotThrow(() -> primaryDataLakeServiceClient.listFileSystems().iterator().hasNext());
    }

    @Test
    public void listFileSystemsMarker() {
        for (int i = 0; i < 10; i++) {
            primaryDataLakeServiceClient.createFileSystem(generateFileSystemName());
        }

        Iterator<FileSystemItem> listResponse = primaryDataLakeServiceClient.listFileSystems().iterator();
        String firstFileSystemName = listResponse.next().getName();

        // Assert that the second segment is indeed after the first alphabetically
        assertTrue(firstFileSystemName.compareTo(listResponse.next().getName()) < 0);
    }

    @Test
    public void listFileSystemsDetails() {
        Map<String, String> metadata = Collections.singletonMap("foo", "bar");
        String fileSystemName = generateFileSystemName();
        primaryDataLakeServiceClient.createFileSystemWithResponse(fileSystemName, metadata, null, null);

        assertEquals(metadata, primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions()
                .setDetails(new FileSystemListDetails().setRetrieveMetadata(true))
                .setPrefix(fileSystemName), null)
            .iterator().next().getMetadata());
    }

    @SuppressWarnings("resource")
    @Test
    public void listFileSystemsMaxResults() {
        String fileSystemName = generateFileSystemName();
        String fileSystemPrefix = fileSystemName.substring(0, Math.min(60, fileSystemName.length()));

        List<DataLakeFileSystemClient> fileSystems = new ArrayList<>();
        try {
            for (int i = 0; i < 5; i++) {
                fileSystems.add(primaryDataLakeServiceClient.createFileSystem(fileSystemPrefix + i));
            }

            assertEquals(3, primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions()
                    .setPrefix(fileSystemPrefix).setMaxResultsPerPage(3), null)
                .iterableByPage()
                .iterator()
                .next()
                .getValue()
                .size());
        } finally {
            fileSystems.forEach(DataLakeFileSystemClient::delete);
        }
    }

    @Test
    public void listFileSystemsMaxResultsByPage() {
        String fileSystemName = generateFileSystemName();
        String fileSystemPrefix = fileSystemName.substring(0, Math.min(60, fileSystemName.length()));

        List<DataLakeFileSystemClient> fileSystems = new ArrayList<>();
        try {
            for (int i = 0; i < 5; i++) {
                fileSystems.add(primaryDataLakeServiceClient.createFileSystem(fileSystemPrefix + i));
            }

            for (PagedResponse<?> page : primaryDataLakeServiceClient.listFileSystems(
                new ListFileSystemsOptions().setPrefix(fileSystemPrefix).setMaxResultsPerPage(3), null)
                .iterableByPage(3)) {
                assertTrue(page.getValue().size() <= 3);
            }
        } finally {
            fileSystems.forEach(DataLakeFileSystemClient::delete);
        }
    }

    @Test
    public void listFileSystemsError() {
        PagedIterable<FileSystemItem> items =  primaryDataLakeServiceClient.listFileSystems();

        assertThrows(DataLakeStorageException.class, () -> items.streamByPage("garbage continuation token").count());
    }

    @Test
    public void listFileSystemsWithTimeoutStillBackedByPagedFlux() {
        List<DataLakeFileSystemClient> fileSystems = new ArrayList<>();
        try {
            for (int i = 0; i < 5; i++) {
                fileSystems.add(primaryDataLakeServiceClient.createFileSystem(generateFileSystemName()));
            }

            assertDoesNotThrow(() -> primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions()
                    .setMaxResultsPerPage(3), Duration.ofSeconds(10))
                .streamByPage()
                .count());
        } finally {
            fileSystems.forEach(DataLakeFileSystemClient::delete);
        }
    }

    @ResourceLock("ServiceProperties")
    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-10-02")
    @Test
    public void listSystemFileSystems() {
        DataLakeAnalyticsLogging logging = new DataLakeAnalyticsLogging().setRead(true).setVersion("1.0")
            .setRetentionPolicy(new DataLakeRetentionPolicy().setDays(5).setEnabled(true));
        DataLakeServiceProperties serviceProps = new DataLakeServiceProperties().setLogging(logging);

        // Ensure $logs container exists. These will be reverted in test cleanup
        primaryDataLakeServiceClient.setPropertiesWithResponse(serviceProps, null, null);

        // allow the service properties to take effect
        waitUntilPredicate(1000, 30, () -> {
            DataLakeServiceProperties properties = primaryDataLakeServiceClient.getProperties();

            if (properties == null || properties.getLogging() == null) {
                return false;
            }

            DataLakeAnalyticsLogging analyticsLogging = properties.getLogging();
            return Objects.equals(analyticsLogging.isRead(), logging.isRead())
                && Objects.equals(analyticsLogging.getVersion(), logging.getVersion())
                && Objects.equals(analyticsLogging.getRetentionPolicy().getDays(), logging.getRetentionPolicy().getDays())
                && Objects.equals(analyticsLogging.getRetentionPolicy().isEnabled(), logging.getRetentionPolicy().isEnabled());
        });

        List<FileSystemItem> fileSystems = primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions()
            .setDetails(new FileSystemListDetails().setRetrieveSystemFileSystems(true)), null)
            .stream().collect(Collectors.toList());

        assertTrue(fileSystems.stream().anyMatch(item -> BlobContainerClient.LOG_CONTAINER_NAME.equals(item.getName())));
    }

    @Test
    public void listFileSystemsEncryptionScope() {
        FileSystemEncryptionScopeOptions encryptionScope = new FileSystemEncryptionScopeOptions()
            .setDefaultEncryptionScope(ENCRYPTION_SCOPE_STRING)
            .setEncryptionScopeOverridePrevented(true);

        DataLakeServiceClient serviceClient = getServiceClientBuilder(getDataLakeCredential(),
            primaryDataLakeServiceClient.getAccountUrl())
            .fileSystemEncryptionScopeOptions(encryptionScope)
            .buildClient();
        DataLakeFileSystemClient fsClient = serviceClient.getFileSystemClient(generateFileSystemName());

        fsClient.create();

        // grab the FileSystemItem that matches the name of the file system with the encryption scope
        FileSystemItemProperties properties = serviceClient.listFileSystems().stream()
            .filter(item -> Objects.equals(item.getName(), fsClient.getFileSystemName()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Expected to find file system with name " + fsClient.getFileSystemName()))
            .getProperties();

        assertEquals(ENCRYPTION_SCOPE_STRING, properties.getEncryptionScope());
        assertTrue(properties.isEncryptionScopeOverridePrevented());
    }

    @Test
    public void getUserDelegationKey() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime expiry = start.plusDays(1);

        Response<UserDelegationKey> response = getOAuthServiceClient().getUserDelegationKeyWithResponse(start, expiry, null, null);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue().getSignedObjectId());
        assertNotNull(response.getValue().getSignedTenantId());
        assertNotNull(response.getValue().getSignedStart());
        assertNotNull(response.getValue().getSignedExpiry());
        assertNotNull(response.getValue().getSignedService());
        assertNotNull(response.getValue().getSignedVersion());
        assertNotNull(response.getValue().getValue());
    }

    @Test
    public void getUserDelegationKeyMin() {
        assertEquals(200, getOAuthServiceClient()
            .getUserDelegationKeyWithResponse(null, OffsetDateTime.now().plusDays(1), null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("getUserDelegationKeyErrorSupplier")
    public void getUserDelegationKeyError(OffsetDateTime start, OffsetDateTime expiry, Class<? extends Throwable> exception) {
        assertThrows(exception, () -> getOAuthServiceClient().getUserDelegationKey(start, expiry));
    }

    private static Stream<Arguments> getUserDelegationKeyErrorSupplier() {
        return Stream.of(
            // start | expiry | exception
            Arguments.of(null, null, NullPointerException.class),
            Arguments.of(OffsetDateTime.now(), OffsetDateTime.now().minusDays(1), IllegalArgumentException.class)
        );
    }

    @Test
    public void builderBearerTokenValidation() {
        // Technically no additional checks need to be added to datalake builder since the corresponding blob builder fails
        String endpoint = BlobUrlParts.parse(primaryDataLakeServiceClient.getAccountUrl()).setScheme("http").toUrl()
            .toString();

        assertThrows(IllegalArgumentException.class, () -> new DataLakeServiceClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildClient());
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        DataLakeServiceClient serviceClient = getServiceClientBuilder(getDataLakeCredential(),
            primaryDataLakeServiceClient.getAccountUrl())
            .addPolicy(getPerCallVersionPolicy())
            .buildClient();

        Response<DataLakeFileSystemClient> response = assertDoesNotThrow(() ->
            serviceClient.createFileSystemWithResponse(generateFileSystemName(), null, null, null));
        assertEquals("2019-02-02", response.getHeaders().getValue(X_MS_VERSION));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreFileSystem() {
        DataLakeFileSystemClient cc1 = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        cc1.create();
        String blobName = generatePathName();
        cc1.getFileClient(blobName).upload(DATA.getDefaultBinaryData());
        cc1.delete();
        FileSystemItem fileSystemItem = primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions()
                .setPrefix(cc1.getFileSystemName())
                .setDetails(new FileSystemListDetails().setRetrieveDeleted(true)), null)
            .iterator().next();

        assertFalse(cc1.exists());

        // Wait until the file system is garbage collected.
        DataLakeFileSystemClient restoredContainerClient = waitUntilFileSystemIsDeleted(() ->
            primaryDataLakeServiceClient.undeleteFileSystem(fileSystemItem.getName(), fileSystemItem.getVersion()));

        List<PathItem> pathItems = restoredContainerClient.listPaths().stream().collect(Collectors.toList());
        assertEquals(1, pathItems.size());
        assertEquals(blobName, pathItems.get(0).getName());
    }

    // Restoring into an existing file system is not supported.
//    @SuppressWarnings("deprecation")
//    @Test
//    public void restoreFileSystemIntoOtherFileSystem() {
//        DataLakeFileSystemClient cc1 = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
//        cc1.create();
//        String blobName = generatePathName();
//        cc1.getFileClient(blobName).upload(DATA.getDefaultBinaryData());
//        cc1.delete();
//        FileSystemItem fileSystemItem = primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions()
//                .setPrefix(cc1.getFileSystemName())
//                .setDetails(new FileSystemListDetails().setRetrieveDeleted(true)), null)
//            .iterator().next();
//        String destinationFileSystemName = generateFileSystemName();
//
//        // Wait until the file system is garbage collected.
//        DataLakeFileSystemClient restoredContainerClient = waitUntilFileSystemIsDeleted(
//            () -> primaryDataLakeServiceClient.undeleteFileSystemWithResponse(
//                new FileSystemUndeleteOptions(fileSystemItem.getName(), fileSystemItem.getVersion())
//                    .setDestinationFileSystemName(destinationFileSystemName), null, Context.NONE))
//            .getValue();
//
//        assertEquals(destinationFileSystemName, restoredContainerClient.getFileSystemName());
//
//        List<PathItem> pathItems = restoredContainerClient.listPaths().stream().collect(Collectors.toList());
//        assertEquals(1, pathItems.size());
//        assertEquals(blobName, pathItems.get(0).getName());
//    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreFileSystemWithResponse() {
        DataLakeFileSystemClient cc1 = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        cc1.create();
        String blobName = generatePathName();
        cc1.getFileClient(blobName).upload(DATA.getDefaultBinaryData());
        cc1.delete();
        FileSystemItem fileSystemItem = primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions()
                .setPrefix(cc1.getFileSystemName())
                .setDetails(new FileSystemListDetails().setRetrieveDeleted(true)), null)
            .iterator().next();

        // Wait until the file system is garbage collected.
        Response<DataLakeFileSystemClient> response = waitUntilFileSystemIsDeleted(() ->
            primaryDataLakeServiceClient.undeleteFileSystemWithResponse(
                new FileSystemUndeleteOptions(fileSystemItem.getName(), fileSystemItem.getVersion()),
                Duration.ofMinutes(1), Context.NONE));

        assertEquals(201, response.getStatusCode());

        List<PathItem> pathItems = response.getValue().listPaths().stream().collect(Collectors.toList());
        assertEquals(1, pathItems.size());
        assertEquals(blobName, pathItems.get(0).getName());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreFileSystemAsync() {
        DataLakeFileSystemAsyncClient cc1 = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());
        String blobName = generatePathName();

        Mono<List<PathItem>> blobContainerItemMono = cc1.create()
            .then(cc1.getFileAsyncClient(blobName).upload(DATA.getDefaultFlux(), new ParallelTransferOptions()))
            .then(cc1.delete())
            .then(primaryDataLakeServiceAsyncClient.listFileSystems(new ListFileSystemsOptions()
                    .setPrefix(cc1.getFileSystemName())
                    .setDetails(new FileSystemListDetails().setRetrieveDeleted(true)))
                .next())
            .flatMap(blobContainerItem -> waitUntilFileSystemIsDeletedAsync(primaryDataLakeServiceAsyncClient
                .undeleteFileSystem(blobContainerItem.getName(), blobContainerItem.getVersion())))
            .flatMap(restoredContainerClient -> restoredContainerClient.listPaths().collectList());

        StepVerifier.create(blobContainerItemMono)
            .assertNext(pathItems -> {
                assertEquals(1, pathItems.size());
                assertEquals(blobName, pathItems.get(0).getName());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreFileSystemAsyncWithResponse() {
        DataLakeFileSystemAsyncClient cc1 = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        Mono<Response<DataLakeFileSystemAsyncClient>> blobContainerItemMono = cc1.create()
            .then(cc1.getFileAsyncClient(generatePathName()).upload(DATA.getDefaultFlux(), new ParallelTransferOptions()))
            .then(cc1.delete())
            .then(primaryDataLakeServiceAsyncClient.listFileSystems(new ListFileSystemsOptions()
                    .setPrefix(cc1.getFileSystemName())
                    .setDetails(new FileSystemListDetails().setRetrieveDeleted(true)))
                .next())
            .flatMap(blobContainerItem -> waitUntilFileSystemIsDeletedAsync(
                primaryDataLakeServiceAsyncClient.undeleteFileSystemWithResponse(
                    new FileSystemUndeleteOptions(blobContainerItem.getName(), blobContainerItem.getVersion()))));

        StepVerifier.create(blobContainerItemMono)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(201, response.getStatusCode());
                assertNotNull(response.getValue());
                assertEquals(cc1.getFileSystemName(), response.getValue().getFileSystemName());
            })
            .verifyComplete();
    }

    @Test
    public void restoreFileSystemError() {
        assertThrows(DataLakeStorageException.class, () ->
                primaryDataLakeServiceClient.undeleteFileSystem(generateFileSystemName(), "01D60F8BB59A4652"));
    }

    @SuppressWarnings("deprecation")
    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreFileSystemIntoExistingFileSystemError() {
        DataLakeFileSystemClient cc1 = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        cc1.create();
        cc1.getFileClient(generatePathName()).upload(DATA.getDefaultBinaryData());
        cc1.delete();
        FileSystemItem fileSystemItem = primaryDataLakeServiceClient.listFileSystems(
            new ListFileSystemsOptions()
                .setPrefix(cc1.getFileSystemName())
                .setDetails(new FileSystemListDetails().setRetrieveDeleted(true)),
            null).stream().findFirst()
            .orElseThrow(() -> new RuntimeException("Expected to find a deleted file system."));

        DataLakeFileSystemClient cc2 = primaryDataLakeServiceClient.createFileSystem(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, () -> waitUntilFileSystemIsDeleted(() ->
            primaryDataLakeServiceClient.undeleteFileSystemWithResponse(
                new FileSystemUndeleteOptions(fileSystemItem.getName(), fileSystemItem.getVersion())
                    .setDestinationFileSystemName(cc2.getFileSystemName()), null, Context.NONE)));
    }

    @Test
    public void setConnectionStringOnServiceClientBuilder() {
        String connectionString = ENVIRONMENT.getPrimaryAccount().getConnectionString();
        DataLakeServiceClientBuilder serviceClientBuilder = getServiceClientBuilder(getDataLakeCredential(),
            primaryDataLakeServiceClient.getAccountUrl());

        serviceClientBuilder.connectionString(connectionString);

        assertDoesNotThrow(serviceClientBuilder::buildClient);
    }

    @Test
    public void setConnectionStringWithSasOnServiceClientBuilder() {
        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true);

        String sas = primaryDataLakeServiceClient.generateAccountSas(new AccountSasSignatureValues(
            testResourceNamer.now().plusDays(1), permissions, service, resourceType));

        String connectionString = String.format("BlobEndpoint=%s;SharedAccessSignature=%s;",
            ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(), "?" + sas);

        DataLakeServiceClientBuilder serviceClientBuilder = getServiceClientBuilder(getDataLakeCredential(),
            primaryDataLakeServiceClient.getAccountUrl());

        serviceClientBuilder.connectionString(connectionString);

        assertDoesNotThrow(serviceClientBuilder::buildClient);
    }

    @Test
    public void defaultAudience() {
        DataLakeServiceClient aadServiceClient = getOAuthServiceClientBuilder()
            .audience(null) // should default to "https://storage.azure.com/"
            .buildClient();

        assertNotNull(aadServiceClient.getProperties());
    }

    @Test
    public void storageAccountAudience() {
        DataLakeServiceClient aadServiceClient = getOAuthServiceClientBuilder()
            .audience(DataLakeAudience.createDataLakeServiceAccountAudience(primaryDataLakeServiceClient.getAccountName()))
            .buildClient();

        assertNotNull(aadServiceClient.getProperties());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        DataLakeServiceClient aadServiceClient = getOAuthServiceClientBuilder()
            .audience(DataLakeAudience.createDataLakeServiceAccountAudience("badAudience"))
            .buildClient();

        assertNotNull(aadServiceClient.getProperties());
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", dataLakeFileSystemClient.getAccountName());
        DataLakeAudience audience = DataLakeAudience.fromString(url);

        DataLakeServiceClient aadServiceClient = getOAuthServiceClientBuilder()
            .audience(audience)
            .buildClient();

        assertNotNull(aadServiceClient.getProperties());
    }

//    @Test
//    public void renameFileSystem() {
//        String oldName = generateFileSystemName();
//        String newName = generateFileSystemName();
//        primaryDataLakeServiceClient.createFileSystem(oldName);
//
//        DataLakeFileSystemClient renamedContainer = primaryDataLakeServiceClient.renameFileSystem(oldName, newName);
//
//        assertEquals(200, renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode());
//    }
//
//    @Test
//    public void renameFileSystemSas() {
//        String oldName = generateFileSystemName();
//        String newName = generateFileSystemName();
//        primaryDataLakeServiceClient.createFileSystem(oldName);
//        String sas = primaryDataLakeServiceClient.generateAccountSas(new AccountSasSignatureValues(
//            testResourceNamer.now().plusHours(1), AccountSasPermission.parse("rwdxlacuptf"),
//            AccountSasService.parse("b"), AccountSasResourceType.parse("c")));
//        DataLakeServiceClient serviceClient = getServiceClient(sas, primaryDataLakeServiceClient.getAccountUrl());
//
//        DataLakeFileSystemClient renamedContainer = serviceClient.renameFileSystem(oldName, newName);
//
//        assertEquals(200, renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode());
//    }
//
//    @ParameterizedTest
//    @MethodSource("renameFileSystemACSupplier")
//    public void renameFileSystemAC(String leaseID) {
//        leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, leaseID);
//
//        assertEquals(200, primaryDataLakeServiceClient.renameFileSystemWithResponse(
//            new FileSystemRenameOptions(dataLakeFileSystemClient.getFileSystemName(), generateFileSystemName())
//                .setRequestConditions(new DataLakeRequestConditions().setLeaseId(leaseID)), null, null)
//            .getStatusCode());
//    }
//
//    private static Stream<String> renameFileSystemACSupplier() {
//        return Stream.of(null, RECEIVED_LEASE_ID);
//    }
//
//    @Test
//    public void renameFileSystemACFail() {
//        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, GARBAGE_LEASE_ID);
//
//        assertThrows(DataLakeStorageException.class, () -> primaryDataLakeServiceClient.renameFileSystemWithResponse(
//            new FileSystemRenameOptions(DataLakeFileSystemClient.getFileSystemName(), generateFileSystemName())
//                .setRequestConditions(new DataLakeRequestConditions().setLeaseId(leaseID)), null, null));
//    }
//
//    @ParameterizedTest
//    @MethodSource("renameFileSystemACIllegalSupplier")
//    public void renameFileSystemACIllegal(OffsetDateTime modified, OffsetDateTime unmodified, String match,
//        String noneMatch) {
//        DataLakeRequestConditions ac = new DataLakeRequestConditions().setIfMatch(match)
//            .setIfNoneMatch(noneMatch).setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);
//
//        assertThrows(UnsupportedOperationException.class, () -> primaryDataLakeServiceClient.renameFileSystemWithResponse(
//            new FileSystemRenameOptions(dataLakeFileSystemClient.getFileSystemName(), generateFileSystemName())
//                .setRequestConditions(ac), null, null));
//    }
//
//    private static Stream<Arguments> renameFileSystemACIllegalSupplier() {
//        return Stream.of(
//            Arguments.of(OLD_DATE, null, null, null),
//            Arguments.of(null, NEW_DATE, null, null),
//            Arguments.of(null, null, RECEIVED_ETAG, null),
//            Arguments.of(null, null, null, GARBAGE_ETAG)
//        );
//    }
//
//    @Test
//    public void renameFileSystemError() {
//        assertThrows(DataLakeStorageException.class, () ->
//                primaryDataLakeServiceClient.renameFileSystem(generateFileSystemName(), generateFileSystemName()));
//    }

    private static <T> T waitUntilFileSystemIsDeleted(
        Callable<T> waitUntilOperation) {
        for (int i = 0; i < 30; i++) {
            try {
                return waitUntilOperation.call();
            } catch (Exception ex) {
                if (ex instanceof DataLakeStorageException && ((DataLakeStorageException) ex).getStatusCode() == 409) {
                    // Only repeat if the exception was a service exception and the status code was 409.
                    sleepIfLiveTesting(1000);
                } else if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                } else {
                    throw new RuntimeException(ex);
                }
            }
        }

        try {
            return waitUntilOperation.call();
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new RuntimeException(ex);
            }
        }
    }

    private static <T> Mono<T> waitUntilFileSystemIsDeletedAsync(Mono<T> waitUntilOperation) {
        Predicate<Throwable> retryPredicate = ex ->
            ex instanceof DataLakeStorageException && ((DataLakeStorageException) ex).getStatusCode() == 409;

        Retry retry = ENVIRONMENT.getTestMode() == TestMode.PLAYBACK
            ? Retry.max(30).filter(retryPredicate)
            : Retry.fixedDelay(30, Duration.ofMillis(1000)).filter(retryPredicate);

        return waitUntilOperation.retryWhen(retry);
    }
}
