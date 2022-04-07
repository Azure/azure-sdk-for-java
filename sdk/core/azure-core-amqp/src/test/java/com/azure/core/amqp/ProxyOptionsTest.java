// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.azure.core.amqp.ProxyOptions.SYSTEM_DEFAULTS;

public class ProxyOptionsTest {

    /**
     * Test System default proxy configuration properties are set
     */
    @Test
    public void systemDefaultProxyConfiguration() {
        Assertions.assertEquals(ProxyAuthenticationType.NONE, SYSTEM_DEFAULTS.getAuthentication());
        Assertions.assertNull(SYSTEM_DEFAULTS.getCredential());
        Assertions.assertNull(SYSTEM_DEFAULTS.getProxyAddress());
    }
}
