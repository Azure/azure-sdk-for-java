// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
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
import com.azure.storage.file.datalake.models.ListFileSystemsOptions;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.options.FileSystemEncryptionScopeOptions;
import com.azure.storage.file.datalake.options.FileSystemUndeleteOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

public class ServiceAsyncApiTests extends DataLakeTestBase {
    private static void validatePropsSet(DataLakeServiceProperties sent, DataLakeServiceProperties received) {
        assertEquals(sent.getLogging().isRead(), received.getLogging().isRead());
        assertEquals(sent.getLogging().isWrite(), received.getLogging().isWrite());
        assertEquals(sent.getLogging().isDelete(), received.getLogging().isDelete());
        assertEquals(sent.getLogging().getRetentionPolicy().isEnabled(),
            received.getLogging().getRetentionPolicy().isEnabled());
        assertEquals(sent.getLogging().getRetentionPolicy().getDays(),
            received.getLogging().getRetentionPolicy().getDays());
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
        assertEquals(sent.getHourMetrics().getRetentionPolicy().isEnabled(),
            received.getHourMetrics().getRetentionPolicy().isEnabled());
        assertEquals(sent.getHourMetrics().getRetentionPolicy().getDays(),
            received.getHourMetrics().getRetentionPolicy().getDays());
        assertEquals(sent.getHourMetrics().getVersion(), received.getHourMetrics().getVersion());

        assertEquals(sent.getMinuteMetrics().isEnabled(), received.getMinuteMetrics().isEnabled());
        assertEquals(sent.getMinuteMetrics().isIncludeApis(), received.getMinuteMetrics().isIncludeApis());
        assertEquals(sent.getMinuteMetrics().getRetentionPolicy().isEnabled(),
            received.getMinuteMetrics().getRetentionPolicy().isEnabled());
        assertEquals(sent.getMinuteMetrics().getRetentionPolicy().getDays(),
            received.getMinuteMetrics().getRetentionPolicy().getDays());
        assertEquals(sent.getMinuteMetrics().getVersion(), received.getMinuteMetrics().getVersion());

        assertEquals(sent.getDeleteRetentionPolicy().isEnabled(), received.getDeleteRetentionPolicy().isEnabled());
        assertEquals(sent.getDeleteRetentionPolicy().getDays(), received.getDeleteRetentionPolicy().getDays());

        assertEquals(sent.getStaticWebsite().isEnabled(), received.getStaticWebsite().isEnabled());
        assertEquals(sent.getStaticWebsite().getIndexDocument(), received.getStaticWebsite().getIndexDocument());
        assertEquals(sent.getStaticWebsite().getErrorDocument404Path(),
            received.getStaticWebsite().getErrorDocument404Path());
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

                StepVerifier.create(primaryDataLakeServiceAsyncClient.setPropertiesWithResponse(sentProperties))
                    .assertNext(p -> {
                        assertNotNull(p.getHeaders().getValue(X_MS_REQUEST_ID));
                        assertNotNull(p.getHeaders().getValue(X_MS_VERSION));
                    })
                    .verifyComplete();

                // Service properties may take up to 30s to take effect. If they weren't already in place, wait.
                sleepIfRunningAgainstService(30 * 1000);

                StepVerifier.create(primaryDataLakeServiceAsyncClient.getProperties())
                    .assertNext(p -> validatePropsSet(sentProperties, p))
                    .verifyComplete();

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

        assertAsyncResponseStatusCode(primaryDataLakeServiceAsyncClient.setPropertiesWithResponse(sentProperties),
            202);
    }

    @ResourceLock("ServiceProperties")
    @Test
    public void setPropsCorsCheck() {
        Mono<Response<Void>> response = primaryDataLakeServiceAsyncClient.getProperties()
            .flatMap(r -> {
                // Some properties are not set and this test validates that they are not null when sent to the service
                r.setCors(Collections.singletonList(new DataLakeCorsRule().setAllowedOrigins("microsoft.com")
                        .setMaxAgeInSeconds(60)
                        .setAllowedMethods("GET")
                        .setAllowedHeaders("x-ms-version")));
                return primaryDataLakeServiceAsyncClient.setPropertiesWithResponse(r);
            });

        assertAsyncResponseStatusCode(response, 202);
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @ResourceLock("ServiceProperties")
    @Test
    public void setPropsStaticWebsite() {
        Mono<DataLakeServiceProperties> serviceProperties = primaryDataLakeServiceAsyncClient.getProperties()
            .flatMap(r -> Mono.just(r.setStaticWebsite(new DataLakeStaticWebsite()
                    .setEnabled(true)
                    .setErrorDocument404Path("error/404.html")
                    .setDefaultIndexDocumentPath("index.html"))));

        Mono<Response<Void>> response1 = serviceProperties.flatMap(r -> primaryDataLakeServiceAsyncClient
            .setPropertiesWithResponse(r));

        assertAsyncResponseStatusCode(response1, 202);

        StepVerifier.create(Mono.zip(primaryDataLakeServiceAsyncClient.getProperties(), serviceProperties))
            .assertNext(p -> {
                assertTrue(p.getT1().getStaticWebsite().isEnabled());
                assertEquals(p.getT2().getStaticWebsite().getErrorDocument404Path(),
                    p.getT1().getStaticWebsite().getErrorDocument404Path());
                assertEquals(p.getT2().getStaticWebsite().getDefaultIndexDocumentPath(),
                    p.getT1().getStaticWebsite().getDefaultIndexDocumentPath());
            })
            .verifyComplete();
    }

    @ResourceLock("ServiceProperties")
    @Test
    public void setPropsError() {
        StepVerifier.create(getServiceAsyncClient(getDataLakeCredential(),
            "https://error.blob.core.windows.net").setProperties(new DataLakeServiceProperties()))
            .verifyError(Exception.class);
    }

    @ResourceLock("ServiceProperties")
    @Test
    public void getPropsMin() {
        assertAsyncResponseStatusCode(primaryDataLakeServiceAsyncClient.getPropertiesWithResponse(), 200);
    }

    @ResourceLock("ServiceProperties")
    @Test
    public void getPropsError() {
        StepVerifier.create(getServiceAsyncClient(getDataLakeCredential(),
            "https://error.blob.core.windows.net").getProperties())
            .verifyError(Exception.class);
    }

    @Test
    public void createFileSystemEncryptionScope() {
        FileSystemEncryptionScopeOptions encryptionScope = new FileSystemEncryptionScopeOptions()
            .setDefaultEncryptionScope(ENCRYPTION_SCOPE_STRING)
            .setEncryptionScopeOverridePrevented(true);

        DataLakeFileSystemAsyncClient fsClient = getServiceClientBuilder(getDataLakeCredential(),
            primaryDataLakeServiceAsyncClient.getAccountUrl())
            .fileSystemEncryptionScopeOptions(encryptionScope)
            .buildAsyncClient()
            .getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(fsClient.create().then(fsClient.getProperties()))
            .assertNext(p -> {
                assertEquals(ENCRYPTION_SCOPE_STRING, p.getEncryptionScope());
                assertTrue(p.isEncryptionScopeOverridePrevented());
            })
            .verifyComplete();
    }

    @Test
    public void listFileSystems() {
        StepVerifier.create(primaryDataLakeServiceAsyncClient.listFileSystems(new ListFileSystemsOptions().setPrefix(prefix)))
            .thenConsumeWhile(c -> {
                assertTrue(c.getName().startsWith(prefix));
                assertNotNull(c.getProperties().getLastModified());
                assertNotNull(c.getProperties().getETag());
                assertNotNull(c.getProperties().getLeaseStatus());
                assertNotNull(c.getProperties().getLeaseState());
                assertNull(c.getProperties().getLeaseDuration());
                assertNull(c.getProperties().getPublicAccess());
                assertFalse(c.getProperties().hasLegalHold());
                assertFalse(c.getProperties().hasImmutabilityPolicy());
                return true;
            })
            .verifyComplete();
    }

    @Test
    public void listFileSystemsMin() {
        StepVerifier.create(primaryDataLakeServiceAsyncClient.listFileSystems())
            .thenConsumeWhile(r -> {
                assertNotNull(r);
                return true;
            })
            .verifyComplete();
    }

    @Test
    public void listFileSystemsMarker() {
        Mono<List<FileSystemItem>> step = Mono.defer(() -> primaryDataLakeServiceAsyncClient.createFileSystem(generateFileSystemName()))
            .repeat(10)
            .then(Mono.just(primaryDataLakeServiceAsyncClient.listFileSystems().toStream().collect(Collectors.toList())));

        StepVerifier.create(step)
            .assertNext(r -> {
                String firstFileSystemName = r.get(0).getName();
                String secondName = r.get(1).getName();
                assertTrue(firstFileSystemName.compareTo(secondName) < 0);
            })
            .verifyComplete();
    }

    @Test
    public void listFileSystemsDetails() {
        Map<String, String> metadata = Collections.singletonMap("foo", "bar");
        String fileSystemName = generateFileSystemName();

        StepVerifier.create(primaryDataLakeServiceAsyncClient.createFileSystemWithResponse(fileSystemName, metadata,
            null)
            .thenMany(primaryDataLakeServiceAsyncClient.listFileSystems(new ListFileSystemsOptions()
                .setDetails(new FileSystemListDetails().setRetrieveMetadata(true))
                .setPrefix(fileSystemName))))
            .assertNext(r -> assertEquals(metadata, r.getMetadata()))
            .verifyComplete();
    }

    @SuppressWarnings("resource")
    @Test
    public void listFileSystemsMaxResults() {
        String fileSystemName = generateFileSystemName();
        String fileSystemPrefix = fileSystemName.substring(0, Math.min(60, fileSystemName.length()));

        List<DataLakeFileSystemAsyncClient> fileSystems = new ArrayList<>();
        try {
            Flux<PagedResponse<FileSystemItem>> response = primaryDataLakeServiceAsyncClient
                .createFileSystem(fileSystemPrefix + 0)
                .flatMap(r -> {
                    fileSystems.add(r);
                    return Mono.just(r);
                })
                .then(primaryDataLakeServiceAsyncClient.createFileSystem(fileSystemPrefix + 1))
                    .flatMap(r -> {
                        fileSystems.add(r);
                        return Mono.just(r);
                    })
                .then(primaryDataLakeServiceAsyncClient.createFileSystem(fileSystemPrefix + 2))
                    .flatMap(r -> {
                        fileSystems.add(r);
                        return Mono.just(r);
                    })
                .then(primaryDataLakeServiceAsyncClient.createFileSystem(fileSystemPrefix + 3))
                    .flatMap(r -> {
                        fileSystems.add(r);
                        return Mono.just(r);
                    })
                .then(primaryDataLakeServiceAsyncClient.createFileSystem(fileSystemPrefix + 4))
                    .flatMap(r -> {
                        fileSystems.add(r);
                        return Mono.just(r);
                    })
                .thenMany(primaryDataLakeServiceAsyncClient.listFileSystems(new ListFileSystemsOptions()
                    .setPrefix(fileSystemPrefix).setMaxResultsPerPage(3)).byPage());

            StepVerifier.create(response)
                .assertNext(r -> assertEquals(3, r.getValue().size()))
                .expectNextCount(1)
                .verifyComplete();
        } finally {
            fileSystems.forEach(r -> r.delete().block());
        }
    }

    @Test
    public void listFileSystemsMaxResultsByPage() {
        String fileSystemName = generateFileSystemName();
        String fileSystemPrefix = fileSystemName.substring(0, Math.min(60, fileSystemName.length()));

        List<DataLakeFileSystemAsyncClient> fileSystems = new ArrayList<>();
        try {
            Flux<PagedResponse<FileSystemItem>> response = primaryDataLakeServiceAsyncClient
                .createFileSystem(fileSystemPrefix + 0)
                .flatMap(r -> {
                    fileSystems.add(r);
                    return Mono.just(r);
                })
                .then(primaryDataLakeServiceAsyncClient.createFileSystem(fileSystemPrefix + 1))
                .flatMap(r -> {
                    fileSystems.add(r);
                    return Mono.just(r);
                })
                .then(primaryDataLakeServiceAsyncClient.createFileSystem(fileSystemPrefix + 2))
                .flatMap(r -> {
                    fileSystems.add(r);
                    return Mono.just(r);
                })
                .then(primaryDataLakeServiceAsyncClient.createFileSystem(fileSystemPrefix + 3))
                .flatMap(r -> {
                    fileSystems.add(r);
                    return Mono.just(r);
                })
                .then(primaryDataLakeServiceAsyncClient.createFileSystem(fileSystemPrefix + 4))
                .flatMap(r -> {
                    fileSystems.add(r);
                    return Mono.just(r);
                })
                .thenMany(primaryDataLakeServiceAsyncClient.listFileSystems(new ListFileSystemsOptions()
                    .setPrefix(fileSystemPrefix).setMaxResultsPerPage(3)).byPage(3));

            StepVerifier.create(response)
                .thenConsumeWhile(r -> {
                    assertTrue(r.getValue().size() <= 3);
                    return true;
                })
                .verifyComplete();
        } finally {
            fileSystems.forEach(r -> r.delete().block());
        }
    }

    @ResourceLock("ServiceProperties")
    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-10-02")
    @Test
    public void listSystemFileSystems() {
        //todo isbr remove blocking
        DataLakeAnalyticsLogging logging = new DataLakeAnalyticsLogging().setRead(true).setVersion("1.0")
            .setRetentionPolicy(new DataLakeRetentionPolicy().setDays(5).setEnabled(true));
        DataLakeServiceProperties serviceProps = new DataLakeServiceProperties().setLogging(logging);

        // Ensure $logs container exists. These will be reverted in test cleanup
        primaryDataLakeServiceAsyncClient.setPropertiesWithResponse(serviceProps).block();

        // allow the service properties to take effect
        waitUntilPredicate(1000, 30, () -> {
            DataLakeServiceProperties properties = primaryDataLakeServiceAsyncClient.getProperties().block();

            if (properties == null || properties.getLogging() == null) {
                return false;
            }

            DataLakeAnalyticsLogging analyticsLogging = properties.getLogging();
            return Objects.equals(analyticsLogging.isRead(), logging.isRead())
                && Objects.equals(analyticsLogging.getVersion(), logging.getVersion())
                && Objects.equals(analyticsLogging.getRetentionPolicy().getDays(), logging.getRetentionPolicy().getDays())
                && Objects.equals(analyticsLogging.getRetentionPolicy().isEnabled(), logging.getRetentionPolicy().isEnabled());
        });

        List<FileSystemItem> fileSystems = primaryDataLakeServiceAsyncClient.listFileSystems(new ListFileSystemsOptions()
                .setDetails(new FileSystemListDetails().setRetrieveSystemFileSystems(true)))
            .toStream().collect(Collectors.toList());

        assertTrue(fileSystems.stream().anyMatch(item -> BlobContainerClient.LOG_CONTAINER_NAME.equals(item.getName())));
    }

    @Test
    public void listFileSystemsEncryptionScope() {
        //todo isbr remove blocking
        FileSystemEncryptionScopeOptions encryptionScope = new FileSystemEncryptionScopeOptions()
            .setDefaultEncryptionScope(ENCRYPTION_SCOPE_STRING)
            .setEncryptionScopeOverridePrevented(true);

        DataLakeServiceAsyncClient serviceClient = getServiceClientBuilder(getDataLakeCredential(),
            primaryDataLakeServiceAsyncClient.getAccountUrl())
            .fileSystemEncryptionScopeOptions(encryptionScope)
            .buildAsyncClient();
        DataLakeFileSystemAsyncClient fsClient = serviceClient.getFileSystemAsyncClient(generateFileSystemName());

        fsClient.create().block();

        // grab the FileSystemItem that matches the name of the file system with the encryption scope
        FileSystemItemProperties properties = serviceClient.listFileSystems().toStream()
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

        StepVerifier.create(getOAuthServiceAsyncClient().getUserDelegationKeyWithResponse(start, expiry))
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
                assertNotNull(r.getValue().getSignedObjectId());
                assertNotNull(r.getValue().getSignedTenantId());
                assertNotNull(r.getValue().getSignedStart());
                assertNotNull(r.getValue().getSignedExpiry());
                assertNotNull(r.getValue().getSignedService());
                assertNotNull(r.getValue().getSignedVersion());
                assertNotNull(r.getValue().getValue());
            })
            .verifyComplete();
    }

    @Test
    public void getUserDelegationKeyMin() {
        assertAsyncResponseStatusCode(getOAuthServiceAsyncClient()
            .getUserDelegationKeyWithResponse(null, OffsetDateTime.now().plusDays(1)), 200);
    }

    @ParameterizedTest
    @MethodSource("getUserDelegationKeyErrorSupplier")
    public void getUserDelegationKeyError(OffsetDateTime start, OffsetDateTime expiry, Class<? extends Throwable> exception) {
        StepVerifier.create(getOAuthServiceAsyncClient().getUserDelegationKey(start, expiry))
            .verifyError(exception);
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
        String endpoint = BlobUrlParts.parse(primaryDataLakeServiceAsyncClient.getAccountUrl()).setScheme("http").toUrl()
            .toString();

        assertThrows(IllegalArgumentException.class, () -> new DataLakeServiceClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildAsyncClient());
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        DataLakeServiceAsyncClient serviceClient = getServiceClientBuilder(getDataLakeCredential(),
            primaryDataLakeServiceAsyncClient.getAccountUrl())
            .addPolicy(getPerCallVersionPolicy())
            .buildAsyncClient();

        StepVerifier.create(serviceClient.createFileSystemWithResponse(generateFileSystemName(), null, null))
            .assertNext(r -> assertEquals("2019-02-02", r.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreFileSystem() {
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
    public void restoreFileSystemWithResponse() {
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
        StepVerifier.create(primaryDataLakeServiceAsyncClient.undeleteFileSystem(generateFileSystemName(),
            "01D60F8BB59A4652"))
            .verifyError(DataLakeStorageException.class);
    }

    @SuppressWarnings("deprecation")
    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreFileSystemIntoExistingFileSystemError() {
        DataLakeFileSystemAsyncClient cc1 = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        Mono<Response<DataLakeFileSystemAsyncClient>> blobContainerItemMono = cc1.create()
            .then(cc1.getFileAsyncClient(generatePathName()).upload(DATA.getDefaultBinaryData(), new ParallelTransferOptions()))
            .then(cc1.delete())
            .then(Mono.zip(primaryDataLakeServiceAsyncClient.listFileSystems(new ListFileSystemsOptions()
                    .setPrefix(cc1.getFileSystemName())
                    .setDetails(new FileSystemListDetails().setRetrieveDeleted(true)))
                .next(), primaryDataLakeServiceAsyncClient.createFileSystem(generateFileSystemName())))
            .flatMap(tuple -> waitUntilFileSystemIsDeletedAsync(primaryDataLakeServiceAsyncClient
                .undeleteFileSystemWithResponse(
                    new FileSystemUndeleteOptions(tuple.getT1().getName(), tuple.getT1().getVersion())
                        .setDestinationFileSystemName(tuple.getT2().getFileSystemName()))));

        StepVerifier.create(blobContainerItemMono)
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setConnectionStringOnServiceClientBuilder() {
        String connectionString = ENVIRONMENT.getPrimaryAccount().getConnectionString();
        DataLakeServiceClientBuilder serviceClientBuilder = getServiceClientBuilder(getDataLakeCredential(),
            primaryDataLakeServiceAsyncClient.getAccountUrl());

        serviceClientBuilder.connectionString(connectionString);

        assertDoesNotThrow(serviceClientBuilder::buildAsyncClient);
    }

    @Test
    public void setConnectionStringWithSasOnServiceClientBuilder() {
        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true);

        String sas = primaryDataLakeServiceAsyncClient.generateAccountSas(new AccountSasSignatureValues(
            testResourceNamer.now().plusDays(1), permissions, service, resourceType));

        String connectionString = String.format("BlobEndpoint=%s;SharedAccessSignature=%s;",
            ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(), "?" + sas);

        DataLakeServiceClientBuilder serviceClientBuilder = getServiceClientBuilder(getDataLakeCredential(),
            primaryDataLakeServiceAsyncClient.getAccountUrl());

        serviceClientBuilder.connectionString(connectionString);

        assertDoesNotThrow(serviceClientBuilder::buildAsyncClient);
    }

    private static <T> Mono<T> waitUntilFileSystemIsDeletedAsync(Mono<T> waitUntilOperation) {
        Predicate<Throwable> retryPredicate = ex ->
            ex instanceof DataLakeStorageException && ((DataLakeStorageException) ex).getStatusCode() == 409;

        Retry retry = ENVIRONMENT.getTestMode() == TestMode.PLAYBACK
            ? Retry.max(30).filter(retryPredicate)
            : Retry.fixedDelay(30, Duration.ofMillis(1000)).filter(retryPredicate);

        return waitUntilOperation.retryWhen(retry);
    }

    @Test
    public void defaultAudience() {
        DataLakeServiceAsyncClient aadServiceClient = getOAuthServiceClientBuilder()
            .audience(null) // should default to "https://storage.azure.com/"
            .buildAsyncClient();

        StepVerifier.create(aadServiceClient.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void storageAccountAudience() {
        DataLakeServiceAsyncClient aadServiceClient = getOAuthServiceClientBuilder()
            .audience(DataLakeAudience.createDataLakeServiceAccountAudience(primaryDataLakeServiceAsyncClient.getAccountName()))
            .buildAsyncClient();

        StepVerifier.create(aadServiceClient.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        DataLakeServiceAsyncClient aadServiceClient = getOAuthServiceClientBuilder()
            .audience(DataLakeAudience.createDataLakeServiceAccountAudience("badAudience"))
            .buildAsyncClient();

        StepVerifier.create(aadServiceClient.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", dataLakeFileSystemAsyncClient.getAccountName());
        DataLakeAudience audience = DataLakeAudience.fromString(url);

        DataLakeServiceAsyncClient aadServiceClient = getOAuthServiceClientBuilder()
            .audience(audience)
            .buildAsyncClient();

        StepVerifier.create(aadServiceClient.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

}
