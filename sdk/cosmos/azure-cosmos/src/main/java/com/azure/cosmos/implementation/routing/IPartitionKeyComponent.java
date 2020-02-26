// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.OutputStream;

interface IPartitionKeyComponent {
    int CompareTo(IPartitionKeyComponent other);

    int GetTypeOrdinal();

    void JsonEncode(JsonGenerator writer);

    void WriteForHashing(OutputStream outputStream);

    void WriteForHashingV2(OutputStream binaryWriter);

    void WriteForBinaryEncoding(OutputStream binaryWriter);

    IPartitionKeyComponent Truncate();
}
