// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.heartbeat;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * <h1>WebApp Heartbeat Property Provider</h1>
 *
 * <p>This class is a concrete implementation of {@link HeartBeatPayloadProviderInterface} It
 * enables setting Web-apps Metadata to heartbeat payload.
 */
public class WebAppsHeartbeatProvider implements HeartBeatPayloadProviderInterface {

    private static final ClientLogger logger = new ClientLogger(WebAppsHeartbeatProvider.class);
    private static final String WEBSITE_SITE_NAME = "appSrv_SiteName";
    private static final String WEBSITE_HOSTNAME = "appSrv_wsHost";
    private static final String WEBSITE_HOME_STAMPNAME = "appSrv_wsStamp";
    private static final String WEBSITE_OWNER_NAME = "appSrv_wsOwner";
    private static final String WEBSITE_RESOURCE_GROUP = "appSrv_ResourceGroup";
    // Only populated in Azure functions
    private static final String WEBSITE_SLOT_NAME = "appSrv_SlotName";
    /**
     * Collection holding default properties for this default provider.
     */
    private final Set<String> defaultFields;
    /**
     * Map for storing environment variables.
     */
    private Map<String, String> environmentMap;

    /**
     * Constructor that initializes fields and load environment variables.
     */
    public WebAppsHeartbeatProvider() {
        defaultFields = new HashSet<>();
        environmentMap = System.getenv();
        initializeDefaultFields(defaultFields);
    }

    /**
     * Populates the default Fields with the properties.
     */
    private static void initializeDefaultFields(Set<String> defaultFields) {
        defaultFields.add(WEBSITE_SITE_NAME);
        defaultFields.add(WEBSITE_HOSTNAME);
        defaultFields.add(WEBSITE_HOME_STAMPNAME);
        defaultFields.add(WEBSITE_OWNER_NAME);
        defaultFields.add(WEBSITE_RESOURCE_GROUP);
        defaultFields.add(WEBSITE_SLOT_NAME);
    }

    @Override
    public Callable<Boolean> setDefaultPayload(HeartbeatExporter provider) {
        return new Callable<>() {

            final Set<String> enabledProperties = defaultFields;

            @Override
            public Boolean call() {

                boolean hasSetValues = false;
                // update environment variable to account for
                updateEnvironmentVariableMap();
                for (String fieldName : enabledProperties) {
                    try {
                        switch (fieldName) {
                            case WEBSITE_SITE_NAME:
                                String webSiteName = getWebsiteSiteName();
                                if (Strings.isNullOrEmpty(webSiteName)) {
                                    break;
                                }
                                provider.addHeartBeatProperty(fieldName, webSiteName, true);
                                hasSetValues = true;
                                break;
                            case WEBSITE_HOSTNAME:
                                String webSiteHostName = getWebsiteHostName();
                                if (Strings.isNullOrEmpty(webSiteHostName)) {
                                    break;
                                }
                                provider.addHeartBeatProperty(fieldName, webSiteHostName, true);
                                hasSetValues = true;
                                break;
                            case WEBSITE_HOME_STAMPNAME:
                                String websiteHomeStampName = getWebsiteHomeStampName();
                                if (Strings.isNullOrEmpty(websiteHomeStampName)) {
                                    break;
                                }
                                provider.addHeartBeatProperty(fieldName, websiteHomeStampName, true);
                                hasSetValues = true;
                                break;
                            case WEBSITE_OWNER_NAME:
                                String websiteOwnerName = getWebsiteOwnerName();
                                if (Strings.isNullOrEmpty(websiteOwnerName)) {
                                    break;
                                }
                                provider.addHeartBeatProperty(fieldName, websiteOwnerName, true);
                                hasSetValues = true;
                                break;
                            case WEBSITE_RESOURCE_GROUP:
                                String websiteResourceGroup = getWebsiteResourceGroup();
                                if (Strings.isNullOrEmpty(websiteResourceGroup)) {
                                    break;
                                }
                                provider.addHeartBeatProperty(fieldName, websiteResourceGroup, true);
                                hasSetValues = true;
                                break;
                            case WEBSITE_SLOT_NAME:
                                String websiteSlotName = getWebsiteSlotName();
                                if (Strings.isNullOrEmpty(websiteSlotName)) {
                                    break;
                                }
                                provider.addHeartBeatProperty(fieldName, websiteSlotName, true);
                                hasSetValues = true;
                                break;
                            default:
                                logger.verbose("Unknown web apps property encountered");
                                break;
                        }
                    } catch (RuntimeException e) {
                        logger.warning("Failed to obtain heartbeat property", e);
                    }
                }
                return hasSetValues;
            }
        };
    }

    /**
     * Returns the name of the website by reading environment variable.
     */
    private String getWebsiteSiteName() {
        return environmentMap.get("WEBSITE_SITE_NAME");
    }

    /**
     * Returns the website host name by reading environment variable.
     */
    private String getWebsiteHostName() {
        return environmentMap.get("WEBSITE_HOSTNAME");
    }

    /**
     * Returns the website home stamp name by reading environment variable.
     */
    private String getWebsiteHomeStampName() {
        return environmentMap.get("WEBSITE_HOME_STAMPNAME");
    }

    /**
     * Returns the website owner name by reading environment variable.
     */
    private String getWebsiteOwnerName() {
        return environmentMap.get("WEBSITE_OWNER_NAME");
    }

    /**
     * Returns the website resource group by reading environment variable.
     */
    private String getWebsiteResourceGroup() {
        return environmentMap.get("WEBSITE_RESOURCE_GROUP");
    }

    /**
     * Returns the website slot name by reading environment variable.
     */
    private String getWebsiteSlotName() {
        return environmentMap.get("WEBSITE_SLOT_NAME");
    }

    /**
     * This method updates the environment variable at every call to add the payload, to cover hotswap
     * scenarios.
     */
    private void updateEnvironmentVariableMap() {
        environmentMap = System.getenv();
    }
}
