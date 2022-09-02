/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import java.util.Map;

final class ResourceParser {

    private static final String DEFAULT_SERVICE_NAME = "unknown_service:java";

    private ResourceParser() {
    }

    static void updateRoleNameAndInstance(AbstractTelemetryBuilder builder, Resource resource) {
        Map<String, String> existingTags = builder.build().getTags();
        if (existingTags == null
            || !existingTags.containsKey(ContextTagKeys.AI_CLOUD_ROLE.toString())) {
            String serviceName = resource.getAttribute(ResourceAttributes.SERVICE_NAME);
            if (serviceName == null || DEFAULT_SERVICE_NAME.equals(serviceName)) {
                String websiteSiteName = Strings.trimAndEmptyToNull(System.getenv("WEBSITE_SITE_NAME"));
                if (websiteSiteName != null) {
                    serviceName = websiteSiteName;
                }
            }
            String serviceNamespace = resource.getAttribute(ResourceAttributes.SERVICE_NAMESPACE);
            String roleName = null;
            if (serviceName != null && serviceNamespace != null) {
                roleName = "[" + serviceNamespace + "]/" + serviceName;
            } else if (serviceName != null) {
                roleName = serviceName;
            } else if (serviceNamespace != null) {
                roleName = "[" + serviceNamespace + "]";
            }
            if (roleName != null) {
                builder.addTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), roleName);
            }
        }

        if (existingTags == null
            || !existingTags.containsKey(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString())) {
            String roleInstance = resource.getAttribute(ResourceAttributes.SERVICE_INSTANCE_ID);
            if (roleInstance == null) {
                roleInstance = Strings.trimAndEmptyToNull(System.getenv("WEBSITE_INSTANCE_ID"));
            }
            if (roleInstance == null) {
                roleInstance = System.getenv("HOSTNAME"); // default hostname
            }
            if (roleInstance != null) {
                builder.addTag(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), roleInstance);
            }
        }
    }
}
