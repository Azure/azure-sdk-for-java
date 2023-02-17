// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.data.appconfiguration.implementation.models.CompositionType;
import com.azure.data.appconfiguration.implementation.models.KeyValue;
import com.azure.data.appconfiguration.implementation.models.KeyValueFields;
import com.azure.data.appconfiguration.implementation.models.KeyValueFilter;
import com.azure.data.appconfiguration.implementation.models.Snapshot;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSettingSnapshot;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SnapshotFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

public class Utility {
    public static final String APP_CONFIG_TRACING_NAMESPACE_VALUE = "Microsoft.AppConfiguration";
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";

    public static ConfigurationSettingSnapshot toConfigurationSettingSnapshot(Snapshot snapshot) {
        return new ConfigurationSettingSnapshot(toSnapshotFilters(snapshot.getFilters()))
            .setTags(snapshot.getTags())
            .setCompositionType(com.azure.data.appconfiguration.models.CompositionType.fromString(snapshot.getCompositionType().toString()))
            .setRetentionPeriod(snapshot.getRetentionPeriod());
    }

    public static Snapshot toSnapshot(ConfigurationSettingSnapshot configurationSettingSnapshot) {
        return new Snapshot().setFilters(toKeyValueFilter(configurationSettingSnapshot.getFilters()))
            .setCompositionType(CompositionType.fromString(configurationSettingSnapshot.getCompositionType().toString()))
            .setTags(configurationSettingSnapshot.getTags())
            .setRetentionPeriod(configurationSettingSnapshot.getRetentionPeriod());
    }


    public static IterableStream<SnapshotFilter> toSnapshotFilters(Iterable<KeyValueFilter> filters) {
        List<SnapshotFilter> result = new ArrayList<>();
        for (KeyValueFilter filter : filters) {
            final SnapshotFilter snapshotFilter = new SnapshotFilter(filter.getKey());
            snapshotFilter.setLabel(filter.getLabel());
            result.add(snapshotFilter);
        }
        return IterableStream.of(result);
    }

    public static List<KeyValueFilter> toKeyValueFilter(Iterable<SnapshotFilter> filters) {
        List<KeyValueFilter> result = new ArrayList<>();
        for (SnapshotFilter filter : filters) {
            final KeyValueFilter keyValueFilter = new KeyValueFilter();
            keyValueFilter.setKey(filter.getKey());
            keyValueFilter.setLabel(filter.getLabel());
            result.add(keyValueFilter);
        }
        return result;
    }


    public static KeyValue toKeyValue(ConfigurationSetting setting) {
        return new KeyValue()
                   .setKey(setting.getKey())
                   .setValue(setting.getValue())
                   .setLabel(setting.getLabel())
                   .setContentType(setting.getContentType())
                   .setEtag(setting.getETag())
                   .setLastModified(setting.getLastModified())
                   .setLocked(setting.isReadOnly())
                   .setTags(setting.getTags());
    }

    public static ConfigurationSetting toConfigurationSetting(KeyValue keyValue) {

        final String contentType = keyValue.getContentType();
        final String key = keyValue.getKey();
        final String value = keyValue.getValue();
        final String label = keyValue.getLabel();
        final String etag = keyValue.getEtag();
        final Map<String, String> tags = keyValue.getTags();
        final ConfigurationSetting setting = new ConfigurationSetting()
                                                 .setKey(key)
                                                 .setValue(value)
                                                 .setLabel(label)
                                                 .setContentType(contentType)
                                                 .setETag(etag)
                                                 .setTags(tags);
        ConfigurationSettingHelper.setLastModified(setting, keyValue.getLastModified());
        ConfigurationSettingHelper.setReadOnly(setting, keyValue.isLocked());

//        return String.format("{\"id\":\"%s\"," +
//                                 "\"description\":\"%s\"," +
//                                 "\"display_name\":\"%s\","
//                                 + "\"enabled\":%s,"
//                                 + "\"conditions\":{\"client_filters\":"
//                                 + "[" +
//                                        "{\"name\":\"Microsoft.Percentage\"," +
//                                        "\"parameters\":{\"Value\":\"30\"}}" +
//                                   "]"
//                                 + "}}",
//            id, description, displayName, isEnabled);
//
//        if (key != null && key.startsWith(FeatureFlagConfigurationSetting.KEY_PREFIX)
//                && FEATURE_FLAG_CONTENT_TYPE.equals(contentType)) {
//
//            return readFeatureFlagConfigurationSetting(node, baseSetting);
//        } else if (SECRET_REFERENCE_CONTENT_TYPE.equals(contentType)) {
//            return readSecretReferenceConfigurationSetting(node, baseSetting);
//        }




        return setting;
    }

    public static SettingFields[] toSettingFieldsArray(List<KeyValueFields> kvFieldsList) {
        return kvFieldsList.stream()
                   .map(keyValueFields -> toSettingFields(keyValueFields))
                   .collect(Collectors.toList())
                   .toArray(new SettingFields[kvFieldsList.size()]);
    }

    public static List<KeyValueFields> toKeyValueFieldsList(SettingFields[] settingFieldsArray) {
        return Arrays.stream(settingFieldsArray)
                   .map(settingFields -> toKeyValueFields(settingFields))
                   .collect(Collectors.toList());
    }

    public static KeyValueFields toKeyValueFields(SettingFields settingFields) {
        return settingFields == null ? null : KeyValueFields.fromString(settingFields.toString());
    }

    public static SettingFields toSettingFields(KeyValueFields keyValueFields) {
        return keyValueFields == null ? null : SettingFields.fromString(keyValueFields.toString());
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

    public static String getIfMatchETag(boolean ifUnchanged, ConfigurationSetting setting) {
        return ifUnchanged ? getETagValue(setting.getETag()) : null;
    }

    public static String getIfMatchETagSnapshot(boolean ifUnchanged, ConfigurationSettingSnapshot snapshot) {
        return ifUnchanged ? getETagValue(snapshot.getETag()) : null;
    }


    public static String getIfNoneMatchETag(boolean onlyIfChanged, ConfigurationSetting setting) {
        return onlyIfChanged ? getETagValue(setting.getETag()) : null;
    }

    public static String getIfNoneMatchETagSnapshot(boolean onlyIfChanged, ConfigurationSettingSnapshot snapshot) {
        return onlyIfChanged ? getETagValue(snapshot.getETag()) : null;
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

    public static Context enableSyncRestProxy(Context context) {
        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }

    public static Context addTracingNamespace(Context context) {
        context = context == null ? Context.NONE : context;
        return context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE);
    }
}
