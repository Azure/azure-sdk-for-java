// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobLeaseRequestConditions;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.LeaseDurationType;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.options.BlobAcquireLeaseOptions;
import com.azure.storage.blob.options.BlobBreakLeaseOptions;
import com.azure.storage.blob.options.BlobChangeLeaseOptions;
import com.azure.storage.blob.options.BlobReleaseLeaseOptions;
import com.azure.storage.blob.options.BlobRenewLeaseOptions;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LeaseAsyncApiTests extends BlobTestBase {

    private BlobAsyncClientBase createBlobAsyncClient() {
        BlockBlobAsyncClient bc = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bc.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
        return bc;
    }

    @ParameterizedTest
    @MethodSource("acquireBlobLeaseSupplier")
    public void acquireBlobLease(String proposedID, int leaseTime, LeaseStateType leaseState,
                                 LeaseDurationType leaseDuration) {
        BlobAsyncClientBase bc = createBlobAsyncClient();
        BlobLeaseAsyncClient leaseClient = createLeaseAsyncClient(bc, proposedID);

        if (proposedID != null) {
            assertEquals(leaseClient.getLeaseId(), proposedID);
        }

        StepVerifier.create(leaseClient.acquireLease(leaseTime))
            .assertNext(r -> {
                assertNotNull(r);
                assertEquals(leaseClient.getLeaseId(), r);
            })
            .verifyComplete();

        StepVerifier.create(bc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                BlobProperties properties = r.getValue();
                HttpHeaders headers = r.getHeaders();

                assertEquals(leaseState, properties.getLeaseState());
                assertEquals(leaseDuration, properties.getLeaseDuration());
                assertTrue(validateBasicHeaders(headers));
            })
            .verifyComplete();
    }

    private static Stream<Arguments> acquireBlobLeaseSupplier() {
        return Stream.of(
            Arguments.of(null, -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE),
            Arguments.of(null, 25, LeaseStateType.LEASED, LeaseDurationType.FIXED),
            Arguments.of(CoreUtils.randomUuid().toString(), -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE)
        );
    }

    @Test
    public void acquireBlobLeaseMin() {
        assertAsyncResponseStatusCode(createLeaseAsyncClient(createBlobAsyncClient()).acquireLeaseWithResponse(
            new BlobAcquireLeaseOptions(-1)), 201);
    }

    @ParameterizedTest
    @ValueSource(ints = { -10, 10, 70 })
    public void acquireBlobLeaseDurationFail(int duration) {
        BlobLeaseAsyncClient leaseClient = createLeaseAsyncClient(createBlobAsyncClient());
        StepVerifier.create(leaseClient.acquireLease(duration))
                .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("acquireBlobLeaseDurationFailSupplier")
    public void acquireBlobLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                   String tags) {
        BlobAsyncClientBase bc = createBlobAsyncClient();
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<String>> response = bc.setTags(t)
            .then(setupBlobMatchCondition(bc, match))
            .flatMap(r -> {
                String newMatch = r;
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setTagsConditions(tags);

                return createLeaseAsyncClient(bc).acquireLeaseWithResponse(
                    new BlobAcquireLeaseOptions(-1).setRequestConditions(mac));
            });

        assertAsyncResponseStatusCode(response, 201);
    }

    private static Stream<Arguments> acquireBlobLeaseDurationFailSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, null, "\"foo\" = 'bar'"));
    }

    @ParameterizedTest
    @MethodSource("acquireBlobLeaseACFailSupplier")
    public void acquireBlobLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                       String noneMatch, String tags) {
        BlobAsyncClientBase bc = createBlobAsyncClient();

        Mono<Response<String>> response = setupBlobMatchCondition(bc, noneMatch)
            .flatMap(r -> {
                String newNoneMatch = r;
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setTagsConditions(tags);

                return createLeaseAsyncClient(bc).acquireLeaseWithResponse(
                    new BlobAcquireLeaseOptions(-1).setRequestConditions(mac));
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    private static Stream<Arguments> acquireBlobLeaseACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, null, "\"notfoo\" = 'notbar'"));
    }

    @Test
    public void acquireBlobLeaseError() {
        BlockBlobAsyncClient bc = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        StepVerifier.create(createLeaseAsyncClient(bc).acquireLease(20))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void renewBlobLease() {
        BlobAsyncClientBase bc = createBlobAsyncClient();

        Mono<Tuple2<Response<String>, BlobLeaseAsyncClient>> response = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID)
            .flatMap(r -> {
                BlobLeaseAsyncClient leaseClient = createLeaseAsyncClient(bc, r);
                sleepIfRunningAgainstService(16000);
                return Mono.zip(leaseClient.renewLeaseWithResponse(new BlobRenewLeaseOptions()), Mono.just(leaseClient));
            });

        StepVerifier.create(response)
            .assertNext(r -> {
                validateBasicHeaders(r.getT1().getHeaders());
                assertNotNull(r.getT1().getValue());
                assertEquals(r.getT2().getLeaseId(), r.getT1().getValue());
            })
            .verifyComplete();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals(LeaseStateType.LEASED, r.getLeaseState()))
            .verifyComplete();
    }

    @Test
    public void renewBlobLeaseMin() {
        BlobAsyncClientBase bc = createBlobAsyncClient();

        Mono<Response<String>> response = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(bc, r).renewLeaseWithResponse(new BlobRenewLeaseOptions()));

        assertAsyncResponseStatusCode(response, 200);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("acquireBlobLeaseDurationFailSupplier")
    public void renewBlobLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String tags) {
        BlobAsyncClientBase bc = createBlobAsyncClient();
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<String>> response = bc.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID), setupBlobMatchCondition(bc, match)))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setTagsConditions(tags);

                return createLeaseAsyncClient(bc, newLease)
                    .renewLeaseWithResponse(new BlobRenewLeaseOptions().setRequestConditions(mac));
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("acquireBlobLeaseACFailSupplier")
    public void renewBlobLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String tags) {
        BlobAsyncClientBase bc = createBlobAsyncClient();

        Mono<Response<String>> response = Mono.zip(setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID), setupBlobMatchCondition(bc, noneMatch))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setTagsConditions(tags);

                return createLeaseAsyncClient(bc, newLease).renewLeaseWithResponse(
                    new BlobRenewLeaseOptions().setRequestConditions(mac));
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void renewBlobLeaseError() {
        BlockBlobAsyncClient bc = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        StepVerifier.create(createLeaseAsyncClient(bc, "id").renewLease())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void releaseBlobLease() {
        BlobAsyncClientBase bc = createBlobAsyncClient();

        Mono<Response<Void>> response = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(bc, r).releaseLeaseWithResponse(new BlobReleaseLeaseOptions()));

        StepVerifier.create(response)
            .assertNext(r -> assertTrue(validateBasicHeaders(r.getHeaders())))
            .verifyComplete();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals(LeaseStateType.AVAILABLE, r.getLeaseState()))
            .verifyComplete();
    }

    @Test
    public void releaseBlobLeaseMin() {
        BlobAsyncClientBase bc = createBlobAsyncClient();

        Mono<Response<Void>> response = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(bc, r).releaseLeaseWithResponse(
                new BlobReleaseLeaseOptions()));

        assertAsyncResponseStatusCode(response, 200);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("acquireBlobLeaseDurationFailSupplier")
    public void releaseBlobLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                   String tags) {
        BlobAsyncClientBase bc = createBlobAsyncClient();
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<Void>> response = bc.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID), setupBlobMatchCondition(bc, match)))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setTagsConditions(tags);

                return createLeaseAsyncClient(bc, newLease).releaseLeaseWithResponse(
                    new BlobReleaseLeaseOptions().setRequestConditions(mac));
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("acquireBlobLeaseACFailSupplier")
    public void releaseBlobLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                       String tags) {
        BlobAsyncClientBase bc = createBlobAsyncClient();

        Mono<Response<Void>> response = Mono.zip(setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID), setupBlobMatchCondition(bc, noneMatch))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setTagsConditions(tags);

                return createLeaseAsyncClient(bc, newLease).releaseLeaseWithResponse(
                    new BlobReleaseLeaseOptions().setRequestConditions(mac));
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void releaseBlobLeaseError() {
        BlockBlobAsyncClient bc = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        StepVerifier.create(createLeaseAsyncClient(bc, "id").releaseLease())
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("breakBlobLeaseSupplier")
    public void breakBlobLease(int leaseTime, Integer breakPeriod, int remainingTime) {
        BlobAsyncClientBase bc = createBlobAsyncClient();
        BlobLeaseAsyncClient leaseClient = createLeaseAsyncClient(bc, testResourceNamer.randomUuid());

        StepVerifier.create(leaseClient.acquireLease(leaseTime)
            .then(leaseClient.breakLeaseWithResponse(new BlobBreakLeaseOptions()
            .setBreakPeriod(breakPeriod == null ? null : Duration.ofSeconds(breakPeriod)))))
            .assertNext(r -> {
                assertTrue(r.getValue() <= remainingTime);
                assertTrue(validateBasicHeaders(r.getHeaders()));
            })
            .verifyComplete();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertTrue(r.getLeaseState() == LeaseStateType.BROKEN
                || r.getLeaseState() == LeaseStateType.BREAKING))
            .verifyComplete();
    }

    private static Stream<Arguments> breakBlobLeaseSupplier() {
        return Stream.of(
            Arguments.of(-1, null, 0),
            Arguments.of(-1, 20, 25),
            Arguments.of(20, 15, 16));
    }

    @Test
    public void breakBlobLeaseMin() {
        BlobAsyncClientBase bc = createBlobAsyncClient();

        assertAsyncResponseStatusCode(setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID)
            .then(createLeaseAsyncClient(bc).breakLeaseWithResponse(new BlobBreakLeaseOptions())),
            202);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("acquireBlobLeaseDurationFailSupplier")
    public void breakBlobLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String tags) {
        BlobAsyncClientBase bc = createBlobAsyncClient();
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<Integer>> response = bc.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(bc, RECEIVED_ETAG), setupBlobMatchCondition(bc, match)))
            .flatMap(tuple -> {
                String newMatch = tuple.getT2();
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setTagsConditions(tags);

                return createLeaseAsyncClient(bc).breakLeaseWithResponse(
                    new BlobBreakLeaseOptions().setRequestConditions(mac));
            });

        assertAsyncResponseStatusCode(response, 202);
    }

    @ParameterizedTest
    @MethodSource("acquireBlobLeaseACFailSupplier")
    public void breakBlobLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String tags) {
        BlobAsyncClientBase bc = createBlobAsyncClient();

        Mono<Response<Integer>> response = Mono.zip(setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID), setupBlobMatchCondition(bc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setTagsConditions(tags);

                return createLeaseAsyncClient(bc).breakLeaseWithResponse(new BlobBreakLeaseOptions().
                    setRequestConditions(mac));
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void breakBlobLeaseError() {
        BlobAsyncClientBase bc = createBlobAsyncClient();
        StepVerifier.create(createLeaseAsyncClient(bc).breakLease())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void changeBlobLease() {
        BlobAsyncClientBase bc = createBlobAsyncClient();
        BlobLeaseAsyncClient leaseClient = createLeaseAsyncClient(bc, testResourceNamer.randomUuid());
        String newLeaseId = testResourceNamer.randomUuid();


        Mono<BlobLeaseAsyncClient> client = leaseClient.acquireLease(15)
            .then(leaseClient.changeLeaseWithResponse(new BlobChangeLeaseOptions(newLeaseId)))
            .flatMap(r -> {
                assertEquals(r.getValue(), newLeaseId);
                assertEquals(r.getValue(), leaseClient.getLeaseId());
                assertTrue(validateBasicHeaders(r.getHeaders()));
                return Mono.just(createLeaseAsyncClient(bc, r.getValue()));
            });

        assertAsyncResponseStatusCode(client.flatMap(r -> r.releaseLeaseWithResponse(new BlobReleaseLeaseOptions())), 200);
    }

    @Test
    public void changeBlobLeaseMin() {
        BlobAsyncClientBase bc = createBlobAsyncClient();

        Mono<Response<String>> response = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(bc, r).changeLeaseWithResponse(
                new BlobChangeLeaseOptions(testResourceNamer.randomUuid())));

        assertAsyncResponseStatusCode(response, 200);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("acquireBlobLeaseDurationFailSupplier")
    public void changeBlobLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                  String tags) {
        BlobAsyncClientBase bc = createBlobAsyncClient();
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<String>> response = bc.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID), setupBlobMatchCondition(bc, match)))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setTagsConditions(tags);

                return createLeaseAsyncClient(bc, newLease).changeLeaseWithResponse(
                    new BlobChangeLeaseOptions(testResourceNamer.randomUuid()).setRequestConditions(mac));
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("acquireBlobLeaseACFailSupplier")
    public void changeBlobLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                      String noneMatch, String tags) {
        BlobAsyncClientBase bc = createBlobAsyncClient();

        Mono<Response<String>> response = Mono.zip(setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID), setupBlobMatchCondition(bc, noneMatch))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setTagsConditions(tags);

                return createLeaseAsyncClient(bc, newLease).changeLeaseWithResponse(
                    new BlobChangeLeaseOptions(testResourceNamer.randomUuid()).setRequestConditions(mac));
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void changeBlobLeaseError() {
        BlockBlobAsyncClient bc = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        StepVerifier.create(createLeaseAsyncClient(bc, "id").changeLease("id"))
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("acquireContainerLeaseSupplier")
    public void acquireContainerLease(String proposedID, int leaseTime, LeaseStateType leaseState,
                                      LeaseDurationType leaseDuration) {
        BlobLeaseAsyncClient leaseClient = createLeaseAsyncClient(ccAsync, proposedID);

        StepVerifier.create(leaseClient.acquireLeaseWithResponse(new BlobAcquireLeaseOptions(leaseTime)))
            .assertNext(r -> {
                assertEquals(leaseClient.getLeaseId(), r.getValue());
                assertNotNull(r.getValue());
                assertTrue(validateBasicHeaders(r.getHeaders()));
            })
            .verifyComplete();

        StepVerifier.create(ccAsync.getProperties())
            .assertNext(r -> {
                assertEquals(r.getLeaseState(), leaseState);
                assertEquals(r.getLeaseDuration(), leaseDuration);
            })
            .verifyComplete();
    }

    private static Stream<Arguments> acquireContainerLeaseSupplier() {
        return Stream.of(
            Arguments.of(null, -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE),
            Arguments.of(null, 25, LeaseStateType.LEASED, LeaseDurationType.FIXED),
            Arguments.of(CoreUtils.randomUuid().toString(), -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE)
        );
    }

    @Test
    public void acquireContainerLeaseMin() {
        assertAsyncResponseStatusCode(createLeaseAsyncClient(ccAsync).acquireLeaseWithResponse(
            new BlobAcquireLeaseOptions(-1)), 201);
    }

    @ParameterizedTest
    @ValueSource(ints = { -10, 10, 70 })
    public void acquireContainerLeaseDurationFail(int duration) {
        BlobLeaseAsyncClient leaseClient = createLeaseAsyncClient(ccAsync);
        StepVerifier.create(leaseClient.acquireLease(duration))
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("acquireContainerLeaseACSupplier")
    public void acquireContainerLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                        String noneMatch) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(ccAsync).acquireLeaseWithResponse(
            new BlobAcquireLeaseOptions(-1).setRequestConditions(mac)), 201);
    }

    private static Stream<Arguments> acquireContainerLeaseACSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null),
            Arguments.of(null, NEW_DATE, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, GARBAGE_ETAG));
    }

    @ParameterizedTest
    @MethodSource("acquireContainerLeaseACFailSupplier")
    public void acquireContainerLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(createLeaseAsyncClient(ccAsync).acquireLeaseWithResponse(
            new BlobAcquireLeaseOptions(-1).setRequestConditions(mac)))
            .verifyError(BlobStorageException.class);
    }

    private static Stream<Arguments> acquireContainerLeaseACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null),
            Arguments.of(null, OLD_DATE));
    }

    @Test
    public void acquireContainerLeaseError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        StepVerifier.create(createLeaseAsyncClient(ccAsync).acquireLease(50))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void renewContainerLease() {
        Mono <Tuple2<Response<String>, BlobLeaseAsyncClient>> response = setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .flatMap(leaseID -> {
                BlobLeaseAsyncClient leaseClient = createLeaseAsyncClient(ccAsync, leaseID);
                // If running in live mode wait for the lease to expire to ensure we are actually renewing it
                sleepIfRunningAgainstService(16000);
                return Mono.zip(leaseClient.renewLeaseWithResponse(new BlobRenewLeaseOptions()), Mono.just(leaseClient));
            });

        StepVerifier.create(response)
            .assertNext(r -> {
                assertEquals(r.getT2().getLeaseId(), r.getT1().getValue());
                assertTrue(validateBasicHeaders(r.getT1().getHeaders()));
            })
            .verifyComplete();

        StepVerifier.create(ccAsync.getProperties())
            .assertNext(r -> assertEquals(LeaseStateType.LEASED, r.getLeaseState()))
            .verifyComplete();
    }

    @Test
    public void renewContainerLeaseMin() {
        Mono<Response<String>> response = setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(ccAsync, r).renewLeaseWithResponse(
                new BlobRenewLeaseOptions()));

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACSupplier")
    public void renewContainerLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        Mono<Response<String>> response = setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(ccAsync, r).renewLeaseWithResponse(
                new BlobRenewLeaseOptions().setRequestConditions(mac)));

        assertAsyncResponseStatusCode(response, 200);
    }

    private static Stream<Arguments> renewContainerLeaseACSupplier() {
        return Stream.of(
            Arguments.of(OLD_DATE, null),
            Arguments.of(null, NEW_DATE));
    }

    @ParameterizedTest
    @MethodSource("acquireContainerLeaseACFailSupplier")
    public void renewContainerLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        Mono<Response<String>> response = setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(ccAsync, r).renewLeaseWithResponse(
                new BlobRenewLeaseOptions().setRequestConditions(mac)));

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACIllegalSupplier")
    public void renewContainerLeaseACIllegal(String match, String noneMatch) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);
        StepVerifier.create(createLeaseAsyncClient(ccAsync, RECEIVED_ETAG)
            .renewLeaseWithResponse(new BlobRenewLeaseOptions().setRequestConditions(mac)))
            .verifyError(BlobStorageException.class);
    }

    private static Stream<Arguments> renewContainerLeaseACIllegalSupplier() {
        return Stream.of(Arguments.of(RECEIVED_ETAG, null), Arguments.of(null, GARBAGE_ETAG));
    }

    @Test
    public void renewContainerLeaseError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        StepVerifier.create(createLeaseAsyncClient(ccAsync, "id").renewLease())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void releaseContainerLease() {
        Mono<Response<Void>> response = setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(ccAsync, r).releaseLeaseWithResponse(
                new BlobReleaseLeaseOptions()));

        StepVerifier.create(response)
            .assertNext(r -> assertTrue(validateBasicHeaders(r.getHeaders())))
            .verifyComplete();

        StepVerifier.create(ccAsync.getProperties())
            .assertNext(r ->  assertEquals(LeaseStateType.AVAILABLE, r.getLeaseState()))
            .verifyComplete();
    }

    @Test
    public void releaseContainerLeaseMin() {
        Mono<Response<Void>> response = setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(ccAsync, r)
                .releaseLeaseWithResponse(new BlobReleaseLeaseOptions()));

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACSupplier")
    public void releaseContainerLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        Mono<Response<Void>> response = setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(ccAsync, r).releaseLeaseWithResponse(
                new BlobReleaseLeaseOptions().setRequestConditions(mac)));

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("acquireContainerLeaseACFailSupplier")
    public void releaseContainerLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        Mono<Response<Void>> response = setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(ccAsync, r).releaseLeaseWithResponse(
                new BlobReleaseLeaseOptions().setRequestConditions(mac)));

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACIllegalSupplier")
    public void releaseContainerLeaseACIllegal(String match, String noneMatch) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        StepVerifier.create(createLeaseAsyncClient(ccAsync, RECEIVED_LEASE_ID)
            .releaseLeaseWithResponse(new BlobReleaseLeaseOptions().setRequestConditions(mac)))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void releaseContainerLeaseError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        StepVerifier.create(createLeaseAsyncClient(ccAsync, "id").releaseLease())
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("breakBlobLeaseSupplier")
    public void breakContainerLease(int leaseTime, Integer breakPeriod, int remainingTime) {
        BlobLeaseAsyncClient leaseClient = createLeaseAsyncClient(ccAsync, testResourceNamer.randomUuid());

        StepVerifier.create(leaseClient.acquireLease(leaseTime)
            .then(leaseClient.breakLeaseWithResponse(
                new BlobBreakLeaseOptions().setBreakPeriod(breakPeriod == null ? null : Duration.ofSeconds(breakPeriod)))))
            .assertNext(r -> {
                assertTrue(r.getValue() <= remainingTime);
                assertTrue(validateBasicHeaders(r.getHeaders()));
            })
            .verifyComplete();

        StepVerifier.create(ccAsync.getProperties())
            .assertNext(r -> assertTrue(r.getLeaseState() == LeaseStateType.BROKEN
                || r.getLeaseState() == LeaseStateType.BREAKING))
            .verifyComplete();

        if (breakPeriod != null) {
            // If running in live mode wait for the lease to break so we can delete the container after the test completes
            sleepIfRunningAgainstService(breakPeriod * 1000);
        }
    }

    @Test
    public void breakContainerLeaseMin() {
        assertAsyncResponseStatusCode(setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .then(createLeaseAsyncClient(ccAsync).breakLeaseWithResponse(new BlobBreakLeaseOptions())),
            202);
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACSupplier")
    public void breakContainerLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .then(createLeaseAsyncClient(ccAsync).breakLeaseWithResponse(new BlobBreakLeaseOptions()
            .setRequestConditions(mac))), 202);
    }

    @ParameterizedTest
    @MethodSource("acquireContainerLeaseACFailSupplier")
    public void breakContainerLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .then(createLeaseAsyncClient(ccAsync).breakLeaseWithResponse(
                new BlobBreakLeaseOptions().setRequestConditions(mac))))
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACIllegalSupplier")
    public void breakContainerLeaseACIllegal(String match, String noneMatch) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        StepVerifier.create(createLeaseAsyncClient(ccAsync).breakLeaseWithResponse(
            new BlobBreakLeaseOptions().setRequestConditions(mac)))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void breakContainerLeaseError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        StepVerifier.create(createLeaseAsyncClient(ccAsync).breakLease())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void changeContainerLease() {
        Mono<BlobLeaseAsyncClient> client = setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .flatMap(leaseID -> {
                BlobLeaseAsyncClient leaseClient = createLeaseAsyncClient(ccAsync, leaseID);
                assertEquals(leaseClient.getLeaseId(), leaseID);
                return Mono.zip(leaseClient.changeLeaseWithResponse(new BlobChangeLeaseOptions(testResourceNamer.randomUuid())), Mono.just(leaseClient), Mono.just(leaseID));
            })
            .flatMap(tuple -> {
                assertTrue(validateBasicHeaders(tuple.getT1().getHeaders()));
                String newLeaseId = tuple.getT1().getValue();
                assertEquals(newLeaseId, tuple.getT2().getLeaseId());
                assertNotEquals(newLeaseId, tuple.getT3());
                return Mono.just(createLeaseAsyncClient(ccAsync, newLeaseId));
            });

        assertAsyncResponseStatusCode(client.flatMap(r -> r.releaseLeaseWithResponse(
            new BlobReleaseLeaseOptions())), 200);
    }

    @Test
    public void changeContainerLeaseMin() {
        Mono<Response<String>> response = setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(ccAsync, r).changeLeaseWithResponse(
                new BlobChangeLeaseOptions(testResourceNamer.randomUuid())));

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACSupplier")
    public void changeContainerLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        Mono<Response<String>> response = setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(ccAsync, r).changeLeaseWithResponse(
                new BlobChangeLeaseOptions(testResourceNamer.randomUuid()).setRequestConditions(mac)));

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("acquireContainerLeaseACFailSupplier")
    public void changeContainerLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        Mono<Response<String>> response = setupContainerLeaseConditionAsync(ccAsync, RECEIVED_LEASE_ID)
            .flatMap(r -> createLeaseAsyncClient(ccAsync, r).changeLeaseWithResponse(
                new BlobChangeLeaseOptions(testResourceNamer.randomUuid()).setRequestConditions(mac)));

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACIllegalSupplier")
    public void changeContainerLeaseACIllegal(String match, String noneMatch) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        StepVerifier.create(createLeaseAsyncClient(ccAsync, RECEIVED_LEASE_ID)
            .changeLeaseWithResponse(new BlobChangeLeaseOptions(GARBAGE_LEASE_ID).setRequestConditions(mac)))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void changeContainerLeaseError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        StepVerifier.create(createLeaseAsyncClient(ccAsync, "id").changeLease("id"))
            .verifyError(BlobStorageException.class);
    }
}
