// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.idstrategy;

import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import org.apache.kafka.connect.data.Values;
import org.apache.kafka.connect.sink.SinkRecord;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TemplateStrategy extends AbstractIdStrategy {
    private static final String KEY = "key";
    private static final String TOPIC = "topic";
    private static final String PARTITION = "partition";
    private static final String OFFSET = "offset";

    private static final String PATTERN_TEMPLATE = "\\$\\{(%s)\\}";
    private static final Pattern PATTERN;

    private TemplateStrategyConfig config;

    private static final Map<String, Function<SinkRecord, String>> METHODS_BY_VARIABLE;

    static {
        ImmutableMap.Builder<String, Function<SinkRecord, String>> builder = ImmutableMap.builder();
        builder.put(KEY, (r) -> Values.convertToString(r.keySchema(), r.key()));
        builder.put(TOPIC, SinkRecord::topic);
        builder.put(PARTITION, (r) -> r.kafkaPartition().toString());
        builder.put(OFFSET, (r) -> Long.toString(r.kafkaOffset()));
        METHODS_BY_VARIABLE = builder.build();

        String pattern = String.format(PATTERN_TEMPLATE,
            METHODS_BY_VARIABLE.keySet().stream().collect(Collectors.joining("|")));
        PATTERN = Pattern.compile(pattern);
    }

    @Override
    public String generateId(SinkRecord record) {
        String template = config.template();
        return sanitizeId(resolveAll(template, record));
    }

    @Override
    public void configure(Map<String, ?> configs) {
        config = new TemplateStrategyConfig(configs);

        super.configure(configs);
    }

    private String resolveAll(String template, SinkRecord record) {
        int lastIndex = 0;
        StringBuilder output = new StringBuilder();
        Matcher matcher = PATTERN.matcher(template);
        while (matcher.find()) {
            output.append(template, lastIndex, matcher.start())
                .append(METHODS_BY_VARIABLE.get(matcher.group(1)).apply(record));

            lastIndex = matcher.end();
        }
        if (lastIndex < template.length()) {
            output.append(template, lastIndex, template.length());
        }
        return output.toString();
    }
}

