// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.util.CoreUtils
import com.azure.core.util.UserAgentProperties
import spock.lang.Specification

class FileShareUserAgentPropertiesTest extends Specification {

    def "User agent properties not null"() {
        given:
        UserAgentProperties properties = CoreUtils.getUserAgentProperties(
            "azure-storage-file-share.properties")
        expect:
        !properties.getName().matches("UnknownName")
        properties.getVersion().matches("(\\d)+.(\\d)+.(\\d)+([-a-zA-Z0-9.])*")
    }
}
