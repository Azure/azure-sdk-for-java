// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * The delivery status of an SMS message.
 */
public final class SmsDeliveryStatus extends ExpandableStringEnum<SmsDeliveryStatus> {
    /**
     * Static value Delivered for SmsDeliveryStatus.
     */
    public static final SmsDeliveryStatus DELIVERED = fromString("Delivered");

    /**
     * Static value Failed for SmsDeliveryStatus.
     */
    public static final SmsDeliveryStatus FAILED = fromString("Failed");

    /**
     * Creates a new instance of SmsDeliveryStatus value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public SmsDeliveryStatus() {
    }

    /**
     * Creates or finds a SmsDeliveryStatus from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SmsDeliveryStatus.
     */
    public static SmsDeliveryStatus fromString(String name) {
        return fromString(name, SmsDeliveryStatus.class);
    }

    /**
     * Gets known SmsDeliveryStatus values.
     *
     * @return known SmsDeliveryStatus values.
     */
    public static Collection<SmsDeliveryStatus> values() {
        return values(SmsDeliveryStatus.class);
    }
}
