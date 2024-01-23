// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import com.generic.core.util.ClientLogger;
import com.generic.core.util.serializer.JsonSerializer;
import com.generic.core.util.serializer.ObjectSerializer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Codesnippets for {@link BinaryData}.
 */
@SuppressWarnings("unused")
public class BinaryDataJavaDocCodeSnippet {
    /**
     * Codesnippets for {@link BinaryData#fromStream(InputStream)}.
     */
    public void fromStream() {
        // BEGIN: com.generic.core.util.BinaryData.fromStream#InputStream
        final ByteArrayInputStream inputStream = new ByteArrayInputStream("Some Data".getBytes(StandardCharsets.UTF_8));
        BinaryData binaryData = BinaryData.fromStream(inputStream);
        System.out.println(binaryData);
        // END: com.generic.core.util.BinaryData.fromStream#InputStream
    }

    /**
     * Codesnippets for {@link BinaryData#fromStream(InputStream, Long)}.
     */
    public void fromStreamWithLength() {
        // BEGIN: com.generic.core.util.BinaryData.fromStream#InputStream-Long
        byte[] bytes = "Some Data".getBytes(StandardCharsets.UTF_8);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        BinaryData binaryData = BinaryData.fromStream(inputStream, (long) bytes.length);
        System.out.println(binaryData);
        // END: com.generic.core.util.BinaryData.fromStream#InputStream-Long
    }

    /**
     * Codesnippets for {@link BinaryData#fromString(String)}.
     */
    public void fromString() {
        // BEGIN: com.generic.core.util.BinaryData.fromString#String
        final String data = "Some Data";
        // Following will use default character set as StandardCharsets.UTF_8
        BinaryData binaryData = BinaryData.fromString(data);
        System.out.println(binaryData.toString());
        // END: com.generic.core.util.BinaryData.fromString#String
    }

    /**
     * Codesnippets for {@link BinaryData#fromBytes(byte[])}.
     */
    public void fromBytes() {
        // BEGIN: com.generic.core.util.BinaryData.fromBytes#byte
        final byte[] data = "Some Data".getBytes(StandardCharsets.UTF_8);
        BinaryData binaryData = BinaryData.fromBytes(data);
        System.out.println(new String(binaryData.toBytes(), StandardCharsets.UTF_8));
        // END: com.generic.core.util.BinaryData.fromBytes#byte
    }

    /**
     * Codesnippets for {@link BinaryData#fromByteBuffer(ByteBuffer)}.
     */
    public void fromByteBuffer() {
        // BEGIN: com.generic.core.util.BinaryData.fromByteBuffer#ByteBuffer
        final ByteBuffer data = ByteBuffer.wrap("Some Data".getBytes(StandardCharsets.UTF_8));
        BinaryData binaryData = BinaryData.fromByteBuffer(data);
        System.out.println(binaryData);
        // END: com.generic.core.util.BinaryData.fromByteBuffer#ByteBuffer
    }

    /**
     * Codesnippets for {@link BinaryData#fromListByteBuffer(List)}.
     */
    public void fromListByteBuffer() {
        // BEGIN: com.generic.core.util.BinaryData.fromListByteBuffer#List
        final List<ByteBuffer> data = Stream.of("Some ", "data")
            .map(s -> ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)))
            .collect(Collectors.toList());
        BinaryData binaryData = BinaryData.fromListByteBuffer(data);
        System.out.println(binaryData);
        // END: com.generic.core.util.BinaryData.fromListByteBuffer#List
    }

    /**
     * Codesnippets for {@link BinaryData#fromFile(Path)}.
     */
    public void fromFile() {
        // BEGIN: com.generic.core.util.BinaryData.fromFile
        BinaryData binaryData = BinaryData.fromFile(new File("path/to/file").toPath());
        System.out.println(new String(binaryData.toBytes(), StandardCharsets.UTF_8));
        // END: com.generic.core.util.BinaryData.fromFile
    }

    /**
     * Codesnippets for {@link BinaryData#fromFile(Path, int)}.
     */
    public void fromFileWithChunkSize() {
        // BEGIN: com.generic.core.util.BinaryData.fromFile#Path-int
        BinaryData binaryData = BinaryData.fromFile(new File("path/to/file").toPath(), 8092);
        System.out.println(new String(binaryData.toBytes(), StandardCharsets.UTF_8));
        // END: com.generic.core.util.BinaryData.fromFile#Path-int
    }

    /**
     * Codesnippets for {@link BinaryData#fromFile(Path, Long, Long)}.
     */
    public void fromFileSegment() {
        // BEGIN: com.generic.core.util.BinaryData.fromFile#Path-Long-Long
        long position = 1024;
        long length = 100 * 1048;
        BinaryData binaryData = BinaryData.fromFile(
            new File("path/to/file").toPath(), position, length);
        System.out.println(new String(binaryData.toBytes(), StandardCharsets.UTF_8));
        // END: com.generic.core.util.BinaryData.fromFile#Path-Long-Long
    }

    /**
     * Codesnippets for {@link BinaryData#fromFile(Path, Long, Long, int)}.
     */
    public void fromFileSegmentWithChunkSize() {
        // BEGIN: com.generic.core.util.BinaryData.fromFile#Path-Long-Long-int
        long position = 1024;
        long length = 100 * 1048;
        int chunkSize = 8092;
        BinaryData binaryData = BinaryData.fromFile(
            new File("path/to/file").toPath(), position, length, chunkSize);
        System.out.println(new String(binaryData.toBytes(), StandardCharsets.UTF_8));
        // END: com.generic.core.util.BinaryData.fromFile#Path-Long-Long-int
    }

    /**
     * Codesnippets for {@link BinaryData#fromObject(Object)}.
     */
    public void fromObjectDefaultJsonSerializers() {
        // BEGIN: com.generic.core.util.BinaryData.fromObject#Object
        final Person data = new Person().setName("John");

        // Provide your custom serializer or use Azure provided serializers.
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-jackson or
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-gson
        BinaryData binaryData = BinaryData.fromObject(data);

        System.out.println(binaryData);
        // END: com.generic.core.util.BinaryData.fromObject#Object
    }

    private void sendToService(BinaryData binaryData) {
        // no implementation here, only serves as placeholder for a method a customer would call
    }

    /**
     * Codesnippets for {@link BinaryData#fromObject(Object, ObjectSerializer)}.
     */
    public void fromObjectObjectSerializer() {
        // BEGIN: com.generic.core.util.BinaryData.fromObject#Object-ObjectSerializer
        final Person data = new Person().setName("John");

        // Provide your custom serializer or use Azure provided serializers.
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-jackson or
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-gson
        final ObjectSerializer serializer = new MyJsonSerializer(); // Replace this with your Serializer
        BinaryData binaryData = BinaryData.fromObject(data, serializer);

        System.out.println(binaryData.toString());
        // END: com.generic.core.util.BinaryData.fromObject#Object-ObjectSerializer
    }

    /**
     * Codesnippets for {@link BinaryData#toObject(Class)}.
     */
    public void toObjectClassDefaultJsonSerializer() {
        // BEGIN: com.generic.core.util.BinaryData.toObject#Class
        final Person data = new Person().setName("John");

        // Ensure your classpath have the Serializer to serialize the object which implement implement
        // com.generic.core.util.serializer.JsonSerializer interface.
        // Or use Azure provided libraries for this.
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-jackson or
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-gson

        BinaryData binaryData = BinaryData.fromObject(data);

        Person person = binaryData.toObject(Person.class);
        System.out.println(person.getName());
        // END: com.generic.core.util.BinaryData.toObject#Class
    }

    /**
     * Codesnippets for {@link BinaryData#toObject(TypeReference)}.
     */
    public void toObjectTypeReferenceDefaultJsonSerializer() {
        // BEGIN: com.generic.core.util.BinaryData.toObject#TypeReference
        final Person data = new Person().setName("John");

        // Ensure your classpath have the Serializer to serialize the object which implement implement
        // com.generic.core.util.serializer.JsonSerializer interface.
        // Or use Azure provided libraries for this.
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-jackson or
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-gson

        BinaryData binaryData = BinaryData.fromObject(data);

        Person person = binaryData.toObject(TypeReference.createInstance(Person.class));
        System.out.println(person.getName());
        // END: com.generic.core.util.BinaryData.toObject#TypeReference
    }

    /**
     * Codesnippets for {@link BinaryData#toObject(TypeReference)}.
     */
    public void toObjectTypeReferenceDefaultJsonSerializerWithGenerics() {
        // BEGIN: com.generic.core.util.BinaryData.toObject#TypeReference-generic
        final Person person1 = new Person().setName("John");
        final Person person2 = new Person().setName("Jack");

        List<Person> personList = new ArrayList<>();
        personList.add(person1);
        personList.add(person2);

        // Ensure your classpath have the Serializer to serialize the object which implement implement
        // com.generic.core.util.serializer.JsonSerializer interface.
        // Or use Azure provided libraries for this.
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-jackson or
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-gson


        BinaryData binaryData = BinaryData.fromObject(personList);

        List<Person> persons = binaryData.toObject(new TypeReference<List<Person>>() { });
        persons.forEach(person -> System.out.println(person.getName()));
        // END: com.generic.core.util.BinaryData.toObject#TypeReference-generic
    }

    /**
     * Codesnippets for {@link BinaryData#toObject(Class, ObjectSerializer)}.
     */
    public void toObjectClassObjectSerializer() {
        // BEGIN: com.generic.core.util.BinaryData.toObject#Class-ObjectSerializer
        final Person data = new Person().setName("John");

        // Provide your custom serializer or use Azure provided serializers.
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-jackson or
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-gson

        final ObjectSerializer serializer = new MyJsonSerializer(); // Replace this with your Serializer
        BinaryData binaryData = BinaryData.fromObject(data, serializer);

        Person person = binaryData.toObject(Person.class, serializer);
        System.out.println("Name : " + person.getName());
        // END: com.generic.core.util.BinaryData.toObject#Class-ObjectSerializer
    }

    /**
     * Codesnippets for {@link BinaryData#toObject(TypeReference, ObjectSerializer)}.
     */
    public void toObjectTypeReferenceObjectSerializer() {
        // BEGIN: com.generic.core.util.BinaryData.toObject#TypeReference-ObjectSerializer
        final Person data = new Person().setName("John");

        // Provide your custom serializer or use Azure provided serializers.
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-jackson or
        // https://central.sonatype.com/artifact/com.generic/azure-core-serializer-json-gson

        final ObjectSerializer serializer = new MyJsonSerializer(); // Replace this with your Serializer
        BinaryData binaryData = BinaryData.fromObject(data, serializer);

        Person person = binaryData.toObject(TypeReference.createInstance(Person.class), serializer);
        System.out.println("Name : " + person.getName());
        // END: com.generic.core.util.BinaryData.toObject#TypeReference-ObjectSerializer
    }

    /**
     * Codesnippets for {@link BinaryData#toObject(TypeReference, ObjectSerializer)} that uses generics.
     */
    public void toObjectTypeReferenceObjectSerializerWithGenerics() {
        // BEGIN: com.generic.core.util.BinaryData.toObject#TypeReference-ObjectSerializer-generic
        final Person person1 = new Person().setName("John");
        final Person person2 = new Person().setName("Jack");

        List<Person> personList = new ArrayList<>();
        personList.add(person1);
        personList.add(person2);

        final ObjectSerializer serializer = new MyJsonSerializer(); // Replace this with your Serializer
        BinaryData binaryData = BinaryData.fromObject(personList, serializer);

        // Retains the type of the list when deserializing
        List<Person> persons = binaryData.toObject(new TypeReference<List<Person>>() { }, serializer);
        persons.forEach(person -> System.out.println("Name : " + person.getName()));
        // END: com.generic.core.util.BinaryData.toObject#TypeReference-ObjectSerializer-generic
    }

    /**
     * Codesnippets for {@link BinaryData#toStream()}.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void toStream() throws IOException {
        // BEGIN: com.generic.core.util.BinaryData.toStream
        final byte[] data = "Some Data".getBytes(StandardCharsets.UTF_8);
        BinaryData binaryData = BinaryData.fromStream(new ByteArrayInputStream(data), (long) data.length);
        final byte[] bytes = new byte[data.length];
        try (InputStream inputStream = binaryData.toStream()) {
            inputStream.read(bytes, 0, data.length);
            System.out.println(new String(bytes));
        }
        // END: com.generic.core.util.BinaryData.toStream
    }

    /**
     * Codesnippets for {@link BinaryData#toByteBuffer()}.
     */
    public void toReadOnlyByteBuffer() {
        // BEGIN: com.generic.util.BinaryData.toByteBuffer
        final byte[] data = "Some Data".getBytes(StandardCharsets.UTF_8);
        BinaryData binaryData = BinaryData.fromBytes(data);
        final byte[] bytes = new byte[data.length];
        binaryData.toByteBuffer().get(bytes, 0, data.length);
        System.out.println(new String(bytes));
        // END: com.generic.util.BinaryData.toByteBuffer
    }

    /**
     * Codesnippets for {@link BinaryData#isReplayable()},
     * {@link BinaryData#toReplayableBinaryData()}
     */
    public void replayablity() {
        // BEGIN: com.generic.util.BinaryData.replayability
        BinaryData binaryData = binaryDataProducer();

        if (!binaryData.isReplayable()) {
            binaryData = binaryData.toReplayableBinaryData();
        }

        streamConsumer(binaryData.toStream());
        streamConsumer(binaryData.toStream());
        // END: com.generic.util.BinaryData.replayability
    }

    private BinaryData binaryDataProducer() {
        final byte[] data = "Some Data".getBytes(StandardCharsets.UTF_8);
        return BinaryData.fromBytes(data);
    }

    private void streamConsumer(InputStream stream) {
        // no-op
    }

    public static class MyJsonSerializer implements JsonSerializer {
        private static final ClientLogger LOGGER = new ClientLogger(MyJsonSerializer.class);

        @Override
        public <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
            return null;
        }

        @Override
        public <T> T deserializeFromStream(InputStream stream, TypeReference<T> typeReference) {
            return null;
        }

        @Override
        public byte[] serializeToBytes(Object value) {
            return null;
        }

        @Override
        public void serializeToStream(OutputStream stream, Object value) {
        }
    }
}
