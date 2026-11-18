// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.implementation.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.FeatureFilter;
import com.azure.data.appconfiguration.models.FeatureFlag;
import com.azure.data.appconfiguration.models.FeatureFlagAllocation;
import com.azure.data.appconfiguration.models.FeatureFlagConditions;
import com.azure.data.appconfiguration.models.FeatureFlagFields;
import com.azure.data.appconfiguration.models.FeatureFlagSelector;
import com.azure.data.appconfiguration.models.FeatureFlagTelemetryConfiguration;
import com.azure.data.appconfiguration.models.FeatureFlagVariantDefinition;
import com.azure.data.appconfiguration.models.PercentileAllocation;
import com.azure.data.appconfiguration.models.RequirementType;
import com.azure.data.appconfiguration.models.SettingLabel;
import com.azure.data.appconfiguration.models.SettingLabelSelector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ParameterizedClass
@MethodSource("getTestParameters")
public class FeatureFlagIntegrationTest extends TestProxyTestBase {
    private final HttpClient httpClient;
    private final boolean async;
    private final List<FeatureFlag> createdFlags = new ArrayList<>();
    private FeatureFlagClient client;
    private FeatureFlagAsyncClient asyncClient;
    private String namePrefix;

    public FeatureFlagIntegrationTest(HttpClient httpClient, boolean async) {
        this.httpClient = httpClient;
        this.async = async;
    }

    static Stream<Arguments> getTestParameters() {
        return getHttpClients()
            .flatMap(httpClient -> Stream.of(Arguments.of(httpClient, false), Arguments.of(httpClient, true)));
    }

    @Override
    protected void beforeTest() {
        String endpoint = interceptorManager.isPlaybackMode()
            ? new ConfigurationClientCredentials(ConfigurationClientTestBase.FAKE_CONNECTION_STRING).getBaseUri()
            : Objects.requireNonNull(Configuration.getGlobalConfiguration().get("AZ_CONFIG_ENDPOINT"),
                "Set AZ_CONFIG_ENDPOINT to an App Configuration endpoint supporting 2026-05-01-preview.");
        namePrefix = testResourceNamer.randomName("fflive", 24);
        AssertingHttpClientBuilder transport = new AssertingHttpClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
        FeatureFlagClientBuilder builder = new FeatureFlagClientBuilder().endpoint(endpoint)
            .credential(TestHelper.getTokenCredential(interceptorManager))
            .serviceVersion(ConfigurationServiceVersion.V2026_05_01_PREVIEW)
            .httpClient((async ? transport.assertAsync() : transport.assertSync()).build());
        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        } else if (interceptorManager.isPlaybackMode()) {
            interceptorManager.addMatchers(Collections
                .singletonList(new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("Sync-Token"))));
        }
        if (!interceptorManager.isLiveMode()) {
            // Preserve synthetic feature names, ETags, and numeric percentile bounds.
            interceptorManager.removeSanitizers("AZSDK3493", "AZSDK3490", "AZSDK3424", "AZSDK3425");
        }
        if (async) {
            asyncClient = builder.buildAsyncClient();
        } else {
            client = builder.buildClient();
        }
    }

    @Override
    protected void afterTest() {
        assertAll("Deleting feature flags created by this test",
            createdFlags.stream().map(flag -> () -> deleteFeatureFlag(flag, false)));
        createdFlags.clear();
    }

    @Test
    public void featureFlagLifecycle() {
        FeatureFlag expected = new FeatureFlag(namePrefix, true).setLabel(namePrefix + "-prod")
            .setDescription("Live feature flag")
            .setConditions(new FeatureFlagConditions().setRequirementType(RequirementType.ANY)
                .setFilters(Collections.singletonList(
                    new FeatureFilter("Microsoft.Percentage").setParameters(Collections.singletonMap("Value", "50")))))
            .setVariants(Arrays.asList(
                new FeatureFlagVariantDefinition("control").setValue("false").setContentType("application/json"),
                new FeatureFlagVariantDefinition("treatment").setValue("true").setContentType("application/json")))
            .setAllocation(new FeatureFlagAllocation().setDefaultWhenDisabled("control")
                .setDefaultWhenEnabled("treatment")
                .setSeed(namePrefix)
                .setPercentile(Collections.singletonList(new PercentileAllocation("treatment", 0, 50))))
            .setTelemetry(new FeatureFlagTelemetryConfiguration(true)
                .setMetadata(Collections.singletonMap("environment", "test")))
            .setTags(Collections.singletonMap("suite", namePrefix));
        FeatureFlag created = createFeatureFlag(expected);
        assertNotNull(created.getETag());
        assertNotNull(created.getLastModified());

        FeatureFlag retrieved = getFeatureFlag(expected.getName(), expected.getLabel(), null, null).getValue();
        assertNotNull(retrieved);
        assertEquals(expected.getName(), retrieved.getName());
        assertEquals(expected.getLabel(), retrieved.getLabel());
        assertTrue(retrieved.isEnabled());
        assertEquals(expected.getDescription(), retrieved.getDescription());
        assertEquals(expected.getTags(), retrieved.getTags());
        assertEquals(RequirementType.ANY, retrieved.getConditions().getRequirementType());
        assertEquals("Microsoft.Percentage", retrieved.getConditions().getFilters().get(0).getName());
        assertEquals(Collections.singletonMap("Value", "50"),
            retrieved.getConditions().getFilters().get(0).getParameters());
        assertEquals(Arrays.asList("control", "treatment"),
            retrieved.getVariants().stream().map(FeatureFlagVariantDefinition::getName).collect(Collectors.toList()));
        assertEquals("true", retrieved.getVariants().get(1).getValue());
        assertEquals("application/json", retrieved.getVariants().get(1).getContentType());
        assertEquals("control", retrieved.getAllocation().getDefaultWhenDisabled());
        assertEquals("treatment", retrieved.getAllocation().getDefaultWhenEnabled());
        assertEquals(namePrefix, retrieved.getAllocation().getSeed());
        assertEquals(50, retrieved.getAllocation().getPercentile().get(0).getTo());
        assertTrue(retrieved.getTelemetry().isEnabled());
        assertEquals(Collections.singletonMap("environment", "test"), retrieved.getTelemetry().getMetadata());

        FeatureFlag updated = setFeatureFlag(retrieved.setEnabled(false).setDescription("Updated live flag"), true);
        assertNotEquals(created.getETag(), updated.getETag());
        FeatureFlag current = getFeatureFlag(updated.getName(), updated.getLabel(), null, null).getValue();
        assertFalse(current.isEnabled());
        assertEquals("Updated live flag", current.getDescription());

        Response<FeatureFlag> deleted = deleteFeatureFlag(updated, true);
        assertTrue(deleted.getStatusCode() == 200 || deleted.getStatusCode() == 204);
        HttpResponseException missing = assertThrows(HttpResponseException.class,
            () -> getFeatureFlag(updated.getName(), updated.getLabel(), null, null));
        assertEquals(404, missing.getResponse().getStatusCode());
        assertEquals(204, deleteFeatureFlag(updated, false).getStatusCode());
    }

    @Test
    public void conditionalOperations() {
        FeatureFlag initial = createFeatureFlag(new FeatureFlag(namePrefix, true));
        HttpResponseException duplicate
            = assertThrows(HttpResponseException.class, () -> addFeatureFlag(new FeatureFlag(namePrefix, false)));
        assertEquals(412, duplicate.getResponse().getStatusCode());

        FeatureFlag updated = setFeatureFlag(initial.setDescription("Changed"), true);
        assertNotEquals(initial.getETag(), updated.getETag());
        HttpResponseException staleUpdate
            = assertThrows(HttpResponseException.class, () -> setFeatureFlag(initial, true));
        assertEquals(412, staleUpdate.getResponse().getStatusCode());
        HttpResponseException staleDelete
            = assertThrows(HttpResponseException.class, () -> deleteFeatureFlag(initial, true));
        assertEquals(412, staleDelete.getResponse().getStatusCode());

        Response<FeatureFlag> unchanged
            = getFeatureFlag(namePrefix, null, new MatchConditions().setIfNoneMatch(updated.getETag()), null);
        assertEquals(304, unchanged.getStatusCode());
        assertNull(unchanged.getValue());
        Response<FeatureFlag> changed
            = getFeatureFlag(namePrefix, null, new MatchConditions().setIfNoneMatch(initial.getETag()), null);
        assertEquals(200, changed.getStatusCode());
        assertEquals(updated.getETag(), changed.getValue().getETag());
    }

    @Test
    public void labelAndTagFiltering() {
        String production = namePrefix + "-prod";
        String staging = namePrefix + "-stage";
        createFeatureFlag(new FeatureFlag(namePrefix, false));
        createFeatureFlag(new FeatureFlag(namePrefix, true).setLabel(production)
            .setTags(Collections.singletonMap("stage", "production")));
        createFeatureFlag(new FeatureFlag(namePrefix, false).setLabel(staging)
            .setTags(Collections.singletonMap("stage", "production")));
        createFeatureFlag(new FeatureFlag(namePrefix + "-other", false).setLabel(production)
            .setTags(Collections.singletonMap("stage", "staging")));

        assertEquals(3, listFeatureFlags(new FeatureFlagSelector().setNameFilter(namePrefix), false).size());
        List<FeatureFlag> unlabeled
            = listFeatureFlags(new FeatureFlagSelector().setNameFilter(namePrefix).setLabelFilter("\0"), false);
        assertEquals(1, unlabeled.size());
        assertTrue(unlabeled.get(0).getLabel() == null || unlabeled.get(0).getLabel().isEmpty());

        List<FeatureFlag> filtered = listFeatureFlags(new FeatureFlagSelector().setNameFilter(namePrefix + "*")
            .setLabelFilter(production)
            .setTagsFilter(Collections.singletonList("stage=production"))
            .setFields(FeatureFlagFields.NAME, FeatureFlagFields.LABEL, FeatureFlagFields.ENABLED,
                FeatureFlagFields.TAGS),
            false);
        assertEquals(1, filtered.size());
        assertEquals(namePrefix, filtered.get(0).getName());
        assertEquals(production, filtered.get(0).getLabel());
        assertTrue(filtered.get(0).isEnabled());
        assertEquals(Collections.singletonMap("stage", "production"), filtered.get(0).getTags());
        assertNull(filtered.get(0).getETag());

        SettingLabelSelector selector = new SettingLabelSelector().setNameFilter(namePrefix + "*");
        List<SettingLabel> labels = async
            ? asyncClient.listLabels(selector).collectList().block()
            : client.listLabels(selector).stream().collect(Collectors.toList());
        assertNotNull(labels);
        assertEquals(Arrays.asList(production, staging),
            labels.stream().map(SettingLabel::getName).sorted().collect(Collectors.toList()));
    }

    @Test
    public void historicalReadsAndRevisions() {
        FeatureFlag initial = createFeatureFlag(new FeatureFlag(namePrefix, true).setDescription("Before"));
        assertNotNull(initial.getLastModified());
        OffsetDateTime historicalTime = initial.getLastModified().withNano(0).plusSeconds(1);
        // Accept-Datetime has second precision, so the update must occur in a later second.
        sleepIfRunningAgainstService(
            Math.max(0, Duration.between(OffsetDateTime.now(), historicalTime.plusSeconds(1)).toMillis()));
        FeatureFlag updated = setFeatureFlag(initial.setDescription("After").setEnabled(false), true);

        FeatureFlag historical = getFeatureFlag(namePrefix, null, null, historicalTime).getValue();
        assertNotNull(historical);
        assertEquals("Before", historical.getDescription());
        assertTrue(historical.isEnabled());
        assertEquals(initial.getETag(), historical.getETag());
        List<FeatureFlag> historicalList = listFeatureFlags(
            new FeatureFlagSelector().setNameFilter(namePrefix).setAcceptDatetime(historicalTime), false);
        assertEquals(1, historicalList.size());
        assertEquals(initial.getETag(), historicalList.get(0).getETag());

        List<FeatureFlag> revisions = listFeatureFlags(new FeatureFlagSelector().setNameFilter(namePrefix), true);
        assertEquals(2, revisions.size());
        List<String> etags = revisions.stream().map(FeatureFlag::getETag).collect(Collectors.toList());
        assertTrue(etags.contains(initial.getETag()));
        assertTrue(etags.contains(updated.getETag()));
        assertEquals("After", getFeatureFlag(namePrefix, null, null, null).getValue().getDescription());
    }

    @Test
    public void conditionalPaging() {
        FeatureFlag first = createFeatureFlag(new FeatureFlag(namePrefix + "-first", true));
        createFeatureFlag(new FeatureFlag(namePrefix + "-second", false));
        FeatureFlagSelector selector = new FeatureFlagSelector().setNameFilter(namePrefix + "*");
        List<PagedResponse<FeatureFlag>> initialPages = listPages(selector, false);
        assertEquals(2, initialPages.stream().mapToInt(page -> page.getValue().size()).sum());
        List<MatchConditions> conditions = initialPages.stream().map(page -> {
            String etag = page.getHeaders().getValue(HttpHeaderName.ETAG);
            assertNotNull(etag);
            return new MatchConditions().setIfNoneMatch(etag);
        }).collect(Collectors.toList());
        selector.setMatchConditions(conditions);

        List<PagedResponse<FeatureFlag>> unchangedPages = listPages(selector, false);
        assertEquals(initialPages.size(), unchangedPages.size());
        unchangedPages.forEach(page -> {
            assertEquals(304, page.getStatusCode());
            assertTrue(page.getValue().isEmpty());
        });

        FeatureFlag updated = setFeatureFlag(first.setDescription("Invalidates the page ETag"), true);
        List<PagedResponse<FeatureFlag>> changedPages = listPages(selector, false);
        assertTrue(changedPages.stream().anyMatch(page -> page.getStatusCode() == 200));
        assertTrue(changedPages.stream()
            .flatMap(page -> page.getValue().stream())
            .anyMatch(flag -> updated.getETag().equals(flag.getETag())));
    }

    private FeatureFlag createFeatureFlag(FeatureFlag flag) {
        FeatureFlag created = addFeatureFlag(flag).getValue();
        createdFlags.add(flag);
        assertNotNull(created);
        return created;
    }

    private Response<FeatureFlag> addFeatureFlag(FeatureFlag flag) {
        return async
            ? asyncClient.addFeatureFlagWithResponse(flag).block()
            : client.addFeatureFlagWithResponse(flag, Context.NONE);
    }

    private FeatureFlag setFeatureFlag(FeatureFlag flag, boolean ifUnchanged) {
        Response<FeatureFlag> response = async
            ? asyncClient.setFeatureFlagWithResponse(flag, ifUnchanged).block()
            : client.setFeatureFlagWithResponse(flag, ifUnchanged, Context.NONE);
        return response.getValue();
    }

    private Response<FeatureFlag> getFeatureFlag(String name, String label, MatchConditions conditions,
        OffsetDateTime acceptDateTime) {
        return async
            ? asyncClient.getFeatureFlagWithResponse(name, label, conditions, acceptDateTime).block()
            : client.getFeatureFlagWithResponse(name, label, conditions, acceptDateTime, Context.NONE);
    }

    private Response<FeatureFlag> deleteFeatureFlag(FeatureFlag flag, boolean ifUnchanged) {
        return async
            ? asyncClient.deleteFeatureFlagWithResponse(flag, ifUnchanged).block()
            : client.deleteFeatureFlagWithResponse(flag, ifUnchanged, Context.NONE);
    }

    private List<FeatureFlag> listFeatureFlags(FeatureFlagSelector selector, boolean revisions) {
        return listPages(selector, revisions).stream()
            .flatMap(page -> page.getValue().stream())
            .collect(Collectors.toList());
    }

    private List<PagedResponse<FeatureFlag>> listPages(FeatureFlagSelector selector, boolean revisions) {
        if (async) {
            return (revisions ? asyncClient.listFeatureFlagRevisions(selector) : asyncClient.listFeatureFlags(selector))
                .byPage()
                .collectList()
                .block();
        }
        PagedIterable<FeatureFlag> flags
            = revisions ? client.listFeatureFlagRevisions(selector) : client.listFeatureFlags(selector);
        List<PagedResponse<FeatureFlag>> pages = new ArrayList<>();
        flags.iterableByPage().forEach(pages::add);
        return pages;
    }
}
