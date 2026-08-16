// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.http.rest.ResponseBase;
import com.azure.data.appconfiguration.implementation.models.CreateSnapshotHeaders;
import com.azure.data.appconfiguration.implementation.models.KeyValue;
import com.azure.data.appconfiguration.implementation.models.OperationDetails;
import com.azure.data.appconfiguration.implementation.models.SnapshotUpdateParameters;
import com.azure.data.appconfiguration.models.ConfigurationSnapshot;
import com.azure.data.appconfiguration.models.ConfigurationSnapshotStatus;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingLabel;
import com.azure.data.appconfiguration.models.SettingLabelFields;
import com.azure.data.appconfiguration.models.SnapshotFields;

import reactor.core.publisher.Mono;

/**
 * Internal bridge that exposes the old typed convenience method shapes (Response&lt;KeyValue&gt;,
 * PagedResponse&lt;KeyValue&gt;, etc.) on top of the new TypeSpec-generated protocol methods
 * (RequestOptions + BinaryData) on {@link ConfigurationClientImpl}.
 *
 * <p>This exists so the hand-written {@code ConfigurationClient}/{@code ConfigurationAsyncClient} call sites
 * keep working without being rewritten end-to-end after migrating from autorest to typespec-java.</p>
 */
public final class ImplBridge {

    private static final HttpHeaderName ACCEPT_DATETIME = HttpHeaderName.fromString("Accept-Datetime");
    private static final HttpHeaderName SYNC_TOKEN = HttpHeaderName.fromString("Sync-Token");

    private ImplBridge() {
    }

    // -----------------------------------------------------------------------------------------------------
    // Exception remapping
    // -----------------------------------------------------------------------------------------------------
    // The TypeSpec-generated protocol layer throws ResourceNotFoundException / ResourceModifiedException
    // (subclasses of HttpResponseException). The hand-written public API historically threw plain
    // HttpResponseException, so remap to preserve that public contract.

    private static <T> T remap(Supplier<T> action) {
        try {
            return action.get();
        } catch (HttpResponseException ex) {
            throw remap(ex);
        }
    }

    private static HttpResponseException remap(HttpResponseException ex) {
        if (ex.getClass() == HttpResponseException.class) {
            return ex;
        }
        HttpResponseException remapped = new HttpResponseException(ex.getMessage(), ex.getResponse(), ex.getValue());
        remapped.setStackTrace(ex.getStackTrace());
        return remapped;
    }

    // -----------------------------------------------------------------------------------------------------
    // RequestOptions builders
    // -----------------------------------------------------------------------------------------------------

    private static RequestOptions singleKvOptions(String label, String acceptDateTime, String syncToken, String ifMatch,
        String ifNoneMatch, List<SettingFields> fields, List<String> tags, Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        if (label != null) {
            options.addQueryParam("label", label);
        }
        if (fields != null && !fields.isEmpty()) {
            options.addQueryParam("$Select", joinFields(fields));
        }
        if (tags != null) {
            for (String tag : tags) {
                options.addQueryParam("tags", tag);
            }
        }
        if (acceptDateTime != null) {
            options.setHeader(ACCEPT_DATETIME, acceptDateTime);
        }
        if (syncToken != null) {
            options.setHeader(SYNC_TOKEN, syncToken);
        }
        if (ifMatch != null) {
            options.setHeader(HttpHeaderName.IF_MATCH, ifMatch);
        }
        if (ifNoneMatch != null) {
            options.setHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch);
        }
        return options;
    }

    private static String joinFields(List<SettingFields> fields) {
        return fields.stream().map(SettingFields::toString).collect(Collectors.joining(","));
    }

    private static String joinSnapshotFields(List<SnapshotFields> fields) {
        return fields.stream().map(SnapshotFields::toString).collect(Collectors.joining(","));
    }

    private static String joinLabelFields(List<SettingLabelFields> fields) {
        return fields.stream().map(SettingLabelFields::toString).collect(Collectors.joining(","));
    }

    private static String joinStatus(List<ConfigurationSnapshotStatus> statuses) {
        return statuses.stream().map(ConfigurationSnapshotStatus::toString).collect(Collectors.joining(","));
    }

    // -----------------------------------------------------------------------------------------------------
    // putKeyValue (now setConfigurationSettingWithResponse)
    // -----------------------------------------------------------------------------------------------------

    public static Response<KeyValue> putKeyValueWithResponse(ConfigurationClientImpl service, String key, String label,
        String ifMatch, String ifNoneMatch, KeyValue entity, Context context) {
        RequestOptions options = singleKvOptions(label, null, null, ifMatch, ifNoneMatch, null, null, context);
        options.setBody(BinaryData.fromObject(entity));
        return remap(() -> toKeyValueResponse(service.setConfigurationSettingWithResponse(key, options)));
    }

    public static Mono<Response<KeyValue>> putKeyValueWithResponseAsync(ConfigurationClientImpl service, String key,
        String label, String ifMatch, String ifNoneMatch, KeyValue entity, Context context) {
        RequestOptions options = singleKvOptions(label, null, null, ifMatch, ifNoneMatch, null, null, context);
        options.setBody(BinaryData.fromObject(entity));
        return service.setConfigurationSettingWithResponseAsync(key, options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(ImplBridge::toKeyValueResponse);
    }

    // -----------------------------------------------------------------------------------------------------
    // getKeyValue (now getKeyValueWithResponse, protocol)
    // -----------------------------------------------------------------------------------------------------

    public static Response<KeyValue> getKeyValueWithResponse(ConfigurationClientImpl service, String key, String label,
        String acceptDateTime, String syncToken, String ifMatch, String ifNoneMatch, List<SettingFields> fields,
        Context context) {
        RequestOptions options
            = singleKvOptions(label, acceptDateTime, syncToken, ifMatch, ifNoneMatch, fields, null, context);
        return remap(() -> toKeyValueResponse(service.getKeyValueWithResponse(key, options)));
    }

    public static Mono<Response<KeyValue>> getKeyValueWithResponseAsync(ConfigurationClientImpl service, String key,
        String label, String acceptDateTime, String syncToken, String ifMatch, String ifNoneMatch,
        List<SettingFields> fields, Context context) {
        RequestOptions options
            = singleKvOptions(label, acceptDateTime, syncToken, ifMatch, ifNoneMatch, fields, null, context);
        return service.getKeyValueWithResponseAsync(key, options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(ImplBridge::toKeyValueResponse);
    }

    // -----------------------------------------------------------------------------------------------------
    // deleteKeyValue (now deleteConfigurationSettingWithResponse)
    // -----------------------------------------------------------------------------------------------------

    public static Response<KeyValue> deleteKeyValueWithResponse(ConfigurationClientImpl service, String key,
        String label, String ifMatch, Context context) {
        RequestOptions options = singleKvOptions(label, null, null, ifMatch, null, null, null, context);
        return remap(() -> toKeyValueResponseAllowEmpty(service.deleteConfigurationSettingWithResponse(key, options)));
    }

    public static Mono<Response<KeyValue>> deleteKeyValueWithResponseAsync(ConfigurationClientImpl service, String key,
        String label, String ifMatch, Context context) {
        RequestOptions options = singleKvOptions(label, null, null, ifMatch, null, null, null, context);
        return service.deleteConfigurationSettingWithResponseAsync(key, options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(ImplBridge::toKeyValueResponseAllowEmpty);
    }

    // -----------------------------------------------------------------------------------------------------
    // putLock / deleteLock (read-only)
    // -----------------------------------------------------------------------------------------------------

    public static Response<KeyValue> putLockWithResponse(ConfigurationClientImpl service, String key, String label,
        String ifMatch, String ifNoneMatch, Context context) {
        RequestOptions options = singleKvOptions(label, null, null, ifMatch, ifNoneMatch, null, null, context);
        return remap(() -> toKeyValueResponse(service.putLockWithResponse(key, options)));
    }

    public static Mono<Response<KeyValue>> putLockWithResponseAsync(ConfigurationClientImpl service, String key,
        String label, String ifMatch, String ifNoneMatch, Context context) {
        RequestOptions options = singleKvOptions(label, null, null, ifMatch, ifNoneMatch, null, null, context);
        return service.putLockWithResponseAsync(key, options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(ImplBridge::toKeyValueResponse);
    }

    public static Response<KeyValue> deleteLockWithResponse(ConfigurationClientImpl service, String key, String label,
        String ifMatch, String ifNoneMatch, Context context) {
        RequestOptions options = singleKvOptions(label, null, null, ifMatch, ifNoneMatch, null, null, context);
        return remap(() -> toKeyValueResponse(service.deleteLockWithResponse(key, options)));
    }

    public static Mono<Response<KeyValue>> deleteLockWithResponseAsync(ConfigurationClientImpl service, String key,
        String label, String ifMatch, String ifNoneMatch, Context context) {
        RequestOptions options = singleKvOptions(label, null, null, ifMatch, ifNoneMatch, null, null, context);
        return service.deleteLockWithResponseAsync(key, options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(ImplBridge::toKeyValueResponse);
    }

    // -----------------------------------------------------------------------------------------------------
    // listConfigurationSettings (was getKeyValues*)
    // -----------------------------------------------------------------------------------------------------

    public static PagedResponse<KeyValue> getKeyValuesSinglePage(ConfigurationClientImpl service, String keyFilter,
        String labelFilter, String syncToken, String acceptDateTime, List<SettingFields> fields, String snapshotName,
        String ifMatch, String ifNoneMatch, List<String> tagsFilter, Context context) {
        RequestOptions options = listKeyValuesOptions(keyFilter, labelFilter, syncToken, acceptDateTime, fields,
            snapshotName, ifMatch, ifNoneMatch, tagsFilter, context);
        return remap(() -> toKeyValuePage(service.listConfigurationSettingsSinglePage(options)));
    }

    public static Mono<PagedResponse<KeyValue>> getKeyValuesSinglePageAsync(ConfigurationClientImpl service,
        String keyFilter, String labelFilter, String syncToken, String acceptDateTime, List<SettingFields> fields,
        String snapshotName, String ifMatch, String ifNoneMatch, List<String> tagsFilter, Context context) {
        RequestOptions options = listKeyValuesOptions(keyFilter, labelFilter, syncToken, acceptDateTime, fields,
            snapshotName, ifMatch, ifNoneMatch, tagsFilter, context);
        return service.listConfigurationSettingsSinglePageAsync(options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(ImplBridge::toKeyValuePage);
    }

    public static PagedResponse<KeyValue> getKeyValuesNextSinglePage(ConfigurationClientImpl service, String nextLink,
        String acceptDateTime, String ifMatch, String ifNoneMatch, Context context) {
        RequestOptions options = nextPageOptions(acceptDateTime, ifMatch, ifNoneMatch, context);
        return remap(() -> toKeyValuePage(service.listConfigurationSettingsNextSinglePage(nextLink, options)));
    }

    public static Mono<PagedResponse<KeyValue>> getKeyValuesNextSinglePageAsync(ConfigurationClientImpl service,
        String nextLink, String acceptDateTime, String ifMatch, String ifNoneMatch, Context context) {
        RequestOptions options = nextPageOptions(acceptDateTime, ifMatch, ifNoneMatch, context);
        return service.listConfigurationSettingsNextSinglePageAsync(nextLink, options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(ImplBridge::toKeyValuePage);
    }

    private static RequestOptions listKeyValuesOptions(String keyFilter, String labelFilter, String syncToken,
        String acceptDateTime, List<SettingFields> fields, String snapshotName, String ifMatch, String ifNoneMatch,
        List<String> tagsFilter, Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        if (keyFilter != null) {
            options.addQueryParam("key", keyFilter);
        }
        if (labelFilter != null) {
            options.addQueryParam("label", labelFilter);
        }
        if (fields != null && !fields.isEmpty()) {
            options.addQueryParam("$Select", joinFields(fields));
        }
        if (snapshotName != null) {
            options.addQueryParam("snapshot", snapshotName);
        }
        if (tagsFilter != null) {
            for (String tag : tagsFilter) {
                options.addQueryParam("tags", tag);
            }
        }
        if (syncToken != null) {
            options.setHeader(SYNC_TOKEN, syncToken);
        }
        if (acceptDateTime != null) {
            options.setHeader(ACCEPT_DATETIME, acceptDateTime);
        }
        if (ifMatch != null) {
            options.setHeader(HttpHeaderName.IF_MATCH, ifMatch);
        }
        if (ifNoneMatch != null) {
            options.setHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch);
        }
        return options;
    }

    private static RequestOptions nextPageOptions(String acceptDateTime, String ifMatch, String ifNoneMatch,
        Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        if (acceptDateTime != null) {
            options.setHeader(ACCEPT_DATETIME, acceptDateTime);
        }
        if (ifMatch != null) {
            options.setHeader(HttpHeaderName.IF_MATCH, ifMatch);
        }
        if (ifNoneMatch != null) {
            options.setHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch);
        }
        return options;
    }

    // -----------------------------------------------------------------------------------------------------
    // listRevisions
    // -----------------------------------------------------------------------------------------------------

    public static PagedResponse<KeyValue> getRevisionsSinglePage(ConfigurationClientImpl service, String keyFilter,
        String labelFilter, String syncToken, String acceptDateTime, List<SettingFields> fields,
        List<String> tagsFilter, Context context) {
        RequestOptions options = listKeyValuesOptions(keyFilter, labelFilter, syncToken, acceptDateTime, fields, null,
            null, null, tagsFilter, context);
        return remap(() -> toKeyValuePage(service.getRevisionsSinglePage(options)));
    }

    public static Mono<PagedResponse<KeyValue>> getRevisionsSinglePageAsync(ConfigurationClientImpl service,
        String keyFilter, String labelFilter, String syncToken, String acceptDateTime, List<SettingFields> fields,
        List<String> tagsFilter, Context context) {
        RequestOptions options = listKeyValuesOptions(keyFilter, labelFilter, syncToken, acceptDateTime, fields, null,
            null, null, tagsFilter, context);
        return service.getRevisionsSinglePageAsync(options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(ImplBridge::toKeyValuePage);
    }

    public static PagedResponse<KeyValue> getRevisionsNextSinglePage(ConfigurationClientImpl service, String nextLink,
        String acceptDateTime, Context context) {
        RequestOptions options = nextPageOptions(acceptDateTime, null, null, context);
        return remap(() -> toKeyValuePage(service.getRevisionsNextSinglePage(nextLink, options)));
    }

    public static Mono<PagedResponse<KeyValue>> getRevisionsNextSinglePageAsync(ConfigurationClientImpl service,
        String nextLink, String acceptDateTime, Context context) {
        RequestOptions options = nextPageOptions(acceptDateTime, null, null, context);
        return service.getRevisionsNextSinglePageAsync(nextLink, options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(ImplBridge::toKeyValuePage);
    }

    // -----------------------------------------------------------------------------------------------------
    // getSnapshot / listSnapshots
    // -----------------------------------------------------------------------------------------------------

    public static Response<ConfigurationSnapshot> getSnapshotWithResponse(ConfigurationClientImpl service, String name,
        String ifMatch, String ifNoneMatch, List<SnapshotFields> fields, Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        if (fields != null && !fields.isEmpty()) {
            options.addQueryParam("$Select", joinSnapshotFields(fields));
        }
        if (ifMatch != null) {
            options.setHeader(HttpHeaderName.IF_MATCH, ifMatch);
        }
        if (ifNoneMatch != null) {
            options.setHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch);
        }
        return remap(() -> {
            Response<BinaryData> response = service.getSnapshotWithResponse(name, options);
            return new SimpleResponse<>(response,
                response.getValue() == null ? null : response.getValue().toObject(ConfigurationSnapshot.class));
        });
    }

    public static Mono<Response<ConfigurationSnapshot>> getSnapshotWithResponseAsync(ConfigurationClientImpl service,
        String name, String ifMatch, String ifNoneMatch, List<SnapshotFields> fields, Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        if (fields != null && !fields.isEmpty()) {
            options.addQueryParam("$Select", joinSnapshotFields(fields));
        }
        if (ifMatch != null) {
            options.setHeader(HttpHeaderName.IF_MATCH, ifMatch);
        }
        if (ifNoneMatch != null) {
            options.setHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch);
        }
        return service.getSnapshotWithResponseAsync(name, options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(response -> new SimpleResponse<>(response,
                response.getValue() == null ? null : response.getValue().toObject(ConfigurationSnapshot.class)));
    }

    public static PagedResponse<ConfigurationSnapshot> getSnapshotsSinglePage(ConfigurationClientImpl service,
        String nameFilter, String syncToken, List<SnapshotFields> fields, List<ConfigurationSnapshotStatus> statuses,
        Context context) {
        RequestOptions options = snapshotListOptions(nameFilter, syncToken, fields, statuses, context);
        return remap(() -> toSnapshotPage(service.getSnapshotsSinglePage(options)));
    }

    public static Mono<PagedResponse<ConfigurationSnapshot>> getSnapshotsSinglePageAsync(
        ConfigurationClientImpl service, String nameFilter, String syncToken, List<SnapshotFields> fields,
        List<ConfigurationSnapshotStatus> statuses, Context context) {
        RequestOptions options = snapshotListOptions(nameFilter, syncToken, fields, statuses, context);
        return service.getSnapshotsSinglePageAsync(options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(ImplBridge::toSnapshotPage);
    }

    public static PagedResponse<ConfigurationSnapshot> getSnapshotsNextSinglePage(ConfigurationClientImpl service,
        String nextLink, Context context) {
        RequestOptions options = nextPageOptions(null, null, null, context);
        return remap(() -> toSnapshotPage(service.getSnapshotsNextSinglePage(nextLink, options)));
    }

    public static Mono<PagedResponse<ConfigurationSnapshot>>
        getSnapshotsNextSinglePageAsync(ConfigurationClientImpl service, String nextLink, Context context) {
        RequestOptions options = nextPageOptions(null, null, null, context);
        return service.getSnapshotsNextSinglePageAsync(nextLink, options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(ImplBridge::toSnapshotPage);
    }

    private static RequestOptions snapshotListOptions(String nameFilter, String syncToken, List<SnapshotFields> fields,
        List<ConfigurationSnapshotStatus> statuses, Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        if (nameFilter != null) {
            options.addQueryParam("name", nameFilter);
        }
        if (fields != null && !fields.isEmpty()) {
            options.addQueryParam("$Select", joinSnapshotFields(fields));
        }
        if (statuses != null && !statuses.isEmpty()) {
            options.addQueryParam("status", joinStatus(statuses));
        }
        if (syncToken != null) {
            options.setHeader(SYNC_TOKEN, syncToken);
        }
        return options;
    }

    // -----------------------------------------------------------------------------------------------------
    // listLabels
    // -----------------------------------------------------------------------------------------------------

    public static PagedIterable<SettingLabel> getLabels(ConfigurationClientImpl service, String nameFilter,
        String syncToken, String acceptDatetime, List<SettingLabelFields> fields, Context context) {
        RequestOptions options = labelListOptions(nameFilter, syncToken, acceptDatetime, fields, context);
        return new PagedIterable<>(() -> remap(() -> toLabelPage(service.getLabelsSinglePage(options))),
            nextLink -> remap(() -> toLabelPage(
                service.getLabelsNextSinglePage(nextLink, nextPageOptions(null, null, null, context)))));
    }

    public static PagedFlux<SettingLabel> getLabelsAsync(ConfigurationClientImpl service, String nameFilter,
        String syncToken, String acceptDatetime, List<SettingLabelFields> fields) {
        RequestOptions options = labelListOptions(nameFilter, syncToken, acceptDatetime, fields, Context.NONE);
        return new PagedFlux<>(
            () -> service.getLabelsSinglePageAsync(options)
                .onErrorMap(HttpResponseException.class, ImplBridge::remap)
                .map(ImplBridge::toLabelPage),
            nextLink -> service.getLabelsNextSinglePageAsync(nextLink, new RequestOptions())
                .onErrorMap(HttpResponseException.class, ImplBridge::remap)
                .map(ImplBridge::toLabelPage));
    }

    private static PagedResponse<SettingLabel> toLabelPage(PagedResponse<BinaryData> page) {
        return mapPage(page, bd -> bd.toObject(SettingLabel.class));
    }

    private static RequestOptions labelListOptions(String nameFilter, String syncToken, String acceptDatetime,
        List<SettingLabelFields> fields, Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        if (nameFilter != null) {
            options.addQueryParam("name", nameFilter);
        }
        if (fields != null && !fields.isEmpty()) {
            options.addQueryParam("$Select", joinLabelFields(fields));
        }
        if (syncToken != null) {
            options.setHeader(SYNC_TOKEN, syncToken);
        }
        if (acceptDatetime != null) {
            options.setHeader(ACCEPT_DATETIME, acceptDatetime);
        }
        return options;
    }

    // -----------------------------------------------------------------------------------------------------
    // Response/page mappers
    // -----------------------------------------------------------------------------------------------------

    // Treats absent/empty body and 304 Not Modified / 204 No Content responses as a null KeyValue.
    private static Response<KeyValue> toKeyValueResponse(Response<BinaryData> response) {
        int status = response.getStatusCode();
        if (status == 304 || status == 204) {
            return new SimpleResponse<>(response, null);
        }
        BinaryData body = response.getValue();
        KeyValue kv;
        if (body == null) {
            kv = null;
        } else {
            byte[] bytes = body.toBytes();
            kv = (bytes == null || bytes.length == 0) ? null : body.toObject(KeyValue.class);
        }
        return new SimpleResponse<>(response, kv);
    }

    private static Response<KeyValue> toKeyValueResponseAllowEmpty(Response<BinaryData> response) {
        return toKeyValueResponse(response);
    }

    private static PagedResponse<KeyValue> toKeyValuePage(PagedResponse<BinaryData> page) {
        return mapPage(page, bd -> bd.toObject(KeyValue.class));
    }

    private static PagedResponse<ConfigurationSnapshot> toSnapshotPage(PagedResponse<BinaryData> page) {
        return mapPage(page, bd -> bd.toObject(ConfigurationSnapshot.class));
    }

    private static <T> PagedResponse<T> mapPage(PagedResponse<BinaryData> page,
        java.util.function.Function<BinaryData, T> mapper) {
        List<T> items = page.getValue() == null
            ? java.util.Collections.emptyList()
            : page.getValue().stream().map(mapper).collect(Collectors.toList());
        return new PagedResponseBase<>(page.getRequest(), page.getStatusCode(), page.getHeaders(), items,
            page.getContinuationToken(), null);
    }

    // -----------------------------------------------------------------------------------------------------
    // checkKeyValues (HEAD on /kv) — returns Response<Void>; caller maps headers into a PagedResponse.
    // -----------------------------------------------------------------------------------------------------

    public static Response<Void> checkKeyValuesWithResponse(ConfigurationClientImpl service, String keyFilter,
        String labelFilter, String syncToken, String acceptDateTime, List<SettingFields> fields, String snapshotName,
        String ifMatch, String ifNoneMatch, List<String> tagsFilter, Context context) {
        RequestOptions options = listKeyValuesOptions(keyFilter, labelFilter, syncToken, acceptDateTime, fields,
            snapshotName, ifMatch, ifNoneMatch, tagsFilter, context);
        return remap(() -> service.checkKeyValuesWithResponse(options));
    }

    public static Mono<Response<Void>> checkKeyValuesWithResponseAsync(ConfigurationClientImpl service,
        String keyFilter, String labelFilter, String syncToken, String acceptDateTime, List<SettingFields> fields,
        String snapshotName, String ifMatch, String ifNoneMatch, List<String> tagsFilter, Context context) {
        RequestOptions options = listKeyValuesOptions(keyFilter, labelFilter, syncToken, acceptDateTime, fields,
            snapshotName, ifMatch, ifNoneMatch, tagsFilter, context);
        return service.checkKeyValuesWithResponseAsync(options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap);
    }

    // -----------------------------------------------------------------------------------------------------
    // updateSnapshot (status change)
    // -----------------------------------------------------------------------------------------------------

    public static Response<ConfigurationSnapshot> updateSnapshotWithResponse(ConfigurationClientImpl service,
        String name, ConfigurationSnapshotStatus status, String ifMatch, Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        if (ifMatch != null) {
            options.setHeader(HttpHeaderName.IF_MATCH, ifMatch);
        }
        BinaryData entity = BinaryData.fromObject(new SnapshotUpdateParameters().setStatus(status));
        return remap(() -> {
            Response<BinaryData> response
                = service.updateSnapshotWithResponse("application/merge-patch+json", name, entity, options);
            return new SimpleResponse<>(response,
                response.getValue() == null ? null : response.getValue().toObject(ConfigurationSnapshot.class));
        });
    }

    public static Mono<Response<ConfigurationSnapshot>> updateSnapshotWithResponseAsync(ConfigurationClientImpl service,
        String name, ConfigurationSnapshotStatus status, String ifMatch, Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        if (ifMatch != null) {
            options.setHeader(HttpHeaderName.IF_MATCH, ifMatch);
        }
        BinaryData entity = BinaryData.fromObject(new SnapshotUpdateParameters().setStatus(status));
        return service.updateSnapshotWithResponseAsync("application/merge-patch+json", name, entity, options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(response -> new SimpleResponse<>(response,
                response.getValue() == null ? null : response.getValue().toObject(ConfigurationSnapshot.class)));
    }

    // -----------------------------------------------------------------------------------------------------
    // createSnapshot — returns ResponseBase with typed headers so callers can read Operation-Location.
    // -----------------------------------------------------------------------------------------------------

    public static ResponseBase<CreateSnapshotHeaders, ConfigurationSnapshot> createSnapshotWithResponse(
        ConfigurationClientImpl service, String name, ConfigurationSnapshot snapshot, Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        BinaryData entity = BinaryData.fromObject(snapshot);
        return remap(() -> toCreateSnapshotResponse(
            service.createSnapshotWithResponse("application/json", name, entity, options)));
    }

    public static Mono<ResponseBase<CreateSnapshotHeaders, ConfigurationSnapshot>> createSnapshotWithResponseAsync(
        ConfigurationClientImpl service, String name, ConfigurationSnapshot snapshot, Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        BinaryData entity = BinaryData.fromObject(snapshot);
        return service.createSnapshotWithResponseAsync("application/json", name, entity, options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(ImplBridge::toCreateSnapshotResponse);
    }

    private static ResponseBase<CreateSnapshotHeaders, ConfigurationSnapshot>
        toCreateSnapshotResponse(Response<BinaryData> response) {
        ConfigurationSnapshot snapshot
            = response.getValue() == null ? null : response.getValue().toObject(ConfigurationSnapshot.class);
        return new ResponseBase<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), snapshot,
            new CreateSnapshotHeaders(response.getHeaders()));
    }

    // -----------------------------------------------------------------------------------------------------
    // getOperationDetails (LRO polling)
    // -----------------------------------------------------------------------------------------------------

    public static Response<OperationDetails> getOperationDetailsWithResponse(ConfigurationClientImpl service,
        String snapshot, Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        return remap(() -> {
            Response<BinaryData> response = service.getOperationDetailsWithResponse(snapshot, options);
            return new SimpleResponse<>(response,
                response.getValue() == null ? null : response.getValue().toObject(OperationDetails.class));
        });
    }

    public static Mono<Response<OperationDetails>> getOperationDetailsWithResponseAsync(ConfigurationClientImpl service,
        String snapshot, Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        return service.getOperationDetailsWithResponseAsync(snapshot, options)
            .onErrorMap(HttpResponseException.class, ImplBridge::remap)
            .map(response -> new SimpleResponse<>(response,
                response.getValue() == null ? null : response.getValue().toObject(OperationDetails.class)));
    }
}
