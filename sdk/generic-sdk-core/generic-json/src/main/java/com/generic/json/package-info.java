// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>The Azure JSON library provides interfaces for stream-style JSON reading and writing. Stream-style reading and
 * writing has the type itself define how to read JSON to create an instance of itself and how it writes out to JSON.
 * Azure JSON also allows for external implementations for JSON reading and writing by offering a
 * <a href="https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html">service provider interface</a> to load
 * implementations from the classpath. However, if one is not found, the Azure JSON library provides a default
 * implementation.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>{@link com.generic.json.JsonSerializable} is the base of Azure JSON: it's the interface that types implement to
 * provide stream-style JSON reading and writing functionality. The interface has a single implementable method
 * {@link com.generic.json.JsonSerializable#toJson(com.generic.json.JsonWriter) toJson(JsonWriter)} that defines how the
 * object is written as JSON, to the {@link com.generic.json.JsonWriter}, and a static method
 * {@link com.generic.json.JsonSerializable#fromJson(com.generic.json.JsonReader) fromJson(JsonReader)} that defines how to
 * read an instance of the object from JSON, being read from the {@link com.generic.json.JsonReader}. The default
 * implementation of {@link com.generic.json.JsonSerializable#fromJson(com.generic.json.JsonReader) fromJson(JsonReader)}
 * throws an {@link java.lang.UnsupportedOperationException} if the static method isn't hidden (a static method with the
 * same definition) by the type implementing {@link com.generic.json.JsonSerializable}. Given that the type itself manages
 * JSON serialization the type can be fluent, immutable, or a mix of fluent and immutable, it doesn't matter as all
 * logic is self-encapsulated.</p>
 *
 * <p><strong>Sample: All JsonSerializable fields are optional</strong></p>
 *
 * <!-- src_embed com.generic.json.JsonSerializable.ComputerMemory -->
 * <!-- end com.generic.json.JsonSerializable.ComputerMemory -->
 *
 * <p><strong>Sample: All JsonSerializable fields are required</strong></p>
 *
 * <!-- src_embed com.generic.json.JsonSerializable.ComputerProcessor -->
 * <!-- end com.generic.json.JsonSerializable.ComputerProcessor -->
 *
 * <p><strong>Sample: JsonSerializable contains required and optional fields</strong></p>
 *
 * <!-- src_embed com.generic.json.JsonSerializable.VmStatistics -->
 * <!-- end com.generic.json.JsonSerializable.VmStatistics -->
 *
 * <h2>Reading and Writing JSON</h2>
 *
 * <p>{@link com.generic.json.JsonReader} contains APIs and logic for parsing JSON. The type is abstract and consists of
 * both abstract methods for an implementation to implement as well as final method for commonly shared logic that
 * builds on the abstract methods. Similarly, {@link com.generic.json.JsonWriter} contains APIs and logic for writing
 * JSON, and as with {@link com.generic.json.JsonReader}, it contains both abstract methods for implementations to
 * implement and final methods for commonly shared logic that builds on the abstract methods. Both types implement
 * {@link java.io.Closeable} and should be used in try-with-resources blocks to ensure any resources created by
 * the implementations are cleaned up once JSON reading or writing is complete. Both types are used by the
 * {@link com.generic.json.JsonProvider} service provider interface which is used to create instances of
 * {@link com.generic.json.JsonReader} and {@link com.generic.json.JsonWriter} implementations.</p>
 *
 *
 * <p>{@link com.generic.json.JsonProviders} is a utility class that handles finding {@link com.generic.json.JsonProvider}
 * implementations on the classpath and should be the default way to create instances of
 * {@link com.generic.json.JsonReader} and {@link com.generic.json.JsonWriter}. As mentioned earlier, the Azure JSON
 * package provides a default implementation allowing for the library to be used stand-alone.
 * {@link com.generic.json.JsonReader} can be created from {@code byte[]}, {@link java.lang.String},
 * {@link java.io.InputStream}, and {@link java.io.Reader} sources, {@link com.generic.json.JsonWriter} can be created
 * from {@link java.io.OutputStream} and {@link java.io.Writer} sources. No matter the source the functionality will be
 * the same, the options exist to provide the best convenience and performance by reducing type translations.
 *
 * <p><strong>Sample: Reading a JSON byte[]</strong></p>
 *
 * <!-- src_embed com.generic.json.JsonReader.readJsonByteArray -->
 * <!-- end com.generic.json.JsonReader.readJsonByteArray -->
 *
 * <p><strong>Sample: Reading a JSON String</strong></p>
 *
 * <!-- src_embed com.generic.json.JsonReader.readJsonString -->
 * <!-- end com.generic.json.JsonReader.readJsonString -->
 *
 * <p><strong>Sample: Reading a JSON InputStream</strong></p>
 *
 * <!-- src_embed com.generic.json.JsonReader.readJsonInputStream -->
 * <!-- end com.generic.json.JsonReader.readJsonInputStream -->
 *
 * <p><strong>Sample: Reading a JSON Reader</strong></p>
 *
 * <!-- src_embed com.generic.json.JsonReader.readJsonReader -->
 * <!-- end com.generic.json.JsonReader.readJsonReader -->
 *
 * <p><strong>Sample: Writing to a JSON OutputStream</strong></p>
 *
 * <!-- src_embed com.generic.json.JsonWriter.writeJsonOutputStream -->
 * <!-- end com.generic.json.JsonWriter.writeJsonOutputStream -->
 *
 * <p><strong>Sample: Writing to a JSON Writer</strong></p>
 *
 * <!-- src_embed com.generic.json.JsonWriter.writeJsonWriter -->
 * <!-- end com.generic.json.JsonWriter.writeJsonWriter -->
 *
 * @see com.generic.json.JsonSerializable
 * @see com.generic.json.JsonReader
 * @see com.generic.json.JsonWriter
 * @see com.generic.json.JsonProvider
 * @see com.generic.json.JsonProviders
 */
package com.generic.json;
