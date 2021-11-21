// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.storage.common.test.shared.ServiceVersionSpec

class BlobServiceVersionTest extends ServiceVersionSpec {
    @Override
    protected Class getServiceVersionClass() {
        return BlobServiceVersion.class
    }
}
