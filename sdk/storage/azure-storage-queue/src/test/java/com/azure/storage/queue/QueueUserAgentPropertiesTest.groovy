// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.util.CoreUtils
import com.azure.core.util.UserAgentProperties
import spock.lang.Specification

class QueueUserAgentPropertiesTest extends Specification {

    def "User agent properties not null"() {
        given:
        Map<String, String> properties = CoreUtils.getProperties("azure-storage-queue.properties")
        expect:
        properties.get("name") == "azure-storage-queue"
        properties.get("version").matches("(\\d)+.(\\d)+.(\\d)+([-a-zA-Z0-9.])*")
    }
}
