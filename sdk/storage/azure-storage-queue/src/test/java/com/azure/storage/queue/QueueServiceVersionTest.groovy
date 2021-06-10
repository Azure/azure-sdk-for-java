// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.storage.common.test.shared.ServiceVersionTest

class QueueServiceVersionTest extends ServiceVersionTest{
    @Override
    Class getServiceVersionClass() {
        return QueueServiceVersion.class
    }
}
