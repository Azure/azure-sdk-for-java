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
 * <p>{@link com.azure.json.JsonSerializable} is the base of Azure JSON: it's the interface that types implement to
 * provide stream-style JSON reading and writing functionality. The interface has a single implementable method
 * {@link com.azure.json.JsonSerializable#toJson(com.azure.json.JsonWriter) toJson(JsonWriter)} that defines how the
 * object is written as JSON, to the {@link com.azure.json.JsonWriter}, and a static method
 * {@link com.azure.json.JsonSerializable#fromJson(com.azure.json.JsonReader) fromJson(JsonReader)} that defines how to
 * read an instance of the object from JSON, being read from the {@link com.azure.json.JsonReader}. The default
 * implementation of {@link com.azure.json.JsonSerializable#fromJson(com.azure.json.JsonReader) fromJson(JsonReader)}
 * throws an {@link java.lang.UnsupportedOperationException} if the static method isn't hidden (a static method with the
 * same definition) by the type implementing {@link com.azure.json.JsonSerializable}. Given that the type itself manages
 * JSON serialization the type can be fluent, immutable, or a mix of fluent and immutable, it doesn't matter as all
 * logic is self-encapsulated.</p>
 *
 * <p><strong>Sample: All JsonSerializable fields are optional</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonSerializable.ComputerMemory -->
 * <pre>
 *
 * &#47;**
 *  * Implementation of JsonSerializable where all properties are fluently set.
 *  *&#47;
 * public class ComputerMemory implements JsonSerializable&lt;ComputerMemory&gt; &#123;
 *     private long memoryInBytes;
 *     private double clockSpeedInHertz;
 *     private String manufacturer;
 *     private boolean errorCorrecting;
 *
 *     &#47;**
 *      * Sets the memory capacity, in bytes, of the computer memory.
 *      *
 *      * &#64;param memoryInBytes The memory capacity in bytes.
 *      * &#64;return The update ComputerMemory
 *      *&#47;
 *     public ComputerMemory setMemoryInBytes&#40;long memoryInBytes&#41; &#123;
 *         this.memoryInBytes = memoryInBytes;
 *         return this;
 *     &#125;
 *
 *     &#47;**
 *      * Sets the clock speed, in hertz, of the computer memory.
 *      *
 *      * &#64;param clockSpeedInHertz The clock speed in hertz.
 *      * &#64;return The update ComputerMemory
 *      *&#47;
 *     public ComputerMemory setClockSpeedInHertz&#40;double clockSpeedInHertz&#41; &#123;
 *         this.clockSpeedInHertz = clockSpeedInHertz;
 *         return this;
 *     &#125;
 *
 *     &#47;**
 *      * Sets the manufacturer of the computer memory.
 *      *
 *      * &#64;param manufacturer The manufacturer.
 *      * &#64;return The update ComputerMemory
 *      *&#47;
 *     public ComputerMemory setManufacturer&#40;String manufacturer&#41; &#123;
 *         this.manufacturer = manufacturer;
 *         return this;
 *     &#125;
 *
 *     &#47;**
 *      * Sets whether the computer memory is error correcting.
 *      *
 *      * &#64;param errorCorrecting Whether the computer memory is error correcting.
 *      * &#64;return The update ComputerMemory
 *      *&#47;
 *     public ComputerMemory setErrorCorrecting&#40;boolean errorCorrecting&#41; &#123;
 *         this.errorCorrecting = errorCorrecting;
 *         return this;
 *     &#125;
 *
 *     &#64;Override
 *     public JsonWriter toJson&#40;JsonWriter jsonWriter&#41; throws IOException &#123;
 *         return jsonWriter.writeStartObject&#40;&#41;
 *             .writeLongField&#40;&quot;memoryInBytes&quot;, memoryInBytes&#41;
 *             .writeDoubleField&#40;&quot;clockSpeedInHertz&quot;, clockSpeedInHertz&#41;
 *             &#47;&#47; Writing fields with nullable types won't write the field if the value is null. If a nullable field needs
 *             &#47;&#47; to always be written use 'writeNullableField&#40;String, Object, WriteValueCallback&lt;JsonWriter, Object&gt;&#41;'.
 *             &#47;&#47; This will write 'fieldName: null' if the value is null.
 *             .writeStringField&#40;&quot;manufacturer&quot;, manufacturer&#41;
 *             .writeBooleanField&#40;&quot;errorCorrecting&quot;, errorCorrecting&#41;
 *             .writeEndObject&#40;&#41;;
 *     &#125;
 *
 *     &#47;**
 *      * Reads an instance of ComputerMemory from the JsonReader.
 *      *
 *      * &#64;param jsonReader The JsonReader being read.
 *      * &#64;return An instance of ComputerMemory if the JsonReader was pointing to an instance of it, or null if it was
 *      * pointing to JSON null.
 *      * &#64;throws IOException If an error occurs while reading the ComputerMemory.
 *      *&#47;
 *     public static ComputerMemory fromJson&#40;JsonReader jsonReader&#41; throws IOException &#123;
 *         &#47;&#47; 'readObject' will initialize reading if the JsonReader hasn't begun JSON reading and validate that the
 *         &#47;&#47; current state of reading is a JSON start object. If the state isn't JSON start object an exception will be
 *         &#47;&#47; thrown.
 *         return jsonReader.readObject&#40;reader -&gt; &#123;
 *             ComputerMemory deserializedValue = new ComputerMemory&#40;&#41;;
 *
 *             while &#40;reader.nextToken&#40;&#41; != JsonToken.END_OBJECT&#41; &#123;
 *                 String fieldName = reader.getFieldName&#40;&#41;;
 *                 reader.nextToken&#40;&#41;;
 *
 *                 &#47;&#47; In this case field names are case-sensitive but this could be replaced with 'equalsIgnoreCase' to
 *                 &#47;&#47; make them case-insensitive.
 *                 if &#40;&quot;memoryInBytes&quot;.equals&#40;fieldName&#41;&#41; &#123;
 *                     deserializedValue.setMemoryInBytes&#40;reader.getLong&#40;&#41;&#41;;
 *                 &#125; else if &#40;&quot;clockSpeedInHertz&quot;.equals&#40;fieldName&#41;&#41; &#123;
 *                     deserializedValue.setClockSpeedInHertz&#40;reader.getDouble&#40;&#41;&#41;;
 *                 &#125; else if &#40;&quot;manufacturer&quot;.equals&#40;fieldName&#41;&#41; &#123;
 *                     deserializedValue.setManufacturer&#40;reader.getString&#40;&#41;&#41;;
 *                 &#125; else if &#40;&quot;errorCorrecting&quot;.equals&#40;fieldName&#41;&#41; &#123;
 *                     deserializedValue.setErrorCorrecting&#40;reader.getBoolean&#40;&#41;&#41;;
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
 * <!-- end com.azure.json.JsonSerializable.ComputerMemory -->
 *
 * <p><strong>Sample: All JsonSerializable fields are required</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonSerializable.ComputerProcessor -->
 * <pre>
 *
 * &#47;**
 *  * Implementation of JsonSerializable where all properties are set in the constructor.
 *  *&#47;
 * public class ComputerProcessor implements JsonSerializable&lt;ComputerProcessor&gt; &#123;
 *     private final int cores;
 *     private final int threads;
 *     private final String manufacturer;
 *     private final double clockSpeedInHertz;
 *     private final OffsetDateTime releaseDate;
 *
 *     &#47;**
 *      * Creates an instance of ComputerProcessor.
 *      *
 *      * &#64;param cores The number of physical cores.
 *      * &#64;param threads The number of virtual threads.
 *      * &#64;param manufacturer The manufacturer of the processor.
 *      * &#64;param clockSpeedInHertz The clock speed, in hertz, of the processor.
 *      * &#64;param releaseDate The release date of the processor, if unreleased this is null.
 *      *&#47;
 *     public ComputerProcessor&#40;int cores, int threads, String manufacturer, double clockSpeedInHertz,
 *         OffsetDateTime releaseDate&#41; &#123;
 *         &#47;&#47; This constructor could be made package-private or private as 'fromJson' has access to internal APIs.
 *         this.cores = cores;
 *         this.threads = threads;
 *         this.manufacturer = manufacturer;
 *         this.clockSpeedInHertz = clockSpeedInHertz;
 *         this.releaseDate = releaseDate;
 *     &#125;
 *
 *     &#64;Override
 *     public JsonWriter toJson&#40;JsonWriter jsonWriter&#41; throws IOException &#123;
 *         return jsonWriter.writeStartObject&#40;&#41;
 *             .writeIntField&#40;&quot;cores&quot;, cores&#41;
 *             .writeIntField&#40;&quot;threads&quot;, threads&#41;
 *             .writeStringField&#40;&quot;manufacturer&quot;, manufacturer&#41;
 *             .writeDoubleField&#40;&quot;clockSpeedInHertz&quot;, clockSpeedInHertz&#41;
 *             &#47;&#47; 'writeNullableField' will always write a field, even if the value is null.
 *             .writeNullableField&#40;&quot;releaseDate&quot;, releaseDate, &#40;writer, value&#41; -&gt; writer.writeString&#40;value.toString&#40;&#41;&#41;&#41;
 *             .writeEndObject&#40;&#41;
 *             &#47;&#47; In this case 'toJson' eagerly flushes the JsonWriter.
 *             &#47;&#47; Flushing too often may result in performance penalties.
 *             .flush&#40;&#41;;
 *     &#125;
 *
 *     &#47;**
 *      * Reads an instance of ComputerProcessor from the JsonReader.
 *      *
 *      * &#64;param jsonReader The JsonReader being read.
 *      * &#64;return An instance of ComputerProcessor if the JsonReader was pointing to an instance of it, or null if it was
 *      * pointing to JSON null.
 *      * &#64;throws IOException If an error occurs while reading the ComputerProcessor.
 *      * &#64;throws IllegalStateException If any of the required properties to create ComputerProcessor aren't found.
 *      *&#47;
 *     public static ComputerProcessor fromJson&#40;JsonReader jsonReader&#41; throws IOException &#123;
 *         return jsonReader.readObject&#40;reader -&gt; &#123;
 *             &#47;&#47; Local variables to keep track of what values have been found.
 *             &#47;&#47; Some properties have a corresponding 'boolean found&lt;Name&gt;' to track if a JSON property with that name
 *             &#47;&#47; was found. If the value wasn't found an exception will be thrown at the end of reading the object.
 *             int cores = 0;
 *             boolean foundCores = false;
 *             int threads = 0;
 *             boolean foundThreads = false;
 *             String manufacturer = null;
 *             boolean foundManufacturer = false;
 *             double clockSpeedInHertz = 0.0D;
 *             boolean foundClockSpeedInHertz = false;
 *             OffsetDateTime releaseDate = null;
 *
 *             while &#40;reader.nextToken&#40;&#41; != JsonToken.END_OBJECT&#41; &#123;
 *                 String fieldName = reader.getFieldName&#40;&#41;;
 *                 reader.nextToken&#40;&#41;;
 *
 *                 &#47;&#47; Example of case-insensitive names.
 *                 if &#40;&quot;cores&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     cores = reader.getInt&#40;&#41;;
 *                     foundCores = true;
 *                 &#125; else if &#40;&quot;threads&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     threads = reader.getInt&#40;&#41;;
 *                     foundThreads = true;
 *                 &#125; else if &#40;&quot;manufacturer&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     manufacturer = reader.getString&#40;&#41;;
 *                     foundManufacturer = true;
 *                 &#125; else if &#40;&quot;clockSpeedInHertz&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     clockSpeedInHertz = reader.getDouble&#40;&#41;;
 *                     foundClockSpeedInHertz = true;
 *                 &#125; else if &#40;&quot;releaseDate&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     &#47;&#47; For nullable primitives 'getNullable' must be used as it will return null if the current token
 *                     &#47;&#47; is JSON null or pass the reader to the non-null callback method for reading, in this case for
 *                     &#47;&#47; OffsetDateTime it uses 'getString' to call 'OffsetDateTime.parse'.
 *                     releaseDate = reader.getNullable&#40;nonNullReader -&gt; OffsetDateTime.parse&#40;nonNullReader.getString&#40;&#41;&#41;&#41;;
 *                 &#125; else &#123;
 *                     reader.skipChildren&#40;&#41;;
 *                 &#125;
 *             &#125;
 *
 *             &#47;&#47; Check that all required fields were found.
 *             if &#40;foundCores &amp;&amp; foundThreads &amp;&amp; foundManufacturer &amp;&amp; foundClockSpeedInHertz&#41; &#123;
 *                 return new ComputerProcessor&#40;cores, threads, manufacturer, clockSpeedInHertz, releaseDate&#41;;
 *             &#125;
 *
 *             &#47;&#47; If required fields were missing throw an exception.
 *             throw new IOException&#40;&quot;Missing one, or more, required fields. Required fields are 'cores', 'threads', &quot;
 *                 + &quot;'manufacturer', and 'clockSpeedInHertz'.&quot;&#41;;
 *         &#125;&#41;;
 *     &#125;
 * &#125;
 * </pre>
 * <!-- end com.azure.json.JsonSerializable.ComputerProcessor -->
 *
 * <p><strong>Sample: JsonSerializable contains required and optional fields</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonSerializable.VmStatistics -->
 * <pre>
 *
 * &#47;**
 *  * Implementation of JsonSerializable where some properties are set in the constructor and some properties are set using
 *  * fluent methods.
 *  *&#47;
 * public class VmStatistics implements JsonSerializable&lt;VmStatistics&gt; &#123;
 *     private final String vmSize;
 *     private final ComputerProcessor processor;
 *     private final ComputerMemory memory;
 *     private final boolean acceleratedNetwork;
 *     private Map&lt;String, Object&gt; additionalProperties;
 *
 *     &#47;**
 *      * Creates an instance VmStatistics.
 *      *
 *      * &#64;param vmSize The size, or name, of the VM type.
 *      * &#64;param processor The processor of the VM.
 *      * &#64;param memory The memory of the VM.
 *      * &#64;param acceleratedNetwork Whether the VM has accelerated networking.
 *      *&#47;
 *     public VmStatistics&#40;String vmSize, ComputerProcessor processor, ComputerMemory memory, boolean acceleratedNetwork&#41; &#123;
 *         this.vmSize = vmSize;
 *         this.processor = processor;
 *         this.memory = memory;
 *         this.acceleratedNetwork = acceleratedNetwork;
 *     &#125;
 *
 *     &#47;**
 *      * Sets additional properties about the VM.
 *      *
 *      * &#64;param additionalProperties Additional properties of the VM.
 *      * &#64;return The update VmStatistics
 *      *&#47;
 *     public VmStatistics setAdditionalProperties&#40;Map&lt;String, Object&gt; additionalProperties&#41; &#123;
 *         this.additionalProperties = additionalProperties;
 *         return this;
 *     &#125;
 *
 *     &#64;Override
 *     public JsonWriter toJson&#40;JsonWriter jsonWriter&#41; throws IOException &#123;
 *         jsonWriter.writeStartObject&#40;&#41;
 *             .writeStringField&#40;&quot;VMSize&quot;, vmSize&#41;
 *             .writeJsonField&#40;&quot;Processor&quot;, processor&#41;
 *             .writeJsonField&#40;&quot;Memory&quot;, memory&#41;
 *             .writeBooleanField&#40;&quot;AcceleratedNetwork&quot;, acceleratedNetwork&#41;;
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
 *      * Reads an instance of VmStatistics from the JsonReader.
 *      *
 *      * &#64;param jsonReader The JsonReader being read.
 *      * &#64;return An instance of VmStatistics if the JsonReader was pointing to an instance of it, or null if it was
 *      * pointing to JSON null.
 *      * &#64;throws IOException If an error occurs while reading the VmStatistics.
 *      * &#64;throws IllegalStateException If any of the required properties to create VmStatistics aren't found.
 *      *&#47;
 *     public static VmStatistics fromJson&#40;JsonReader jsonReader&#41; throws IOException &#123;
 *         return jsonReader.readObject&#40;reader -&gt; &#123;
 *             String vmSize = null;
 *             boolean foundVmSize = false;
 *             ComputerProcessor processor = null;
 *             boolean foundProcessor = false;
 *             ComputerMemory memory = null;
 *             boolean foundMemory = false;
 *             boolean acceleratedNetwork = false;
 *             boolean foundAcceleratedNetwork = false;
 *             Map&lt;String, Object&gt; additionalProperties = null;
 *
 *             while &#40;reader.nextToken&#40;&#41; != JsonToken.END_OBJECT&#41; &#123;
 *                 String fieldName = reader.getFieldName&#40;&#41;;
 *                 reader.nextToken&#40;&#41;;
 *
 *                 &#47;&#47; Example of case-insensitive names and where serialization named don't match field names.
 *                 if &#40;&quot;VMSize&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     vmSize = reader.getString&#40;&#41;;
 *                     foundVmSize = true;
 *                 &#125; else if &#40;&quot;Processor&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     &#47;&#47; Pass the JsonReader to another JsonSerializable to read the inner object.
 *                     processor = ComputerProcessor.fromJson&#40;reader&#41;;
 *                     foundProcessor = true;
 *                 &#125; else if &#40;&quot;Memory&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     memory = ComputerMemory.fromJson&#40;reader&#41;;
 *                     foundMemory = true;
 *                 &#125; else if &#40;&quot;AcceleratedNetwork&quot;.equalsIgnoreCase&#40;fieldName&#41;&#41; &#123;
 *                     acceleratedNetwork = reader.getBoolean&#40;&#41;;
 *                     foundAcceleratedNetwork = true;
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
 *             if &#40;foundVmSize &amp;&amp; foundProcessor &amp;&amp; foundMemory &amp;&amp; foundAcceleratedNetwork&#41; &#123;
 *                 return new VmStatistics&#40;vmSize, processor, memory, acceleratedNetwork&#41;
 *                     .setAdditionalProperties&#40;additionalProperties&#41;;
 *             &#125;
 *
 *             &#47;&#47; If required fields were missing throw an exception.
 *             throw new IOException&#40;&quot;Missing one, or more, required fields. Required fields are 'VMSize', 'Processor',&quot;
 *                 + &quot;'Memory', and 'AcceleratedNetwork'.&quot;&#41;;
 *         &#125;&#41;;
 *     &#125;
 * &#125;
 * </pre>
 * <!-- end com.azure.json.JsonSerializable.VmStatistics -->
 *
 * <h2>Reading and Writing JSON</h2>
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
 * byte[] json = &#40;&quot;&#123;&#92;&quot;memoryInBytes&#92;&quot;:10000000000,&#92;&quot;clockSpeedInHertz&#92;&quot;:4800000000,&quot;
 *     + &quot;&#92;&quot;manufacturer&#92;&quot;:&#92;&quot;Memory Corp&#92;&quot;,&#92;&quot;errorCorrecting&#92;&quot;:true&#125;&quot;&#41;.getBytes&#40;StandardCharsets.UTF_8&#41;;
 *
 * try &#40;JsonReader jsonReader = JsonProviders.createReader&#40;json&#41;&#41; &#123;
 *     return ComputerMemory.fromJson&#40;jsonReader&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.json.JsonReader.readJsonByteArray -->
 *
 * <p><strong>Sample: Reading a JSON String</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonReader.readJsonString -->
 * <pre>
 * String json = &quot;&#123;&#92;&quot;cores&#92;&quot;:16,&#92;&quot;threads&#92;&quot;:32,&#92;&quot;manufacturer&#92;&quot;:&#92;&quot;Processor Corp&#92;&quot;,&quot;
 *     + &quot;&#92;&quot;clockSpeedInHertz&#92;&quot;:5000000000,&#92;&quot;releaseDate&#92;&quot;:null&#125;&quot;;
 *
 * try &#40;JsonReader jsonReader = JsonProviders.createReader&#40;json&#41;&#41; &#123;
 *     return ComputerProcessor.fromJson&#40;jsonReader&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.json.JsonReader.readJsonString -->
 *
 * <p><strong>Sample: Reading a JSON InputStream</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonReader.readJsonInputStream -->
 * <pre>
 * &#47;&#47; Sample uses String.getBytes as a convenience to show the JSON string in a human-readable form.
 * InputStream json = new ByteArrayInputStream&#40;&#40;&quot;&#123;&#92;&quot;VMSize&#92;&quot;:&#92;&quot;large&#92;&quot;,&#92;&quot;Processor&#92;&quot;:&#123;&#92;&quot;cores&#92;&quot;:8,&quot;
 *     + &quot;&#92;&quot;threads&#92;&quot;16&#92;&quot;,&#92;&quot;manufacturer&#92;&quot;:&#92;&quot;Processor Corp&#92;&quot;,&#92;&quot;clockSpeedInHertz&#92;&quot;:4000000000,&quot;
 *     + &quot;&#92;&quot;releaseDate&#92;&quot;:&#92;&quot;2023-01-01&#92;&quot;&#125;,&#92;&quot;Memory&#92;&quot;:&#123;&#92;&quot;memoryInBytes&#92;&quot;:10000000000,&quot;
 *     + &quot;&#92;&quot;clockSpeedInHertz&#92;&quot;:4800000000,&#92;&quot;manufacturer&#92;&quot;:&#92;&quot;Memory Corp&#92;&quot;,&#92;&quot;errorCorrecting&#92;&quot;:true&#125;,&quot;
 *     + &quot;&#92;&quot;AcceleratedNetwork&#92;&quot;:true,&#92;&quot;CloudProvider&#92;&quot;:&#92;&quot;Azure&#92;&quot;,&#92;&quot;Available&#92;&quot;:true&#125;&quot;&#41;
 *     .getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
 *
 * try &#40;JsonReader jsonReader = JsonProviders.createReader&#40;json&#41;&#41; &#123;
 *     return VmStatistics.fromJson&#40;jsonReader&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.json.JsonReader.readJsonInputStream -->
 *
 * <p><strong>Sample: Reading a JSON Reader</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonReader.readJsonReader -->
 * <pre>
 * Reader json = new StringReader&#40;&quot;&#123;&#92;&quot;VMSize&#92;&quot;:&#92;&quot;large&#92;&quot;,&#92;&quot;Processor&#92;&quot;:&#123;&#92;&quot;cores&#92;&quot;:8,&#92;&quot;threads&#92;&quot;16&#92;&quot;,&quot;
 *     + &quot;&#92;&quot;manufacturer&#92;&quot;:&#92;&quot;Processor Corp&#92;&quot;,&#92;&quot;clockSpeedInHertz&#92;&quot;:4000000000,&#92;&quot;releaseDate&#92;&quot;:&#92;&quot;2023-01-01&#92;&quot;&#125;,&quot;
 *     + &quot;&#92;&quot;Memory&#92;&quot;:&#123;&#92;&quot;memoryInBytes&#92;&quot;:10000000000,&#92;&quot;clockSpeedInHertz&#92;&quot;:4800000000,&quot;
 *     + &quot;&#92;&quot;manufacturer&#92;&quot;:&#92;&quot;Memory Corp&#92;&quot;,&#92;&quot;errorCorrecting&#92;&quot;:true&#125;,&#92;&quot;AcceleratedNetwork&#92;&quot;:true,&quot;
 *     + &quot;&#92;&quot;CloudProvider&#92;&quot;:&#92;&quot;Azure&#92;&quot;,&#92;&quot;Available&#92;&quot;:true&#125;&quot;&#41;;
 *
 * try &#40;JsonReader jsonReader = JsonProviders.createReader&#40;json&#41;&#41; &#123;
 *     return VmStatistics.fromJson&#40;jsonReader&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.json.JsonReader.readJsonReader -->
 *
 * <p><strong>Sample: Writing to a JSON OutputStream</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonWriter.writeJsonOutputStream -->
 * <pre>
 * Map&lt;String, Object&gt; additionalVmProperties = new LinkedHashMap&lt;&gt;&#40;&#41;;
 * additionalVmProperties.put&#40;&quot;CloudProvider&quot;, &quot;Azure&quot;&#41;;
 * additionalVmProperties.put&#40;&quot;Available&quot;, true&#41;;
 *
 * VmStatistics vmStatistics = new VmStatistics&#40;&quot;large&quot;,
 *     new ComputerProcessor&#40;8, 16, &quot;Processor Corp&quot;, 4000000000D, OffsetDateTime.parse&#40;&quot;2023-01-01&quot;&#41;&#41;,
 *     new ComputerMemory&#40;&#41;
 *         .setMemoryInBytes&#40;10000000000L&#41;
 *         .setClockSpeedInHertz&#40;4800000000D&#41;
 *         .setManufacturer&#40;&quot;Memory Corp&quot;&#41;
 *         .setErrorCorrecting&#40;true&#41;,
 *     true&#41;
 *     .setAdditionalProperties&#40;additionalVmProperties&#41;;
 *
 * ByteArrayOutputStream json = new ByteArrayOutputStream&#40;&#41;;
 * try &#40;JsonWriter jsonWriter = JsonProviders.createWriter&#40;json&#41;&#41; &#123;
 *     &#47;&#47; JsonWriter automatically flushes on close.
 *     vmStatistics.toJson&#40;jsonWriter&#41;;
 * &#125;
 *
 * &#47;&#47; &#123;&quot;VMSize&quot;:&quot;large&quot;,&quot;Processor&quot;:&#123;&quot;cores&quot;:8,&quot;threads&quot;:16,&quot;manufacturer&quot;:&quot;Processor Corp&quot;,
 * &#47;&#47;   &quot;clockSpeedInHertz&quot;:4000000000.0,&quot;releaseDate&quot;:&quot;2023-01-01&quot;&#125;,&quot;Memory&quot;:&#123;&quot;memoryInBytes&quot;:10000000000,
 * &#47;&#47;   &quot;clockSpeedInHertz&quot;:4800000000.0,&quot;manufacturer&quot;:&quot;Memory Corp&quot;,&quot;errorCorrecting&quot;:true&#125;,
 * &#47;&#47;   &quot;AcceleratedNetwork&quot;:true,&quot;CloudProvider&quot;:&quot;Azure&quot;,&quot;Available&quot;:true&#125;
 * System.out.println&#40;json&#41;;
 * </pre>
 * <!-- end com.azure.json.JsonWriter.writeJsonOutputStream -->
 *
 * <p><strong>Sample: Writing to a JSON Writer</strong></p>
 *
 * <!-- src_embed com.azure.json.JsonWriter.writeJsonWriter -->
 * <pre>
 * Map&lt;String, Object&gt; additionalVmProperties = new LinkedHashMap&lt;&gt;&#40;&#41;;
 * additionalVmProperties.put&#40;&quot;CloudProvider&quot;, &quot;Azure&quot;&#41;;
 * additionalVmProperties.put&#40;&quot;Available&quot;, true&#41;;
 *
 * VmStatistics vmStatistics = new VmStatistics&#40;&quot;large&quot;,
 *     new ComputerProcessor&#40;8, 16, &quot;Processor Corp&quot;, 4000000000D, OffsetDateTime.parse&#40;&quot;2023-01-01&quot;&#41;&#41;,
 *     new ComputerMemory&#40;&#41;
 *         .setMemoryInBytes&#40;10000000000L&#41;
 *         .setClockSpeedInHertz&#40;4800000000D&#41;
 *         .setManufacturer&#40;&quot;Memory Corp&quot;&#41;
 *         .setErrorCorrecting&#40;true&#41;,
 *     true&#41;
 *     .setAdditionalProperties&#40;additionalVmProperties&#41;;
 *
 * Writer json = new StringWriter&#40;&#41;;
 * try &#40;JsonWriter jsonWriter = JsonProviders.createWriter&#40;json&#41;&#41; &#123;
 *     &#47;&#47; JsonWriter automatically flushes on close.
 *     vmStatistics.toJson&#40;jsonWriter&#41;;
 * &#125;
 *
 * &#47;&#47; &#123;&quot;VMSize&quot;:&quot;large&quot;,&quot;Processor&quot;:&#123;&quot;cores&quot;:8,&quot;threads&quot;:16,&quot;manufacturer&quot;:&quot;Processor Corp&quot;,
 * &#47;&#47;   &quot;clockSpeedInHertz&quot;:4000000000.0,&quot;releaseDate&quot;:&quot;2023-01-01&quot;&#125;,&quot;Memory&quot;:&#123;&quot;memoryInBytes&quot;:10000000000,
 * &#47;&#47;   &quot;clockSpeedInHertz&quot;:4800000000.0,&quot;manufacturer&quot;:&quot;Memory Corp&quot;,&quot;errorCorrecting&quot;:true&#125;,
 * &#47;&#47;   &quot;AcceleratedNetwork&quot;:true,&quot;CloudProvider&quot;:&quot;Azure&quot;,&quot;Available&quot;:true&#125;
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
package com.typespec.json;
