package com.azure.storage.file.datalake;

import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathScheduleDeletionOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileAsyncApiTest extends DataLakeTestBase{
    private DataLakeFileAsyncClient fc;
    private final List<File> createdFiles = new ArrayList<>();

    @BeforeEach
    public void setup() {
        fc = dataLakeFileSystemAsyncClient.createFile(generatePathName()).block();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterEach
    public void cleanup() {
        createdFiles.forEach(File::delete);
    }


    @Test
    public void createMin() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.create())
            .assertNext(r -> assertNotEquals(null, r))
            .verifyComplete();
    }

    @Test
    public void createDefaults() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.createWithResponse(null, null, null, null, null))
            .assertNext(r -> assertEquals(201, r.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void createError() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.createWithResponse(null, null, null, null, new DataLakeRequestConditions().setIfMatch("garbage")))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void createOverwrite() {
        fc = dataLakeFileSystemAsyncClient.createFile(generatePathName()).block();

        StepVerifier.create(fc.create(false))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void exists() {
        fc = dataLakeFileSystemAsyncClient.createFile(generatePathName()).block();

        StepVerifier.create(fc.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void doesNotExist() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.exists())
            .expectNext(false)
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,null", "control,disposition,encoding,language,type"})
    public void createHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                              String contentLanguage, String contentType) {
        // Create does not set md5
        PathHttpHeaders headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType);

        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        fc.createWithResponse(null, null, headers, null, null).block();

        String finalContentType = contentType;
        StepVerifier.create(fc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                assertEquals(cacheControl, r.getValue().getCacheControl());
                assertEquals(contentDisposition, r.getValue().getContentDisposition());
                assertEquals(contentEncoding, r.getValue().getContentEncoding());
                assertEquals(contentLanguage, r.getValue().getContentLanguage());
                assertEquals(finalContentType, r.getValue().getContentType());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }
        fc.createWithResponse(null, null, null, metadata, null).block();

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> assertEquals(metadata, r.getMetadata()))
            .verifyComplete();
    }

    private static boolean olderThan20210410ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2021_04_10);
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void createEncryptionContext() {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());
        dataLakeFileSystemAsyncClient.create().block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName()).create().block();
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        // testing encryption context with create()
        String encryptionContext = "encryptionContext";
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setEncryptionContext(encryptionContext);
        fc.createWithResponse(options, Context.NONE).block();

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> assertEquals(encryptionContext, r.getEncryptionContext()))
            .verifyComplete();

        StepVerifier.create(fc.readWithResponse(null, null, null, false))
            .assertNext(r -> assertEquals(encryptionContext, r.getDeserializedHeaders().getEncryptionContext()));

        // testing encryption context with listPaths()
        StepVerifier.create(dataLakeFileSystemAsyncClient.listPaths(new ListPathsOptions().setRecursive(true)))
            .expectNextCount(1)
            .assertNext(r -> assertEquals(encryptionContext, r.getEncryptionContext()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void createAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                         String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(fc.createWithResponse(null, null, null, null, drc))
            .assertNext(r -> assertEquals(201, r.getStatusCode()))
            .verifyComplete();
    }

    private static Stream<Arguments> modifiedMatchAndLeaseIdSupplier() {
        return Stream.of(
            // modified | unmodified | match        | noneMatch   | leaseID
            Arguments.of(null, null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, null, RECEIVED_LEASE_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void createACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID) {
        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(fc.createWithResponse(null, null, null, null, drc))
            .verifyError(DataLakeStorageException.class);
    }

    private static Stream<Arguments> invalidModifiedMatchAndLeaseIdSupplier() {
        return Stream.of(
            // modified | unmodified | match        | noneMatch   | leaseID
            Arguments.of(NEW_DATE, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, null, GARBAGE_LEASE_ID)
        );
    }

    @Test
    public void createPermissionsAndUmask() {
        StepVerifier.create(fc.createWithResponse("0777", "0057", null, null, null))
            .assertNext(r -> assertEquals(201, r.getStatusCode()))
            .verifyComplete();
    }

    private static boolean olderThan20201206ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2020_12_06);
    }

    @DisabledIf("olderThan20201206ServiceVersion")
    @Test
    public void createOptionsWithACL() {
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);

        fc.createWithResponse(options, null).block();

        StepVerifier.create(fc.getAccessControl())
            .assertNext(r -> {
                    assertEquals(pathAccessControlEntries.get(0), r.getAccessControlList().get(0)); // testing if owner is set the same
                    assertEquals(pathAccessControlEntries.get(1), r.getAccessControlList().get(1)); // testing if owner is set the same
            })
            .verifyComplete();
    }

    @DisabledIf("olderThan20201206ServiceVersion")
    @Test
    public void createOptionsWithOwnerAndGroup() {
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setOwner(ownerName).setGroup(groupName);

        fc.createWithResponse(options, null).block();

        StepVerifier.create(fc.getAccessControl())
            .assertNext(r -> {
                assertEquals(ownerName, r.getOwner());
                assertEquals(groupName, r.getGroup());
            })
            .verifyComplete();
    }

    @Test
    public void createOptionsWithNullOwnerAndGroup() {
        fc.createWithResponse(null, null);

        StepVerifier.create(fc.getAccessControl())
            .assertNext(r -> {
                assertEquals("$superuser", r.getOwner());
                assertEquals("$superuser", r.getGroup());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,null,application/octet-stream", "control,disposition,encoding,language,null,type"},
        nullValues = "null")
    public void createOptionsWithPathHttpHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                                                 String contentLanguage, byte[] contentMD5, String contentType) {
        PathHttpHeaders putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPathHttpHeaders(putHeaders);

        StepVerifier.create(fc.createWithResponse(options, null))
                .assertNext(r -> assertEquals(201, r.getStatusCode()))
                    .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createOptionsWithMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setMetadata(metadata);

        StepVerifier.create(fc.createWithResponse(options, null))
            .assertNext(r -> assertEquals(201, r.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> {
                for (String k : metadata.keySet()) {
                    assertTrue(r.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), r.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @Test
    public void createOptionsWithPermissionsAndUmask() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057");

        fc.createWithResponse(options, null).block();

        StepVerifier.create(fc.getAccessControlWithResponse(true, null, null))
            .assertNext(r -> assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(), r.getValue().getPermissions().toString()))
            .verifyComplete();
    }

    @DisabledIf("olderThan20201206ServiceVersion")
    @Test
    public void createIfNotExistsOptionsWithLeaseId() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId).setLeaseDuration(15);

        StepVerifier.create(fc.createIfNotExistsWithResponse(options, null))
            .assertNext(r -> assertEquals(201, r.getStatusCode()))
            .verifyComplete();
    }

    @DisabledIf("olderThan20201206ServiceVersion")
    @Test
    public void createOptionsWithLeaseId() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId).setLeaseDuration(15);

        StepVerifier.create(fc.createWithResponse(options, null))
            .assertNext(r -> assertEquals(201, r.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void createOptionsWithLeaseIdError() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId);

        // lease duration must also be set, or else exception is thrown
        StepVerifier.create(fc.createWithResponse(options, null))
            .verifyError(DataLakeStorageException.class);
    }

    @DisabledIf("olderThan20201206ServiceVersion")
    @Test
    public void createOptionsWithLeaseDuration() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setLeaseDuration(15).setProposedLeaseId(leaseId);

        StepVerifier.create(fc.createWithResponse(options, null))
            .assertNext(r -> assertEquals(201, r.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> {
                assertEquals(LeaseStatusType.LOCKED, r.getLeaseStatus());
                assertEquals(LeaseStateType.LEASED, r.getLeaseState());
                assertEquals(LeaseDurationType.FIXED, r.getLeaseDuration());
            })
            .verifyComplete();
    }

    @DisabledIf("olderThan20201206ServiceVersion")
    @ParameterizedTest
    @MethodSource("timeExpiresOnOptionsSupplier")
    public void createOptionsWithTimeExpiresOn(DataLakePathScheduleDeletionOptions deletionOptions) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setScheduleDeletionOptions(deletionOptions);

        StepVerifier.create(fc.createWithResponse(options, null))
            .assertNext(r -> assertEquals(201, r.getStatusCode()))
            .verifyComplete();
    }

    private static Stream<DataLakePathScheduleDeletionOptions> timeExpiresOnOptionsSupplier() {
        return Stream.of(new DataLakePathScheduleDeletionOptions(OffsetDateTime.now().plusDays(1)), null);
    }

    @DisabledIf("olderThan20201206ServiceVersion")
    @Test
    public void createOptionsWithTimeToExpireRelativeToNow() {
        DataLakePathScheduleDeletionOptions deletionOptions = new DataLakePathScheduleDeletionOptions(Duration.ofDays(6));
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(deletionOptions);

        StepVerifier.create(fc.createWithResponse(options, null))
            .assertNext(r -> assertEquals(201, r.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> compareDatesWithPrecision(r.getExpiresOn(), r.getCreationTime().plusDays(6)))
            .verifyComplete();
    }
}
