// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation.simple;

import com.azure.core.util.BinaryData;
import io.netty.buffer.ByteBuf;

public interface SimpleBodyCollector {

    void collect(ByteBuf buffer, boolean isLast);

    BinaryData toBinaryData();
}
