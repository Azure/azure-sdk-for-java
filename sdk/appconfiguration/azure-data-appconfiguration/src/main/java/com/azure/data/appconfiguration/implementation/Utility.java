// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.implementation.models.KeyValue;
import com.azure.data.appconfiguration.implementation.models.KeyValueFields;
import com.azure.data.appconfiguration.implementation.models.SnapshotStatus;
import com.azure.data.appconfiguration.implementation.models.SnapshotUpdateParameters;
import com.azure.data.appconfiguration.implementation.models.UpdateSnapshotHeaders;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSnapshot;
import com.azure.data.appconfiguration.models.ConfigurationSnapshotStatus;
import com.azure.data.appconfiguration.models.FeatureFlag;
import com.azure.data.appconfiguration.models.FeatureFlagAllocation;
import com.azure.data.appconfiguration.models.FeatureFlagConditions;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.FeatureFlagGroupAllocation;
import com.azure.data.appconfiguration.models.FeatureFlagPercentileAllocation;
import com.azure.data.appconfiguration.models.FeatureFlagRequirementType;
import com.azure.data.appconfiguration.models.FeatureFlagStatusOverride;
import com.azure.data.appconfiguration.models.FeatureFlagTelemetry;
import com.azure.data.appconfiguration.models.FeatureFlagUserAllocation;
import com.azure.data.appconfiguration.models.FeatureFlagVariant;
import com.azure.data.appconfiguration.models.LabelFields;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingLabelFields;

import reactor.core.publisher.Mono;

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
        return new KeyValue().setValue(setting.getValue())
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

    /*
     * Asynchronously validate that setting and key is not null. The key is used in the service URL,
     *  so it cannot be null.
     */
    public static Mono<ConfigurationSetting> validateSettingAsync(ConfigurationSetting setting) {
        if (setting == null) {
            return Mono.error(new NullPointerException("Configuration setting cannot be null"));
        }
        if (setting.getKey() == null) {
            return Mono.error(new IllegalArgumentException("Parameter 'key' is required and cannot be null."));
        }
        return Mono.just(setting);
    }

    public static Response<ConfigurationSnapshot> updateSnapshotSync(String snapshotName,
        MatchConditions matchConditions, ConfigurationSnapshotStatus status, AzureAppConfigurationImpl serviceClient,
        String endpoint, Context context) {
        final String ifMatch = matchConditions == null ? null : matchConditions.getIfMatch();

        final ResponseBase<UpdateSnapshotHeaders, ConfigurationSnapshot> response
            = serviceClient.updateSnapshotWithResponse(endpoint, snapshotName,
                new SnapshotUpdateParameters().setStatus(status), null, ifMatch, null, null, context);
        return new SimpleResponse<>(response, response.getValue());
    }

    public static Mono<Response<ConfigurationSnapshot>> updateSnapshotAsync(String snapshotName,
        MatchConditions matchConditions, ConfigurationSnapshotStatus status, AzureAppConfigurationImpl serviceClient,
        String endpoint) {
        final String ifMatch = matchConditions == null ? null : matchConditions.getIfMatch();
        return serviceClient
            .updateSnapshotWithResponseAsync(endpoint, snapshotName, new SnapshotUpdateParameters().setStatus(status),
                null, ifMatch, null, null)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    public static List<KeyValueFields> toKeyValueFields(List<SettingFields> settingFields) {
        if (settingFields == null) {
            return null;
        }
        return settingFields.stream().map(f -> KeyValueFields.fromString(f.toString())).collect(Collectors.toList());
    }

    public static List<SnapshotStatus> toSnapshotStatus(List<ConfigurationSnapshotStatus> statusList) {
        if (statusList == null) {
            return null;
        }
        return statusList.stream().map(s -> SnapshotStatus.fromString(s.toString())).collect(Collectors.toList());
    }

    public static List<LabelFields> toLabelFields(List<SettingLabelFields> labelFields) {
        if (labelFields == null) {
            return null;
        }
        return labelFields.stream().map(f -> LabelFields.fromString(f.toString())).collect(Collectors.toList());
    }

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

    // Handle 304 status code to a valid response or pass error as it is if not 304.
    // Async handler
    public static Mono<PagedResponse<KeyValue>> handleNotModifiedErrorToValidResponse(HttpResponseException error) {
        HttpResponse httpResponse = error.getResponse();
        if (httpResponse == null) {
            return Mono.error(error);
        }

        String continuationToken = parseNextLink(httpResponse.getHeaderValue(HttpHeaderName.LINK));
        if (httpResponse.getStatusCode() == 304) {
            return Mono.just(new PagedResponseBase<>(httpResponse.getRequest(), httpResponse.getStatusCode(),
                httpResponse.getHeaders(), null, continuationToken, null));
        }

        return Mono.error(error);
    }

    // Sync Handler
    public static PagedResponse<ConfigurationSetting> handleNotModifiedErrorToValidResponse(HttpResponseException error,
        ClientLogger logger) {
        HttpResponse httpResponse = error.getResponse();
        if (httpResponse == null) {
            throw logger.logExceptionAsError(error);
        }

        String continuationToken = parseNextLink(httpResponse.getHeaderValue(HttpHeaderName.LINK));
        if (httpResponse.getStatusCode() == 304) {
            return new PagedResponseBase<>(httpResponse.getRequest(), httpResponse.getStatusCode(),
                httpResponse.getHeaders(), null, continuationToken, null);
        }

        throw logger.logExceptionAsError(error);
    }

    // Get the ETag from a list
    public static String getPageETag(List<MatchConditions> matchConditionsList, AtomicInteger pageETagIndex) {
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
                if (!CoreUtils.isNullOrEmpty(key) && !CoreUtils.isNullOrEmpty(value)) {
                    tagsFilters.add(key + "=" + value);
                }
            });
        } else {
            tagsFilters = null;
        }
        return tagsFilters;
    }

    /*
     * Get the ETag value from a raw etag string, surrounded in quotation marks.
     */
    public static String getETag(String etag) {
        return getETagValue(etag);
    }

    /*
     * Convert public FeatureFlag model to impl FeatureFlag for sending to the service.
     */
    public static com.azure.data.appconfiguration.implementation.models.FeatureFlag toImplFeatureFlag(
        FeatureFlag flag) {
        if (flag == null) {
            return null;
        }
        com.azure.data.appconfiguration.implementation.models.FeatureFlag impl
            = new com.azure.data.appconfiguration.implementation.models.FeatureFlag()
                .setEnabled(flag.isEnabled())
                .setDescription(flag.getDescription())
                .setTags(flag.getTags());

        if (flag.getConditions() != null) {
            impl.setConditions(toImplConditions(flag.getConditions()));
        }
        if (flag.getVariants() != null) {
            impl.setVariants(flag.getVariants().stream().map(Utility::toImplVariant).collect(Collectors.toList()));
        }
        if (flag.getAllocation() != null) {
            impl.setAllocation(toImplAllocation(flag.getAllocation()));
        }
        if (flag.getTelemetry() != null) {
            com.azure.data.appconfiguration.implementation.models.FeatureFlagTelemetryConfiguration implTelemetry
                = new com.azure.data.appconfiguration.implementation.models.FeatureFlagTelemetryConfiguration(
                    flag.getTelemetry().isEnabled())
                        .setMetadata(flag.getTelemetry().getMetadata());
            impl.setTelemetry(implTelemetry);
        }
        return impl;
    }

    /*
     * Convert impl FeatureFlag to public FeatureFlag model.
     */
    public static FeatureFlag toPublicFeatureFlag(
        com.azure.data.appconfiguration.implementation.models.FeatureFlag impl) {
        if (impl == null) {
            return null;
        }
        FeatureFlag flag = new FeatureFlag()
            .setEnabled(impl.isEnabled())
            .setDescription(impl.getDescription())
            .setTags(impl.getTags())
            .setName(impl.getName())
            .setLabel(impl.getLabel())
            .setLastModified(impl.getLastModified())
            .setEtag(impl.getEtag());

        if (impl.getConditions() != null) {
            flag.setConditions(toPublicConditions(impl.getConditions()));
        }
        if (impl.getVariants() != null) {
            flag.setVariants(
                impl.getVariants().stream().map(Utility::toPublicVariant).collect(Collectors.toList()));
        }
        if (impl.getAllocation() != null) {
            flag.setAllocation(toPublicAllocation(impl.getAllocation()));
        }
        if (impl.getTelemetry() != null) {
            FeatureFlagTelemetry telemetry = new FeatureFlagTelemetry(impl.getTelemetry().isEnabled())
                .setMetadata(impl.getTelemetry().getMetadata());
            flag.setTelemetry(telemetry);
        }
        return flag;
    }

    private static com.azure.data.appconfiguration.implementation.models.FeatureFlagConditions toImplConditions(
        FeatureFlagConditions conditions) {
        com.azure.data.appconfiguration.implementation.models.FeatureFlagConditions impl
            = new com.azure.data.appconfiguration.implementation.models.FeatureFlagConditions();
        if (conditions.getRequirementType() != null) {
            impl.setRequirementType(com.azure.data.appconfiguration.implementation.models.RequirementType
                .fromString(conditions.getRequirementType().toString()));
        }
        if (conditions.getFilters() != null) {
            impl.setFilters(conditions.getFilters().stream().map(Utility::toImplFilter).collect(Collectors.toList()));
        }
        return impl;
    }

    private static FeatureFlagConditions toPublicConditions(
        com.azure.data.appconfiguration.implementation.models.FeatureFlagConditions impl) {
        FeatureFlagConditions conditions = new FeatureFlagConditions();
        if (impl.getRequirementType() != null) {
            conditions
                .setRequirementType(FeatureFlagRequirementType.fromString(impl.getRequirementType().toString()));
        }
        if (impl.getFilters() != null) {
            conditions.setFilters(
                impl.getFilters().stream().map(Utility::toPublicFilter).collect(Collectors.toList()));
        }
        return conditions;
    }

    private static com.azure.data.appconfiguration.implementation.models.FeatureFlagFilter toImplFilter(
        FeatureFlagFilter filter) {
        com.azure.data.appconfiguration.implementation.models.FeatureFlagFilter impl
            = new com.azure.data.appconfiguration.implementation.models.FeatureFlagFilter(filter.getName());
        if (filter.getParameters() != null) {
            // Convert Map<String, Object> to Map<String, String>
            Map<String, String> stringParams = filter.getParameters().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
            impl.setParameters(stringParams);
        }
        return impl;
    }

    private static FeatureFlagFilter toPublicFilter(
        com.azure.data.appconfiguration.implementation.models.FeatureFlagFilter impl) {
        FeatureFlagFilter filter = new FeatureFlagFilter(impl.getName());
        if (impl.getParameters() != null) {
            // Convert Map<String, String> to Map<String, Object>
            impl.getParameters().forEach(filter::addParameter);
        }
        return filter;
    }

    private static com.azure.data.appconfiguration.implementation.models.FeatureFlagVariantDefinition toImplVariant(
        FeatureFlagVariant variant) {
        com.azure.data.appconfiguration.implementation.models.FeatureFlagVariantDefinition impl
            = new com.azure.data.appconfiguration.implementation.models.FeatureFlagVariantDefinition(
                variant.getName())
                    .setValue(variant.getValue())
                    .setContentType(variant.getContentType());
        if (variant.getStatusOverride() != null) {
            impl.setStatusOverride(com.azure.data.appconfiguration.implementation.models.StatusOverride
                .fromString(variant.getStatusOverride().toString()));
        }
        return impl;
    }

    private static FeatureFlagVariant toPublicVariant(
        com.azure.data.appconfiguration.implementation.models.FeatureFlagVariantDefinition impl) {
        FeatureFlagVariant variant = new FeatureFlagVariant(impl.getName())
            .setValue(impl.getValue())
            .setContentType(impl.getContentType());
        if (impl.getStatusOverride() != null) {
            variant.setStatusOverride(FeatureFlagStatusOverride.fromString(impl.getStatusOverride().toString()));
        }
        return variant;
    }

    private static com.azure.data.appconfiguration.implementation.models.FeatureFlagAllocation toImplAllocation(
        FeatureFlagAllocation alloc) {
        com.azure.data.appconfiguration.implementation.models.FeatureFlagAllocation impl
            = new com.azure.data.appconfiguration.implementation.models.FeatureFlagAllocation()
                .setDefaultWhenDisabled(alloc.getDefaultWhenDisabled())
                .setDefaultWhenEnabled(alloc.getDefaultWhenEnabled())
                .setSeed(alloc.getSeed());
        if (alloc.getPercentile() != null) {
            impl.setPercentile(alloc.getPercentile().stream()
                .map(p -> new com.azure.data.appconfiguration.implementation.models.PercentileAllocation(
                    p.getVariant(), p.getFrom(), p.getTo()))
                .collect(Collectors.toList()));
        }
        if (alloc.getUser() != null) {
            impl.setUser(alloc.getUser().stream()
                .map(u -> new com.azure.data.appconfiguration.implementation.models.UserAllocation(u.getVariant(),
                    u.getUsers()))
                .collect(Collectors.toList()));
        }
        if (alloc.getGroup() != null) {
            impl.setGroup(alloc.getGroup().stream()
                .map(g -> new com.azure.data.appconfiguration.implementation.models.GroupAllocation(g.getVariant(),
                    g.getGroups()))
                .collect(Collectors.toList()));
        }
        return impl;
    }

    private static FeatureFlagAllocation toPublicAllocation(
        com.azure.data.appconfiguration.implementation.models.FeatureFlagAllocation impl) {
        FeatureFlagAllocation alloc = new FeatureFlagAllocation()
            .setDefaultWhenDisabled(impl.getDefaultWhenDisabled())
            .setDefaultWhenEnabled(impl.getDefaultWhenEnabled())
            .setSeed(impl.getSeed());
        if (impl.getPercentile() != null) {
            alloc.setPercentile(impl.getPercentile().stream()
                .map(p -> new FeatureFlagPercentileAllocation(p.getVariant(), p.getFrom(), p.getTo()))
                .collect(Collectors.toList()));
        }
        if (impl.getUser() != null) {
            alloc.setUser(impl.getUser().stream()
                .map(u -> new FeatureFlagUserAllocation(u.getVariant(), u.getUsers()))
                .collect(Collectors.toList()));
        }
        if (impl.getGroup() != null) {
            alloc.setGroup(impl.getGroup().stream()
                .map(g -> new FeatureFlagGroupAllocation(g.getVariant(), g.getGroups()))
                .collect(Collectors.toList()));
        }
        return alloc;
    }
}
