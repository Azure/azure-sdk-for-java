// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloudfoundry.environment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *class the result of VcapProcessor
 */
@JsonIgnoreProperties
public class VcapResult implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(VcapResult.class);

    private static final String AZURE_SERVICE_BUS_DOMAIN = "servicebus.windows.net";

    private static final String PROPERTY_SOURCE_NAME = "defaultProperties";
    private static final String RESULT = "result";
    private static final String CONNECTION_STRING = "connectionString";
    private static final String URI = "uri";
    private static final String KEY = "key";
    private static final String DATABASE = "database";
    private static final long serialVersionUID = -4825963001214199795L;

    private final boolean logFlag;

    public VcapResult(ConfigurableEnvironment environment, VcapPojo[] pojos, boolean logFlag) {
        this.logFlag = logFlag;

        populateProperties(environment, pojos);
    }

    /**
     * Populates default properties during @EnvironmentPostProcessor processing.
     * <p>
     * Note that this class gets invoked before Spring creates the logging
     * subsystem, so we just use System.out.println instead.
     */
    private void populateProperties(ConfigurableEnvironment environment, VcapPojo[] pojos) {
        final Map<String, Object> map = new HashMap<>();
        populateDefaultServiceBusProperties(map,
                findPojoForServiceType(VcapServiceType.AZURE_SERVICEBUS, pojos));
        populateDefaultDocumentDBProperties(map,
                findPojoForServiceType(VcapServiceType.AZURE_COSMOSDB, pojos));
        addOrReplace(environment.getPropertySources(), map);
    }

    private VcapPojo findPojoForServiceType(VcapServiceType serviceType, VcapPojo[] pojos) {
        if (serviceType == null) {
            log("VcapResult.findPojoForServiceType: ServiceType is null, no service found.");
            return null;
        }

        VcapPojo pojo = null;

        switch (findCountByServiceType(serviceType, pojos)) {
            case 0:
                log("VcapResult.findPojoForServiceType: No services of type "
                        + serviceType.toString() + " found.");
                break;
            case 1:
                log("VcapResult.findPojoForServiceType: One services of type "
                        + serviceType.toString() + " found.");
                pojo = findByServiceType(serviceType, pojos);
                if (pojo != null) {
                    log("VcapResult.findPojoForServiceType: Found the matching pojo");
                }
                break;
            default:
                log("VcapResult.findPojoForServiceType: More than one service of type "
                        + serviceType.toString()
                        + " found, cannot autoconfigure service, must use factory instead.");
                break;
        }
        return pojo;
    }

    private int findCountByServiceType(VcapServiceType serviceType, VcapPojo[] pojos) {
        int result = 0;

        if (serviceType != null) {
            for (final VcapPojo pojo : pojos) {
                if (serviceType.toString().equals(pojo.getServiceBrokerName())) {
                    result++;
                }
            }
        }

        return result;
    }

    private void populateDefaultServiceBusProperties(Map<String, Object> map, VcapPojo pojo) {
        log("VcapResult.populateDefaultServiceBusProperties " + pojo);
        map.put(Constants.NAMESPACE_SERVICE_BUS + "." + RESULT, this);
        if (pojo != null) {
            map.put(Constants.NAMESPACE_SERVICE_BUS + "." + CONNECTION_STRING, buildServiceBusConnectString(pojo));
            log("VcapResult.populateDefaultServiceBusProperties: Updated Service Bus properties");
        }
    }

    private String buildServiceBusConnectString(VcapPojo pojo) {
        final String connectionString =
                "Endpoint=sb://"
                        + pojo.getServiceConfig().getCredentials().get(Constants.NAMESPACE_NAME)
                        + "."
                        + AZURE_SERVICE_BUS_DOMAIN
                        + "/;"
                        + "SharedAccessKeyName="
                        + pojo.getServiceConfig().getCredentials().get(
                        Constants.SHARED_ACCESS_NAME)
                        + ";"
                        + "SharedAccessKey="
                        + pojo.getServiceConfig().getCredentials().get(
                        Constants.SHARED_ACCESS_KEY_VALUE);
        log("connectionString name = " + connectionString);
        return connectionString;
    }

    private void populateDefaultDocumentDBProperties(Map<String, Object> map,
                                                     VcapPojo pojo) {
        log("VcapResult.populateDefaultDocumentDBProperties " + pojo);
        map.put(Constants.NAMESPACE_DOCUMENTDB + "." + RESULT, this);
        if (pojo != null) {
            map.put(Constants.NAMESPACE_DOCUMENTDB + "." + URI, pojo
                    .getServiceConfig().getCredentials().get(Constants.HOST_ENDPOINT));
            map.put(Constants.NAMESPACE_DOCUMENTDB + "." + KEY, pojo
                    .getServiceConfig().getCredentials().get(Constants.MASTER_KEY));
            map.put(Constants.NAMESPACE_DOCUMENTDB + "." + DATABASE, pojo
                    .getServiceConfig().getCredentials().get(Constants.DATABASE_ID));
            log("VcapResult.populateDefaultDocumentDBProperties: Updated DocumentDB properties");
        }
    }

    private VcapPojo findByServiceType(VcapServiceType serviceType, VcapPojo[] pojos) {
        VcapPojo result = null;

        if (serviceType != null) {
            for (final VcapPojo pojo : pojos) {
                if (serviceType.toString().equals(pojo.getServiceBrokerName())) {
                    result = pojo;
                    break;
                }
            }
        }

        return result;
    }

    private void addOrReplace(MutablePropertySources propertySources,
                              Map<String, Object> map) {
        MapPropertySource target = null;
        if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
            final PropertySource<?> source = propertySources
                    .get(PROPERTY_SOURCE_NAME);
            if (source instanceof MapPropertySource) {
                target = (MapPropertySource) source;
                for (final Entry<String, Object> entry : map.entrySet()) {
                    if (!target.containsProperty(entry.getKey())) {
                        target.getSource().put(entry.getKey(), map.get(entry.getKey()));
                    }
                }
            }
        }
        if (target == null) {
            target = new MapPropertySource(PROPERTY_SOURCE_NAME, map);
        }
        if (!propertySources.contains(PROPERTY_SOURCE_NAME)) {
            propertySources.addLast(target);
        }
    }

    private void log(String msg) {
        if (logFlag) {
            LOGGER.info(msg);
        }
    }

}
