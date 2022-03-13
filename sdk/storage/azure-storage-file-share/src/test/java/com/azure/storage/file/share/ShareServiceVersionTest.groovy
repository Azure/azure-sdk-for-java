// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.storage.common.test.shared.ServiceVersionSpec

class ShareServiceVersionTest extends ServiceVersionSpec {
    @Override
    protected Class getServiceVersionClass() {
        return ShareServiceVersion.class
    }
}
