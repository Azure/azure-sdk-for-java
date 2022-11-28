// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.jaas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public final class JaasResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JaasResolver.class);

    public Optional<Jaas> resolve(String jaasConfig) {
        try {
            String[] arr = parseJaasComponents(jaasConfig);
            return Optional.of(new Jaas(arr[0], arr[1], convertJaasOptionsFromStringToMap(arr[2])));
        } catch (IllegalArgumentException exception) {
            LOGGER.error(exception.getMessage());
            return Optional.empty();
        }
    }

    private String[] parseJaasComponents(String jaasConfig) {
        if (jaasConfig == null || !jaasConfig.contains(" ") || !jaasConfig.endsWith(";")) {
            throw new IllegalArgumentException("The JAAS config is not valid.");
        }
        String[] rst = new String[3];
        String[] paras = jaasConfig.substring(0, jaasConfig.length() - 1).split(" ", 3);
        for (int i = 0; i < paras.length; i++) {
            rst[i] = paras[i];
        }
        return rst;
    }

    private Map<String, String> convertJaasOptionsFromStringToMap(String options) {
        if (!StringUtils.hasText(options)) {
            return new HashMap<>();
        }
        if (!options.contains("=")) {
            throw new IllegalArgumentException("The JAAS options part is not valid.");
        }
        Map<String, String> map = Arrays.stream(options.split(" "))
            .filter(str -> str.contains("="))
            .map(str -> str.split("=", 2))
            .collect(Collectors.toMap(arr -> arr[0], arr -> {
                if (arr[1].length() > 2 && arr[1].startsWith("\"") && arr[1].endsWith("\"")) {
                    return arr[1].substring(1, arr[1].length() - 1);
                }
                return null;
            }));
        return map;
    }

}
