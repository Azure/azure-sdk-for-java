// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.v2.util.ExpandableStringEnum;

public class EnumWithPackagePrivateCtor extends ExpandableStringEnum<EnumWithPackagePrivateCtor> {
    @Deprecated
    EnumWithPackagePrivateCtor() {
    }

    public static EnumWithPackagePrivateCtor fromString(String name) {
        return fromString(name, EnumWithPackagePrivateCtor.class);
    }
}
