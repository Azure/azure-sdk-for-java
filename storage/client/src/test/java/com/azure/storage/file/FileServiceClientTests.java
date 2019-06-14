// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.storage.file.models.CorsRule;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.ListSharesOptions;
import com.azure.storage.file.models.Metrics;
import com.azure.storage.file.models.RetentionPolicy;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareItem;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.StorageErrorException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class FileServiceClientTests extends FileServiceClientTestsBase {
    private FileServiceClient serviceClient;

    private String reallyLongString = "thisisareallylongstringthatexceedsthe64characterlimitallowedoncertainproperties";

    @Override
    public void beforeTest() {
        shareName = getShareName();

        if (interceptorManager.isPlaybackMode()) {
            serviceClient = setupClient((connectionString, endpoint) -> FileServiceAsyncClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildSync());
        } else {
            serviceClient = setupClient((connectionString, endpoint) -> FileServiceAsyncClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .buildSync());
        }
    }

    @Override
    public void afterTest() {
        for (ShareItem share : serviceClient.listShares(new ListSharesOptions().prefix(shareName))) {
            ShareClient client = serviceClient.getShareClient(share.name());
            try {
                client.delete(null);
            } catch (StorageErrorException ex) {
                // Share already deleted, that's what we wanted anyways.
            }
        }
    }

    @Override
    public void getShareDoesNotCreateAShare() {
        ShareClient client = serviceClient.getShareClient(shareName);
        try {
            client.getStatistics();
            fail("getShareAsyncClient shouldn't create a share in Azure.");
        } catch (Exception ex) {
            TestHelpers.assertExceptionStatusCode(ex, 400);
        }
    }

    @Override
    public void createShare() {
        assertNotNull(serviceClient.createShare(shareName));
    }

    @Override
    public void createShareTwiceSameMetadata() {
        assertNotNull(serviceClient.createShare(shareName));

        try {
            serviceClient.createShare(shareName);
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 409);
        }
    }

    @Override
    public void createShareTwiceDifferentMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        assertNotNull(serviceClient.createShare(shareName));

        try {
            serviceClient.createShare(shareName, metadata, null);
            fail("Attempting to create the share twice with different metadata should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 409);
        }
    }

    @Override
    public void createShareInvalidQuota() {
        try {
            serviceClient.createShare(shareName, null, -1);
            fail("Attempting to create a share with a negative quota should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }

        try {
            serviceClient.createShare(shareName, null, 9999999);
            fail("Attempting to create a share with a quota above 5120 GB should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void deleteShare() {
        assertNotNull(serviceClient.createShare(shareName));
        serviceClient.deleteShare(shareName);
    }

    @Override
    public void deleteShareDoesNotExist() {
        try {
            serviceClient.deleteShare(shareName);
            fail("Attempting to delete a share that doesn't exist should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void deleteThenCreateShare() {
        assertNotNull(serviceClient.createShare(shareName));
        serviceClient.deleteShare(shareName);

        TestHelpers.sleep(Duration.ofSeconds(45));

        assertNotNull(serviceClient.createShare(shareName));
    }

    @Override
    public void deleteThenCreateShareTooSoon() {
        assertNotNull(serviceClient.createShare(shareName));
        serviceClient.deleteShare(shareName);

        try {
            serviceClient.createShare(shareName);
            fail("Attempting to re-create a share within 30 seconds of deleting it should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 409);
        }
    }

    @Override
    public void listShares() {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().name(shareName + i)
                .properties(new ShareProperties().quota(2));

            testShares.add(share);
            assertNotNull(serviceClient.createShare(share.name(), share.metadata(), share.properties().quota()));
        }

        Iterator<ShareItem> shares = serviceClient.listShares(defaultOptions()).iterator();
        for (int i = 0; i < 3; i++) {
            TestHelpers.assertSharesAreEqual(testShares.pop(), shares.next());
        }
        assertFalse(shares.hasNext());
    }

    @Override
    public void listSharesWithPrefix() {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().properties(new ShareProperties().quota(2));

            if (i % 2 == 0) {
                share.name(shareName + "prefix" + i);
                testShares.add(share);
            } else {
                share.name(shareName + i);
            }

            assertNotNull(serviceClient.createShare(share.name(), share.metadata(), share.properties().quota()));
        }

        Iterator<ShareItem> shares = serviceClient.listShares(defaultOptions().prefix(shareName + "prefix")).iterator();
        for (int i = 0; i < 2; i++) {
            TestHelpers.assertSharesAreEqual(testShares.pop(), shares.next());
        }
        assertFalse(shares.hasNext());
    }

    @Override
    public void listSharesWithLimit() {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().name(shareName + i)
                .properties(new ShareProperties().quota(2));

            testShares.add(share);
            assertNotNull(serviceClient.createShare(share.name(), share.metadata(), share.properties().quota()));
        }

        Iterator<ShareItem> shares = serviceClient.listShares(defaultOptions().maxResults(2)).iterator();
        for (int i = 0; i < 2; i++) {
            TestHelpers.assertSharesAreEqual(testShares.pop(), shares.next());
        }
        assertFalse(shares.hasNext());
    }

    @Override
    public void listSharesInvalidMaxResults() {
        try {
            serviceClient.listShares(defaultOptions().maxResults(-1)).iterator().hasNext();
            fail("Attempting to list shares with a negative maximum should throw an error");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }

        try {
            serviceClient.listShares(defaultOptions().maxResults(0)).iterator().hasNext();
            fail("Attempting to list shares with a zero maximum should throw an error");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void listSharesIncludeMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().name(shareName + i)
                .properties(new ShareProperties().quota(2));

            if (i % 2 == 0) {
                share.metadata(metadata);
            }

            testShares.add(share);
            assertNotNull(serviceClient.createShare(share.name(), share.metadata(), share.properties().quota()));
        }

        Iterator<ShareItem> shares = serviceClient.listShares(defaultOptions().includeMetadata(true)).iterator();
        for (int i = 0; i < 3; i++) {
            TestHelpers.assertSharesAreEqual(testShares.pop(), shares.next());
        }
        assertFalse(shares.hasNext());
    }

    @Override
    public void listSharesIncludeSnapshots() {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().name(shareName + i)
                .properties(new ShareProperties().quota(2));

            ShareClient client = serviceClient.getShareClient(share.name());

            Response<ShareInfo> createResponse = client.create(null, share.properties().quota());
            TestHelpers.assertResponseStatusCode(createResponse, 201);

            if (i % 2 == 0) {
                Response<ShareSnapshotInfo> snapshotResponse = client.createSnapshot();
                TestHelpers.assertResponseStatusCode(snapshotResponse, 201);

                testShares.add(new ShareItem().name(share.name())
                    .snapshot(snapshotResponse.value().snapshot())
                    .properties(new ShareProperties().quota(share.properties().quota())));
            }

            testShares.add(share);
        }

        Iterator<ShareItem> shares = serviceClient.listShares(defaultOptions().includeSnapshots(true)).iterator();
        for (int i = 0; i < 5; i++) {
            TestHelpers.assertSharesAreEqual(testShares.pop(), shares.next());
        }
        assertFalse(shares.hasNext());
    }

    @Override
    public void listSharesIncludeMetadataAndSnapshots() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().name(shareName + i)
                .properties(new ShareProperties().quota(2));

            if (i % 2 == 0) {
                share.metadata(metadata);
            }

            ShareClient client = serviceClient.getShareClient(share.name());

            Response<ShareInfo> createResponse = client.create(share.metadata(), share.properties().quota());
            TestHelpers.assertResponseStatusCode(createResponse, 201);

            if (i % 2 == 0) {
                Response<ShareSnapshotInfo> snapshotResponse = client.createSnapshot();
                TestHelpers.assertResponseStatusCode(snapshotResponse, 201);

                testShares.add(new ShareItem().name(share.name())
                    .snapshot(snapshotResponse.value().snapshot())
                    .properties(share.properties()));
            }

            testShares.add(share);
        }

        Iterator<ShareItem> shares = serviceClient.listShares(defaultOptions().includeMetadata(true).includeSnapshots(true)).iterator();
        for (int i = 0; i < 5; i++) {
            TestHelpers.assertSharesAreEqual(testShares.pop(), shares.next());
        }
        assertFalse(shares.hasNext());
    }

    @Override
    public void setProperties() {
        FileServiceProperties originalProperties = serviceClient.getProperties().value();

        RetentionPolicy retentionPolicy = new RetentionPolicy().enabled(true)
            .days(3);

        Metrics metrics = new Metrics().enabled(true)
            .includeAPIs(false)
            .retentionPolicy(retentionPolicy)
            .version("1.0");

        FileServiceProperties updatedProperties = new FileServiceProperties().hourMetrics(metrics)
            .minuteMetrics(metrics)
            .cors(new ArrayList<>());

        TestHelpers.assertResponseStatusCode(serviceClient.setProperties(updatedProperties), 202);

        Response<FileServiceProperties> getResponse = serviceClient.getProperties();
        TestHelpers.assertResponseStatusCode(getResponse, 200);
        TestHelpers.assertFileServicePropertiesAreEqual(updatedProperties, getResponse.value());

        TestHelpers.assertResponseStatusCode(serviceClient.setProperties(originalProperties), 202);

        getResponse = serviceClient.getProperties();
        TestHelpers.assertResponseStatusCode(getResponse, 200);
        TestHelpers.assertFileServicePropertiesAreEqual(originalProperties, getResponse.value());
    }

    @Override
    public void setPropertiesTooManyRules() {
        RetentionPolicy retentionPolicy = new RetentionPolicy().enabled(true)
            .days(3);

        Metrics metrics = new Metrics().enabled(true)
            .includeAPIs(false)
            .retentionPolicy(retentionPolicy)
            .version("1.0");

        List<CorsRule> cors = new ArrayList<>();
        for (int i = 0; i < 6; i ++) {
            cors.add(new CorsRule());
        }

        FileServiceProperties properties = new FileServiceProperties().hourMetrics(metrics)
            .minuteMetrics(metrics)
            .cors(cors);

        try {
            serviceClient.setProperties(properties);
            fail("Attempting to set more than 5 CorsRules on files should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void setPropertiesInvalidAllowedHeader() {
        RetentionPolicy retentionPolicy = new RetentionPolicy().enabled(true)
            .days(3);

        Metrics metrics = new Metrics().enabled(true)
            .includeAPIs(false)
            .retentionPolicy(retentionPolicy)
            .version("1.0");

        FileServiceProperties properties = new FileServiceProperties().hourMetrics(metrics)
            .minuteMetrics(metrics)
            .cors(Collections.singletonList(new CorsRule().allowedHeaders(reallyLongString)));

        try {
            serviceClient.setProperties(properties);
            fail("Attempting to set an allowed header longer than 64 characters should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void setPropertiesInvalidExposedHeader() {
        RetentionPolicy retentionPolicy = new RetentionPolicy().enabled(true)
            .days(3);

        Metrics metrics = new Metrics().enabled(true)
            .includeAPIs(false)
            .retentionPolicy(retentionPolicy)
            .version("1.0");

        FileServiceProperties properties = new FileServiceProperties().hourMetrics(metrics)
            .minuteMetrics(metrics)
            .cors(Collections.singletonList(new CorsRule().exposedHeaders(reallyLongString)));

        try {
            serviceClient.setProperties(properties);
            fail("Attempting to set an exposed header longer than 64 characters should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void setPropertiesInvalidAllowedOrigin() {
        RetentionPolicy retentionPolicy = new RetentionPolicy().enabled(true)
            .days(3);

        Metrics metrics = new Metrics().enabled(true)
            .includeAPIs(false)
            .retentionPolicy(retentionPolicy)
            .version("1.0");

        FileServiceProperties properties = new FileServiceProperties().hourMetrics(metrics)
            .minuteMetrics(metrics)
            .cors(Collections.singletonList(new CorsRule().allowedOrigins(reallyLongString)));

        try {
            serviceClient.setProperties(properties);
            fail("Attempting to set an allowed origin longer than 64 characters should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void setPropertiesInvalidAllowedMethod() {
        RetentionPolicy retentionPolicy = new RetentionPolicy().enabled(true)
            .days(3);

        Metrics metrics = new Metrics().enabled(true)
            .includeAPIs(false)
            .retentionPolicy(retentionPolicy)
            .version("1.0");

        FileServiceProperties properties = new FileServiceProperties().hourMetrics(metrics)
            .minuteMetrics(metrics)
            .cors(Collections.singletonList(new CorsRule().allowedMethods("NOTAREALHTTPMETHOD")));

        try {
            serviceClient.setProperties(properties);
            fail("Attempting to set an invalid allowed method should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }
}
