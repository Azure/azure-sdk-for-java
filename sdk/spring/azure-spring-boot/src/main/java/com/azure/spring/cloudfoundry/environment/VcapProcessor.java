// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloudfoundry.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.json.JsonParseException;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parses VCAP_SERVICES environment variable and sets corresponding property values.
 * <p>
 * Note that this class gets invoked before Spring creates the logging subsystem, so
 * we just use System.out.println instead.
 */
@Service
@Configuration
public class VcapProcessor implements EnvironmentPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(VcapProcessor.class);

    public static final String VCAP_SERVICES = "VCAP_SERVICES";
    public static final String LOG_VARIABLE = "COM_MICROSOFT_AZURE_CLOUDFOUNDRY_SERVICE_LOG";
    private static final String AZURE = "azure-";
    private static final String USER_PROVIDED = "user-provided";
    private static final String AZURE_SERVICE_BROKER_NAME = "azure-service-broker-name";
    private static final String AZURE_SERVICE_PLAN = "azure-service-plan";
    private static final String CREDENTIALS = "credentials";
    private boolean logFlag = false;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment confEnv, SpringApplication app) {
        final Map<String, Object> environment = confEnv.getSystemEnvironment();
        final String logValue = (String) environment.get(LOG_VARIABLE);

        if ("true".equals(logValue)) {
            logFlag = true;
        }

        log("VcapParser.postProcessEnvironment: Start");

        final String vcapServices = (String) environment.get(VCAP_SERVICES);
        final List<VcapPojo> vcapPojos = parseVcapService(vcapServices);

        new VcapResult(confEnv, vcapPojos.toArray(new VcapPojo[0]), logFlag);

        log("VcapParser.postProcessEnvironment: End");
    }

    @SuppressWarnings("unchecked")
    private VcapServiceConfig getVcapServiceConfig(@NonNull Map<String, Object> configMap) {
        final VcapServiceConfig serviceConfig = new VcapServiceConfig();

        serviceConfig.setLabel((String) configMap.getOrDefault("label", null));
        serviceConfig.setName((String) configMap.getOrDefault("name", null));
        serviceConfig.setProvider((String) configMap.getOrDefault("provider", null));
        serviceConfig.setSyslogDrainUrl((String) configMap.getOrDefault("syslog_drain_url", null));
        serviceConfig.setPlan((String) configMap.getOrDefault("plan", null));

        final List<String> tags = (List<String>) configMap.get("tags");
        final List<String> volumeMounts = (List<String>) configMap.get("volume_mounts");

        if (tags != null) {
            serviceConfig.setTags(tags.toArray(new String[0]));
        }

        if (volumeMounts != null) {
            serviceConfig.setVolumeMounts(volumeMounts.toArray(new String[0]));
        }

        serviceConfig.setCredentials((Map<String, String>) configMap.get("credentials"));

        return serviceConfig;
    }

    private List<VcapServiceConfig> getVcapServiceConfigList(@NonNull Object value) {
        Assert.isInstanceOf(List.class, value);
        @SuppressWarnings("unchecked") final List<Map<String, Object>> configs = (List<Map<String, Object>>) value;

        return configs.stream().map(this::getVcapServiceConfig).collect(Collectors.toList());
    }

    public List<VcapPojo> parseVcapService(String vcapServices) {
        final List<VcapPojo> results = new ArrayList<>();

        log("VcapParser.parse:  vcapServices = " + vcapServices);

        if (StringUtils.hasText(vcapServices)) {
            try {
                final JsonParser parser = JsonParserFactory.getJsonParser();
                final Map<String, Object> servicesMap = parser.parseMap(vcapServices);
                final Set<Map.Entry<String, Object>> services = servicesMap.entrySet();

                Assert.notNull(services, "Services entrySet cannot be null.");

                for (final Map.Entry<String, Object> serviceEntry : services) {
                    final String name = serviceEntry.getKey();

                    if (name.startsWith(AZURE) || USER_PROVIDED.equals(name)) {
                        Assert.isInstanceOf(List.class, serviceEntry.getValue());
                        final List<VcapServiceConfig> azureServices = getVcapServiceConfigList(serviceEntry.getValue());

                        results.addAll(
                                azureServices.stream()
                                        .map(service -> parseService(name, service, vcapServices))
                                        .filter(Objects::nonNull).collect(Collectors.toList())
                        );
                    }
                }
            } catch (JsonParseException e) {
                LOGGER.error("Error parsing " + vcapServices, e);
            }
        }

        return results;

    }

    private VcapPojo parseService(String serviceBrokerName, VcapServiceConfig serviceConfig, String vCapServices) {
        final VcapPojo result = new VcapPojo();
        final Map<String, String> credentials = serviceConfig.getCredentials();

        if (USER_PROVIDED.equals(serviceBrokerName)) {
            if (credentials == null) {
                return null;
            }

            final String userServiceBrokerName = credentials.remove(AZURE_SERVICE_BROKER_NAME);
            if (userServiceBrokerName == null) {
                return null;
            }

            result.setServiceBrokerName(userServiceBrokerName);
            final String userServicePlan = credentials.remove(AZURE_SERVICE_PLAN);
            serviceConfig.setPlan(userServicePlan);
            serviceConfig.setCredentials(credentials);
        } else {
            result.setServiceBrokerName(serviceBrokerName);
            serviceConfig.setPlan(serviceConfig.getPlan());
            if (credentials == null) {
                LOGGER.error("Found " + serviceBrokerName + ", but missing " + CREDENTIALS + " : " + vCapServices);
            }
        }

        result.setServiceConfig(serviceConfig);
        return result;
    }

    private void log(String msg) {
        if (logFlag) {
            LOGGER.info(msg);
        }
    }
}
