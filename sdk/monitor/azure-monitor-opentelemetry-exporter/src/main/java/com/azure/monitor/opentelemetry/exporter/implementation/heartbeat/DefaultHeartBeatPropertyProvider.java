// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.heartbeat;

import com.azure.monitor.opentelemetry.exporter.implementation.utils.VersionGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This class is a concrete implementation of {@link HeartBeatPayloadProviderInterface}. It enables
 * setting SDK Metadata to heartbeat payload.
 */
public class DefaultHeartBeatPropertyProvider implements HeartBeatPayloadProviderInterface {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHeartBeatPropertyProvider.class);

    /**
     * Collection holding default properties for this default provider.
     */
    final Set<String> defaultFields;

    /**
     * Random GUID that would help in analysis when app has stopped and restarted. Each restart will
     * have a new GUID. If the application is unstable and goes through frequent restarts this will
     * help us identify instability in the analytics backend.
     */
    private static volatile UUID uniqueProcessId;

    private static final String JRE_VERSION = "jreVersion";

    private static final String SDK_VERSION = "sdkVersion";

    private static final String OS_VERSION = "osVersion";

    private static final String PROCESS_SESSION_ID = "processSessionId";

    private static final String OS_TYPE = "osType";

    public DefaultHeartBeatPropertyProvider() {
        defaultFields = new HashSet<>();
        initializeDefaultFields(defaultFields);
    }

    @Override
    public Runnable setDefaultPayload(HeartbeatExporter provider) {
        return new Runnable() {

            final Set<String> enabledProperties = defaultFields;

            @Override
            public void run() {
                for (String fieldName : enabledProperties) {
                    try {
                        switch (fieldName) {
                            case JRE_VERSION:
                                provider.addHeartBeatProperty(fieldName, getJreVersion(), true);
                                break;

                            case SDK_VERSION:
                                provider.addHeartBeatProperty(fieldName, getSdkVersion(), true);
                                break;

                            case OS_VERSION:
                            case OS_TYPE:
                                provider.addHeartBeatProperty(fieldName, getOsVersion(), true);
                                break;

                            case PROCESS_SESSION_ID:
                                provider.addHeartBeatProperty(fieldName, getProcessSessionId(), true);
                                break;

                            default:
                                // We won't accept unknown properties in default providers.
                                logger.trace("Encountered unknown default property");
                                break;
                        }
                    } catch (RuntimeException e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Failed to obtain heartbeat property", e);
                        }
                    }
                }
            }
        };
    }

    /**
     * This method initializes the collection with Default Properties of this provider.
     *
     * @param defaultFields collection to hold default properties.
     */
    private static void initializeDefaultFields(Set<String> defaultFields) {
        defaultFields.add(JRE_VERSION);
        defaultFields.add(SDK_VERSION);
        defaultFields.add(OS_VERSION);
        defaultFields.add(PROCESS_SESSION_ID);
        defaultFields.add(OS_TYPE);
    }

    /**
     * Returns the JDK version being used by the application.
     */
    private static String getJreVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Returns the Application Insights SDK version user is using to instrument his application.
     */
    private static String getSdkVersion() {
        return VersionGenerator.getSdkVersion();
    }

    /**
     * Returns the OS version on which application is running.
     */
    private static String getOsVersion() {
        return System.getProperty("os.name");
    }

    /**
     * Returns the Unique GUID for the application's current session.
     */
    private static String getProcessSessionId() {
        if (uniqueProcessId == null) {
            uniqueProcessId = UUID.randomUUID();
        }
        return uniqueProcessId.toString();
    }
}
