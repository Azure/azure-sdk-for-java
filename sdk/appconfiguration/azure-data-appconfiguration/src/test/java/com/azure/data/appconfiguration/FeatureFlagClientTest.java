// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationAudience;
import com.azure.data.appconfiguration.models.FeatureFlag;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFields;
import com.azure.data.appconfiguration.models.FeatureFlagSelector;
import com.azure.data.appconfiguration.models.SettingLabel;
import com.azure.data.appconfiguration.models.SettingLabelFields;
import com.azure.data.appconfiguration.models.SettingLabelSelector;
import java.time.OffsetDateTime;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureFlagClientTest {
    private static final String ENDPOINT = "https://feature-flags.azconfig.io";
    private static final String RESOURCE = "{\"name\":\"MyFeature\",\"enabled\":true,\"label\":\"production\","
        + "\"etag\":\"version1\",\"last_modified\":\"2026-10-01T10:00:00Z\"}";
    private static final String MEDIA_TYPE
        = "application/json;profile=\"https://azconfig.io/mime-profiles/ff\";charset=utf-8";
    private static final TokenCredential CREDENTIAL
        = request -> Mono.just(new AccessToken("test-token", OffsetDateTime.MAX));

    private FeatureFlagClientBuilder builder(Function<HttpRequest, HttpResponse> responder) {
        HttpClient httpClient = request -> Mono.fromSupplier(() -> responder.apply(request));
        return new FeatureFlagClientBuilder().endpoint(ENDPOINT).credential(CREDENTIAL).httpClient(httpClient);
    }

    private static MockHttpResponse response(HttpRequest request, int status, String body) {
        return new MockHttpResponse(request, status,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
            body.getBytes(StandardCharsets.UTF_8));
    }

    private static MockHttpResponse withHeader(MockHttpResponse response, HttpHeaderName name, String value) {
        response.getHeaders().set(name, value);
        return response;
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void addUsesDedicatedResourceAndCreateOnlyCondition(boolean async) {
        AtomicInteger requests = new AtomicInteger();
        FeatureFlagClientBuilder builder = builder(request -> {
            requests.incrementAndGet();
            assertEquals(HttpMethod.PUT, request.getHttpMethod());
            assertEquals("/ff/MyFeature", request.getUrl().getPath());
            assertTrue(request.getUrl().getQuery().contains("api-version=2026-05-01-preview"));
            assertTrue(request.getUrl().getQuery().contains("label=production"));
            assertEquals("*", request.getHeaders().getValue(HttpHeaderName.IF_NONE_MATCH));
            assertEquals(MEDIA_TYPE, request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            assertEquals(MEDIA_TYPE + ", application/problem+json",
                request.getHeaders().getValue(HttpHeaderName.ACCEPT));
            assertFalse(request.getHeaders().getValue(HttpHeaderName.ACCEPT).contains("\\"));
            String body = request.getBodyAsBinaryData().toString();
            assertTrue(body.contains("\"name\":\"MyFeature\""));
            assertTrue(body.contains("\"enabled\":true"));
            assertFalse(body.contains(".appconfig.featureflag/"));
            assertFalse(body.contains("\"value\":"));
            return response(request, 200, RESOURCE);
        });
        FeatureFlag flag = new FeatureFlag("MyFeature", true).setLabel("production");
        FeatureFlag result = async
            ? builder.buildAsyncClient().addFeatureFlag(flag).block()
            : builder.buildClient().addFeatureFlag(flag);
        assertNotNull(result);
        assertEquals("version1", result.getETag());
        assertEquals(OffsetDateTime.parse("2026-10-01T10:00:00Z"), result.getLastModified());
        assertEquals(1, requests.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void setAndDeleteHonorETags(boolean async) {
        AtomicInteger requests = new AtomicInteger();
        FeatureFlagClientBuilder builder = builder(request -> {
            requests.incrementAndGet();
            assertEquals("\"version1\"", request.getHeaders().getValue(HttpHeaderName.IF_MATCH));
            assertNull(request.getHeaders().getValue(HttpHeaderName.IF_NONE_MATCH));
            return response(request, 200, RESOURCE);
        });
        FeatureFlag flag = new FeatureFlag("MyFeature", true).setETag("version1").setLabel("production");
        if (async) {
            FeatureFlagAsyncClient client = builder.buildAsyncClient();
            assertEquals("MyFeature", client.setFeatureFlagWithResponse(flag, true).block().getValue().getName());
            assertEquals("MyFeature", client.deleteFeatureFlagWithResponse(flag, true).block().getValue().getName());
        } else {
            FeatureFlagClient client = builder.buildClient();
            assertEquals("MyFeature", client.setFeatureFlagWithResponse(flag, true, Context.NONE).getValue().getName());
            assertEquals("MyFeature",
                client.deleteFeatureFlagWithResponse(flag, true, Context.NONE).getValue().getName());
        }
        assertEquals(2, requests.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void unconditionalSetDoesNotSendETag(boolean async) {
        FeatureFlagClientBuilder builder = builder(request -> {
            assertNull(request.getHeaders().getValue(HttpHeaderName.IF_MATCH));
            assertNull(request.getHeaders().getValue(HttpHeaderName.IF_NONE_MATCH));
            return response(request, 200, RESOURCE);
        });
        FeatureFlag flag = new FeatureFlag("MyFeature", true).setETag("old");
        assertNotNull(async
            ? builder.buildAsyncClient().setFeatureFlag(flag).block()
            : builder.buildClient().setFeatureFlag(flag));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void conditionalGetPreservesNotModifiedResponse(boolean async) {
        OffsetDateTime dateTime = OffsetDateTime.parse("2026-10-01T10:00:00Z");
        FeatureFlagClientBuilder builder = builder(request -> {
            assertEquals(HttpMethod.GET, request.getHttpMethod());
            assertEquals("\"version1\"", request.getHeaders().getValue(HttpHeaderName.IF_NONE_MATCH));
            assertEquals("Thu, 1 Oct 2026 10:00:00 GMT",
                request.getHeaders().getValue(HttpHeaderName.fromString("Accept-Datetime")));
            return withHeader(response(request, 304, ""), HttpHeaderName.ETAG, "\"version1\"");
        });
        MatchConditions conditions = new MatchConditions().setIfNoneMatch("\"version1\"");
        Response<FeatureFlag> result = async
            ? builder.buildAsyncClient()
                .getFeatureFlagWithResponse("MyFeature", "production", conditions, dateTime)
                .block()
            : builder.buildClient()
                .getFeatureFlagWithResponse("MyFeature", "production", conditions, dateTime, Context.NONE);
        assertNotNull(result);
        assertEquals(304, result.getStatusCode());
        assertNull(result.getValue());
        assertEquals("\"version1\"", result.getHeaders().getValue(HttpHeaderName.ETAG));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void deleteAllowsEmptyResponse(boolean async) {
        FeatureFlagClientBuilder builder = builder(request -> response(request, 204, ""));
        assertNull(async
            ? builder.buildAsyncClient().deleteFeatureFlag("MyFeature").block()
            : builder.buildClient().deleteFeatureFlag("MyFeature"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void listPreservesFiltersAndHeadersAcrossPages(boolean async) {
        AtomicInteger requests = new AtomicInteger();
        FeatureFlagClientBuilder builder = builder(request -> {
            int page = requests.getAndIncrement();
            assertEquals("/ff", request.getUrl().getPath());
            String query = request.getUrl().getQuery();
            assertTrue(query.contains("name=My%2A"), query);
            assertTrue(query.contains("label=production"));
            assertTrue(query.contains("tags=team%3Dplatform"));
            assertTrue(query.contains("tags=stage%3Dpreview"));
            assertTrue(query.contains("%24select=name%2Cenabled"));
            assertEquals("\"page" + page + "\"", request.getHeaders().getValue(HttpHeaderName.IF_NONE_MATCH));
            assertNotNull(request.getHeaders().getValue(HttpHeaderName.fromString("Accept-Datetime")));
            assertTrue(request.getHeaders()
                .getValue(HttpHeaderName.ACCEPT)
                .contains("profile=\"https://azconfig.io/mime-profiles/ffset\""));
            if (page == 0) {
                assertFalse(query.contains("after="));
                return withHeader(
                    response(request, 200,
                        "{\"items\":[" + RESOURCE + "],"
                            + "\"@nextLink\":\"/ff?api-version=2026-05-01-preview&after=abc%2B%3D\"}"),
                    HttpHeaderName.ETAG, "\"page0\"");
            }
            assertTrue(query.contains("after=abc%2B%3D"));
            return withHeader(response(request, 200, "{\"items\":[" + RESOURCE + "]}"), HttpHeaderName.ETAG,
                "\"page1\"");
        });
        FeatureFlagSelector selector = new FeatureFlagSelector().setNameFilter("My*")
            .setLabelFilter("production")
            .setTagsFilter(Arrays.asList("team=platform", "stage=preview"))
            .setFields(FeatureFlagFields.NAME, FeatureFlagFields.ENABLED)
            .setAcceptDatetime(OffsetDateTime.parse("2026-10-01T10:00:00Z"))
            .setMatchConditions(Arrays.asList(new MatchConditions().setIfNoneMatch("page0"),
                new MatchConditions().setIfNoneMatch("page1")));
        List<PagedResponse<FeatureFlag>> pages = new ArrayList<>();
        if (async) {
            pages.addAll(builder.buildAsyncClient().listFeatureFlags(selector).byPage().collectList().block());
        } else {
            builder.buildClient().listFeatureFlags(selector).iterableByPage().forEach(pages::add);
        }
        assertEquals(2, pages.size());
        assertEquals(2, requests.get());
        assertEquals("MyFeature", pages.get(1).getValue().get(0).getName());
        assertEquals("\"page1\"", pages.get(1).getHeaders().getValue(HttpHeaderName.ETAG));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void unchangedPageContinuesUsingLinkHeader(boolean async) {
        AtomicInteger requests = new AtomicInteger();
        FeatureFlagClientBuilder builder = builder(request -> {
            if (requests.getAndIncrement() == 0) {
                return withHeader(response(request, 304, ""), HttpHeaderName.LINK,
                    "</ff?api-version=2026-05-01-preview&after=next>; rel=\"next\"");
            }
            assertTrue(request.getUrl().getQuery().contains("after=next"));
            assertEquals("\"second\"", request.getHeaders().getValue(HttpHeaderName.IF_NONE_MATCH));
            return response(request, 200, "{\"items\":[" + RESOURCE + "]}");
        });
        FeatureFlagSelector selector = new FeatureFlagSelector().setMatchConditions(Arrays
            .asList(new MatchConditions().setIfNoneMatch("first"), new MatchConditions().setIfNoneMatch("second")));
        List<PagedResponse<FeatureFlag>> pages = new ArrayList<>();
        if (async) {
            pages.addAll(builder.buildAsyncClient().listFeatureFlags(selector).byPage().collectList().block());
        } else {
            builder.buildClient().listFeatureFlags(selector).iterableByPage().forEach(pages::add);
        }
        assertEquals(304, pages.get(0).getStatusCode());
        assertTrue(pages.get(0).getValue().isEmpty());
        assertEquals(1, pages.get(1).getValue().size());
        assertEquals(2, requests.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void revisionsUseSeparateEndpoint(boolean async) {
        FeatureFlagClientBuilder builder = builder(request -> {
            assertEquals("/ff-revisions", request.getUrl().getPath());
            assertTrue(request.getUrl().getQuery().contains("name=MyFeature"));
            return response(request, 200, "{\"items\":[" + RESOURCE + "]}");
        });
        FeatureFlagSelector selector = new FeatureFlagSelector().setNameFilter("MyFeature");
        long count = async
            ? builder.buildAsyncClient().listFeatureFlagRevisions(selector).count().block()
            : builder.buildClient().listFeatureFlagRevisions(selector).stream().count();
        assertEquals(1, count);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void labelListingIsResourceSpecific(boolean async) {
        List<String> resourceTypes = new ArrayList<>();
        HttpClient httpClient = request -> {
            assertEquals("/labels", request.getUrl().getPath());
            resourceTypes.add(request.getUrl().getQuery());
            return Mono.just(response(request, 200, "{\"items\":[{\"name\":\"production\"}]}"));
        };
        FeatureFlagClientBuilder featureBuilder
            = new FeatureFlagClientBuilder().endpoint(ENDPOINT).credential(CREDENTIAL).httpClient(httpClient);
        ConfigurationClientBuilder configurationBuilder
            = new ConfigurationClientBuilder().endpoint(ENDPOINT).credential(CREDENTIAL).httpClient(httpClient);
        SettingLabelSelector selector
            = new SettingLabelSelector().setNameFilter("prod*").setFields(SettingLabelFields.NAME);
        List<SettingLabel> labels = async
            ? featureBuilder.buildAsyncClient().listLabels(selector).collectList().block()
            : featureBuilder.buildClient().listLabels(selector).stream().collect(Collectors.toList());
        assertEquals("production", labels.get(0).getName());
        if (async) {
            configurationBuilder.buildAsyncClient().listLabels(selector).collectList().block();
        } else {
            configurationBuilder.buildClient().listLabels(selector).stream().count();
        }
        assertTrue(resourceTypes.get(0).contains("resourcetype=ff"));
        assertTrue(resourceTypes.get(1).contains("resourcetype=kv"));
    }

    @Test
    void olderKeyValueVersionsDoNotSendResourceType() {
        HttpClient httpClient = request -> {
            assertFalse(request.getUrl().getQuery().contains("resourcetype"));
            return Mono.just(response(request, 200, "{\"items\":[]}"));
        };
        new ConfigurationClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIAL)
            .httpClient(httpClient)
            .serviceVersion(ConfigurationServiceVersion.V2023_11_01)
            .buildClient()
            .listLabels(new SettingLabelSelector())
            .stream()
            .count();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void legacyFeatureFlagSettingsStillUseKeyValues(boolean async) {
        HttpClient httpClient = request -> {
            assertTrue(request.getUrl().getPath().startsWith("/kv/"));
            assertFalse(request.getUrl().getPath().startsWith("/ff/"));
            assertTrue(request.getUrl().getPath().contains(".appconfig.featureflag"));
            assertTrue(
                request.getBodyAsBinaryData().toString().contains("application/vnd.microsoft.appconfig.ff+json"));
            return Mono.just(response(request, 200,
                "{\"key\":\".appconfig.featureflag/MyFeature\","
                    + "\"value\":\"{\\\"id\\\":\\\"MyFeature\\\",\\\"enabled\\\":true,"
                    + "\\\"conditions\\\":{\\\"client_filters\\\":[]}}\","
                    + "\"content_type\":\"application/vnd.microsoft.appconfig.ff+json;charset=utf-8\"}"));
        };
        ConfigurationClientBuilder builder
            = new ConfigurationClientBuilder().endpoint(ENDPOINT).credential(CREDENTIAL).httpClient(httpClient);
        FeatureFlagConfigurationSetting flag = new FeatureFlagConfigurationSetting("MyFeature", true);
        assertTrue((async
            ? builder.buildAsyncClient().setConfigurationSetting(flag).block()
            : builder.buildClient().setConfigurationSetting(flag)) instanceof FeatureFlagConfigurationSetting);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void syncTokensAreReused(boolean async) {
        AtomicInteger requests = new AtomicInteger();
        FeatureFlagClientBuilder builder = builder(request -> {
            String syncToken = request.getHeaders().getValue(HttpHeaderName.fromString("Sync-Token"));
            assertTrue(syncToken.contains("external=externalValue"));
            if (requests.getAndIncrement() > 0) {
                assertTrue(syncToken.contains("server=serverValue"));
            }
            return withHeader(response(request, 200, RESOURCE), HttpHeaderName.fromString("Sync-Token"),
                "server=serverValue;sn=1");
        });
        if (async) {
            FeatureFlagAsyncClient client = builder.buildAsyncClient();
            client.updateSyncToken("external=externalValue;sn=1");
            client.getFeatureFlag("MyFeature").block();
            client.getFeatureFlag("MyFeature").block();
        } else {
            FeatureFlagClient client = builder.buildClient();
            client.updateSyncToken("external=externalValue;sn=1");
            client.getFeatureFlag("MyFeature");
            client.getFeatureFlag("MyFeature");
        }
        assertEquals(2, requests.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void audienceIsAppliedToAuthentication(boolean async) {
        List<String> scopes = new ArrayList<>();
        TokenCredential credential = request -> {
            scopes.addAll(request.getScopes());
            return Mono.just(new AccessToken("test-token", OffsetDateTime.MAX));
        };
        FeatureFlagClientBuilder builder = builder(request -> {
            assertEquals("Bearer test-token", request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
            return response(request, 200, RESOURCE);
        }).credential(credential).audience(ConfigurationAudience.AZURE_GOVERNMENT);
        if (async) {
            builder.buildAsyncClient().getFeatureFlag("MyFeature").block();
        } else {
            builder.buildClient().getFeatureFlag("MyFeature");
        }
        assertEquals(Collections.singletonList(ConfigurationAudience.AZURE_GOVERNMENT + "/.default"), scopes);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void errorsAreNotTreatedAsEmptyResources(boolean async) {
        for (int status : new int[] { 401, 404, 409, 412 }) {
            FeatureFlagClientBuilder builder = builder(request -> response(request, status,
                "{\"type\":\"test\",\"title\":\"Rejected\",\"status\":" + status + "}"));
            HttpResponseException error = assertThrows(HttpResponseException.class, () -> {
                if (async) {
                    builder.buildAsyncClient().getFeatureFlag("MyFeature").block();
                } else {
                    builder.buildClient().getFeatureFlag("MyFeature");
                }
            });
            assertEquals(status, error.getResponse().getStatusCode());
        }
    }

    @Test
    void validatesBeforeSendingRequests() {
        FeatureFlagClientBuilder builder = builder(request -> {
            throw new AssertionError("Invalid input must not send requests.");
        });
        FeatureFlagClient client = builder.buildClient();
        FeatureFlagAsyncClient asyncClient = builder.buildAsyncClient();
        assertThrows(IllegalArgumentException.class, () -> client.getFeatureFlag(""));
        assertThrows(IllegalArgumentException.class, () -> asyncClient.getFeatureFlag("").block());
        assertThrows(NullPointerException.class, () -> client.addFeatureFlag(null));
        assertThrows(NullPointerException.class, () -> asyncClient.addFeatureFlag(null).block());
        FeatureFlag flag = new FeatureFlag("MyFeature", true);
        assertThrows(IllegalArgumentException.class, () -> client.setFeatureFlagWithResponse(flag, true, Context.NONE));
        assertThrows(IllegalArgumentException.class, () -> asyncClient.setFeatureFlagWithResponse(flag, true).block());
        assertThrows(IllegalArgumentException.class,
            () -> client.deleteFeatureFlagWithResponse(flag, true, Context.NONE));
        assertThrows(IllegalArgumentException.class,
            () -> asyncClient.deleteFeatureFlagWithResponse(flag, true).block());
        assertThrows(IllegalArgumentException.class,
            () -> builder.serviceVersion(ConfigurationServiceVersion.V2023_11_01).buildClient());
        assertThrows(IllegalArgumentException.class, builder::buildAsyncClient);
    }

    @Test
    void repeatedIterationResetsPageConditions() {
        FeatureFlagClientBuilder builder = builder(request -> {
            assertEquals("\"first\"", request.getHeaders().getValue(HttpHeaderName.IF_NONE_MATCH));
            return response(request, 200, "{\"items\":[]}");
        });
        FeatureFlagSelector selector = new FeatureFlagSelector()
            .setMatchConditions(Collections.singletonList(new MatchConditions().setIfNoneMatch("first")));
        PagedIterable<FeatureFlag> sync = builder.buildClient().listFeatureFlags(selector);
        sync.stream().count();
        sync.stream().count();
        PagedFlux<FeatureFlag> async = builder.buildAsyncClient().listFeatureFlags(selector);
        async.collectList().block();
        async.collectList().block();
    }

    @ParameterizedTest
    @EnumSource(ConfigurationServiceVersion.class)
    void serviceVersionGatesFeatureFlagsAndLabelResourceTypes(ConfigurationServiceVersion version) {
        boolean supportsFeatureFlags = version != ConfigurationServiceVersion.V1_0
            && version != ConfigurationServiceVersion.V2023_10_01
            && version != ConfigurationServiceVersion.V2023_11_01;
        for (boolean async : new boolean[] { false, true }) {
            FeatureFlagClientBuilder featureBuilder = builder(request -> {
                assertTrue(request.getUrl().getQuery().contains("api-version=" + version.getVersion()));
                assertTrue(request.getUrl().getQuery().contains("resourcetype=ff"));
                return response(request, 200, "{\"items\":[]}");
            }).serviceVersion(version);
            if (supportsFeatureFlags) {
                if (async) {
                    featureBuilder.buildAsyncClient().listLabels().collectList().block();
                } else {
                    featureBuilder.buildClient().listLabels().stream().count();
                }
            } else {
                assertThrows(IllegalArgumentException.class, featureBuilder::buildClient);
                assertThrows(IllegalArgumentException.class, featureBuilder::buildAsyncClient);
            }

            AtomicInteger requests = new AtomicInteger();
            HttpClient httpClient = request -> Mono.fromSupplier(() -> {
                assertTrue(request.getUrl().getQuery().contains("api-version=" + version.getVersion()));
                assertEquals(supportsFeatureFlags, request.getUrl().getQuery().contains("resourcetype=kv"));
                if (requests.getAndIncrement() == 0) {
                    return response(request, 200, "{\"items\":[{\"name\":\"first\"}],"
                        + "\"@nextLink\":\"/labels?api-version=" + version.getVersion() + "&after=next\"}");
                }
                assertTrue(request.getUrl().getQuery().contains("after=next"));
                return response(request, 200, "{\"items\":[{\"name\":\"second\"}]}");
            });
            ConfigurationClientBuilder configurationBuilder = new ConfigurationClientBuilder().endpoint(ENDPOINT)
                .credential(CREDENTIAL)
                .serviceVersion(version)
                .httpClient(httpClient);
            if (async) {
                assertEquals(2, configurationBuilder.buildAsyncClient().listLabels().count().block());
            } else {
                assertEquals(2, configurationBuilder.buildClient().listLabels().stream().count());
            }
            assertEquals(2, requests.get());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void interleavedIteratorsHaveIndependentPageConditions(boolean revisions) {
        String path = revisions ? "/ff-revisions" : "/ff";
        AtomicInteger requests = new AtomicInteger();
        HttpClient httpClient = request -> Mono.fromSupplier(() -> {
            requests.incrementAndGet();
            assertEquals(path, request.getUrl().getPath());
            String query = request.getUrl().getQuery();
            int page = query.contains("after=page2") ? 2 : query.contains("after=page1") ? 1 : 0;
            assertEquals("\"page" + page + "\"", request.getHeaders().getValue(HttpHeaderName.IF_NONE_MATCH));
            String nextLink = page < 2
                ? ",\"@nextLink\":\"" + path + "?api-version=2026-05-01-preview&after=page" + (page + 1) + "\""
                : "";
            return response(request, 200,
                "{\"items\":[{\"name\":\"page" + page + "\",\"enabled\":true}]" + nextLink + "}");
        });
        FeatureFlagClient client = new FeatureFlagClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIAL)
            .httpClient(new AssertingHttpClientBuilder(httpClient).assertSync().build())
            .buildClient();
        FeatureFlagSelector selector
            = new FeatureFlagSelector().setMatchConditions(Arrays.asList(new MatchConditions().setIfNoneMatch("page0"),
                new MatchConditions().setIfNoneMatch("page1"), new MatchConditions().setIfNoneMatch("page2")));
        PagedIterable<FeatureFlag> flags
            = revisions ? client.listFeatureFlagRevisions(selector) : client.listFeatureFlags(selector);
        assertEquals(0, requests.get());
        Iterator<PagedResponse<FeatureFlag>> first = flags.iterableByPage().iterator();
        Iterator<PagedResponse<FeatureFlag>> second = flags.iterableByPage().iterator();
        for (int page = 0; page < 3; page++) {
            assertTrue(first.hasNext());
            assertEquals("page" + page, first.next().getValue().get(0).getName());
            assertTrue(second.hasNext());
            assertEquals("page" + page, second.next().getValue().get(0).getName());
        }
        assertFalse(first.hasNext());
        assertFalse(second.hasNext());
        assertEquals(6, requests.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void labelResourceTypeAndDateArePreservedOnNextPages(boolean async) {
        AtomicInteger requests = new AtomicInteger();
        HttpClient httpClient = request -> {
            int index = requests.getAndIncrement();
            String query = request.getUrl().getQuery();
            String resourceType = index < 2 ? "ff" : "kv";
            assertTrue(query.contains("resourcetype=" + resourceType), query);
            assertEquals(2, query.split("resourcetype=").length, query);
            assertNotNull(request.getHeaders().getValue(HttpHeaderName.fromString("Accept-Datetime")));
            if (index % 2 == 0) {
                String nextLink
                    = "/labels?api-version=2026-05-01-preview&after=next" + (index == 0 ? "&resourceType=ff" : "");
                return Mono.just(
                    response(request, 200, "{\"items\":[{\"name\":\"first\"}],\"@nextLink\":\"" + nextLink + "\"}"));
            }
            assertTrue(query.contains("after=next"));
            return Mono.just(response(request, 200, "{\"items\":[{\"name\":\"second\"}]}"));
        };
        FeatureFlagClientBuilder featureBuilder
            = new FeatureFlagClientBuilder().endpoint(ENDPOINT).credential(CREDENTIAL).httpClient(httpClient);
        ConfigurationClientBuilder configurationBuilder
            = new ConfigurationClientBuilder().endpoint(ENDPOINT).credential(CREDENTIAL).httpClient(httpClient);
        SettingLabelSelector selector
            = new SettingLabelSelector().setAcceptDatetime(OffsetDateTime.parse("2026-10-01T10:00:00Z"));
        if (async) {
            assertEquals(2, featureBuilder.buildAsyncClient().listLabels(selector).count().block());
            assertEquals(2, configurationBuilder.buildAsyncClient().listLabels(selector).count().block());
        } else {
            assertEquals(2, featureBuilder.buildClient().listLabels(selector).stream().count());
            assertEquals(2, configurationBuilder.buildClient().listLabels(selector).stream().count());
        }
        assertEquals(4, requests.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void connectionStringUsesSharedKeyAuthentication(boolean async) {
        String secret = Base64.getEncoder().encodeToString("test-secret".getBytes(StandardCharsets.UTF_8));
        FeatureFlagClientBuilder builder = builder(request -> {
            assertTrue(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION).startsWith("HMAC-SHA256 "));
            assertEquals(MEDIA_TYPE, request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            return response(request, 200, RESOURCE);
        }).credential(null).connectionString("Endpoint=" + ENDPOINT + ";Id=test-id;Secret=" + secret);
        assertEquals(ENDPOINT, async ? builder.buildAsyncClient().getEndpoint() : builder.buildClient().getEndpoint());
        if (async) {
            builder.buildAsyncClient().setFeatureFlag(new FeatureFlag("MyFeature", true)).block();
        } else {
            builder.buildClient().setFeatureFlag(new FeatureFlag("MyFeature", true));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void customPipelineDoesNotRequireSeparateCredentials(boolean async) {
        HttpClient httpClient = request -> {
            assertEquals("/ff/MyFeature", request.getUrl().getPath());
            assertEquals(MEDIA_TYPE + ", application/problem+json",
                request.getHeaders().getValue(HttpHeaderName.ACCEPT));
            return Mono.just(response(request, 200, RESOURCE));
        };
        FeatureFlagClientBuilder builder = new FeatureFlagClientBuilder().endpoint(ENDPOINT)
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build());
        assertNotNull(async
            ? builder.buildAsyncClient().getFeatureFlag("MyFeature").block()
            : builder.buildClient().getFeatureFlag("MyFeature"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void namesAndLabelsAreEncodedWithoutKeyPrefixes(boolean async) {
        String name = "Feature /+?#";
        String label = "prod +/#?";
        FeatureFlagClientBuilder builder = builder(request -> {
            assertEquals("/ff/" + name, URI.create(request.getUrl().toString()).getPath());
            assertTrue(URI.create(request.getUrl().toString()).getQuery().contains("label=" + label),
                request.getUrl().toString());
            assertTrue(request.getUrl().getQuery().contains("%2B"), request.getUrl().toString());
            return response(request, 200, RESOURCE);
        });
        assertNotNull(async
            ? builder.buildAsyncClient().getFeatureFlag(name, label).block()
            : builder.buildClient().getFeatureFlag(name, label));
    }
}
