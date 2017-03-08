/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.proton.amqp.messaging.AmqpSequence;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Data;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;

public class UnexpectedEventDataBodyException extends RuntimeException {
    
    private static final Map<Class, String> KNOWN_SECTIONS = new HashMap<Class, String>() {{
        put(AmqpValue.class, AmqpConstants.AMQP_VALUE);
        put(AmqpSequence.class, AmqpConstants.AMQP_SEQUENCE);
    }};
    
    private final Class bodySection;
    
    public UnexpectedEventDataBodyException(final Class actualBodySection) {
        super(KNOWN_SECTIONS.containsKey(actualBodySection)
            ? String.format("AmqpMessage Body Section will be available in %s.getBody() only if it is of type: %s. " + 
                    "If AmqpMessage has any other type as part of Body Section - it will be added to %s.getSystemProperties()." + 
                    " Use '%s' as Key to fetch this from %s.getSystemProperties().",
                    EventData.class, Data.class, EventData.class, KNOWN_SECTIONS.get(actualBodySection), EventData.class)
            : "AmqpMessage Body Section cannot be mapped to any EventData section.");
        this.bodySection = actualBodySection;
    }
    
    // used for testing
    public String getSystemPropertyName() {
        return KNOWN_SECTIONS.get(this.bodySection);
    }    
}
