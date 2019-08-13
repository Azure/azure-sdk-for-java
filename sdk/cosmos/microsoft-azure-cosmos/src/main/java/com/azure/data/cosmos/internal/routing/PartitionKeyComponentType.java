// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

enum PartitionKeyComponentType {
    UNDEFINED(0x0),
    NULL(0x1),
    FALSE(0x2),
    TRUE(0x3),
    MINNUMBER(0x4),
    NUMBER(0x5),
    MAXNUMBER(0x6),
    MINSTRING(0x7),
    STRING(0x8),
    MAXSTRING(0x9),
    INFINITY(0xFF);

    public final int type;
    PartitionKeyComponentType(int type) {
        this.type = type;
    }
}
