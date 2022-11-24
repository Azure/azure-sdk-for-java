// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.jaas;

import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.convertJaasStringToMap;

public final class JaasResolver {

    private JaasResolver() {
    }

    public static Jaas resolve(String jaasConfig) {
        String[] arr = extractLoginModuleAndControlFlagFromJaasString(jaasConfig);
        Jaas jaas = new Jaas(arr[0], arr[1]);
        jaas.setOptions(convertJaasStringToMap(jaasConfig));
        return jaas;
    }

    private static String[] extractLoginModuleAndControlFlagFromJaasString(String jaasConfig) {
        if (jaasConfig == null || !jaasConfig.contains(" ") || !jaasConfig.endsWith(";")) {
            return new String[2];
        }
        return jaasConfig.substring(0, jaasConfig.length() - 1).split(" ", 3);
    }

}
