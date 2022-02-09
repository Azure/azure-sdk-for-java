// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cloudfoundry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;


/**
 * Converts Azure service broker metadata into Spring Cloud Azure configuration properties.
 *
 * @author Warren
 */
public class AzureCloudFoundryEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final Logger LOG = LoggerFactory.getLogger(AzureCloudFoundryEnvironmentPostProcessor.class);

    private static final String VCAP_SERVICES_ENVVAR = "VCAP_SERVICES";

    private static final JsonParser PARSER = JsonParserFactory.getJsonParser();

    private static final int ORDER = ConfigFileApplicationListener.DEFAULT_ORDER - 1;

    @SuppressWarnings("unchecked")
    private static Properties retrieveCfProperties(Map<String, Object> vcapMap, AzureCfService azureCfService) {
        Properties properties = new Properties();

        try {
            List<Object> serviceBindings = (List<Object>) vcapMap.get(azureCfService.getCfServiceName());

            if (serviceBindings == null) {
                return properties;
            }

            if (serviceBindings.size() != 1) {
                LOG.warn("The service " + azureCfService.getCfServiceName() + " has to be bound to a "
                    + "Cloud Foundry application once and only once.");
                return properties;
            }

            Map<String, Object> serviceBinding = (Map<String, Object>) serviceBindings.get(0);
            Map<String, String> credentialsMap = (Map<String, String>) serviceBinding.get("credentials");
            azureCfService.getCfToAzureProperties().forEach(
                (cfPropKey, azurePropKey) -> properties.put(azurePropKey, credentialsMap.get(cfPropKey)));
        } catch (ClassCastException e) {
            LOG.warn("Unexpected format of CF (VCAP) properties", e);
        }

        return properties;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!StringUtils.isEmpty(environment.getProperty(VCAP_SERVICES_ENVVAR))) {
            Map<String, Object> vcapMap = PARSER.parseMap(environment.getProperty(VCAP_SERVICES_ENVVAR));

            Properties azureCfServiceProperties = new Properties();

            Set<AzureCfService> servicesToMap = new HashSet<>(Arrays.asList(AzureCfService.values()));

            servicesToMap.forEach(service -> azureCfServiceProperties.putAll(retrieveCfProperties(vcapMap, service)));

            environment.getPropertySources()
                .addFirst(new PropertiesPropertySource("azureCf", azureCfServiceProperties));
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
