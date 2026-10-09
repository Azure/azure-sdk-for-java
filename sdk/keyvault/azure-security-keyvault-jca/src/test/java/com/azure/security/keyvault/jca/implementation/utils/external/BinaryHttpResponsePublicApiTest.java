// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils.external;

import com.azure.security.keyvault.jca.implementation.utils.HttpUtil;
import com.azure.security.keyvault.jca.implementation.utils.HttpUtil.BinaryHttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BinaryHttpResponsePublicApiTest {

    @Test
    public void responseMetadataIsAccessibleOutsideTheOwningPackage() {
        BinaryHttpResponse response = HttpUtil.getAiaBytesWithMetadata("unsupported://example.test");

        assertNotNull(response);
        assertNull(response.getBody());
        assertNull(response.getCacheControl());
        assertNull(response.getDate());
        assertNull(response.getAge());
        assertNull(response.getExpires());
    }
}
