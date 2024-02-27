// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.OutputStream;

interface IPartitionKeyComponent {
    int compareTo(IPartitionKeyComponent other);

    int getTypeOrdinal();

    void jsonEncode(JsonGenerator writer);

    void writeForHashing(OutputStream outputStream);

    void writeForHashingV2(OutputStream binaryWriter);

    void writeForBinaryEncoding(OutputStream binaryWriter);

    IPartitionKeyComponent truncate();
    Object toObject();
}
