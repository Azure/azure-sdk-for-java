// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * Minimal Apache Arrow IPC FlatBuffer metadata accessors used internally by the Blob Storage ListBlobs Arrow reader.
 * <p>
 * The classes in this package are thin {@link com.google.flatbuffers.Table}/{@link com.google.flatbuffers.Struct}
 * accessors over the FlatBuffer-encoded metadata of the Apache Arrow IPC format. They expose only the small subset of
 * the {@code org.apache.arrow.flatbuf} schema that {@code BlobListArrowStreamReader} requires, so that the main
 * (Java 8 baseline) compile classpath does not depend on the {@code arrow-format} artifact, which ships Java 11
 * bytecode.
 * <p>
 * The field orderings (FlatBuffer vtable slots) and enum values are defined by the public Apache Arrow columnar format
 * specification (see {@code Schema.fbs} and {@code Message.fbs} in the Apache Arrow project, licensed under the Apache
 * License, Version 2.0) and must match the on-the-wire layout produced by the Storage service.
 */
package com.azure.storage.blob.implementation.util.arrow;
