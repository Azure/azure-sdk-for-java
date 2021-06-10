// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.storage.common.test.shared.ServiceVersionTest

class ShareServiceVersionTest extends ServiceVersionTest {
    @Override
    Class getServiceVersionClass() {
        return ShareServiceVersion.class
    }
}
