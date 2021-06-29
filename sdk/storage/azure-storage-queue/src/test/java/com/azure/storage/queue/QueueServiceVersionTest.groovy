// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.storage.common.test.shared.ServiceVersionSpec

class QueueServiceVersionTest extends ServiceVersionSpec {
    @Override
    protected Class getServiceVersionClass() {
        return QueueServiceVersion.class
    }
}
