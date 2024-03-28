// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.idstrategy;

import org.apache.kafka.connect.data.Values;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import com.jayway.jsonpath.JsonPath;

import java.util.Map;

class ProvidedInStrategy extends AbstractIdStrategy {
    protected enum ProvidedIn {
        KEY,
        VALUE
    }

    private final ProvidedIn where;

    private ProvidedInConfig config;

    ProvidedInStrategy(ProvidedIn where) {
        this.where = where;
    }

    @Override
    public String generateId(SinkRecord record) {
        String value = where == ProvidedIn.KEY
            ? Values.convertToString(record.keySchema(), record.key())
            : Values.convertToString(record.valueSchema(), record.value());
        try {
            Object object = JsonPath.parse(value).read(config.jsonPath());
            return sanitizeId(Values.convertToString(null, object));
        } catch (Exception e) {
            throw new ConnectException("Could not evaluate JsonPath " + config.jsonPath(), e);
        }
    }

    @Override
    public void configure(Map<String, ?> configs) {
        config = new ProvidedInConfig(configs);
        super.configure(configs);
    }
}

