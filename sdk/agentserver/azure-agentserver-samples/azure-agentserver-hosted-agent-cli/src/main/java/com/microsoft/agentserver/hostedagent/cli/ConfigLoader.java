// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.hostedagent.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads a YAML configuration file and flattens it into a JCommander-style argument list.
 *
 * <p>The YAML keys mirror the CLI option names (without the leading {@code --}). The nested {@code env}
 * block (a map, or a list of {@code KEY=VALUE} strings) is expanded into repeated {@code --env KEY=VALUE}
 * arguments. The sub-command itself is always given on the command line, not in the file. For example:</p>
 *
 * <pre>
 *   endpoint: https://acct.services.ai.azure.com/api/projects/proj
 *   name: my-agent
 *   image: myacr.azurecr.io/my-agent:latest
 *   cpu: 2
 *   memory: 4Gi
 *   model: gpt-5.4
 *   env:
 *     LOG_LEVEL: debug
 * </pre>
 *
 * <p>becomes {@code --endpoint … --name … --image … --cpu 2 --memory 4Gi --model gpt-5.4
 * --env LOG_LEVEL=debug}. Values supplied on the command line are appended after these, so an explicit
 * flag always overrides the file.</p>
 */
final class ConfigLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final String ENV_KEY = "env";

    private final Map<String, Object> values;

    private ConfigLoader(Map<String, Object> values) {
        this.values = values;
    }

    /**
     * Reads and parses the YAML file at {@code path}.
     */
    @SuppressWarnings("unchecked")
    static ConfigLoader load(String path) {
        try {
            Map<String, Object> parsed = YAML_MAPPER.readValue(new File(path), Map.class);
            return new ConfigLoader(parsed == null ? new LinkedHashMap<>() : parsed);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read config file '" + path + "': " + e.getMessage(), e);
        }
    }

    /**
     * Flattens every key into an ordered {@code --flag value} argument list. Keys are only emitted when the
     * selected command accepts the matching option ({@code allowedFlags}); scalar keys whose flag already
     * appears in {@code cliFlags} are skipped so an explicit command-line option always wins; the repeatable
     * {@code env} entries are always emitted when {@code --env} is accepted (they accumulate, and a later
     * {@code --env} for the same key overrides an earlier one).
     */
    List<String> toArguments(Set<String> cliFlags, Set<String> allowedFlags) {
        List<String> args = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            String flag = ENV_KEY.equals(key) ? "--env" : "--" + key;
            if (!allowedFlags.contains(flag)) {
                continue;
            }
            if (ENV_KEY.equals(key)) {
                appendEnv(args, value);
            } else if (!cliFlags.contains(flag)) {
                args.add(flag);
                args.add(String.valueOf(value));
            }
        }
        return args;
    }

    @SuppressWarnings("unchecked")
    private static void appendEnv(List<String> args, Object value) {
        if (value instanceof Map) {
            for (Map.Entry<Object, Object> env : ((Map<Object, Object>) value).entrySet()) {
                args.add("--env");
                args.add(env.getKey() + "=" + env.getValue());
            }
        } else if (value instanceof List) {
            for (Object env : (List<Object>) value) {
                args.add("--env");
                args.add(String.valueOf(env));
            }
        } else {
            throw new IllegalArgumentException(
                "Config key 'env' must be a map or a list of KEY=VALUE strings, but was: " + value);
        }
    }
}
