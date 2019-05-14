// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class EventDataUtil {

    @SuppressWarnings("serial")
    public static final Set<String> RESERVED_SYSTEM_PROPERTIES = Collections.unmodifiableSet(new HashSet<String>() {{
            add(AmqpConstants.OFFSET_ANNOTATION_NAME);
            add(AmqpConstants.PARTITION_KEY_ANNOTATION_NAME);
            add(AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME);
            add(AmqpConstants.ENQUEUED_TIME_UTC_ANNOTATION_NAME);
            add(AmqpConstants.PUBLISHER_ANNOTATION_NAME);
        }});
}
