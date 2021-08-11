// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Codesnippets for {@link BinaryData}.
 */
public class BinaryDataJavaDocCodeSnippet {
    /**
     * Codesnippets for {@link BinaryData#fromStream(InputStream)}.
     */
    public void fromStream() {
        // BEGIN: com.azure.core.util.BinaryData.fromStream#InputStream
        final ByteArrayInputStream inputStream = new ByteArrayInputStream("Some Data".getBytes(StandardCharsets.UTF_8));
        BinaryData binaryData = BinaryData.fromStream(inputStream);
        System.out.println(binaryData.toString());
        // END: com.azure.core.util.BinaryData.fromStream#InputStream
    }

    /**
     * Codesnippets for {@link BinaryData#fromStreamAsync(InputStream)}
     */
    public void fromStreamAsync() throws InterruptedException {
        // BEGIN: com.azure.core.util.BinaryData.fromStreamAsync#InputStream
        final ByteArrayInputStream inputStream = new ByteArrayInputStream("Some Data".getBytes(StandardCharsets.UTF_8));

        Mono<BinaryData> binaryDataMono = BinaryData.fromStreamAsync(inputStream);

        Disposable subscriber = binaryDataMono
            .map(binaryData -> {
                System.out.println(binaryData.toString());
                return true;
            })
            .subscribe();

        // So that your program wait for above subscribe to complete.
        TimeUnit.SECONDS.sleep(5);
        subscriber.dispose();
        // END: com.azure.core.util.BinaryData.fromStreamAsync#InputStream
    }

    /**
     * Codesnippets for {@link BinaryData#fromFlux(Flux)}.
     */
    public void fromFlux() throws InterruptedException {
        // BEGIN: com.azure.core.util.BinaryData.fromFlux#Flux
        final byte[] data = "Some Data".getBytes(StandardCharsets.UTF_8);
        final Flux<ByteBuffer> dataFlux = Flux.just(ByteBuffer.wrap(data));

        Mono<BinaryData> binaryDataMono = BinaryData.fromFlux(dataFlux);

        Disposable subscriber = binaryDataMono
            .map(binaryData -> {
                System.out.println(binaryData.toString());
                return true;
            })
            .subscribe();

        // So that your program wait for above subscribe to complete.
        TimeUnit.SECONDS.sleep(5);
        subscriber.dispose();
        // END: com.azure.core.util.BinaryData.fromFlux#Flux
    }

    /**
     * Codesnippets for {@link BinaryData#fromString(String)}.
     */
    public void fromString() {
        // BEGIN: com.azure.core.util.BinaryData.fromString#String
        final String data = "Some Data";
        // Following will use default character set as StandardCharsets.UTF_8
        BinaryData binaryData = BinaryData.fromString(data);
        System.out.println(binaryData.toString());
        // END: com.azure.core.util.BinaryData.fromString#String
    }

    /**
     * Codesnippets for {@link BinaryData#fromBytes(byte[])}.
     */
    public void fromBytes() {
        // BEGIN: com.azure.core.util.BinaryData.fromBytes#byte
        final byte[] data = "Some Data".getBytes(StandardCharsets.UTF_8);
        BinaryData binaryData = BinaryData.fromBytes(data);
        System.out.println(new String(binaryData.toBytes(), StandardCharsets.UTF_8));
        // END: com.azure.core.util.BinaryData.fromBytes#byte
    }

    /**
     * Codesnippets for {@link BinaryData#fromFile(Path)}.
     */
    public void fromFile() {
        // BEGIN: com.azure.core.util.BinaryData.fromFile
        BinaryData binaryData = BinaryData.fromFile(new File("path/to/file").toPath());
        System.out.println(new String(binaryData.toBytes(), StandardCharsets.UTF_8));
        // END: com.azure.core.util.BinaryData.fromFile
    }

    /**
     * Codesnippets for {@link BinaryData#fromFile(Path, int)}.
     */
    public void fromFileWithChunkSize() {
        // BEGIN: com.azure.core.util.BinaryData.fromFile#Path-int
        BinaryData binaryData = BinaryData.fromFile(new File("path/to/file").toPath(), 8092);
        System.out.println(new String(binaryData.toBytes(), StandardCharsets.UTF_8));
        // END: com.azure.core.util.BinaryData.fromFile#Path-int
    }

    /**
     * Codesnippets for {@link BinaryData#fromObject(Object)}.
     */
    public void fromObjectDefaultJsonSerializers() {
        // BEGIN: com.azure.core.util.BinaryData.fromObject#Object
        class Person {
            @JsonProperty
            private String name;

            @JsonSetter
            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @JsonGetter
            public String getName() {
                return name;
            }
        }
        final Person data = new Person().setName("John");

        // Provide your custom serializer or use Azure provided serializers.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson
        BinaryData binaryData = BinaryData.fromObject(data);

        System.out.println(binaryData.toString());
        // END: com.azure.core.util.BinaryData.fromObject#Object
    }

    /**
     * Codesnippets for {@link BinaryData#fromObjectAsync(Object)}.
     */
    public void fromObjectAsyncDefaultJsonSerializer() throws InterruptedException {
        // BEGIN: com.azure.core.util.BinaryData.fromObjectAsync#Object
        class Person {
            @JsonProperty
            private String name;

            @JsonSetter
            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @JsonGetter
            public String getName() {
                return name;
            }
        }
        final Person data = new Person().setName("John");

        // Provide your custom serializer or use Azure provided serializers.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson
        Disposable subscriber = BinaryData.fromObjectAsync(data)
            .subscribe(binaryData -> System.out.println(binaryData.toString()));

        // So that your program wait for above subscribe to complete.
        TimeUnit.SECONDS.sleep(5);
        subscriber.dispose();
        // END: com.azure.core.util.BinaryData.fromObjectAsync#Object
    }

    private void sendToService(BinaryData binaryData) {
        // no implementation here, only serves as placeholder for a method a customer would call
    }

    /**
     * Codesnippets for {@link BinaryData#fromObject(Object, ObjectSerializer)}.
     */
    public void fromObjectObjectSerializer() {
        // BEGIN: com.azure.core.util.BinaryData.fromObject#Object-ObjectSerializer
        class Person {
            @JsonProperty
            private String name;

            @JsonSetter
            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @JsonGetter
            public String getName() {
                return name;
            }
        }
        final Person data = new Person().setName("John");

        // Provide your custom serializer or use Azure provided serializers.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson
        final ObjectSerializer serializer =
            new MyJsonSerializer(); // Replace this with your Serializer
        BinaryData binaryData = BinaryData.fromObject(data, serializer);

        System.out.println(binaryData.toString());
        // END: com.azure.core.util.BinaryData.fromObject#Object-ObjectSerializer
    }

    /**
     * Codesnippets for {@link BinaryData#fromObjectAsync(Object, ObjectSerializer)}.
     */
    public void fromObjectAsyncObjectSerializer() throws InterruptedException {
        // BEGIN: com.azure.core.util.BinaryData.fromObjectAsync#Object-ObjectSerializer
        class Person {
            @JsonProperty
            private String name;

            @JsonSetter
            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @JsonGetter
            public String getName() {
                return name;
            }
        }
        final Person data = new Person().setName("John");

        // Provide your custom serializer or use Azure provided serializers.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson
        final ObjectSerializer serializer =
            new MyJsonSerializer(); // Replace this with your Serializer
        Disposable subscriber = BinaryData.fromObjectAsync(data, serializer)
            .subscribe(binaryData -> System.out.println(binaryData.toString()));

        // So that your program wait for above subscribe to complete.
        TimeUnit.SECONDS.sleep(5);
        subscriber.dispose();
        // END: com.azure.core.util.BinaryData.fromObjectAsync#Object-ObjectSerializer
    }

    /**
     * Codesnippets for {@link BinaryData#toObject(Class)}.
     */
    public void toObjectClassDefaultJsonSerializer() {
        // BEGIN: com.azure.core.util.BinaryData.toObject#Class
        class Person {
            @JsonProperty
            private String name;

            @JsonSetter
            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @JsonGetter
            public String getName() {
                return name;
            }
        }
        final Person data = new Person().setName("John");

        // Ensure your classpath have the Serializer to serialize the object which implement implement
        // com.azure.core.util.serializer.JsonSerializer interface.
        // Or use Azure provided libraries for this.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson

        BinaryData binaryData = BinaryData.fromObject(data);

        Person person = binaryData.toObject(Person.class);
        System.out.println(person.getName());
        // END: com.azure.core.util.BinaryData.toObject#Class
    }

    /**
     * Codesnippets for {@link BinaryData#toObject(TypeReference)}.
     */
    public void toObjectTypeReferenceDefaultJsonSerializer() {
        // BEGIN: com.azure.core.util.BinaryData.toObject#TypeReference
        class Person {
            @JsonProperty
            private String name;

            @JsonSetter
            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @JsonGetter
            public String getName() {
                return name;
            }
        }
        final Person data = new Person().setName("John");

        // Ensure your classpath have the Serializer to serialize the object which implement implement
        // com.azure.core.util.serializer.JsonSerializer interface.
        // Or use Azure provided libraries for this.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson

        BinaryData binaryData = BinaryData.fromObject(data);

        Person person = binaryData.toObject(TypeReference.createInstance(Person.class));
        System.out.println(person.getName());
        // END: com.azure.core.util.BinaryData.toObject#TypeReference
    }

    /**
     * Codesnippets for {@link BinaryData#toObject(TypeReference)}.
     */
    public void toObjectTypeReferenceDefaultJsonSerializerWithGenerics() {
        // BEGIN: com.azure.core.util.BinaryData.toObject#TypeReference-generic
        final Person person1 = new Person().setName("John");
        final Person person2 = new Person().setName("Jack");

        List<Person> personList = new ArrayList<>();
        personList.add(person1);
        personList.add(person2);

        // Ensure your classpath have the Serializer to serialize the object which implement implement
        // com.azure.core.util.serializer.JsonSerializer interface.
        // Or use Azure provided libraries for this.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson


        BinaryData binaryData = BinaryData.fromObject(personList);

        List<Person> persons = binaryData.toObject(new TypeReference<List<Person>>() { });
        persons.forEach(person -> System.out.println(person.getName()));
        // END: com.azure.core.util.BinaryData.toObject#TypeReference-generic
    }

    /**
     * Codesnippets for {@link BinaryData#toObject(Class, ObjectSerializer)}.
     */
    public void toObjectClassObjectSerializer() {
        // BEGIN: com.azure.core.util.BinaryData.toObject#Class-ObjectSerializer
        class Person {
            @JsonProperty
            private String name;

            @JsonSetter
            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @JsonGetter
            public String getName() {
                return name;
            }
        }
        final Person data = new Person().setName("John");

        // Provide your custom serializer or use Azure provided serializers.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson

        final ObjectSerializer serializer =
            new MyJsonSerializer(); // Replace this with your Serializer
        BinaryData binaryData = BinaryData.fromObject(data, serializer);

        Person person = binaryData.toObject(Person.class, serializer);
        System.out.println("Name : " + person.getName());
        // END: com.azure.core.util.BinaryData.toObject#Class-ObjectSerializer
    }

    /**
     * Codesnippets for {@link BinaryData#toObject(TypeReference, ObjectSerializer)}.
     */
    public void toObjectTypeReferenceObjectSerializer() {
        // BEGIN: com.azure.core.util.BinaryData.toObject#TypeReference-ObjectSerializer
        class Person {
            @JsonProperty
            private String name;

            @JsonSetter
            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @JsonGetter
            public String getName() {
                return name;
            }
        }
        final Person data = new Person().setName("John");

        // Provide your custom serializer or use Azure provided serializers.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson

        final ObjectSerializer serializer =
            new MyJsonSerializer(); // Replace this with your Serializer
        BinaryData binaryData = BinaryData.fromObject(data, serializer);

        Person person = binaryData.toObject(TypeReference.createInstance(Person.class), serializer);
        System.out.println("Name : " + person.getName());

        // END: com.azure.core.util.BinaryData.toObject#TypeReference-ObjectSerializer
    }

    /**
     * Codesnippets for {@link BinaryData#toObject(TypeReference, ObjectSerializer)} that uses generics.
     */
    public void toObjectTypeReferenceObjectSerializerWithGenerics() {
        // BEGIN: com.azure.core.util.BinaryData.toObject#TypeReference-ObjectSerializer-generic
        final Person person1 = new Person().setName("John");
        final Person person2 = new Person().setName("Jack");

        List<Person> personList = new ArrayList<>();
        personList.add(person1);
        personList.add(person2);

        final ObjectSerializer serializer =
            new MyJsonSerializer(); // Replace this with your Serializer
        BinaryData binaryData = BinaryData.fromObject(personList, serializer);

        // Retains the type of the list when deserializing
        List<Person> persons = binaryData.toObject(new TypeReference<List<Person>>() { }, serializer);
        persons.forEach(person -> System.out.println("Name : " + person.getName()));
        // END: com.azure.core.util.BinaryData.toObject#TypeReference-ObjectSerializer-generic
    }

    /**
     * Codesnippets for {@link BinaryData#toObjectAsync(Class)}.
     */
    public void toObjectAsyncClassDefaultJsonSerializer() throws InterruptedException {
        // BEGIN: com.azure.core.util.BinaryData.toObjectAsync#Class
        class Person {
            @JsonProperty
            private String name;

            @JsonSetter
            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @JsonGetter
            public String getName() {
                return name;
            }
        }
        final Person data = new Person().setName("John");

        // Ensure your classpath have the Serializer to serialize the object which implement implement
        // com.azure.core.util.serializer.JsonSerializer interface.
        // Or use Azure provided libraries for this.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson

        BinaryData binaryData = BinaryData.fromObject(data);

        Disposable subscriber = binaryData.toObjectAsync(Person.class)
            .subscribe(person -> System.out.println(person.getName()));

        // So that your program wait for above subscribe to complete.
        TimeUnit.SECONDS.sleep(5);
        subscriber.dispose();
        // END: com.azure.core.util.BinaryData.toObjectAsync#Class
    }

    /**
     * Codesnippets for {@link BinaryData#toObjectAsync(TypeReference)}.
     */
    public void toObjectAsyncTypeReferenceDefaultJsonSerializer() throws InterruptedException {
        // BEGIN: com.azure.core.util.BinaryData.toObjectAsync#TypeReference
        class Person {
            @JsonProperty
            private String name;

            @JsonSetter
            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @JsonGetter
            public String getName() {
                return name;
            }
        }
        final Person data = new Person().setName("John");

        // Ensure your classpath have the Serializer to serialize the object which implement implement
        // com.azure.core.util.serializer.JsonSerializer interface.
        // Or use Azure provided libraries for this.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson

        BinaryData binaryData = BinaryData.fromObject(data);

        Disposable subscriber = binaryData.toObjectAsync(TypeReference.createInstance(Person.class))
            .subscribe(person -> System.out.println(person.getName()));

        // So that your program wait for above subscribe to complete.
        TimeUnit.SECONDS.sleep(5);
        subscriber.dispose();
        // END: com.azure.core.util.BinaryData.toObjectAsync#TypeReference
    }

    /**
     * Codesnippets for {@link BinaryData#toObjectAsync(TypeReference)}.
     */
    public void toObjectAsyncTypeReferenceDefaultJsonSerializerGeneric() throws InterruptedException {
        // BEGIN: com.azure.core.util.BinaryData.toObjectAsync#TypeReference-generic
        final Person person1 = new Person().setName("John");
        final Person person2 = new Person().setName("Jack");

        List<Person> personList = new ArrayList<>();
        personList.add(person1);
        personList.add(person2);

        BinaryData binaryData = BinaryData.fromObject(personList);

        Disposable subscriber = binaryData.toObjectAsync(new TypeReference<List<Person>>() { })
            .subscribe(persons -> persons.forEach(person -> System.out.println(person.getName())));

        // So that your program wait for above subscribe to complete.
        TimeUnit.SECONDS.sleep(5);
        subscriber.dispose();
        // END: com.azure.core.util.BinaryData.toObjectAsync#TypeReference-generic
    }

    /**
     * Codesnippets for {@link BinaryData#toObjectAsync(Class, ObjectSerializer)}.
     */
    public void toObjectAsyncClassObjectSerializer() throws InterruptedException {
        // BEGIN: com.azure.core.util.BinaryData.toObjectAsync#Class-ObjectSerializer
        class Person {
            @JsonProperty
            private String name;

            @JsonSetter
            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @JsonGetter
            public String getName() {
                return name;
            }
        }
        final Person data = new Person().setName("John");

        // Provide your custom serializer or use Azure provided serializers.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson

        final ObjectSerializer serializer =
            new MyJsonSerializer(); // Replace this with your Serializer
        BinaryData binaryData = BinaryData.fromObject(data, serializer);

        Disposable subscriber = binaryData.toObjectAsync(Person.class, serializer)
            .subscribe(person -> System.out.println(person.getName()));

        // So that your program wait for above subscribe to complete.
        TimeUnit.SECONDS.sleep(5);
        subscriber.dispose();
        // END: com.azure.core.util.BinaryData.toObjectAsync#Class-ObjectSerializer
    }

    /**
     * Codesnippets for {@link BinaryData#toObjectAsync(TypeReference, ObjectSerializer)}.
     */
    public void toObjectAsyncTypeReferenceObjectSerializer() throws InterruptedException {
        // BEGIN: com.azure.core.util.BinaryData.toObjectAsync#TypeReference-ObjectSerializer
        class Person {
            @JsonProperty
            private String name;

            @JsonSetter
            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @JsonGetter
            public String getName() {
                return name;
            }
        }
        final Person data = new Person().setName("John");

        // Provide your custom serializer or use Azure provided serializers.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson

        final ObjectSerializer serializer =
            new MyJsonSerializer(); // Replace this with your Serializer
        BinaryData binaryData = BinaryData.fromObject(data, serializer);

        Disposable subscriber = binaryData
            .toObjectAsync(TypeReference.createInstance(Person.class), serializer)
            .subscribe(person -> System.out.println(person.getName()));

        // So that your program wait for above subscribe to complete.
        TimeUnit.SECONDS.sleep(5);
        subscriber.dispose();
        // END: com.azure.core.util.BinaryData.toObjectAsync#TypeReference-ObjectSerializer
    }

    /**
     * Codesnippets for {@link BinaryData#toObjectAsync(TypeReference, ObjectSerializer)} that uses generics.
     */
    public void toObjectAsyncTypeReferenceObjectSerializerGeneric() throws InterruptedException {
        // BEGIN: com.azure.core.util.BinaryData.toObjectAsync#TypeReference-ObjectSerializer-generic
        final Person person1 = new Person().setName("John");
        final Person person2 = new Person().setName("Jack");

        List<Person> personList = new ArrayList<>();
        personList.add(person1);
        personList.add(person2);

        final ObjectSerializer serializer =
            new MyJsonSerializer(); // Replace this with your Serializer
        BinaryData binaryData = BinaryData.fromObject(personList, serializer);

        Disposable subscriber = binaryData
            .toObjectAsync(new TypeReference<List<Person>>() { }, serializer) // retains the generic type information
            .subscribe(persons -> persons.forEach(person -> System.out.println(person.getName())));

        // So that your program wait for above subscribe to complete.
        TimeUnit.SECONDS.sleep(5);
        subscriber.dispose();
        // END: com.azure.core.util.BinaryData.toObjectAsync#TypeReference-ObjectSerializer-generic
    }

    /**
     * Codesnippets for {@link BinaryData#toStream()}.
     */
    public void toStream() throws IOException {
        // BEGIN: com.azure.core.util.BinaryData.toStream
        final byte[] data = "Some Data".getBytes(StandardCharsets.UTF_8);
        BinaryData binaryData = BinaryData.fromStream(new ByteArrayInputStream(data));
        final byte[] bytes = new byte[data.length];
        binaryData.toStream().read(bytes, 0, data.length);
        System.out.println(new String(bytes));
        // END: com.azure.core.util.BinaryData.toStream
    }

    /**
     * Codesnippets for {@link BinaryData#toByteBuffer()}.
     */
    public void toReadOnlyByteBuffer() {
        // BEGIN: com.azure.util.BinaryData.toByteBuffer
        final byte[] data = "Some Data".getBytes(StandardCharsets.UTF_8);
        BinaryData binaryData = BinaryData.fromBytes(data);
        final byte[] bytes = new byte[data.length];
        binaryData.toByteBuffer().get(bytes, 0, data.length);
        System.out.println(new String(bytes));
        // END: com.azure.util.BinaryData.toByteBuffer
    }

    public static class MyJsonSerializer implements JsonSerializer {
        private final ClientLogger logger = new ClientLogger(MyJsonSerializer.class);
        private final ObjectMapper mapper;
        private final TypeFactory typeFactory;

        public MyJsonSerializer() {
            this.mapper = new ObjectMapper();
            this.typeFactory = mapper.getTypeFactory();
        }

        @Override
        public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
            if (stream == null) {
                return null;
            }

            try {
                return mapper.readValue(stream, typeFactory.constructType(typeReference.getJavaType()));
            } catch (IOException ex) {
                throw logger.logExceptionAsError(new UncheckedIOException(ex));
            }
        }

        @Override
        public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
            return Mono.fromCallable(() -> deserialize(stream, typeReference));
        }


        @Override
        public void serialize(OutputStream stream, Object value) {
            try {
                mapper.writeValue(stream, value);
            } catch (IOException ex) {
                throw logger.logExceptionAsError(new UncheckedIOException(ex));
            }
        }

        @Override
        public Mono<Void> serializeAsync(OutputStream stream, Object value) {
            return Mono.fromRunnable(() -> serialize(stream, value));
        }
    }
}
