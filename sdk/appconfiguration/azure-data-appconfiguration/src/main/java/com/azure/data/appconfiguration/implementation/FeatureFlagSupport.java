// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.models.FeatureFlag;
import com.azure.data.appconfiguration.models.FeatureFlagSelector;
import com.azure.data.appconfiguration.models.SettingLabel;
import com.azure.data.appconfiguration.models.SettingLabelSelector;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

/**
 * Typed adaptation of the generated feature flag protocol operations.
 */
public final class FeatureFlagSupport {
    private static final ClientLogger LOGGER = new ClientLogger(FeatureFlagSupport.class);
    private static final HttpHeaderName ACCEPT_DATETIME = HttpHeaderName.fromString("Accept-Datetime");
    private static final String FEATURE_FLAG_MEDIA_TYPE
        = "application/json;profile=\"https://azconfig.io/mime-profiles/ff\";charset=utf-8";
    private static final String FEATURE_FLAG_SET_MEDIA_TYPE
        = "application/json;profile=\"https://azconfig.io/mime-profiles/ffset\";charset=utf-8";

    private FeatureFlagSupport() {
    }

    public static RequestOptions options(String label, MatchConditions conditions, OffsetDateTime acceptDateTime,
        Context context) {
        RequestOptions options = new RequestOptions().setContext(context == null ? Context.NONE : context);
        options.setHeader(HttpHeaderName.ACCEPT, FEATURE_FLAG_MEDIA_TYPE + ", application/problem+json");
        if (label != null) {
            options.addQueryParam("label", label);
        }
        if (acceptDateTime != null) {
            options.setHeader(ACCEPT_DATETIME, DateTimeFormatter.RFC_1123_DATE_TIME.format(acceptDateTime));
        }
        if (conditions != null) {
            if (conditions.getIfMatch() != null) {
                options.setHeader(HttpHeaderName.IF_MATCH, quoteETag(conditions.getIfMatch()));
            }
            if (conditions.getIfNoneMatch() != null) {
                options.setHeader(HttpHeaderName.IF_NONE_MATCH, quoteETag(conditions.getIfNoneMatch()));
            }
        }
        return options;
    }

    public static String validateName(String name) {
        if (CoreUtils.isNullOrEmpty(name)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'name' cannot be null or empty."));
        }
        return name;
    }

    public static MatchConditions updateConditions(FeatureFlag flag, boolean ifUnchanged) {
        if (flag == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'featureFlag' cannot be null."));
        }
        validateName(flag.getName());
        if (!ifUnchanged) {
            return null;
        }
        if (CoreUtils.isNullOrEmpty(flag.getETag())) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'featureFlag.etag' is required when 'ifUnchanged' is true."));
        }
        return new MatchConditions().setIfMatch(flag.getETag());
    }

    private static String quoteETag(String etag) {
        return "*".equals(etag) || etag.startsWith("\"") || etag.startsWith("W/\"") ? etag : "\"" + etag + "\"";
    }

    public static RequestOptions putOptions(FeatureFlag flag, MatchConditions conditions, Context context) {
        updateConditions(flag, false);
        return options(flag.getLabel(), conditions, null, context)
            .setHeader(HttpHeaderName.CONTENT_TYPE, FEATURE_FLAG_MEDIA_TYPE)
            .setBody(BinaryData.fromObject(flag));
    }

    public static Response<FeatureFlag> toResponse(Response<BinaryData> response) {
        BinaryData body = response.getValue();
        FeatureFlag flag = body == null || body.toBytes().length == 0 ? null : body.toObject(FeatureFlag.class);
        return new SimpleResponse<>(response, flag);
    }

    public static Response<FeatureFlag> notModified(HttpResponseException error) {
        HttpResponse response = error.getResponse();
        if (response == null || response.getStatusCode() != 304) {
            throw error;
        }
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
    }

    public static PagedIterable<FeatureFlag> list(ConfigurationClientImpl client, FeatureFlagSelector selector,
        boolean revisions, Context context) {
        AtomicInteger pageIndex = new AtomicInteger();
        return new PagedIterable<>(() -> {
            pageIndex.set(0);
            return getPage(client, selector, revisions, null, pageIndex.getAndIncrement(), context);
        }, nextLink -> getPage(client, selector, revisions, nextLink, pageIndex.getAndIncrement(), context));
    }

    public static PagedFlux<FeatureFlag> listAsync(ConfigurationClientImpl client, FeatureFlagSelector selector,
        boolean revisions) {
        return PagedFlux.create(() -> {
            AtomicInteger pageIndex = new AtomicInteger();
            return (nextLink, pageSize) -> FluxUtil.withContext(context -> {
                RequestOptions options = listOptions(selector, nextLink, pageIndex.getAndIncrement(), context);
                PagedFlux<BinaryData> pages = revisions
                    ? client.getFeatureFlagClients().getFeatureFlagRevisionsAsync(options)
                    : client.getFeatureFlagClients().getFeatureFlagsAsync(options);
                return pages.byPage()
                    .next()
                    .map(FeatureFlagSupport::toPage)
                    .onErrorResume(HttpResponseException.class, error -> Mono.just(notModifiedPage(error)));
            }).flux();
        });
    }

    private static PagedResponse<FeatureFlag> getPage(ConfigurationClientImpl client, FeatureFlagSelector selector,
        boolean revisions, String nextLink, int pageIndex, Context context) {
        RequestOptions options = listOptions(selector, nextLink, pageIndex, context);
        try {
            PagedIterable<BinaryData> pages = revisions
                ? client.getFeatureFlagClients().getFeatureFlagRevisions(options)
                : client.getFeatureFlagClients().getFeatureFlags(options);
            return toPage(pages.iterableByPage().iterator().next());
        } catch (HttpResponseException error) {
            return notModifiedPage(error);
        }
    }

    private static RequestOptions listOptions(FeatureFlagSelector selector, String nextLink, int pageIndex,
        Context context) {
        List<MatchConditions> conditions = selector == null ? null : selector.getMatchConditions();
        MatchConditions condition
            = conditions != null && pageIndex < conditions.size() ? conditions.get(pageIndex) : null;
        RequestOptions options = options(selector == null ? null : selector.getLabelFilter(), condition,
            selector == null ? null : selector.getAcceptDateTime(), context);
        options.setHeader(HttpHeaderName.ACCEPT, FEATURE_FLAG_SET_MEDIA_TYPE + ", application/problem+json");
        if (selector != null) {
            if (selector.getNameFilter() != null) {
                options.addQueryParam("name", selector.getNameFilter());
            }
            if (selector.getFields().length > 0) {
                options.addQueryParam("$Select",
                    Arrays.stream(selector.getFields()).map(Object::toString).collect(Collectors.joining(",")));
            }
            if (selector.getTagsFilter() != null) {
                selector.getTagsFilter().forEach(tag -> options.addQueryParam("tags", tag));
            }
        }
        addAfter(options, nextLink);
        return options;
    }

    private static void addAfter(RequestOptions options, String nextLink) {
        if (nextLink == null) {
            return;
        }
        // Fetch one page through the generated public operation so each page receives its own match conditions.
        // The service's continuation link contains an already-encoded After token.
        int queryStart = nextLink.indexOf('?');
        Iterator<Map.Entry<String, String>> parameters
            = CoreUtils.parseQueryParameters(queryStart < 0 ? nextLink : nextLink.substring(queryStart + 1));
        while (parameters.hasNext()) {
            Map.Entry<String, String> parameter = parameters.next();
            if ("after".equalsIgnoreCase(parameter.getKey())) {
                options.addQueryParam("After", parameter.getValue(), true);
                return;
            }
        }
        throw LOGGER.logExceptionAsError(
            new IllegalArgumentException("The continuation link does not contain an After token."));
    }

    private static PagedResponse<FeatureFlag> toPage(PagedResponse<BinaryData> page) {
        List<FeatureFlag> items
            = page.getValue().stream().map(value -> value.toObject(FeatureFlag.class)).collect(Collectors.toList());
        return new PagedResponseBase<>(page.getRequest(), page.getStatusCode(), page.getHeaders(), items,
            page.getContinuationToken(), null);
    }

    private static PagedResponse<FeatureFlag> notModifiedPage(HttpResponseException error) {
        Response<FeatureFlag> response = notModified(error);
        return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            Collections.emptyList(), Utility.parseNextLink(response.getHeaders().getValue(HttpHeaderName.LINK)), null);
    }

    public static PagedIterable<SettingLabel> labels(ConfigurationClientImpl client, SettingLabelSelector selector,
        Context context) {
        return ImplBridge.getLabels(client, selector == null ? null : selector.getNameFilter(), null,
            labelDateTime(selector), selector == null ? null : selector.getFields(), context, "ff");
    }

    public static PagedFlux<SettingLabel> labelsAsync(ConfigurationClientImpl client, SettingLabelSelector selector) {
        return ImplBridge.getLabelsAsync(client, selector == null ? null : selector.getNameFilter(), null,
            labelDateTime(selector), selector == null ? null : selector.getFields(), "ff");
    }

    private static String labelDateTime(SettingLabelSelector selector) {
        return selector == null || selector.getAcceptDateTime() == null
            ? null
            : DateTimeFormatter.RFC_1123_DATE_TIME.format(selector.getAcceptDateTime());
    }
}
