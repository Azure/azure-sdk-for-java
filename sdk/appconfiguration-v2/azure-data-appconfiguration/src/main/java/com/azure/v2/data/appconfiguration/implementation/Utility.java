// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.data.appconfiguration.implementation;

import com.azure.v2.data.appconfiguration.models.ConfigurationSetting;
import com.azure.v2.data.appconfiguration.implementation.models.KeyValue;
import com.azure.v2.data.appconfiguration.models.SettingFields;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMatchConditions;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.paging.PagedResponse;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.CoreUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * App Configuration Utility methods, use internally.
 */
public class Utility {
    public static final String ID = "id";
    public static final String DESCRIPTION = "description";
    public static final String DISPLAY_NAME = "display_name";
    public static final String ENABLED = "enabled";
    public static final String CONDITIONS = "conditions";
    public static final String CLIENT_FILTERS = "client_filters";
    public static final String NAME = "name";
    public static final String PARAMETERS = "parameters";
    public static final String URI = "uri";

    /**
     * Represents any value in Etag.
     */
    public static final String ETAG_ANY = "*";

    /*
     * Translate public ConfigurationSetting to KeyValue autorest generated class.
     */
    public static KeyValue toKeyValue(ConfigurationSetting setting) {
        return new KeyValue().setKey(setting.getKey())
            .setValue(setting.getValue())
            .setLabel(setting.getLabel())
            .setContentType(setting.getContentType())
            .setEtag(setting.getETag())
            .setLastModified(setting.getLastModified())
            .setLocked(setting.isReadOnly())
            .setTags(setting.getTags());
    }

    // SettingFields[] to List<SettingFields>
    public static List<SettingFields> toSettingFieldsList(SettingFields[] settingFieldsArray) {
        return new ArrayList<>(Arrays.asList(settingFieldsArray));
    }

    /*
     * Azure Configuration service requires that the ETag value is surrounded in quotation marks.
     *
     * @param ETag The ETag to get the value for. If null is pass in, an empty string is returned.
     * @return The ETag surrounded by quotations. (ex. "ETag")
     */
    private static String getETagValue(String etag) {
        return (etag == null || "*".equals(etag)) ? etag : "\"" + etag + "\"";
    }

    /*
     * Get HTTP header value, if-match or if-none-match.. Used to perform an operation only if the targeted resource's
     * etag matches the value provided.
     */
    public static String getETag(boolean isETagRequired, ConfigurationSetting setting) {
        return isETagRequired ? getETagValue(setting.getETag()) : null;
    }

    /*
     * Ensure that setting is not null. And, key cannot be null because it is part of the service REST URL.
     */
    public static void validateSetting(ConfigurationSetting setting) {
        Objects.requireNonNull(setting);

        if (setting.getKey() == null) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null.");
        }
    }

    //        public static Response<ConfigurationSnapshot> updateSnapshotSync(String snapshotName,
    //                                                                         MatchConditions matchConditions, ConfigurationSnapshotStatus status, AzureAppConfigurationImpl serviceClient,
    //                                                                         Context context) {
    //            final String ifMatch = matchConditions == null ? null : matchConditions.getIfMatch();
    //
    //            final ResponseBase<UpdateSnapshotHeaders, ConfigurationSnapshot> response
    //                = serviceClient.updateSnapshotWithResponse(snapshotName, new SnapshotUpdateParameters().setStatus(status),
    //                    ifMatch, null, context);
    //            return new SimpleResponse<>(response, response.getValue());
    //        }

    // Parse the next link from the link header, if it exists. And return the continuation token url without the "<" and ">"
    public static String parseNextLink(String nextLink) {
        // actual value of next link: </kv?api-version=2023-10-01&$Select=&after=a2V5MTg4Cg%3D%3D>; rel="next"
        // The format of nextLink is always: "<url>; rel="next"", so we need to remove the "<" and ">" and the "; rel="next""
        if (nextLink == null) {
            return null;
        }
        String[] parts = nextLink.split(";");

        return parts[0].substring(1, parts[0].length() - 1);
    }

    // Sync Handler
    public static PagedResponse<ConfigurationSetting>
        handleNotModifiedErrorToValidResponse(HttpResponseException error) {
        Response<BinaryData> httpResponse = error.getResponse();
        if (httpResponse != null) {
            String continuationToken = parseNextLink(httpResponse.getHeaders().getValue(HttpHeaderName.LINK));
            if (httpResponse.getStatusCode() == 304) {
                return new PagedResponse<>(httpResponse.getRequest(), httpResponse.getStatusCode(),
                    httpResponse.getHeaders(), null, continuationToken, null, null, null, null);
            }
        }

        // HttpResponseException is already logged in instrumentation policy
        throw error;
    }

    // Get the ETag from a list
    public static String getPageETag(List<HttpMatchConditions> matchConditionsList, AtomicInteger pageETagIndex) {
        if (CoreUtils.isNullOrEmpty(matchConditionsList)) {
            return null;
        }

        String nextPageETag = null;
        int pageETagIndexValue = pageETagIndex.get();
        if (pageETagIndexValue < matchConditionsList.size()) {
            nextPageETag = matchConditionsList.get(pageETagIndexValue).getIfNoneMatch();
            pageETagIndex.set(pageETagIndexValue + 1);
        }

        return nextPageETag;
    }

    // Convert the Map<String, String> to a filter string
    public static List<String> getTagsFilterInString(Map<String, String> tagsFilter) {
        List<String> tagsFilters;

        if (tagsFilter != null) {
            tagsFilters = new ArrayList<>();
            tagsFilter.forEach((key, value) -> {
                if (!isNullOrEmpty(key) && !isNullOrEmpty(value)) {
                    tagsFilters.add(key + "=" + value);
                }
            });
        } else {
            tagsFilters = null;
        }
        return tagsFilters;
    }

    /**
     * Turns an array into a string mapping each element to a string and delimits them using a coma.
     *
     * @param array Array being formatted to a string.
     * @param mapper Function that maps each element to a string.
     * @param <T> Generic representing the type of the array.
     * @return Array with each element mapped and delimited, otherwise null if the array is empty or null.
     */
    public static <T> String arrayToString(T[] array, Function<T, String> mapper) {
        if (isNullOrEmpty(array)) {
            return null;
        }

        return Arrays.stream(array).map(mapper).collect(Collectors.joining(","));
    }

    public static String toStringMapper(SettingFields field) {
        return field.toString().toLowerCase(Locale.US);
    }

}
