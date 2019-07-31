// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
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

import static com.azure.storage.file.FileTestHelpers.setupClient;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class FileServiceClientTests extends FileServiceClientTestBase {
    private final ClientLogger fileServiceLogger = new ClientLogger(FileServiceClientTests.class);

    private FileServiceClient fileServiceClient;

    @Override
    public void beforeTest() {
        shareName = getShareName();

        if (interceptorManager.isPlaybackMode()) {
            fileServiceClient = setupClient((connectionString, endpoint) -> new FileServiceClientBuilder()
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildClient(), true, fileServiceLogger);
        } else {
            fileServiceClient = setupClient((connectionString, endpoint) -> new FileServiceClientBuilder()
                .connectionString(connectionString)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .buildClient(), false, fileServiceLogger);
        }
    }

    @Override
    public void afterTest() {
        for (ShareItem share : fileServiceClient.listShares(new ListSharesOptions().prefix(shareName))) {
            ShareClient client = fileServiceClient.getShareClient(share.name());
            try {
                client.delete();
            } catch (StorageErrorException ex) {
                // Share already deleted, that's what we wanted anyways.
            }
        }
    }

    @Override
    public void getShareDoesNotCreateAShare() {
        ShareClient client = fileServiceClient.getShareClient(shareName);
        try {
            client.getStatistics();
            fail("getShareAsyncClient shouldn't create a share in Azure.");
        } catch (Exception ex) {
            FileTestHelpers.assertExceptionStatusCode(ex, 404);
        }
    }

    @Override
    public void createShare() {
        FileTestHelpers.assertResponseStatusCode(fileServiceClient.createShare(shareName), 201);
    }

    @Override
    public void createShareTwiceSameMetadata() {
        FileTestHelpers.assertResponseStatusCode(fileServiceClient.createShare(shareName), 201);

        try {
            fileServiceClient.createShare(shareName);
        } catch (Exception exception) {
            FileTestHelpers.assertExceptionStatusCode(exception, 409);
        }
    }

    @Override
    public void createShareTwiceDifferentMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        FileTestHelpers.assertResponseStatusCode(fileServiceClient.createShare(shareName), 201);

        try {
            fileServiceClient.createShare(shareName, metadata, null);
            fail("Attempting to create the share twice with different metadata should throw an exception.");
        } catch (Exception exception) {
            FileTestHelpers.assertExceptionStatusCode(exception, 409);
        }
    }

    @Override
    public void createShareInvalidQuota() {
        try {
            fileServiceClient.createShare(shareName, null, -1);
            fail("Attempting to create a share with a negative quota should throw an exception.");
        } catch (Exception exception) {
            FileTestHelpers.assertExceptionStatusCode(exception, 400);
        }

        try {
            fileServiceClient.createShare(shareName, null, 9999999);
            fail("Attempting to create a share with a quota above 5120 GB should throw an exception.");
        } catch (Exception exception) {
            FileTestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void deleteShare() {
        FileTestHelpers.assertResponseStatusCode(fileServiceClient.createShare(shareName), 201);
        FileTestHelpers.assertResponseStatusCode(fileServiceClient.deleteShare(shareName), 202);
    }

    @Override
    public void deleteShareDoesNotExist() {
        try {
            fileServiceClient.deleteShare(shareName);
            fail("Attempting to delete a share that doesn't exist should throw an exception.");
        } catch (Exception exception) {
            FileTestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void deleteThenCreateShareFromFileServiceClient() {
        FileTestHelpers.assertResponseStatusCode(fileServiceClient.createShare(shareName), 201);
        FileTestHelpers.assertResponseStatusCode(fileServiceClient.deleteShare(shareName), 202);

        FileTestHelpers.sleepInRecordMode(Duration.ofSeconds(45));

        FileTestHelpers.assertResponseStatusCode(fileServiceClient.createShare(shareName), 201);
    }

    @Override
    public void deleteThenCreateShareTooSoonFromFileServiceClient() {
        FileTestHelpers.assertResponseStatusCode(fileServiceClient.createShare(shareName), 201);
        FileTestHelpers.assertResponseStatusCode(fileServiceClient.deleteShare(shareName), 202);

        try {
            fileServiceClient.createShare(shareName);
            fail("Attempting to re-create a share within 30 seconds of deleting it should throw an exception.");
        } catch (Exception exception) {
            FileTestHelpers.assertExceptionStatusCode(exception, 409);
        }
    }

    @Override
    public void listShares() {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().name(shareName + i)
                .properties(new ShareProperties().quota(2));

            testShares.add(share);
            FileTestHelpers.assertResponseStatusCode(fileServiceClient.createShare(share.name(), share.metadata(), share.properties().quota()), 201);
        }

        Iterator<ShareItem> shares = fileServiceClient.listShares(defaultOptions()).iterator();
        for (int i = 0; i < 3; i++) {
            FileTestHelpers.assertSharesAreEqual(testShares.pop(), shares.next());
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

            FileTestHelpers.assertResponseStatusCode(fileServiceClient.createShare(share.name(), share.metadata(), share.properties().quota()), 201);
        }

        Iterator<ShareItem> shares = fileServiceClient.listShares(defaultOptions().prefix(shareName + "prefix")).iterator();
        for (int i = 0; i < 2; i++) {
            FileTestHelpers.assertSharesAreEqual(testShares.pop(), shares.next());
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
            FileTestHelpers.assertResponseStatusCode(fileServiceClient.createShare(share.name(), share.metadata(), share.properties().quota()), 201);
        }

        Iterator<ShareItem> shares = fileServiceClient.listShares(defaultOptions().maxResults(2)).iterator();
        for (int i = 0; i < 2; i++) {
            FileTestHelpers.assertSharesAreEqual(testShares.pop(), shares.next());
        }
        assertFalse(shares.hasNext());
    }

    @Override
    public void listSharesInvalidMaxResults() {
        try {
            fileServiceClient.listShares(defaultOptions().maxResults(-1)).iterator().hasNext();
            fail("Attempting to list shares with a negative maximum should throw an error");
        } catch (Exception exception) {
            FileTestHelpers.assertExceptionStatusCode(exception, 400);
        }

        try {
            fileServiceClient.listShares(defaultOptions().maxResults(0)).iterator().hasNext();
            fail("Attempting to list shares with a zero maximum should throw an error");
        } catch (Exception exception) {
            FileTestHelpers.assertExceptionStatusCode(exception, 400);
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
            FileTestHelpers.assertResponseStatusCode(fileServiceClient.createShare(share.name(), share.metadata(), share.properties().quota()), 201);
        }

        Iterator<ShareItem> shares = fileServiceClient.listShares(defaultOptions().includeMetadata(true)).iterator();
        for (int i = 0; i < 3; i++) {
            FileTestHelpers.assertSharesAreEqual(testShares.pop(), shares.next());
        }
        assertFalse(shares.hasNext());
    }

    @Override
    public void listSharesIncludeSnapshots() {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().name(shareName + i)
                .properties(new ShareProperties().quota(2));

            ShareClient client = fileServiceClient.getShareClient(share.name());

            Response<ShareInfo> createResponse = client.create(null, share.properties().quota());
            FileTestHelpers.assertResponseStatusCode(createResponse, 201);

            if (i % 2 == 0) {
                Response<ShareSnapshotInfo> snapshotResponse = client.createSnapshot();
                FileTestHelpers.assertResponseStatusCode(snapshotResponse, 201);

                testShares.add(new ShareItem().name(share.name())
                    .snapshot(snapshotResponse.value().snapshot())
                    .properties(new ShareProperties().quota(share.properties().quota())));
            }

            testShares.add(share);
        }

        Iterator<ShareItem> shares = fileServiceClient.listShares(defaultOptions().includeSnapshots(true)).iterator();
        for (int i = 0; i < 5; i++) {
            FileTestHelpers.assertSharesAreEqual(testShares.pop(), shares.next());
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

            ShareClient client = fileServiceClient.getShareClient(share.name());

            Response<ShareInfo> createResponse = client.create(share.metadata(), share.properties().quota());
            FileTestHelpers.assertResponseStatusCode(createResponse, 201);

            if (i % 2 == 0) {
                Response<ShareSnapshotInfo> snapshotResponse = client.createSnapshot();
                FileTestHelpers.assertResponseStatusCode(snapshotResponse, 201);

                testShares.add(new ShareItem().name(share.name())
                    .snapshot(snapshotResponse.value().snapshot())
                    .properties(share.properties()));
            }

            testShares.add(share);
        }

        Iterator<ShareItem> shares = fileServiceClient.listShares(defaultOptions().includeMetadata(true).includeSnapshots(true)).iterator();
        for (int i = 0; i < 5; i++) {
            FileTestHelpers.assertSharesAreEqual(testShares.pop(), shares.next());
        }
        assertFalse(shares.hasNext());
    }

    @Override
    public void setFileServiceProperties() {
        FileServiceProperties originalProperties = fileServiceClient.getProperties().value();

        RetentionPolicy retentionPolicy = new RetentionPolicy().enabled(true)
            .days(3);

        Metrics metrics = new Metrics().enabled(true)
            .includeAPIs(false)
            .retentionPolicy(retentionPolicy)
            .version("1.0");

        FileServiceProperties updatedProperties = new FileServiceProperties().hourMetrics(metrics)
            .minuteMetrics(metrics)
            .cors(new ArrayList<>());

        FileTestHelpers.assertResponseStatusCode(fileServiceClient.setProperties(updatedProperties), 202);

        Response<FileServiceProperties> getResponse = fileServiceClient.getProperties();
        FileTestHelpers.assertResponseStatusCode(getResponse, 200);
        FileTestHelpers.assertFileServicePropertiesAreEqual(updatedProperties, getResponse.value());

        FileTestHelpers.assertResponseStatusCode(fileServiceClient.setProperties(originalProperties), 202);

        getResponse = fileServiceClient.getProperties();
        FileTestHelpers.assertResponseStatusCode(getResponse, 200);
        FileTestHelpers.assertFileServicePropertiesAreEqual(originalProperties, getResponse.value());
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
        for (int i = 0; i < 6; i++) {
            cors.add(new CorsRule());
        }

        FileServiceProperties properties = new FileServiceProperties().hourMetrics(metrics)
            .minuteMetrics(metrics)
            .cors(cors);

        try {
            fileServiceClient.setProperties(properties);
            fail("Attempting to set more than 5 CorsRules on files should throw an exception.");
        } catch (Exception exception) {
            FileTestHelpers.assertExceptionStatusCode(exception, 400);
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
            fileServiceClient.setProperties(properties);
            fail("Attempting to set an allowed header longer than 64 characters should throw an exception.");
        } catch (Exception exception) {
            FileTestHelpers.assertExceptionStatusCode(exception, 400);
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
            fileServiceClient.setProperties(properties);
            fail("Attempting to set an exposed header longer than 64 characters should throw an exception.");
        } catch (Exception exception) {
            FileTestHelpers.assertExceptionStatusCode(exception, 400);
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
            fileServiceClient.setProperties(properties);
            fail("Attempting to set an allowed origin longer than 64 characters should throw an exception.");
        } catch (Exception exception) {
            FileTestHelpers.assertExceptionStatusCode(exception, 400);
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
            fileServiceClient.setProperties(properties);
            fail("Attempting to set an invalid allowed method should throw an exception.");
        } catch (Exception exception) {
            FileTestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }
}
