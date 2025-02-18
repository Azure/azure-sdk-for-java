// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.util.CoreUtils;

class SampleHelper {
    static String generateRandomName(String prefix, int length) {
        int len = length > prefix.length() ? length - prefix.length() : 0;
        return prefix + CoreUtils.randomUuid().toString().substring(0, len);
    }
}
