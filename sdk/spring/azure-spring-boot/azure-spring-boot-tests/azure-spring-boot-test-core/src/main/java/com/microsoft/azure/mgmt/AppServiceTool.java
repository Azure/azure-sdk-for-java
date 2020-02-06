/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.mgmt;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.JavaVersion;
import com.microsoft.azure.management.appservice.PricingTier;
import com.microsoft.azure.management.appservice.RuntimeStack;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApps;
import com.microsoft.azure.management.appservice.WebContainer;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class AppServiceTool {

    private WebApps webApps;

    public AppServiceTool(Access access) {
        webApps = Azure
                .authenticate(access.credentials())
                .withSubscription(access.subscription())
                .webApps();
    }

    public WebApp createAppService(String resourceGroup, String prefix, Map<String, String> settings) {
        final String appName = SdkContext.randomResourceName(prefix, 20);

        log.info("Creating web app " + appName);

        final WebApp app = webApps
                .define(appName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(resourceGroup)
                .withNewLinuxPlan(PricingTier.STANDARD_S1)
                .withBuiltInImage(RuntimeStack.JAVA_8_JRE8)
                .withSystemAssignedManagedServiceIdentity()
                .withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole.OWNER)
                .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                .withWebContainer(WebContainer.JAVA_8)
                .withAppSettings(settings)
                .withContainerLoggingEnabled()
                .create();

        log.info("Created web app " + app.name());
        return app;
    }

}
