// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationError;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationErrorType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.KeyValuePairString;

import java.util.ArrayList;
import java.util.List;

public class ConfigErrorTracker {
    private final List<CollectionConfigurationError> errors = new ArrayList<>();

    private static final ClientLogger LOGGER = new ClientLogger(ConfigErrorTracker.class);

    public void addError(String message, String eTag, String id, boolean isDerivedMetricId) {
        CollectionConfigurationError error = new CollectionConfigurationError();
        error.setMessage(message);
        error.setCollectionConfigurationErrorType(setErrorType(message));

        KeyValuePairString keyValuePair1 = new KeyValuePairString();
        keyValuePair1.setKey("ETag");
        keyValuePair1.setValue(eTag);

        KeyValuePairString keyValuePair2 = new KeyValuePairString();
        keyValuePair2.setKey(isDerivedMetricId ? "DerivedMetricInfoId" : "DocumentStreamInfoId");
        keyValuePair2.setValue(id);

        List<KeyValuePairString> data = new ArrayList<>();
        data.add(keyValuePair1);
        data.add(keyValuePair2);

        error.setData(data);

        errors.add(error);
        // This message gets logged once for every error we see on config validation. Config validation
        // only happens once per config change.
        LOGGER.verbose("{}. Due to this misconfiguration the {} rule with id {} will be ignored by the SDK.", message,
            isDerivedMetricId ? "derived metric" : "document filter conjunction", id);
    }

    private CollectionConfigurationErrorType setErrorType(String message) {
        if (message.contains("telemetry type")) {
            return CollectionConfigurationErrorType.METRIC_TELEMETRY_TYPE_UNSUPPORTED;
        } else if (message.contains("duplicate metric id")) {
            return CollectionConfigurationErrorType.METRIC_DUPLICATE_IDS;
        }
        return CollectionConfigurationErrorType.FILTER_FAILURE_TO_CREATE_UNEXPECTED;
    }

    public List<CollectionConfigurationError> getErrors() {
        return new ArrayList<>(errors);
    }
}
