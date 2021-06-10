// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.storage.common.test.shared.ServiceVersionTest

class BlobServiceVersionTest extends ServiceVersionTest {

    @Override
    Class getServiceVersionClass() {
        return BlobServiceVersion.class
    }
}
