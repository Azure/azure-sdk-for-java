// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.storage.blob.sas.BlobSasServiceVersion
import com.azure.storage.common.test.shared.ServiceVersionTest

class BlobSasServiceVersionTest extends ServiceVersionTest {

    @Override
    Class getServiceVersionClass() {
        return BlobSasServiceVersion.class
    }
}
