// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.models.FoundryFeaturesOptInKeys;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FoundryFeaturesHeaderVerificationTest {
    private static final HttpHeaderName FOUNDRY_FEATURES = HttpHeaderName.fromString("Foundry-Features");

    @Test
    public void allowPreviewAddsAreaSpecificHeaders() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        AIProjectClientBuilder builder = createBuilder(httpClient).allowPreview(true);

        builder.beta().buildBetaModelsClient().getModelVersionWithResponse("model", "1", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.MODELS_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        builder.beta().buildBetaRedTeamsClient().getRedTeamWithResponse("red-team", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.RED_TEAMS_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        builder.beta()
            .buildBetaEvaluationTaxonomiesClient()
            .getEvaluationTaxonomyWithResponse("taxonomy", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.EVALUATIONS_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        builder.beta()
            .buildBetaEvaluatorsClient()
            .getEvaluatorVersionWithResponse("evaluator", "1", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.EVALUATIONS_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        builder.beta().buildBetaInsightsClient().getInsightWithResponse("insight", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.INSIGHTS_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        builder.beta().buildBetaSchedulesClient().getScheduleWithResponse("schedule", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.SCHEDULES_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        builder.beta().buildBetaRoutinesClient().getRoutineWithResponse("routine", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.ROUTINES_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        builder.beta().buildBetaSkillsClient().getSkillWithResponse("skill", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.SKILLS_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        builder.beta().buildBetaDatasetsClient().getGenerationJobWithResponse("job", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.DATA_GENERATION_JOBS_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        builder.buildEvaluationRulesClient()
            .createOrUpdateEvaluationRuleWithResponse("rule", BinaryData.fromString("{}"), new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.EVALUATIONS_V1_PREVIEW.toString(), foundryFeatures(httpClient));
    }

    @Test
    public void allowPreviewDoesNotOverrideExplicitHeader() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        String explicitHeader = FoundryFeaturesOptInKeys.INSIGHTS_V1_PREVIEW.toString();
        RequestOptions requestOptions = new RequestOptions().setHeader(FOUNDRY_FEATURES, explicitHeader);

        createBuilder(httpClient).allowPreview(true)
            .beta()
            .buildBetaDatasetsClient()
            .getGenerationJobWithResponse("job", requestOptions);

        assertEquals(explicitHeader, foundryFeatures(httpClient));
    }

    @Test
    public void allowPreviewFalseDoesNotAddHeader() {
        RecordingHttpClient httpClient = new RecordingHttpClient();

        createBuilder(httpClient).allowPreview(false)
            .beta()
            .buildBetaDatasetsClient()
            .getGenerationJobWithResponse("job", new RequestOptions());

        assertNull(foundryFeatures(httpClient));
    }

    @Test
    public void allowPreviewUsesBuiltClientFeatureHeaderWithoutPathMatching() {
        RecordingHttpClient httpClient = new RecordingHttpClient();

        createBuilder(httpClient).endpoint("https://localhost:8080/api/projects/project/evaluations/evaluation")
            .allowPreview(true)
            .beta()
            .buildBetaDatasetsClient()
            .getGenerationJobWithResponse("job", new RequestOptions());

        assertEquals(FoundryFeaturesOptInKeys.DATA_GENERATION_JOBS_V1_PREVIEW.toString(), foundryFeatures(httpClient));
    }

    private static AIProjectClientBuilder createBuilder(RecordingHttpClient httpClient) {
        return new AIProjectClientBuilder().endpoint("https://localhost:8080/api/projects/project")
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .serviceVersion(AIProjectsServiceVersion.V1);
    }

    private static String foundryFeatures(RecordingHttpClient httpClient) {
        return httpClient.getLastRequest().getHeaders().getValue(FOUNDRY_FEATURES);
    }

    private static final class RecordingHttpClient implements HttpClient {
        private final List<HttpRequest> requests = new ArrayList<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            this.requests.add(request);
            HttpHeaders responseHeaders
                = new HttpHeaders().set(HttpHeaderName.fromString("Content-Type"), "application/json");
            return Mono
                .just(new MockHttpResponse(request, 200, responseHeaders, "{}".getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request, Context context) {
            return send(request);
        }

        HttpRequest getLastRequest() {
            return this.requests.get(this.requests.size() - 1);
        }
    }
}
