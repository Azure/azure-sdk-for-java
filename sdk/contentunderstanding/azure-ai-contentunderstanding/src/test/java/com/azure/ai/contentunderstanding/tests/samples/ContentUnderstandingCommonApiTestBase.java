// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.AudioVisualContent;
import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerConfig;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerOperationStatus;
import com.azure.ai.contentunderstanding.models.ContentFieldDefinition;
import com.azure.ai.contentunderstanding.models.ContentFieldSchema;
import com.azure.ai.contentunderstanding.models.ContentFieldType;
import com.azure.ai.contentunderstanding.models.ContentUnderstandingDefaults;
import com.azure.ai.contentunderstanding.models.GenerationMethod;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.test.TestMode;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class ContentUnderstandingCommonApiTestBase extends ContentUnderstandingClientTestBase {
    @Override
    protected void beforeTest() {
        super.beforeTest();
        if (getTestMode() != TestMode.LIVE) {
            interceptorManager.addSanitizers(Collections.singletonList(
                new TestProxySanitizer("$..modelDeployments.*", null, "REDACTED", TestProxySanitizerType.BODY_KEY)));
        }
    }

    protected void verifyListAnalyzersSupportsConfiguredServiceVersion() {
        Optional<PagedResponse<com.azure.ai.contentunderstanding.models.ContentAnalyzer>> firstPage
            = contentUnderstandingClient.listAnalyzers().streamByPage().findFirst();

        assertTrue(firstPage.isPresent(), "listAnalyzers should return a first page");
    }

    protected void verifyAsyncListAnalyzersSupportsConfiguredServiceVersion() {
        PagedResponse<ContentAnalyzer> firstPage
            = contentUnderstandingAsyncClient.listAnalyzers().byPage().next().block();

        assertNotNull(firstPage);
    }

    protected void verifyAnalyzerManagementSupportsConfiguredServiceVersion() {
        String sourceAnalyzerId = testResourceNamer.randomName("common_source_", 50);
        String copiedAnalyzerId = testResourceNamer.randomName("common_copy_", 50);
        boolean sourceAnalyzerDeleted = false;
        boolean copiedAnalyzerDeleted = false;
        try {
            SyncPoller<ContentAnalyzerOperationStatus, ContentAnalyzer> createPoller = contentUnderstandingClient
                .beginCreateAnalyzer(sourceAnalyzerId, createAnalyzer("Common API compatibility analyzer"), true);
            ContentAnalyzer created = requireSuccessfulResult(createPoller.waitForCompletion().getStatus(),
                createPoller::getFinalResult, "Common API analyzer creation");
            assertEquals(sourceAnalyzerId, created.getAnalyzerId());

            ContentAnalyzer retrieved = contentUnderstandingClient.getAnalyzer(sourceAnalyzerId);
            assertEquals(sourceAnalyzerId, retrieved.getAnalyzerId());

            boolean listed = contentUnderstandingClient.listAnalyzers()
                .stream()
                .anyMatch(analyzer -> sourceAnalyzerId.equals(analyzer.getAnalyzerId()));
            assertTrue(listed, "Created analyzer should be returned by listAnalyzers");

            Map<String, String> updatedTags = new LinkedHashMap<>();
            updatedTags.put("compatibility", getServiceVersion().getVersion());
            ContentAnalyzer update = new ContentAnalyzer().setBaseAnalyzerId(retrieved.getBaseAnalyzerId())
                .setDescription("Updated common API compatibility analyzer")
                .setTags(updatedTags);
            contentUnderstandingClient.updateAnalyzer(sourceAnalyzerId, update);

            ContentAnalyzer updated = contentUnderstandingClient.getAnalyzer(sourceAnalyzerId);
            assertEquals("Updated common API compatibility analyzer", updated.getDescription());
            assertEquals(getServiceVersion().getVersion(), updated.getTags().get("compatibility"));

            SyncPoller<ContentAnalyzerOperationStatus, ContentAnalyzer> copyPoller
                = contentUnderstandingClient.beginCopyAnalyzer(copiedAnalyzerId, sourceAnalyzerId);
            ContentAnalyzer copied = requireSuccessfulResult(copyPoller.waitForCompletion().getStatus(),
                copyPoller::getFinalResult, "Common API analyzer copy");
            assertEquals(copiedAnalyzerId, copied.getAnalyzerId());

            contentUnderstandingClient.deleteAnalyzer(copiedAnalyzerId);
            copiedAnalyzerDeleted = true;
            contentUnderstandingClient.deleteAnalyzer(sourceAnalyzerId);
            sourceAnalyzerDeleted = true;
        } finally {
            if (!copiedAnalyzerDeleted) {
                deleteAnalyzerIfPresent(copiedAnalyzerId);
            }
            if (!sourceAnalyzerDeleted) {
                deleteAnalyzerIfPresent(sourceAnalyzerId);
            }
        }
    }

    protected void verifyAsyncAnalyzerManagementSupportsConfiguredServiceVersion() {
        String sourceAnalyzerId = testResourceNamer.randomName("common_async_source_", 50);
        String copiedAnalyzerId = testResourceNamer.randomName("common_async_copy_", 50);
        boolean sourceAnalyzerDeleted = false;
        boolean copiedAnalyzerDeleted = false;
        try {
            ContentAnalyzer created = contentUnderstandingAsyncClient
                .beginCreateAnalyzer(sourceAnalyzerId, createAnalyzer("Async common API compatibility analyzer"), true)
                .last()
                .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                    "Async common API analyzer creation"))
                .block();
            assertNotNull(created);
            assertEquals(sourceAnalyzerId, created.getAnalyzerId());

            ContentAnalyzer retrieved = contentUnderstandingAsyncClient.getAnalyzer(sourceAnalyzerId).block();
            assertNotNull(retrieved);
            assertEquals(sourceAnalyzerId, retrieved.getAnalyzerId());

            Map<String, String> updatedTags = new LinkedHashMap<>();
            updatedTags.put("compatibility", getServiceVersion().getVersion());
            ContentAnalyzer update = new ContentAnalyzer().setBaseAnalyzerId(retrieved.getBaseAnalyzerId())
                .setDescription("Updated async common API compatibility analyzer")
                .setTags(updatedTags);
            ContentAnalyzer updated = contentUnderstandingAsyncClient.updateAnalyzer(sourceAnalyzerId, update).block();
            assertNotNull(updated);
            assertEquals("Updated async common API compatibility analyzer", updated.getDescription());
            assertEquals(getServiceVersion().getVersion(), updated.getTags().get("compatibility"));

            ContentAnalyzer copied
                = contentUnderstandingAsyncClient.beginCopyAnalyzer(copiedAnalyzerId, sourceAnalyzerId)
                    .last()
                    .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                        "Async common API analyzer copy"))
                    .block();
            assertNotNull(copied);
            assertEquals(copiedAnalyzerId, copied.getAnalyzerId());

            contentUnderstandingAsyncClient.deleteAnalyzer(copiedAnalyzerId).block();
            copiedAnalyzerDeleted = true;
            contentUnderstandingAsyncClient.deleteAnalyzer(sourceAnalyzerId).block();
            sourceAnalyzerDeleted = true;
        } finally {
            if (!copiedAnalyzerDeleted) {
                deleteAnalyzerIfPresentAsync(copiedAnalyzerId);
            }
            if (!sourceAnalyzerDeleted) {
                deleteAnalyzerIfPresentAsync(sourceAnalyzerId);
            }
        }
    }

    protected void verifyAnalysisLifecycleSupportsConfiguredServiceVersion() throws Exception {
        String urlOperationId = null;
        String binaryOperationId = null;
        try {
            AnalysisInput input = new AnalysisInput().setUrl(
                "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/invoice.pdf");
            SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> urlPoller
                = contentUnderstandingClient.beginAnalyze("prebuilt-documentSearch", Collections.singletonList(input));
            PollResponse<ContentAnalyzerAnalyzeOperationStatus> urlCompletion = urlPoller.waitForCompletion();
            AnalysisResult urlResult = requireSuccessfulResult(urlCompletion.getStatus(), urlPoller::getFinalResult,
                "Common API URL analysis");
            assertAnalysisVersion(urlResult);
            urlOperationId = requireOperationId(urlCompletion.getValue(), "Common API URL analysis");

            SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> binaryPoller
                = contentUnderstandingClient.beginAnalyzeBinary("prebuilt-documentSearch",
                    PreviewSampleTestSupport.readSample("sample_invoice.pdf"));
            PollResponse<ContentAnalyzerAnalyzeOperationStatus> binaryCompletion = binaryPoller.waitForCompletion();
            AnalysisResult binaryResult = requireSuccessfulResult(binaryCompletion.getStatus(),
                binaryPoller::getFinalResult, "Common API binary analysis");
            assertAnalysisVersion(binaryResult);
            binaryOperationId = requireOperationId(binaryCompletion.getValue(), "Common API binary analysis");

            contentUnderstandingClient.deleteResult(urlOperationId);
            urlOperationId = null;
            contentUnderstandingClient.deleteResult(binaryOperationId);
            binaryOperationId = null;
        } finally {
            deleteResultIfPresent(urlOperationId);
            deleteResultIfPresent(binaryOperationId);
        }
    }

    protected void verifyAsyncAnalysisLifecycleSupportsConfiguredServiceVersion() throws Exception {
        AtomicReference<String> urlOperationId = new AtomicReference<>();
        AtomicReference<String> binaryOperationId = new AtomicReference<>();
        try {
            AnalysisInput input = new AnalysisInput().setUrl(
                "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/invoice.pdf");
            PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> urlPoller
                = contentUnderstandingAsyncClient.beginAnalyze("prebuilt-documentSearch",
                    Collections.singletonList(input));
            AnalysisResult urlResult = urlPoller.last()
                .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                    "Async common API URL analysis")
                        .doOnNext(ignored -> urlOperationId
                            .set(requireOperationId(response.getValue(), "Async common API URL analysis"))))
                .block();
            assertAnalysisVersion(urlResult);
            assertNotNull(urlOperationId.get());

            PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> binaryPoller
                = contentUnderstandingAsyncClient.beginAnalyzeBinary("prebuilt-documentSearch",
                    PreviewSampleTestSupport.readSample("sample_invoice.pdf"));
            AnalysisResult binaryResult = binaryPoller.last()
                .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                    "Async common API binary analysis")
                        .doOnNext(ignored -> binaryOperationId
                            .set(requireOperationId(response.getValue(), "Async common API binary analysis"))))
                .block();
            assertAnalysisVersion(binaryResult);
            assertNotNull(binaryOperationId.get());

            contentUnderstandingAsyncClient.deleteResult(urlOperationId.get()).block();
            urlOperationId.set(null);
            contentUnderstandingAsyncClient.deleteResult(binaryOperationId.get()).block();
            binaryOperationId.set(null);
        } finally {
            deleteResultIfPresentAsync(urlOperationId.get());
            deleteResultIfPresentAsync(binaryOperationId.get());
        }
    }

    protected void verifyDefaultsSupportConfiguredServiceVersion() {
        ContentUnderstandingDefaults current = contentUnderstandingClient.getDefaults();
        assertNotNull(current);
        assertNotNull(current.getModelDeployments());
        assertTrue(!current.getModelDeployments().isEmpty(), "Defaults should contain model deployments");

        String configuredModel = getModelProfile().getCompletionModel();
        String modelName = current.getModelDeployments().containsKey(configuredModel)
            ? configuredModel
            : current.getModelDeployments().keySet().iterator().next();
        String deploymentName = current.getModelDeployments().get(modelName);
        ContentUnderstandingDefaults updated
            = contentUnderstandingClient.updateDefaults(Collections.singletonMap(modelName, deploymentName));
        assertNotNull(updated);
        assertEquals(deploymentName, updated.getModelDeployments().get(modelName));

        ContentUnderstandingDefaults persisted = contentUnderstandingClient.getDefaults();
        assertEquals(deploymentName, persisted.getModelDeployments().get(modelName));
    }

    protected void verifyAsyncDefaultsSupportConfiguredServiceVersion() {
        ContentUnderstandingDefaults current = contentUnderstandingAsyncClient.getDefaults().block();
        assertNotNull(current);
        assertNotNull(current.getModelDeployments());
        assertTrue(!current.getModelDeployments().isEmpty(), "Defaults should contain model deployments");

        String configuredModel = getModelProfile().getCompletionModel();
        String modelName = current.getModelDeployments().containsKey(configuredModel)
            ? configuredModel
            : current.getModelDeployments().keySet().iterator().next();
        String deploymentName = current.getModelDeployments().get(modelName);
        ContentUnderstandingDefaults updated
            = contentUnderstandingAsyncClient.updateDefaults(Collections.singletonMap(modelName, deploymentName))
                .block();
        assertNotNull(updated);
        assertEquals(deploymentName, updated.getModelDeployments().get(modelName));

        ContentUnderstandingDefaults persisted = contentUnderstandingAsyncClient.getDefaults().block();
        assertNotNull(persisted);
        assertEquals(deploymentName, persisted.getModelDeployments().get(modelName));
    }

    protected void verifyResultFileSupportsConfiguredServiceVersion() {
        String operationId = null;
        try {
            AnalysisInput input = new AnalysisInput().setUrl(
                "https://github.com/Azure-Samples/azure-ai-content-understanding-assets/raw/refs/heads/main/videos/sdk_samples/FlightSimulator.mp4");
            SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> poller
                = contentUnderstandingClient.beginAnalyze("prebuilt-videoSearch", Collections.singletonList(input));
            PollResponse<ContentAnalyzerAnalyzeOperationStatus> completion = poller.waitForCompletion();
            AnalysisResult result
                = requireSuccessfulResult(completion.getStatus(), poller::getFinalResult, "Common API video analysis");
            assertAnalysisVersion(result);
            operationId = requireOperationId(completion.getValue(), "Common API video analysis");

            AudioVisualContent videoContent = null;
            for (Object content : result.getContents()) {
                if (content instanceof AudioVisualContent) {
                    videoContent = (AudioVisualContent) content;
                    break;
                }
            }
            assertNotNull(videoContent);
            assertNotNull(videoContent.getKeyFrameTimes());
            assertTrue(!videoContent.getKeyFrameTimes().isEmpty(), "Video result should contain keyframes");

            long firstKeyFrame = videoContent.getKeyFrameTimes().get(0).toMillis();
            BinaryData file = contentUnderstandingClient.getResultFile(operationId, "keyframes/" + firstKeyFrame);
            assertNotNull(file);
            assertTrue(file.toBytes().length > 0, "Result file should contain bytes");

            contentUnderstandingClient.deleteResult(operationId);
            operationId = null;
        } finally {
            deleteResultIfPresent(operationId);
        }
    }

    protected void verifyAsyncResultFileSupportsConfiguredServiceVersion() {
        AtomicReference<String> operationId = new AtomicReference<>();
        try {
            AnalysisInput input = new AnalysisInput().setUrl(
                "https://github.com/Azure-Samples/azure-ai-content-understanding-assets/raw/refs/heads/main/videos/sdk_samples/FlightSimulator.mp4");
            PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> poller = contentUnderstandingAsyncClient
                .beginAnalyze("prebuilt-videoSearch", Collections.singletonList(input));
            AnalysisResult result = poller.last()
                .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                    "Async common API video analysis")
                        .doOnNext(ignored -> operationId
                            .set(requireOperationId(response.getValue(), "Async common API video analysis"))))
                .block();
            assertAnalysisVersion(result);
            assertNotNull(operationId.get());

            AudioVisualContent videoContent = null;
            for (Object content : result.getContents()) {
                if (content instanceof AudioVisualContent) {
                    videoContent = (AudioVisualContent) content;
                    break;
                }
            }
            assertNotNull(videoContent);
            assertNotNull(videoContent.getKeyFrameTimes());
            assertTrue(!videoContent.getKeyFrameTimes().isEmpty(), "Video result should contain keyframes");

            long firstKeyFrame = videoContent.getKeyFrameTimes().get(0).toMillis();
            BinaryData file
                = contentUnderstandingAsyncClient.getResultFile(operationId.get(), "keyframes/" + firstKeyFrame)
                    .block();
            assertNotNull(file);
            assertTrue(file.toBytes().length > 0, "Result file should contain bytes");

            contentUnderstandingAsyncClient.deleteResult(operationId.get()).block();
            operationId.set(null);
        } finally {
            deleteResultIfPresentAsync(operationId.get());
        }
    }

    private ContentAnalyzer createAnalyzer(String description) {
        Map<String, ContentFieldDefinition> fields = new LinkedHashMap<>();
        fields.put("title",
            new ContentFieldDefinition().setType(ContentFieldType.STRING)
                .setMethod(GenerationMethod.EXTRACT)
                .setDescription("Document title"));
        ContentFieldSchema schema = new ContentFieldSchema().setName("common_api_schema")
            .setDescription("Schema used by the common API compatibility tests")
            .setFields(fields);
        return new ContentAnalyzer().setBaseAnalyzerId("prebuilt-document")
            .setDescription(description)
            .setConfig(new ContentAnalyzerConfig().setOcrEnabled(true).setLayoutEnabled(true))
            .setFieldSchema(schema)
            .setModels(Collections.singletonMap("completion", getModelProfile().getCompletionModel()));
    }

    private void deleteAnalyzerIfPresent(String analyzerId) {
        try {
            contentUnderstandingClient.deleteAnalyzer(analyzerId);
        } catch (RuntimeException ignored) {
            // Best-effort cleanup for a partially completed lifecycle.
        }
    }

    private void deleteAnalyzerIfPresentAsync(String analyzerId) {
        try {
            contentUnderstandingAsyncClient.deleteAnalyzer(analyzerId).block();
        } catch (RuntimeException ignored) {
            // Best-effort cleanup for a partially completed lifecycle.
        }
    }

    private void assertAnalysisVersion(AnalysisResult result) {
        assertNotNull(result);
        assertEquals(getServiceVersion().getVersion(), result.getApiVersion());
        assertNotNull(result.getContents());
        assertTrue(!result.getContents().isEmpty(), "Analysis result should contain content");
    }

    static <T> T requireSuccessfulResult(LongRunningOperationStatus status, Supplier<T> finalResult,
        String operationName) {
        if (status != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            throw new IllegalStateException(operationName + " completed unsuccessfully with status: " + status);
        }
        T result = finalResult.get();
        if (result == null) {
            throw new IllegalStateException(operationName + " completed without a final result.");
        }
        return result;
    }

    static <T> Mono<T> requireSuccessfulResult(LongRunningOperationStatus status, Mono<T> finalResult,
        String operationName) {
        if (status != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            return Mono
                .error(new IllegalStateException(operationName + " completed unsuccessfully with status: " + status));
        }
        return finalResult
            .switchIfEmpty(Mono.error(new IllegalStateException(operationName + " completed without a final result.")));
    }

    static String requireOperationId(ContentAnalyzerAnalyzeOperationStatus operationStatus, String operationName) {
        if (operationStatus == null || operationStatus.getId() == null || operationStatus.getId().trim().isEmpty()) {
            throw new IllegalStateException(operationName + " completed without an operation ID.");
        }
        return operationStatus.getId();
    }

    private void deleteResultIfPresent(String operationId) {
        if (operationId == null) {
            return;
        }
        try {
            contentUnderstandingClient.deleteResult(operationId);
        } catch (RuntimeException ignored) {
            // Best-effort cleanup for a partially completed analysis lifecycle.
        }
    }

    private void deleteResultIfPresentAsync(String operationId) {
        if (operationId == null) {
            return;
        }
        try {
            contentUnderstandingAsyncClient.deleteResult(operationId).block();
        } catch (RuntimeException ignored) {
            // Best-effort cleanup for a partially completed analysis lifecycle.
        }
    }
}
