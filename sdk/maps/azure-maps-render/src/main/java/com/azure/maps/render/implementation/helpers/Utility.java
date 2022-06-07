// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.render.implementation.helpers;

import com.azure.maps.render.implementation.models.IncludeText;

public class Utility {
    public static IncludeText toIncludeTextPrivate(boolean includeText) {
        return IncludeText.fromString(String.valueOf(includeText));
    }
}
