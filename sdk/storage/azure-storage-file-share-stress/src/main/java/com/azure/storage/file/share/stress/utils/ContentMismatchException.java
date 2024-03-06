// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.stress.utils;

public class ContentMismatchException extends RuntimeException {
    public ContentMismatchException() {
        super("crc mismatch");
    }
}
