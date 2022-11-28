// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.jaas;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public final class JaasResolver {

    private JaasResolver() {
    }

    public static Jaas resolve(String jaasConfig) {
        String[] arr = extractLoginModuleAndControlFlagFromJaasString(jaasConfig);
        Jaas jaas = new Jaas(arr[0], arr[1]);
        jaas.setOptions(convertJaasOptionsFromStringToMap(jaasConfig));
        return jaas;
    }

    private static String[] extractLoginModuleAndControlFlagFromJaasString(String jaasConfig) {
        if (jaasConfig == null || !jaasConfig.contains(" ") || !jaasConfig.endsWith(";")) {
            return new String[2];
        }
        return jaasConfig.substring(0, jaasConfig.length() - 1).split(" ", 3);
    }

    private static Map<String, String> convertJaasOptionsFromStringToMap(String source) {
        if (source == null || source.split(" ").length < 3 || !source.endsWith(";")) {
            return Collections.emptyMap();
        }
        Map<String, String> map = Arrays.stream(source.substring(0, source.length() - 1).split(" "))
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
