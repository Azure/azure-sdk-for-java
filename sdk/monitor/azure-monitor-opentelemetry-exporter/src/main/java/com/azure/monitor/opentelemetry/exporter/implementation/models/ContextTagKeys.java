// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for ContextTagKeys. */
public final class ContextTagKeys extends ExpandableStringEnum<ContextTagKeys> {
    /** Static value ai.application.ver for ContextTagKeys. */
    public static final ContextTagKeys AI_APPLICATION_VER = fromString("ai.application.ver");

    /** Static value ai.device.id for ContextTagKeys. */
    public static final ContextTagKeys AI_DEVICE_ID = fromString("ai.device.id");

    /** Static value ai.device.locale for ContextTagKeys. */
    public static final ContextTagKeys AI_DEVICE_LOCALE = fromString("ai.device.locale");

    /** Static value ai.device.model for ContextTagKeys. */
    public static final ContextTagKeys AI_DEVICE_MODEL = fromString("ai.device.model");

    /** Static value ai.device.oemName for ContextTagKeys. */
    public static final ContextTagKeys AI_DEVICE_OEM_NAME = fromString("ai.device.oemName");

    /** Static value ai.device.osVersion for ContextTagKeys. */
    public static final ContextTagKeys AI_DEVICE_OS_VERSION = fromString("ai.device.osVersion");

    /** Static value ai.device.type for ContextTagKeys. */
    public static final ContextTagKeys AI_DEVICE_TYPE = fromString("ai.device.type");

    /** Static value ai.location.ip for ContextTagKeys. */
    public static final ContextTagKeys AI_LOCATION_IP = fromString("ai.location.ip");

    /** Static value ai.location.country for ContextTagKeys. */
    public static final ContextTagKeys AI_LOCATION_COUNTRY = fromString("ai.location.country");

    /** Static value ai.location.province for ContextTagKeys. */
    public static final ContextTagKeys AI_LOCATION_PROVINCE = fromString("ai.location.province");

    /** Static value ai.location.city for ContextTagKeys. */
    public static final ContextTagKeys AI_LOCATION_CITY = fromString("ai.location.city");

    /** Static value ai.operation.id for ContextTagKeys. */
    public static final ContextTagKeys AI_OPERATION_ID = fromString("ai.operation.id");

    /** Static value ai.operation.name for ContextTagKeys. */
    public static final ContextTagKeys AI_OPERATION_NAME = fromString("ai.operation.name");

    /** Static value ai.operation.parentId for ContextTagKeys. */
    public static final ContextTagKeys AI_OPERATION_PARENT_ID = fromString("ai.operation.parentId");

    /** Static value ai.operation.syntheticSource for ContextTagKeys. */
    public static final ContextTagKeys AI_OPERATION_SYNTHETIC_SOURCE = fromString("ai.operation.syntheticSource");

    /** Static value ai.operation.correlationVector for ContextTagKeys. */
    public static final ContextTagKeys AI_OPERATION_CORRELATION_VECTOR = fromString("ai.operation.correlationVector");

    /** Static value ai.session.id for ContextTagKeys. */
    public static final ContextTagKeys AI_SESSION_ID = fromString("ai.session.id");

    /** Static value ai.session.isFirst for ContextTagKeys. */
    public static final ContextTagKeys AI_SESSION_IS_FIRST = fromString("ai.session.isFirst");

    /** Static value ai.user.accountId for ContextTagKeys. */
    public static final ContextTagKeys AI_USER_ACCOUNT_ID = fromString("ai.user.accountId");

    /** Static value ai.user.id for ContextTagKeys. */
    public static final ContextTagKeys AI_USER_ID = fromString("ai.user.id");

    /** Static value ai.user.authUserId for ContextTagKeys. */
    public static final ContextTagKeys AI_USER_AUTH_USER_ID = fromString("ai.user.authUserId");

    /** Static value ai.cloud.role for ContextTagKeys. */
    public static final ContextTagKeys AI_CLOUD_ROLE = fromString("ai.cloud.role");

    /** Static value ai.cloud.roleVer for ContextTagKeys. */
    public static final ContextTagKeys AI_CLOUD_ROLE_VER = fromString("ai.cloud.roleVer");

    /** Static value ai.cloud.roleInstance for ContextTagKeys. */
    public static final ContextTagKeys AI_CLOUD_ROLE_INSTANCE = fromString("ai.cloud.roleInstance");

    /** Static value ai.cloud.location for ContextTagKeys. */
    public static final ContextTagKeys AI_CLOUD_LOCATION = fromString("ai.cloud.location");

    /** Static value ai.internal.sdkVersion for ContextTagKeys. */
    public static final ContextTagKeys AI_INTERNAL_SDK_VERSION = fromString("ai.internal.sdkVersion");

    /** Static value ai.internal.agentVersion for ContextTagKeys. */
    public static final ContextTagKeys AI_INTERNAL_AGENT_VERSION = fromString("ai.internal.agentVersion");

    /** Static value ai.internal.nodeName for ContextTagKeys. */
    public static final ContextTagKeys AI_INTERNAL_NODE_NAME = fromString("ai.internal.nodeName");

    /**
     * Creates or finds a ContextTagKeys from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ContextTagKeys.
     */
    @JsonCreator
    public static ContextTagKeys fromString(String name) {
        return fromString(name, ContextTagKeys.class);
    }

    /** @return known ContextTagKeys values. */
    public static Collection<ContextTagKeys> values() {
        return values(ContextTagKeys.class);
    }
}
