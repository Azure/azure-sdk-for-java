// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake

import com.azure.storage.common.test.shared.ServiceVersionSpec

class DataLakeServiceVersionTest extends ServiceVersionSpec {
    @Override
    protected Class getServiceVersionClass() {
        return DataLakeServiceVersion.class
    }
}
