// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.developer.loadtesting.models.DailyRecurrence;
import com.azure.developer.loadtesting.models.FileValidationStatus;
import com.azure.developer.loadtesting.models.LoadTest;
import com.azure.developer.loadtesting.models.LoadTestConfiguration;
import com.azure.developer.loadtesting.models.LoadTestingFileType;
import com.azure.developer.loadtesting.models.NotificationRule;
import com.azure.developer.loadtesting.models.PassFailTestResult;
import com.azure.developer.loadtesting.models.ScheduleTestsTrigger;
import com.azure.developer.loadtesting.models.TestAppComponents;
import com.azure.developer.loadtesting.models.TestFileInfo;
import com.azure.developer.loadtesting.models.TestRunEndedEventCondition;
import com.azure.developer.loadtesting.models.TestRunEndedNotificationEventFilter;
import com.azure.developer.loadtesting.models.TestRunStatus;
import com.azure.developer.loadtesting.models.TestServerMetricsConfiguration;
import com.azure.developer.loadtesting.models.TestsNotificationEventFilter;
import com.azure.developer.loadtesting.models.TestsNotificationRule;
import com.azure.developer.loadtesting.models.Trigger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class LoadTestAdministrationTests extends LoadTestingClientTestBase {

    // Helpers
    private BinaryData getFileBodyFromResource(String fileName) {
        URL url = LoadTestAdministrationTests.class.getClassLoader().getResource(fileName);

        return BinaryData.fromFile(new File(url.getPath()).toPath());
    }

    // Puts and Patches

    @Test
    @Order(1)
    public void createOrUpdateTest() {
        LoadTest loadTest = new LoadTest().setDisplayName("Java SDK Sample Test")
            .setDescription("Sample Test")
            .setLoadTestConfiguration(new LoadTestConfiguration().setEngineInstances(1));
        LoadTestAdministrationClient adminClient = getLoadTestAdministrationClient();
        LoadTest response = adminClient.createOrUpdateTest(newTestId, loadTest);

        assertEquals(newTestId, response.getTestId());
    }

    @Test
    @Order(2)
    public void beginUploadTestFileAdditionalFiles() {
        BinaryData file = getFileBodyFromResource(uploadCsvFileName);
        RequestOptions requestOptions
            = new RequestOptions().addQueryParam("fileType", LoadTestingFileType.ADDITIONAL_ARTIFACTS.toString());
        PollResponse<BinaryData> response
            = getLoadTestAdministrationClient().beginUploadTestFile(newTestId, uploadCsvFileName, file, requestOptions)
                .poll();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
    }

    @Test
    @Order(3)
    public void beginUploadTestFileTestScript() {
        BinaryData file = getFileBodyFromResource(uploadJmxFileName);
        RequestOptions fileUploadRequestOptions
            = new RequestOptions().addQueryParam("fileType", LoadTestingFileType.TEST_SCRIPT.toString());
        SyncPoller<BinaryData, BinaryData> poller = getLoadTestAdministrationClient().beginUploadTestFile(newTestId,
            uploadJmxFileName, file, fileUploadRequestOptions);
        poller = setPlaybackSyncPollerPollInterval(poller);
        PollResponse<BinaryData> response = poller.waitForCompletion();
        BinaryData fileBinary = poller.getFinalResult();

        try (JsonReader jsonReader = JsonProviders.createReader(fileBinary.toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            assertEquals(FileValidationStatus.VALIDATION_SUCCESS.toString(), jsonTree.get("validationStatus"));
            assertEquals(uploadJmxFileName, jsonTree.get("fileName"));
        } catch (IOException e) {
            fail("Encountered exception while reading test file data", e);
        }

        assertNotNull(response.getValue());
    }

    @Test
    @Order(4)
    public void createOrUpdateAppComponents() {

        TestAppComponents appComponents = getTestAppComponents();
        LoadTestAdministrationClient adminClient = getLoadTestAdministrationClient();

        TestAppComponents response = adminClient.createOrUpdateAppComponents(newTestId, appComponents);
        assertNotNull(response);
    }

    @Test
    @Order(5)
    public void createOrUpdateServerMetricsConfig() {

        TestServerMetricsConfiguration serverMetricsConfig = getTestServerMetricsConfiguration();
        LoadTestAdministrationClient adminClient = getLoadTestAdministrationClient();

        TestServerMetricsConfiguration response
            = adminClient.createOrUpdateServerMetricsConfig(newTestId, serverMetricsConfig);
        assertNotNull(response);
    }

    // Gets

    @Test
    @Order(7)
    public void getTestFile() {
        TestFileInfo response = getLoadTestAdministrationClient().getTestFile(newTestId, uploadJmxFileName);

        assertEquals(LoadTestingFileType.TEST_SCRIPT.toString(), response.getFileType().toString());
        assertEquals(uploadJmxFileName, response.getFileName());

        TestFileInfo additionalFileResponse
            = getLoadTestAdministrationClient().getTestFile(newTestId, uploadCsvFileName);

        assertEquals(LoadTestingFileType.ADDITIONAL_ARTIFACTS.toString(),
            additionalFileResponse.getFileType().toString());
        assertEquals(uploadCsvFileName, additionalFileResponse.getFileName());
    }

    @Test
    @Order(8)
    public void getTest() {
        LoadTest response = getLoadTestAdministrationClient().getTest(newTestId);

        assertNotNull(response);
        assertEquals(newTestId, response.getTestId());
    }

    @Test
    @Order(10)
    public void getAppComponents() {
        TestAppComponents response = getLoadTestAdministrationClient().getAppComponents(newTestId);
        assertNotNull(response);

        assertTrue(response.getComponents().containsKey(defaultAppComponentResourceId));
        assertEquals(defaultAppComponentResourceId,
            response.getComponents().get(defaultAppComponentResourceId).getResourceId());
    }

    @Test
    @Order(11)
    public void getServerMetricsConfig() {
        TestServerMetricsConfiguration response = getLoadTestAdministrationClient().getServerMetricsConfig(newTestId);

        assertNotNull(response);
        assertTrue(response.getMetrics().containsKey(defaultServerMetricId));
    }

    // Lists

    @Test
    @Order(12)
    public void listTestFiles() {
        PagedIterable<TestFileInfo> response = getLoadTestAdministrationClient().listTestFiles(newTestId);
        boolean uploadFilesFound = response.stream().anyMatch((fileInfo) -> {
            return fileInfo.getFileName().equals(uploadJmxFileName) || fileInfo.getFileName().equals(uploadCsvFileName);
        });

        assertTrue(uploadFilesFound);
    }

    @LiveOnly
    @Test
    @Order(13)
    public void listTests() {
        PagedIterable<LoadTest> response
            = getLoadTestAdministrationClient().listTests("lastModifiedDateTime desc", null, null, null);
        boolean found = response.stream().anyMatch((loadTest) -> {
            return loadTest.getTestId().equals(newTestId);
        });

        assertTrue(found);
    }

    // Trigger CRUD tests

    @Test
    @Order(14)
    public void createOrUpdateTrigger() {
        ScheduleTestsTrigger trigger = new ScheduleTestsTrigger().setDisplayName("sample-trigger")
            .setDescription("Sample trigger for testing")
            .setTestIds(Arrays.asList(newTestId))
            .setStartDateTime(OffsetDateTime.now().plusDays(2))
            .setRecurrence(new DailyRecurrence().setInterval(1));

        Trigger response = getLoadTestAdministrationClient().createOrUpdateTrigger(triggerId, trigger);

        assertNotNull(response);
        assertNotNull(response.getTriggerId());
        assertEquals(triggerId, response.getTriggerId());
        assertEquals("sample-trigger", response.getDisplayName());
    }

    @Test
    @Order(15)
    public void getTrigger() {
        Trigger response = getLoadTestAdministrationClient().getTrigger(triggerId);

        assertNotNull(response);
        assertEquals(triggerId, response.getTriggerId());
        assertEquals("sample-trigger", response.getDisplayName());
    }

    @Test
    @Order(16)
    public void listTriggers() {
        PagedIterable<Trigger> response = getLoadTestAdministrationClient().listTriggers(null, null, null, null);

        boolean found = response.stream().anyMatch(trigger -> triggerId.equals(trigger.getTriggerId()));
        assertTrue(found);
    }

    @Test
    @Order(17)
    public void deleteTrigger() {
        assertDoesNotThrow(() -> {
            getLoadTestAdministrationClient().deleteTrigger(triggerId);
        });
    }

    // Notification Rule CRUD tests

    @Test
    @Order(18)
    public void createOrUpdateNotificationRule() {
        // Build the event filter condition
        TestRunEndedEventCondition condition = new TestRunEndedEventCondition()
            .setTestRunStatuses(Arrays.asList(TestRunStatus.DONE, TestRunStatus.FAILED))
            .setTestRunResults(Arrays.asList(PassFailTestResult.PASSED, PassFailTestResult.FAILED));

        // Build the event filter
        TestRunEndedNotificationEventFilter eventFilter
            = new TestRunEndedNotificationEventFilter().setCondition(condition);

        // Build the event filters map
        Map<String, TestsNotificationEventFilter> eventFilters = new HashMap<>();
        eventFilters.put("testRunEnded", eventFilter);

        // Build the notification rule using the strongly-typed TestsNotificationRule subclass.
        TestsNotificationRule rule = new TestsNotificationRule().setDisplayName("Test Notification Rule")
            .setTestIds(Arrays.asList(newTestId))
            .setActionGroupIds(Arrays.asList(actionGroupId))
            .setEventFilters(eventFilters);

        NotificationRule response
            = getLoadTestAdministrationClient().createOrUpdateNotificationRule(notificationRuleId, rule);

        assertNotNull(response);
        assertNotNull(response.getNotificationRuleId());
        assertEquals(notificationRuleId, response.getNotificationRuleId());
        assertEquals("Test Notification Rule", response.getDisplayName());
    }

    @Test
    @Order(19)
    public void getNotificationRule() {
        NotificationRule response = getLoadTestAdministrationClient().getNotificationRule(notificationRuleId);

        assertNotNull(response);
        assertEquals(notificationRuleId, response.getNotificationRuleId());
        assertEquals("Test Notification Rule", response.getDisplayName());
    }

    @Test
    @Order(20)
    public void listNotificationRules() {
        PagedIterable<NotificationRule> response
            = getLoadTestAdministrationClient().listNotificationRules(null, null, null, null);

        boolean found = response.stream().anyMatch(rule -> notificationRuleId.equals(rule.getNotificationRuleId()));
        assertTrue(found);
    }

    @Test
    @Order(21)
    public void deleteNotificationRule() {
        assertDoesNotThrow(() -> {
            getLoadTestAdministrationClient().deleteNotificationRule(notificationRuleId);
        });
    }

    @Test
    @Order(22)
    public void beginGenerateTestPlanRecommendations() {
        RequestOptions requestOptions = new RequestOptions();
        SyncPoller<BinaryData, BinaryData> poller
            = getLoadTestAdministrationClient().beginGenerateTestPlanRecommendations(recordingTestId, requestOptions);
        poller = setPlaybackSyncPollerPollInterval(poller);

        PollResponse<BinaryData> response = poller.waitForCompletion();

        assertNotNull(response);
        assertTrue(response.getStatus().isComplete());
    }

    @Test
    @Order(23)
    public void beginCloneTest() {
        RequestOptions requestOptions = new RequestOptions();
        BinaryData cloneRequest = BinaryData.fromString(String.format("{\"newTestId\":\"%s\"}", cloneTestId));
        SyncPoller<BinaryData, BinaryData> poller
            = getLoadTestAdministrationClient().beginCloneTest(newTestId, cloneRequest, requestOptions);
        poller = setPlaybackSyncPollerPollInterval(poller);

        PollResponse<BinaryData> response = poller.waitForCompletion();

        assertNotNull(response);
        assertTrue(response.getStatus().isComplete());
    }

    @Test
    @Order(24)
    public void getClonedTest() {
        LoadTest response = getLoadTestAdministrationClient().getTest(cloneTestId);

        assertNotNull(response);
        assertEquals(cloneTestId, response.getTestId());
    }

    @Test
    @Order(25)
    public void deleteClonedTest() {
        assertDoesNotThrow(() -> {
            getLoadTestAdministrationClient().deleteTestWithResponse(cloneTestId, null);
        });
    }

    // Deletes

    @Test
    @Order(26)
    public void deleteTestFile() {
        assertDoesNotThrow(() -> {
            getLoadTestAdministrationClient().deleteTestFileWithResponse(newTestId, uploadCsvFileName, null);
        });
        assertDoesNotThrow(() -> {
            getLoadTestAdministrationClient().deleteTestFileWithResponse(newTestId, uploadJmxFileName, null);
        });
    }

    @Test
    @Order(27)
    public void deleteTest() {
        assertDoesNotThrow(() -> {
            getLoadTestAdministrationClient().deleteTestWithResponse(newTestId, null);
        });
    }
}
