// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.util.Context;
import com.azure.data.appconfiguration.implementation.models.KeyValue;
import com.azure.data.appconfiguration.implementation.models.KeyValueFields;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * App Configuration Utility methods, use internally.
 */
public class Utility {
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";
    public static final String APP_CONFIG_TRACING_NAMESPACE_VALUE = "Microsoft.AppConfiguration";

    static final String ID = "id";
    static final String DESCRIPTION = "description";
    static final String DISPLAY_NAME = "display_name";
    static final String ENABLED = "enabled";
    static final String CONDITIONS = "conditions";
    static final String CLIENT_FILTERS = "client_filters";
    static final String NAME = "name";
    static final String PARAMETERS = "parameters";
    static final String URI = "uri";

    /**
     * Represents any value in Etag.
     */
    public static final String ETAG_ANY = "*";

    /*
     * Translate public ConfigurationSetting to KeyValue autorest generated class.
     */
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

    // Translate generated List<KeyValueFields> to public-explored SettingFields[].
    public static SettingFields[] toSettingFieldsArray(List<KeyValueFields> kvFieldsList) {
        int size = kvFieldsList.size();
        SettingFields[] fields = new SettingFields[size];
        for (int i = 0; i < size; i++) {
            fields[i] = toSettingFields(kvFieldsList.get(i));
        }
        return fields;
    }

    // Translate generated KeyValueFields to public-explored SettingFields.
    public static SettingFields toSettingFields(KeyValueFields keyValueFields) {
        return keyValueFields == null ? null : SettingFields.fromString(keyValueFields.toString());
    }

    // Translate public-explored SettingFields[] to generated List<KeyValueFields>.
    public static List<KeyValueFields> toKeyValueFieldsList(SettingFields[] settingFieldsArray) {
        int size = settingFieldsArray.length;
        List<KeyValueFields> keyValueFields = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            keyValueFields.add(toKeyValueFields(settingFieldsArray[i]));
        }
        return keyValueFields;
    }

    // Translate public-explored SettingFields to generated KeyValueFields.
    public static KeyValueFields toKeyValueFields(SettingFields settingFields) {
        return settingFields == null ? null : KeyValueFields.fromString(settingFields.toString());
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
    public static String getEtag(boolean isEtagRequired, ConfigurationSetting setting) {
        return isEtagRequired ? getETagValue(setting.getETag()) : null;
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

    /**
     * Enable the sync stack rest proxy.
     *
     * @param context It offers a means of passing arbitrary data (key-value pairs) to pipeline policies.
     * Most applications do not need to pass arbitrary data to the pipeline and can pass Context.NONE or null.
     *
     * @return The Context.
     */
    public static Context enableSyncRestProxy(Context context) {
        context = context == null ? Context.NONE : context;
        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }

    public static Context addTracingNamespace(Context context) {
        context = context == null ? Context.NONE : context;
        return context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE);
    }
}
