// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>The Azure JSON library provides interfaces for stream-style JSON reading and writing. Stream-style reading and
 * writing has the type itself defines how to read JSON to create and instance of itself and how it writes JSON.
 * Azure JSON also provides the ability to provide implementations for JSON reading and writing by offering a service
 * provider interface to load implementations from the classpath, the library provides a default implementation if
 * one cannot be found on the classpath.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>{@link com.azure.json.JsonSerializable} is the base of Azure JSON, it is the interface that types implement to
 * provide stream-style JSON reading and writing functionality. The interface has a single implementable method
 * {@link com.azure.json.JsonSerializable#toJson(com.azure.json.JsonWriter)} that defines how the object will be written
 * as JSON, to the {@link com.azure.json.JsonWriter}, and an over-writable static method
 * {@link com.azure.json.JsonSerializable#fromJson(com.azure.json.JsonReader)} that defines how to read an instance of
 * the object from JSON, being read from the {@link com.azure.json.JsonReader}. The default implementation of
 * {@link com.azure.json.JsonSerializable#fromJson(com.azure.json.JsonReader)} will throw an
 * {@link java.lang.UnsupportedOperationException} if the static method isn't overwritten. Given that the type itself
 * manages JSON serialization the type can be fluent, immutable, or a mix of fluent and immutable, it does not matter
 * as all logic is self-encapsulated.</p>
 *
 * <p><strong>Sample: Fluent JsonSerializable</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonSerializable.fluent -->
 * <pre>
 *
 * &#47;**
 *  * Implementation of JsonSerializable where all properties are fluently set.
 *  *&#47;
 * public class FluentJsonSerializableExample implements JsonSerializable&lt;FluentJsonSerializableExample&gt; &#123;
 *     private int anInt;
 *     private boolean aBoolean;
 *     private String aString;
 *     private Double aNullableDecimal;
 *
 *     &#47;**
 *      * Sets an integer value.
 *      *
 *      * &#64;param anInt The integer value.
 *      * &#64;return The update FluentJsonSerializableExample
 *      *&#47;
 *     public FluentJsonSerializableExample setAnInt&#40;int anInt&#41; &#123;
 *         this.anInt = anInt;
 *         return this;
 *     &#125;
 *
 *     &#47;**
 *      * Sets a boolean value.
 *      *
 *      * &#64;param aBoolean The boolean value.
 *      * &#64;return The update FluentJsonSerializableExample
 *      *&#47;
 *     public FluentJsonSerializableExample setABoolean&#40;boolean aBoolean&#41; &#123;
 *         this.aBoolean = aBoolean;
 *         return  this;
 *     &#125;
 *
 *     &#47;**
 *      * Sets a string value.
 *      *
 *      * &#64;param aString The string value.
 *      * &#64;return The update FluentJsonSerializableExample
 *      *&#47;
 *     public FluentJsonSerializableExample setAString&#40;String aString&#41; &#123;
 *         this.aString = aString;
 *         return this;
 *     &#125;
 *
 *     &#47;**
 *      * Sets a nullable decimal value.
 *      *
 *      * &#64;param aNullableDecimal The nullable decimal value.
 *      * &#64;return The update FluentJsonSerializableExample
 *      *&#47;
 *     public FluentJsonSerializableExample setANullableDecimal&#40;Double aNullableDecimal&#41; &#123;
 *         this.aNullableDecimal = aNullableDecimal;
 *         return this;
 *     &#125;
 *
 *     &#64;Override
 *     public JsonWriter toJson&#40;JsonWriter jsonWriter&#41; throws IOException &#123;
 *         &#47;&#47; Optionally 'flush' can be included in the method call chain. It isn't necessary as the caller into this
 *         &#47;&#47; method should handle flushing the JsonWriter.
 *         return jsonWriter.writeStartObject&#40;&#41;
 *             .writeIntField&#40;&quot;int&quot;, anInt&#41;
 *             .writeBooleanField&#40;&quot;boolean&quot;, aBoolean&#41;
 *             &#47;&#47; Writing fields with nullable types won't write the field if the value is null. If a nullable field needs
 *             &#47;&#47; to always be written use 'writeNullableField&#40;String, Object, WriteValueCallback&lt;JsonWriter, Object&gt;&#41;'.
 *             &#47;&#47; This will write 'fieldName: null' if the value is null.
 *             .writeStringField&#40;&quot;string&quot;, aString&#41;
 *             .writeNumberField&#40;&quot;decimal&quot;, aNullableDecimal&#41;
 *             .writeEndObject&#40;&#41;;
 *     &#125;
 *
 *     &#47;**
 *      * Reads an instance of FluentJsonSerializableExample from the JsonReader.
 *      *
 *      * &#64;param jsonReader The JsonReader being read.
 *      * &#64;return An instance of FluentJsonSerializableExample if the JsonReader was pointing to an instance of it, or
 *      * null if it was pointing to JSON null.
 *      * &#64;throws IOException If an error occurs while reading the FluentJsonSerializableExample.
 *      *&#47;
 *     public static FluentJsonSerializableExample fromJson&#40;JsonReader jsonReader&#41; throws IOException &#123;
 *         &#47;&#47; 'readObject' will initialize reading if the JsonReader hasn't begun JSON reading and validate that the
 *         &#47;&#47; current state of reading is a JSON start object. If the state isn't JSON start object an exception will be
 *         &#47;&#47; thrown.
 *         return jsonReader.readObject&#40;reader -&gt; &#123;
 *             FluentJsonSerializableExample deserializedValue = new FluentJsonSerializableExample&#40;&#41;;
 *
 *             while &#40;reader.nextToken&#40;&#41; != JsonToken.END_OBJECT&#41; &#123;
 *                 String fieldName = reader.getFieldName&#40;&#41;;
 *                 reader.nextToken&#40;&#41;;
 *
 *                 &#47;&#47; In this case field names are case-sensitive but this could be replaced with 'equalsIgnoreCase' to
 *                 &#47;&#47; make them case-insensitive.
 *                 if &#40;&quot;int&quot;.equals&#40;fieldName&#41;&#41; &#123;
 *                     deserializedValue.setAnInt&#40;reader.getInt&#40;&#41;&#41;;
 *                 &#125; else if &#40;&quot;boolean&quot;.equals&#40;fieldName&#41;&#41; &#123;
 *                     deserializedValue.setABoolean&#40;reader.getBoolean&#40;&#41;&#41;;
 *                 &#125; else if &#40;&quot;string&quot;.equals&#40;fieldName&#41;&#41; &#123;
 *                     deserializedValue.setAString&#40;reader.getString&#40;&#41;&#41;;
 *                 &#125; else if &#40;&quot;decimal&quot;.equals&#40;fieldName&#41;&#41; &#123;
 *                     &#47;&#47; For nullable primitives 'getNullable' must be used as it will return null if the current token
 *                     &#47;&#47; is JSON null or pass the reader to the non-null callback method for reading, in this case for
 *                     &#47;&#47; Double it is 'getDouble'.
 *                     deserializedValue.setANullableDecimal&#40;reader.getNullable&#40;JsonReader::getDouble&#41;&#41;;
 *                 &#125; else &#123;
 *                     &#47;&#47; Fallthrough case of an unknown property. In this instance the value is skipped, if it's a JSON
 *                     &#47;&#47; array or object the reader will progress until it terminated. This could also throw an exception
 *                     &#47;&#47; if unknown properties should cause that or be read into an additional properties Map for further
 *                     &#47;&#47; usage.
 *                     reader.skipChildren&#40;&#41;;
 *                 &#125;
 *             &#125;
 *
 *             return deserializedValue;
 *         &#125;&#41;;
 *     &#125;
 * &#125;
 * </pre>
 * <!-- end com.azure.json.JsonSerializable.fluent -->
 *
 * <p><strong>Sample: Immutable JsonSerializable</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonSerializable.immutable -->
 * <pre>
 *
 * &#47;**
 *  * Implementation of JsonSerializable where all properties are set in the constructor.
 *  *&#47;
 * public class ImmutableJsonSerializableExample implements JsonSerializable&lt;FluentJsonSerializableExample&gt; &#123;
 *     private final int anInt;
 *     private final boolean aBoolean;
 *     private final String aString;
 *     private final Double aNullableDecimal;
 *
 *     &#47;**
 *      * Creates an instance of ImmutableJsonSerializableExample.
 *      *
 *      * &#64;param anInt The integer value.
 *      * &#64;param aBoolean The boolean value.
 *      * &#64;param aString The string value.
 *      * &#64;param aNullableDecimal The nullable decimal value.
 *      *&#47;
 *     public ImmutableJsonSerializableExample&#40;int anInt, boolean aBoolean, String aString, Double aNullableDecimal&#41; &#123;
 *         &#47;&#47; This constructor could be made package-private or private as 'fromJson' has access to internal APIs.
 *         this.anInt = anInt;
 *         this.aBoolean = aBoolean;
 *         this.aString = aString;
 *         this.aNullableDecimal = aNullableDecimal;
 *     &#125;
 *
 *     &#64;Override
 *     public JsonWriter toJson&#40;JsonWriter jsonWriter&#41; throws IOException &#123;
 *         return jsonWriter.writeStartObject&#40;&#41;
 *             .writeIntField&#40;&quot;int&quot;, anInt&#41;
 *             .writeBooleanField&#40;&quot;boolean&quot;, aBoolean&#41;
 *             .writeStringField&#40;&quot;string&quot;, aString&#41;
 *             &#47;&#47; 'writeNullableField' will always write a field, even if the value is null.
 *             .writeNullableField&#40;&quot;decimal&quot;, aNullableDecimal, JsonWriter::writeDouble&#41;
 *             .writeEndObject&#40;&#41;
 *             &#47;&#47; In this case 'toJson' eagerly flushes the JsonWriter.
 *             &#47;&#47; Flushing too often may result in performance penalties.
 *             .flush&#40;&#41;;
 *     &#125;
 *
 *     &#47;**
 *      * Reads an instance of ImmutableJsonSerializableExample from the JsonReader.
 *      *
 *      * &#64;param jsonReader The JsonReader being read.
 *      * &#64;return An instance of ImmutableJsonSerializableExample if the JsonReader was pointing to an instance of it, or
 *      * null if it was pointing to JSON null.
 *      * &#64;throws IOException If an error occurs while reading the ImmutableJsonSerializableExample.
 *      * &#64;throws IllegalStateException If any of the required properties to create ImmutableJsonSerializableExample
 *      * aren't found.
 *      *&#47;
 *     public static ImmutableJsonSerializableExample fromJson&#40;JsonReader jsonReader&#41; throws IOException &#123;
 *         return jsonReader.readObject&#40;reader -&gt; &#123;
 *             &#47;&#47; Local variables to keep track of what values have been found.
 *             &#47;&#47; Some properties have a corresponding 'boolean found&lt;Name&gt;' to track if a JSON property with that name
 *             &#47;&#47; was found. If the value wasn't found an exception will be thrown at the end of reading the object.
 *             int anInt = 0;
 *             boolean foundAnInt = false;
 *             boolean aBoolean = false;
 *             boolean foundABoolean = false;
 *             String aString = null;
 *             Double aNullableDecimal = null;
 *
 *             while &#40;reader.nextToken&#40;&#41; != JsonToken.END_OBJECT&#41; &#123;
 *                 String fieldName = reader.getFieldName&#40;&#41;;
 *                 reader.nextToken&#40;&#41;;
 *
 *                 &#47;&#47; Example of case-insensitive names.
 *                 if &#40;&quot;int&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     anInt = reader.getInt&#40;&#41;;
 *                     foundAnInt = true;
 *                 &#125; else if &#40;&quot;boolean&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     aBoolean = reader.getBoolean&#40;&#41;;
 *                     foundABoolean = true;
 *                 &#125; else if &#40;&quot;string&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     aString = reader.getString&#40;&#41;;
 *                 &#125; else if &#40;&quot;decimal&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     aNullableDecimal = reader.getNullable&#40;JsonReader::getDouble&#41;;
 *                 &#125; else &#123;
 *                     reader.skipChildren&#40;&#41;;
 *                 &#125;
 *             &#125;
 *
 *             &#47;&#47; Check that all required fields were found.
 *             if &#40;foundAnInt &amp;&amp; foundABoolean&#41; &#123;
 *                 return new ImmutableJsonSerializableExample&#40;anInt, aBoolean, aString, aNullableDecimal&#41;;
 *             &#125;
 *
 *             &#47;&#47; If required fields were missing throw an exception.
 *             throw new IOException&#40;&quot;Missing one, or more, required fields. Required fields are 'int' and 'boolean'.&quot;&#41;;
 *         &#125;&#41;;
 *     &#125;
 * &#125;
 * </pre>
 * <!-- end com.azure.json.JsonSerializable.immutable -->
 *
 * <p><strong>Sample: Mixed JsonSerializable</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonSerializable.mixed -->
 * <pre>
 *
 * import java.io.IOException;
 * import java.util.LinkedHashMap;
 * import java.util.Map;
 *
 * &#47;**
 *  * Implementation of JsonSerializable where some properties are set in the constructor and some properties are set
 *  * using fluent methods.
 *  *&#47;
 * public class MixedJsonSerializableExample implements JsonSerializable&lt;MixedJsonSerializableExample&gt; &#123;
 *     private final int anInt;
 *     private final boolean aBoolean;
 *     private String aString;
 *     private Double aNullableDecimal;
 *     private Map&lt;String, Object&gt; additionalProperties;
 *
 *     &#47;**
 *      * Creates an instance of MixedJsonSerializableExample.
 *      *
 *      * &#64;param anInt The integer value.
 *      * &#64;param aBoolean The boolean value.
 *      *&#47;
 *     public MixedJsonSerializableExample&#40;int anInt, boolean aBoolean&#41; &#123;
 *         this.anInt = anInt;
 *         this.aBoolean = aBoolean;
 *     &#125;
 *
 *     &#47;**
 *      * Sets a string value.
 *      *
 *      * &#64;param aString The string value.
 *      * &#64;return The update MixedJsonSerializableExample
 *      *&#47;
 *     public MixedJsonSerializableExample setAString&#40;String aString&#41; &#123;
 *         this.aString = aString;
 *         return this;
 *     &#125;
 *
 *     &#47;**
 *      * Sets a nullable decimal value.
 *      *
 *      * &#64;param aNullableDecimal The nullable decimal value.
 *      * &#64;return The update MixedJsonSerializableExample
 *      *&#47;
 *     public MixedJsonSerializableExample setANullableDecimal&#40;Double aNullableDecimal&#41; &#123;
 *         this.aNullableDecimal = aNullableDecimal;
 *         return this;
 *     &#125;
 *
 *     &#47;**
 *      * Sets additional properties found while deserializing the JSON object.
 *      *
 *      * &#64;param additionalProperties Additional JSON properties.
 *      * &#64;return The update MixedJsonSerializableExample
 *      *&#47;
 *     public MixedJsonSerializableExample setAdditionalProperties&#40;Map&lt;String, Object&gt; additionalProperties&#41; &#123;
 *         this.additionalProperties = additionalProperties;
 *         return this;
 *     &#125;
 *
 *     &#64;Override
 *     public JsonWriter toJson&#40;JsonWriter jsonWriter&#41; throws IOException &#123;
 *         jsonWriter.writeStartObject&#40;&#41;
 *             .writeIntField&#40;&quot;int&quot;, anInt&#41;
 *             .writeBooleanField&#40;&quot;boolean&quot;, aBoolean&#41;
 *             .writeStringField&#40;&quot;string&quot;, aString&#41;
 *             .writeNullableField&#40;&quot;decimal&quot;, aNullableDecimal, JsonWriter::writeDouble&#41;;
 *
 *         &#47;&#47; Include additional properties in JSON serialization.
 *         if &#40;additionalProperties != null&#41; &#123;
 *             for &#40;Map.Entry&lt;String, Object&gt; additionalProperty : additionalProperties.entrySet&#40;&#41;&#41; &#123;
 *                 jsonWriter.writeUntypedField&#40;additionalProperty.getKey&#40;&#41;, additionalProperty.getValue&#40;&#41;&#41;;
 *             &#125;
 *         &#125;
 *
 *         return jsonWriter.writeEndObject&#40;&#41;;
 *     &#125;
 *
 *     &#47;**
 *      * Reads an instance of MixedJsonSerializableExample from the JsonReader.
 *      *
 *      * &#64;param jsonReader The JsonReader being read.
 *      * &#64;return An instance of MixedJsonSerializableExample if the JsonReader was pointing to an instance of it, or
 *      * null if it was pointing to JSON null.
 *      * &#64;throws IOException If an error occurs while reading the MixedJsonSerializableExample.
 *      * &#64;throws IllegalStateException If any of the required properties to create MixedJsonSerializableExample
 *      * aren't found.
 *      *&#47;
 *     public static MixedJsonSerializableExample fromJson&#40;JsonReader jsonReader&#41; throws IOException &#123;
 *         return jsonReader.readObject&#40;reader -&gt; &#123;
 *             int anInt = 0;
 *             boolean foundAnInt = false;
 *             boolean aBoolean = false;
 *             boolean foundABoolean = false;
 *             String aString = null;
 *             Double aNullableDecimal = null;
 *             Map&lt;String, Object&gt; additionalProperties = null;
 *
 *             while &#40;reader.nextToken&#40;&#41; != JsonToken.END_OBJECT&#41; &#123;
 *                 String fieldName = reader.getFieldName&#40;&#41;;
 *                 reader.nextToken&#40;&#41;;
 *
 *                 &#47;&#47; Example of case-insensitive names.
 *                 if &#40;&quot;int&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     anInt = reader.getInt&#40;&#41;;
 *                     foundAnInt = true;
 *                 &#125; else if &#40;&quot;boolean&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     aBoolean = reader.getBoolean&#40;&#41;;
 *                     foundABoolean = true;
 *                 &#125; else if &#40;&quot;string&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     aString = reader.getString&#40;&#41;;
 *                 &#125; else if &#40;&quot;decimal&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     aNullableDecimal = reader.getNullable&#40;JsonReader::getDouble&#41;;
 *                 &#125; else &#123;
 *                     &#47;&#47; Fallthrough case but the JSON property is maintained.
 *                     if &#40;additionalProperties == null&#41; &#123;
 *                         &#47;&#47; Maintain ordering of additional properties using a LinkedHashMap.
 *                         additionalProperties = new LinkedHashMap&lt;&gt;&#40;&#41;;
 *                     &#125;
 *
 *                     &#47;&#47; Additional properties are unknown types, use 'readUntyped'.
 *                     additionalProperties.put&#40;fieldName, reader.readUntyped&#40;&#41;&#41;;
 *                 &#125;
 *             &#125;
 *
 *             &#47;&#47; Check that all required fields were found.
 *             if &#40;foundAnInt &amp;&amp; foundABoolean&#41; &#123;
 *                 return new MixedJsonSerializableExample&#40;anInt, aBoolean&#41;
 *                     .setAString&#40;aString&#41;
 *                     .setANullableDecimal&#40;aNullableDecimal&#41;
 *                     .setAdditionalProperties&#40;additionalProperties&#41;;
 *             &#125;
 *
 *             &#47;&#47; If required fields were missing throw an exception.
 *             throw new IOException&#40;&quot;Missing one, or more, required fields. Required fields are 'int' and 'boolean'.&quot;&#41;;
 *         &#125;&#41;;
 *     &#125;
 * &#125;
 * </pre>
 * <!-- end com.azure.json.JsonSerializable.mixed -->
 *
 * <p>{@link com.azure.json.JsonReader} contains APIs and logic for parsing JSON. The type is abstract and consists of
 * both abstract methods for an implementation to implement as well as final method for commonly shared logic that
 * builds on the abstract methods. Similarly, {@link com.azure.json.JsonWriter} contains APIs and logic for writing
 * JSON, and as with {@link com.azure.json.JsonReader}, it contains both abstract methods for implementations to
 * implement and final methods for commonly shared logic that builds on the abstract methods. Both types implement
 * {@link java.io.Closeable} and should be used in try-with-resources blocks to ensure any resources created by
 * the implementations are cleaned up once JSON reading or writing is complete. Both types are used by the
 * {@link com.azure.json.JsonProvider} service provider interface which is used to create instances of
 * {@link com.azure.json.JsonReader} and {@link com.azure.json.JsonWriter} implementations.</p>
 *
 *
 * <p>{@link com.azure.json.JsonProviders} is a utility class that handles finding {@link com.azure.json.JsonProvider}
 * implementations on the classpath and should be the default way to create instances of
 * {@link com.azure.json.JsonReader} and {@link com.azure.json.JsonWriter}. As mentioned earlier, the Azure JSON
 * package provides a default implementation allowing for the library to be used stand-alone.
 * {@link com.azure.json.JsonReader} can be created from {@code byte[]}, {@link java.lang.String},
 * {@link java.io.InputStream}, and {@link java.io.Reader} sources, {@link com.azure.json.JsonWriter} can be created
 * from {@link java.io.OutputStream} and {@link java.io.Writer} sources. No matter the source the functionality will be
 * the same, the options exist to provide the best convenience and performance by reducing type translations.
 *
 * <p><strong>Sample: Reading a JSON byte[]</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonReader.readJsonByteArray -->
 * <pre>
 * &#47;&#47; Sample uses String.getBytes as a convenience to show the JSON string in a human-readable form.
 * byte[] json = &quot;&#123;&#92;&quot;int&#92;&quot;:10,&#92;&quot;boolean&#92;&quot;:true,&#92;&quot;string&#92;&quot;:&#92;&quot;hello&#92;&quot;,&#92;&quot;aNullableDecimal&#92;&quot;:null&#125;&quot;
 *     .getBytes&#40;StandardCharsets.UTF_8&#41;;
 *
 * try &#40;JsonReader jsonReader = JsonProviders.createReader&#40;json&#41;&#41; &#123;
 *     return FluentJsonSerializableExample.fromJson&#40;jsonReader&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.json.JsonReader.readJsonByteArray -->
 *
 * <p><strong>Sample: Reading a JSON String</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonReader.readJsonString -->
 * <pre>
 * String json = &quot;&#123;&#92;&quot;int&#92;&quot;:10,&#92;&quot;boolean&#92;&quot;:true,&#92;&quot;string&#92;&quot;:&#92;&quot;hello&#92;&quot;,&#92;&quot;aNullableDecimal&#92;&quot;:null&#125;&quot;;
 *
 * try &#40;JsonReader jsonReader = JsonProviders.createReader&#40;json&#41;&#41; &#123;
 *     return FluentJsonSerializableExample.fromJson&#40;jsonReader&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.json.JsonReader.readJsonString -->
 *
 * <p><strong>Sample: Reading a JSON InputStream</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonReader.readJsonInputStream -->
 * <pre>
 * &#47;&#47; Sample uses String.getBytes as a convenience to show the JSON string in a human-readable form.
 * InputStream json = new ByteArrayInputStream&#40;
 *     &quot;&#123;&#92;&quot;int&#92;&quot;:10,&#92;&quot;boolean&#92;&quot;:true,&#92;&quot;string&#92;&quot;:&#92;&quot;hello&#92;&quot;,&#92;&quot;aNullableDecimal&#92;&quot;:null&#125;&quot;
 *         .getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
 *
 * try &#40;JsonReader jsonReader = JsonProviders.createReader&#40;json&#41;&#41; &#123;
 *     return FluentJsonSerializableExample.fromJson&#40;jsonReader&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.json.JsonReader.readJsonInputStream -->
 *
 * <p><strong>Sample: Reading a JSON Reader</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonReader.readJsonReader -->
 * <pre>
 * Reader json = new StringReader&#40;&quot;&#123;&#92;&quot;int&#92;&quot;:10,&#92;&quot;boolean&#92;&quot;:true,&#92;&quot;string&#92;&quot;:&#92;&quot;hello&#92;&quot;,&#92;&quot;aNullableDecimal&#92;&quot;:null&#125;&quot;&#41;;
 *
 * try &#40;JsonReader jsonReader = JsonProviders.createReader&#40;json&#41;&#41; &#123;
 *     return FluentJsonSerializableExample.fromJson&#40;jsonReader&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.json.JsonReader.readJsonReader -->
 *
 * <p><strong>Sample: Writing to a JSON OutputStream</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonWriter.writeJsonOutputStream -->
 * <pre>
 * ImmutableJsonSerializableExample object = new ImmutableJsonSerializableExample&#40;10, true, &quot;hello&quot;, null&#41;;
 *
 * ByteArrayOutputStream json = new ByteArrayOutputStream&#40;&#41;;
 * try &#40;JsonWriter jsonWriter = JsonProviders.createWriter&#40;json&#41;&#41; &#123;
 *     &#47;&#47; JsonWriter automatically flushes on close.
 *     object.toJson&#40;jsonWriter&#41;;
 * &#125;
 *
 * System.out.println&#40;json&#41;;
 * </pre>
 * <!-- end com.azure.json.JsonWriter.writeJsonOutputStream -->
 *
 * <p><strong>Sample: Writing to a JSON Writer</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonWriter.writeJsonWriter -->
 * <pre>
 * ImmutableJsonSerializableExample object = new ImmutableJsonSerializableExample&#40;10, true, &quot;hello&quot;, null&#41;;
 *
 * Writer json = new StringWriter&#40;&#41;;
 * try &#40;JsonWriter jsonWriter = JsonProviders.createWriter&#40;json&#41;&#41; &#123;
 *     &#47;&#47; JsonWriter automatically flushes on close.
 *     object.toJson&#40;jsonWriter&#41;;
 * &#125;
 *
 * System.out.println&#40;json&#41;;
 * </pre>
 * <!-- end com.azure.json.JsonWriter.writeJsonWriter -->
 *
 * @see com.azure.json.JsonSerializable
 * @see com.azure.json.JsonReader
 * @see com.azure.json.JsonWriter
 * @see com.azure.json.JsonProvider
 * @see com.azure.json.JsonProviders
 */
package com.azure.json;
