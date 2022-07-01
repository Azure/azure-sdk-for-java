// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;


import com.azure.core.implementation.AccessibleByteArrayOutputStream;
import com.azure.core.implementation.models.jsonflatten.ClassWithFlattenedProperties;
import com.azure.core.implementation.models.jsonflatten.FlattenedProduct;
import com.azure.core.implementation.models.jsonflatten.FlattenedPropertiesAndJsonAnyGetter;
import com.azure.core.implementation.models.jsonflatten.JsonFlattenNestedInner;
import com.azure.core.implementation.models.jsonflatten.JsonFlattenOnArrayType;
import com.azure.core.implementation.models.jsonflatten.JsonFlattenOnCollectionType;
import com.azure.core.implementation.models.jsonflatten.JsonFlattenOnJsonIgnoredProperty;
import com.azure.core.implementation.models.jsonflatten.JsonFlattenOnPrimitiveType;
import com.azure.core.implementation.models.jsonflatten.JsonFlattenWithJsonInfoDiscriminator;
import com.azure.core.implementation.models.jsonflatten.SampleResource;
import com.azure.core.implementation.models.jsonflatten.School;
import com.azure.core.implementation.models.jsonflatten.Student;
import com.azure.core.implementation.models.jsonflatten.Teacher;
import com.azure.core.implementation.models.jsonflatten.VirtualMachineIdentity;
import com.azure.core.implementation.models.jsonflatten.VirtualMachineScaleSet;
import com.azure.core.implementation.models.jsonflatten.VirtualMachineScaleSetNetworkConfiguration;
import com.azure.core.implementation.models.jsonflatten.VirtualMachineScaleSetNetworkProfile;
import com.azure.core.implementation.models.jsonflatten.VirtualMachineScaleSetVMProfile;
import com.azure.json.DefaultJsonReader;
import com.azure.json.DefaultJsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import wiremock.com.google.common.collect.ImmutableList;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FlatteningSerializerTests {

    @Test
    public void canFlatten() {
        Foo foo = new Foo();
        foo.bar("hello.world");
        //
        List<String> baz = Arrays.asList("hello", "hello.world");
        foo.baz(baz);

        HashMap<String, String> qux = new LinkedHashMap<>();
        qux.put("hello", "world");
        qux.put("a.b", "c.d");
        qux.put("bar.b", "uuzz");
        qux.put("bar.a", "ttyy");
        foo.qux(qux);

        // serialization
        String serialized = writeJson(foo);
        assertEquals("{\"$type\":\"foo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}}}", serialized);

        // deserialization
        Foo deserialized = readJson(serialized, Foo::fromJson);
        assertEquals("hello.world", deserialized.bar());
        Assertions.assertArrayEquals(new String[]{"hello", "hello.world"}, deserialized.baz().toArray());
        assertNotNull(deserialized.qux());
        assertEquals("world", deserialized.qux().get("hello"));
        assertEquals("c.d", deserialized.qux().get("a.b"));
        assertEquals("ttyy", deserialized.qux().get("bar.a"));
        assertEquals("uuzz", deserialized.qux().get("bar.b"));
    }

    @Test
    public void canSerializeMapKeysWithDotAndSlash() {
        String serialized = writeJson(prepareSchoolModel());
        assertEquals("{\"teacher\":{\"students\":{\"af.B/C\":{},\"af.B/D\":{}}},\"properties\":{\"name\":\"school1\"},\"tags\":{\"foo.aa\":\"bar\",\"x.y\":\"zz\"}}", serialized);
    }

    /**
     * Validates decoding and encoding of a type with type id containing dot and no additional properties For decoding
     * and encoding base type will be used.
     */
    @Test
    public void canHandleTypeWithTypeIdContainingDotAndNoProperties() {
        String rabbitSerialized = "{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}";
        String shelterSerialized = "{\"properties\":{\"animalsInfo\":[{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}},{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}}]}}";

        AnimalWithTypeIdContainingDot rabbitDeserialized = readJson(rabbitSerialized,
            AnimalWithTypeIdContainingDot::fromJson);
        assertTrue(rabbitDeserialized instanceof RabbitWithTypeIdContainingDot);
        assertNotNull(rabbitDeserialized);

        AnimalShelter shelterDeserialized = readJson(shelterSerialized, AnimalShelter::fromJson);
        assertNotNull(shelterDeserialized);
        assertEquals(2, shelterDeserialized.animalsInfo().size());
        for (FlattenableAnimalInfo animalInfo : shelterDeserialized.animalsInfo()) {
            assertTrue(animalInfo.animal() instanceof RabbitWithTypeIdContainingDot);
            assertNotNull(animalInfo.animal());
        }
    }

    /**
     * Validates that decoding and encoding of a type with type id containing dot and can be done. For decoding and
     * encoding base type will be used.
     */
    @Test
    public void canHandleTypeWithTypeIdContainingDot0() {
        List<String> meals = Arrays.asList("carrot", "apple");
        //
        AnimalWithTypeIdContainingDot animalToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        String serialized = writeJson(animalToSerialize);
        //
        assertEquals("{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}", serialized);

        // De-Serialize
        //
        AnimalWithTypeIdContainingDot animalDeserialized = readJson(serialized,
            AnimalWithTypeIdContainingDot::fromJson);
        assertTrue(animalDeserialized instanceof RabbitWithTypeIdContainingDot);
        RabbitWithTypeIdContainingDot rabbit = (RabbitWithTypeIdContainingDot) animalDeserialized;
        assertNotNull(rabbit.meals());
        assertEquals(rabbit.meals().size(), 2);
    }

    /**
     * Validates that decoding and encoding of a type with type id containing dot and can be done. For decoding and
     * encoding concrete type will be used.
     */
    @Test
    public void canHandleTypeWithTypeIdContainingDot1() {
        List<String> meals = Arrays.asList("carrot", "apple");
        //
        RabbitWithTypeIdContainingDot rabbitToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        String serialized = writeJson(rabbitToSerialize);
        //
        assertEquals("{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}", serialized);

        // De-Serialize
        //
        RabbitWithTypeIdContainingDot rabbitDeserialized = readJson(serialized,
            RabbitWithTypeIdContainingDot::fromJson);
        assertNotNull(rabbitDeserialized);
        assertNotNull(rabbitDeserialized.meals());
        assertEquals(rabbitDeserialized.meals().size(), 2);
    }


    /**
     * Validates that decoding and encoding of a type with flattenable property and type id containing dot and can be
     * done. For decoding and encoding base type will be used.
     */
    @Test
    public void canHandleTypeWithFlattenablePropertyAndTypeIdContainingDot0() {
        AnimalWithTypeIdContainingDot animalToSerialize = new DogWithTypeIdContainingDot()
            .withBreed("AKITA")
            .withCuteLevel(10);

        // serialization
        String serialized = writeJson(animalToSerialize);

        assertEquals("{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10}}",
            serialized);

        // de-serialization
        AnimalWithTypeIdContainingDot animalDeserialized = readJson(serialized,
            AnimalWithTypeIdContainingDot::fromJson);
        assertTrue(animalDeserialized instanceof DogWithTypeIdContainingDot);
        DogWithTypeIdContainingDot dogDeserialized = (DogWithTypeIdContainingDot) animalDeserialized;
        assertNotNull(dogDeserialized);
        assertEquals(dogDeserialized.breed(), "AKITA");
        assertEquals(dogDeserialized.cuteLevel(), (Integer) 10);
    }

    /**
     * Validates that decoding and encoding of a type with flattenable property and type id containing dot and can be
     * done. For decoding and encoding concrete type will be used.
     */
    @Test
    public void canHandleTypeWithFlattenablePropertyAndTypeIdContainingDot1() {
        DogWithTypeIdContainingDot dogToSerialize = new DogWithTypeIdContainingDot().withBreed("AKITA").withCuteLevel(10);

        // serialization
        String serialized = writeJson(dogToSerialize);

        assertEquals("{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10}}",
            serialized);

        // de-serialization
        DogWithTypeIdContainingDot dogDeserialized = readJson(serialized, DogWithTypeIdContainingDot::fromJson);
        assertNotNull(dogDeserialized);
        assertEquals(dogDeserialized.breed(), "AKITA");
        assertEquals(dogDeserialized.cuteLevel(), (Integer) 10);
    }

    /**
     * Validates that decoding and encoding of an array of type with type id containing dot and can be done. For decoding
     * and encoding base type will be used.
     */
    @Test
    public void canHandleArrayOfTypeWithTypeIdContainingDot0() {
        List<String> meals = Arrays.asList("carrot", "apple");
        //
        AnimalWithTypeIdContainingDot animalToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        List<AnimalWithTypeIdContainingDot> animalsToSerialize = new ArrayList<>();
        animalsToSerialize.add(animalToSerialize);
        String serialized = writeJson(animalsToSerialize);
        assertEquals("[{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}]",
            serialized);

        // De-serialize
        //
        List<AnimalWithTypeIdContainingDot> animalsDeserialized = readJsons(serialized,
            AnimalWithTypeIdContainingDot::fromJson);
        assertNotNull(animalsDeserialized);
        assertEquals(1, animalsDeserialized.size());
        AnimalWithTypeIdContainingDot animalDeserialized = animalsDeserialized.get(0);
        assertTrue(animalDeserialized instanceof RabbitWithTypeIdContainingDot);
        RabbitWithTypeIdContainingDot rabbitDeserialized = (RabbitWithTypeIdContainingDot) animalDeserialized;
        assertNotNull(rabbitDeserialized.meals());
        assertEquals(rabbitDeserialized.meals().size(), 2);
    }

    /**
     * Validates that decoding and encoding of an array of type with type id containing dot and can be done. For decoding
     * and encoding concrete type will be used.
     */
    @Test
    public void canHandleArrayOfTypeWithTypeIdContainingDot1() {
        List<String> meals = Arrays.asList("carrot", "apple");
        //
        RabbitWithTypeIdContainingDot rabbitToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        List<RabbitWithTypeIdContainingDot> rabbitsToSerialize = new ArrayList<>();
        rabbitsToSerialize.add(rabbitToSerialize);
        String serialized = writeJson(rabbitsToSerialize);
        assertEquals("[{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}]",
            serialized);

        // De-serialize
        //
        List<RabbitWithTypeIdContainingDot> rabbitsDeserialized = readJsons(serialized,
            RabbitWithTypeIdContainingDot::fromJson);
        assertNotNull(rabbitsDeserialized);
        assertEquals(1, rabbitsDeserialized.size());
        RabbitWithTypeIdContainingDot rabbitDeserialized = rabbitsDeserialized.get(0);
        assertNotNull(rabbitDeserialized.meals());
        assertEquals(rabbitDeserialized.meals().size(), 2);
    }


    /**
     * Validates that decoding and encoding of a composed type with type id containing dot and can be done.
     */
    @Test
    public void canHandleComposedTypeWithTypeIdContainingDot0() {
        List<String> meals = Arrays.asList("carrot", "apple");
        AnimalWithTypeIdContainingDot animalToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        FlattenableAnimalInfo animalInfoToSerialize = new FlattenableAnimalInfo().withAnimal(animalToSerialize);
        List<FlattenableAnimalInfo> animalsInfoSerialized = ImmutableList.of(animalInfoToSerialize);
        AnimalShelter animalShelterToSerialize = new AnimalShelter().withAnimalsInfo(animalsInfoSerialized);
        String serialized = writeJson(animalShelterToSerialize);
        assertEquals("{\"properties\":{\"animalsInfo\":[{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}}]}}",
            serialized);

        // de-serialization
        //
        AnimalShelter shelterDeserialized = readJson(serialized, AnimalShelter::fromJson);
        assertNotNull(shelterDeserialized.animalsInfo());
        assertEquals(shelterDeserialized.animalsInfo().size(), 1);
        FlattenableAnimalInfo animalsInfoDeserialized = shelterDeserialized.animalsInfo().get(0);
        assertTrue(animalsInfoDeserialized.animal() instanceof RabbitWithTypeIdContainingDot);
        AnimalWithTypeIdContainingDot animalDeserialized = animalsInfoDeserialized.animal();
        assertTrue(animalDeserialized instanceof RabbitWithTypeIdContainingDot);
        RabbitWithTypeIdContainingDot rabbitDeserialized = (RabbitWithTypeIdContainingDot) animalDeserialized;
        assertNotNull(rabbitDeserialized);
        assertNotNull(rabbitDeserialized.meals());
        assertEquals(rabbitDeserialized.meals().size(), 2);
    }

    @Test
    public void canHandleComposedSpecificPolymorphicTypeWithTypeId() {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = readJson(serializedCollectionWithTypeId, ComposeTurtles::fromJson);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1());
        assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        assertEquals("{\"turtlesSet1\":[{\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\",\"age\":100,\"size\":10},{\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\",\"age\":200,\"size\":20}]}",
            writeJson(composedTurtleDeserialized));
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet1Lead\":{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}}";
        // de-serialization
        //
        composedTurtleDeserialized = readJson(serializedScalarWithTypeId, ComposeTurtles::fromJson);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1Lead());
        assertEquals(10, (long) composedTurtleDeserialized.turtlesSet1Lead().size());
        assertEquals(100, (long) composedTurtleDeserialized.turtlesSet1Lead().age());
        //
        assertEquals("{\"turtlesSet1Lead\":{\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\",\"age\":100,\"size\":10}}",
            writeJson(composedTurtleDeserialized));
    }

    @Test
    public void canHandleComposedSpecificPolymorphicTypeWithoutTypeId() {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10 },{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = readJson(serializedCollectionWithTypeId, ComposeTurtles::fromJson);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1());
        assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        assertEquals("{\"turtlesSet1\":[{\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\",\"age\":100,\"size\":10},{\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\",\"age\":200,\"size\":20}]}",
            writeJson(composedTurtleDeserialized));
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet1Lead\":{\"age\":100,\"size\":10 }}";
        // de-serialization
        //
        composedTurtleDeserialized = readJson(serializedScalarWithTypeId, ComposeTurtles::fromJson);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1Lead());
        assertEquals(100, (long) composedTurtleDeserialized.turtlesSet1Lead().age());
        //
        assertEquals("{\"turtlesSet1Lead\":{\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\",\"age\":100,\"size\":10}}",
            writeJson(composedTurtleDeserialized));
    }

    @Test
    public void canHandleComposedSpecificPolymorphicTypeWithAndWithoutTypeId() {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = readJson(serializedCollectionWithTypeId, ComposeTurtles::fromJson);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1());
        assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        assertEquals("{\"turtlesSet1\":[{\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\",\"age\":100,\"size\":10},{\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\",\"age\":200,\"size\":20}]}",
            writeJson(composedTurtleDeserialized));
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithTypeId() {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = readJson(serializedCollectionWithTypeId, ComposeTurtles::fromJson);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2());
        assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        assertTrue(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        assertTrue(composedTurtleDeserialized.turtlesSet2().get(1) instanceof TurtleWithTypeIdContainingDot);
        //
        assertEquals("{\"turtlesSet2\":[{\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\",\"age\":100,\"size\":10},{\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\",\"age\":200,\"size\":20}]}",
            writeJson(composedTurtleDeserialized));
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet2Lead\":{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}}";
        // de-serialization
        //
        composedTurtleDeserialized = readJson(serializedScalarWithTypeId, ComposeTurtles::fromJson);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2Lead());
        assertTrue(composedTurtleDeserialized.turtlesSet2Lead() instanceof TurtleWithTypeIdContainingDot);
        assertEquals(10, (long) ((TurtleWithTypeIdContainingDot) composedTurtleDeserialized.turtlesSet2Lead()).size());
        assertEquals(100, (long) composedTurtleDeserialized.turtlesSet2Lead().age());
        //
        assertEquals("{\"turtlesSet2Lead\":{\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\",\"age\":100,\"size\":10}}",
            writeJson(composedTurtleDeserialized));
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithoutTypeId() {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10 },{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = readJson(serializedCollectionWithTypeId, ComposeTurtles::fromJson);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2());
        assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        Assertions.assertFalse(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        Assertions.assertFalse(composedTurtleDeserialized.turtlesSet2().get(1) instanceof TurtleWithTypeIdContainingDot);
        //
        // -- Validate scalar property
        //
        assertEquals("{\"turtlesSet2\":[{\"@odata.type\":\"NonEmptyAnimalWithTypeIdContainingDot\",\"age\":100},{\"@odata.type\":\"NonEmptyAnimalWithTypeIdContainingDot\",\"age\":200}]}",
            writeJson(composedTurtleDeserialized));
        //
        String serializedScalarWithTypeId = "{\"turtlesSet2Lead\":{\"age\":100,\"size\":10 }}";
        // de-serialization
        //
        composedTurtleDeserialized = readJson(serializedScalarWithTypeId, ComposeTurtles::fromJson);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2Lead());
        //
        assertEquals("{\"turtlesSet2Lead\":{\"@odata.type\":\"NonEmptyAnimalWithTypeIdContainingDot\",\"age\":100}}",
            writeJson(composedTurtleDeserialized));
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithAndWithoutTypeId() {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = readJson(serializedCollectionWithTypeId, ComposeTurtles::fromJson);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2());
        assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        assertTrue(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        assertNotNull(composedTurtleDeserialized.turtlesSet2().get(1));
        //
        assertEquals("{\"turtlesSet2\":[{\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\",\"age\":100,\"size\":10},{\"@odata.type\":\"NonEmptyAnimalWithTypeIdContainingDot\",\"age\":200}]}",
            writeJson(composedTurtleDeserialized));
    }

    @Test
    public void canHandleEscapedProperties() {
        FlattenedProduct productToSerialize = new FlattenedProduct()
            .setProductName("drink")
            .setProductType("chai");

        // serialization
        //
        String serialized = writeJson(productToSerialize);
        assertEquals("{\"properties\":{\"p.name\":\"drink\",\"type\":\"chai\"}}", serialized);

        // de-serialization
        //
        FlattenedProduct productDeserialized = readJson(serialized, FlattenedProduct::fromJson);
        assertNotNull(productDeserialized);
        assertEquals(productDeserialized.getProductName(), "drink");
        assertEquals(productDeserialized.getProductType(), "chai");
    }

    @Test
    public void canHandleSinglePropertyBeingFlattened() {
        ClassWithFlattenedProperties classWithFlattenedProperties = new ClassWithFlattenedProperties("random", "E24JJxztP");

        String serialized = writeJson(classWithFlattenedProperties);
        assertEquals("{\"@odata\":{\"type\":\"random\"},\"@odata.etag\":\"E24JJxztP\"}", serialized);

        ClassWithFlattenedProperties deserialized = readJson(serialized, ClassWithFlattenedProperties::fromJson);
        assertNotNull(deserialized);
        assertEquals(classWithFlattenedProperties.getOdataType(), deserialized.getOdataType());
        assertEquals(classWithFlattenedProperties.getOdataETag(), deserialized.getOdataETag());
    }

    @Test
    public void canHandleMultiLevelPropertyFlattening() {
        VirtualMachineScaleSet virtualMachineScaleSet = new VirtualMachineScaleSet()
            .setVirtualMachineProfile(new VirtualMachineScaleSetVMProfile()
                .setNetworkProfile(new VirtualMachineScaleSetNetworkProfile()
                    .setNetworkInterfaceConfigurations(Collections.singletonList(
                        new VirtualMachineScaleSetNetworkConfiguration().setName("name").setPrimary(true)))));

        String serialized = writeJson(virtualMachineScaleSet);
        String expected = "{\"properties\":{\"virtualMachineProfile\":{\"networkProfile\":{\"networkInterfaceConfigurations\":[{\"name\":\"name\",\"properties\":{\"primary\":true}}]}}}}";
        assertEquals(expected, serialized);

        VirtualMachineScaleSet deserialized = readJson(serialized, VirtualMachineScaleSet::fromJson);
        assertNotNull(deserialized);

        VirtualMachineScaleSetNetworkConfiguration expectedConfig = virtualMachineScaleSet.getVirtualMachineProfile()
            .getNetworkProfile()
            .getNetworkInterfaceConfigurations()
            .get(0);

        VirtualMachineScaleSetNetworkConfiguration actualConfig = deserialized.getVirtualMachineProfile()
            .getNetworkProfile()
            .getNetworkInterfaceConfigurations()
            .get(0);

        assertEquals(expectedConfig.getName(), actualConfig.getName());
        assertEquals(expectedConfig.getPrimary(), actualConfig.getPrimary());
    }

    @Test
    public void jsonFlattenOnArrayType() {
        JsonFlattenOnArrayType expected = new JsonFlattenOnArrayType()
            .setJsonFlattenArray(new String[]{"hello", "goodbye", null});

        String expectedSerialization = "{\"jsonflatten\":{\"array\":[\"hello\",\"goodbye\",null]}}";
        String actualSerialization = writeJson(expected);

        assertEquals(expectedSerialization, actualSerialization);

        JsonFlattenOnArrayType deserialized = readJson(actualSerialization, JsonFlattenOnArrayType::fromJson);
        assertArrayEquals(expected.getJsonFlattenArray(), deserialized.getJsonFlattenArray());
    }

    @Test
    public void jsonFlattenOnCollectionTypeList() {
        final List<String> listCollection = Arrays.asList("hello", "goodbye", null);
        JsonFlattenOnCollectionType expected = new JsonFlattenOnCollectionType()
            .setJsonFlattenCollection(Collections.unmodifiableList(listCollection));

        String expectedSerialization = "{\"jsonflatten\":{\"collection\":[\"hello\",\"goodbye\",null]}}";
        String actualSerialization = writeJson(expected);

        assertEquals(expectedSerialization, actualSerialization);

        JsonFlattenOnCollectionType deserialized = readJson(actualSerialization, JsonFlattenOnCollectionType::fromJson);
        assertEquals(expected.getJsonFlattenCollection().size(), deserialized.getJsonFlattenCollection().size());
        for (int i = 0; i < expected.getJsonFlattenCollection().size(); i++) {
            assertEquals(expected.getJsonFlattenCollection().get(i), deserialized.getJsonFlattenCollection().get(i));
        }
    }

    @Test
    public void jsonFlattenOnJsonIgnoredProperty() {
        JsonFlattenOnJsonIgnoredProperty expected = new JsonFlattenOnJsonIgnoredProperty()
            .setName("name")
            .setIgnored("ignored");

        String expectedSerialization = "{\"name\":\"name\"}";
        String actualSerialization = writeJson(expected);

        assertEquals(expectedSerialization, actualSerialization);

        JsonFlattenOnJsonIgnoredProperty deserialized = readJson(actualSerialization,
            JsonFlattenOnJsonIgnoredProperty::fromJson);
        assertEquals(expected.getName(), deserialized.getName());
        assertNull(deserialized.getIgnored());
    }

    @Test
    public void jsonFlattenOnPrimitiveType() {
        JsonFlattenOnPrimitiveType expected = new JsonFlattenOnPrimitiveType()
            .setJsonFlattenBoolean(true)
            .setJsonFlattenDecimal(1.25D)
            .setJsonFlattenNumber(2)
            .setJsonFlattenString("string");

        String expectedSerialization = "{\"jsonflatten\":{\"boolean\":true,\"decimal\":1.25,\"number\":2,\"string\":\"string\"}}";
        String actualSerialization = writeJson(expected);

        assertEquals(expectedSerialization, actualSerialization);

        JsonFlattenOnPrimitiveType deserialized = readJson(actualSerialization, JsonFlattenOnPrimitiveType::fromJson);
        assertEquals(expected.isJsonFlattenBoolean(), deserialized.isJsonFlattenBoolean());
        assertEquals(expected.getJsonFlattenDecimal(), deserialized.getJsonFlattenDecimal());
        assertEquals(expected.getJsonFlattenNumber(), deserialized.getJsonFlattenNumber());
        assertEquals(expected.getJsonFlattenString(), deserialized.getJsonFlattenString());
    }

    @Test
    public void jsonFlattenWithJsonInfoDiscriminator() {
        JsonFlattenWithJsonInfoDiscriminator expected = new JsonFlattenWithJsonInfoDiscriminator()
            .setJsonFlattenDiscriminator("discriminator");

        String expectedSerialization = "{\"type\":\"JsonFlattenWithJsonInfoDiscriminator\",\"jsonflatten\":{\"discriminator\":\"discriminator\"}}";
        String actualSerialization = writeJson(expected);

        assertEquals(expectedSerialization, actualSerialization);

        JsonFlattenWithJsonInfoDiscriminator deserialized = readJson(actualSerialization,
            JsonFlattenWithJsonInfoDiscriminator::fromJson);
        assertEquals(expected.getJsonFlattenDiscriminator(), deserialized.getJsonFlattenDiscriminator());
    }

    @Test
    public void flattenedPropertiesAndJsonAnyGetter() {
        FlattenedPropertiesAndJsonAnyGetter expected = new FlattenedPropertiesAndJsonAnyGetter()
            .setString("string")
            .addAdditionalProperty("key1", "value1")
            .addAdditionalProperty("key2", "value2");

        String expectedSerialization = "{\"flattened\":{\"string\":\"string\"},\"key1\":\"value1\",\"key2\":\"value2\"}";
        String actualSerialization = writeJson(expected);

        assertEquals(expectedSerialization, actualSerialization);

        FlattenedPropertiesAndJsonAnyGetter deserialized = readJson(actualSerialization,
            FlattenedPropertiesAndJsonAnyGetter::fromJson);
        assertEquals(expected.getString(), deserialized.getString());
        assertEquals(expected.additionalProperties().size(), deserialized.additionalProperties().size());
        for (String key : expected.additionalProperties().keySet()) {
            assertEquals(expected.additionalProperties().get(key), deserialized.additionalProperties().get(key));
        }
    }

    //@Test
    public void flattenedPropertiesAndJsonAnyGetterWithSameNameAsFlattened() {
        // What should we expect in this case as 'setString' sets a flattened property with the containing field being
        // named 'flattened' and there is an additional property with field name 'flattened'.
        FlattenedPropertiesAndJsonAnyGetter expected = new FlattenedPropertiesAndJsonAnyGetter()
            .setString("string")
            .addAdditionalProperty("key1", "value1")
            .addAdditionalProperty("key2", "value2")
            .addAdditionalProperty("flattened", "value3");

        String expectedSerialization = "{\"flattened\":{\"string\":\"string\"},\"key1\":\"value1\",\"key2\":\"value2\"}";
        String actualSerialization = writeJson(expected);

        assertEquals(expectedSerialization, actualSerialization);

        FlattenedPropertiesAndJsonAnyGetter deserialized = readJson(actualSerialization,
            FlattenedPropertiesAndJsonAnyGetter::fromJson);
        assertEquals(expected.getString(), deserialized.getString());
        assertEquals(expected.additionalProperties().size(), deserialized.additionalProperties().size());
        for (String key : expected.additionalProperties().keySet()) {
            assertEquals(expected.additionalProperties().get(key), deserialized.additionalProperties().get(key));
        }
    }

    @Test
    public void jsonFlattenFinalMap() {
        final HashMap<String, String> mapProperties = new HashMap<>();
        mapProperties.put("/subscriptions/0-0-0-0-0/resourcegroups/0/providers/Microsoft.ManagedIdentity/0", "value");
        School school = new School().setTags(mapProperties);

        String actualSerialization = writeJson(school);
        String expectedSerialization = "{\"tags\":{\"/subscriptions/0-0-0-0-0/resourcegroups"
            + "/0/providers/Microsoft.ManagedIdentity/0\":\"value\"}}";
        assertEquals(expectedSerialization, actualSerialization);
    }

    @Test
    public void jsonFlattenNestedInner() {
        JsonFlattenNestedInner expected = new JsonFlattenNestedInner();
        VirtualMachineIdentity identity = new VirtualMachineIdentity();
        final Map<String, Object> map = new HashMap<>();
        map.put("/subscriptions/0-0-0-0-0/resourcegroups/0/providers/Microsoft.ManagedIdentity/userAssignedIdentities/0",
            new Object());
        identity.setType(Arrays.asList("SystemAssigned, UserAssigned"));
        identity.setUserAssignedIdentities(map);
        expected.setIdentity(identity);

        String expectedSerialization = "{\"identity\":{\"type\":[\"SystemAssigned, UserAssigned\"],"
            + "\"userAssignedIdentities\":{\"/subscriptions/0-0-0-0-0/resourcegroups/0/providers/"
            + "Microsoft.ManagedIdentity/userAssignedIdentities/0\":{}}}}";
        String actualSerialization = writeJson(expected);

        assertEquals(expectedSerialization, actualSerialization);
    }

    @Test
    public void jsonFlattenRepeatedPropertyNameDeserialize() {
        SampleResource deserialized = readJson(
            "{\"name\":\"...-01\",\"properties\":{\"registrationTtl\":\"10675199.02:48:05.4775807\",\"authorizationRules\":[]}}",
            SampleResource::fromJson);

        assertEquals("10675199.02:48:05.4775807", deserialized.getRegistrationTtl());
        assertNull(deserialized.getNamePropertiesName());
    }

    @ParameterizedTest
    @MethodSource("emptyDanglingNodeJsonSupplier")
    public void jsonFlattenEmptyDanglingNodesDeserialize(String json, Object expected) {
        // test to verify null dangling nodes are still retained and set to null
        FlattenDangling deserialized = readJson(json, FlattenDangling::fromJson);

        assertEquals(expected, deserialized.getFlattenedProperty());
    }

    private static String writeJson(JsonSerializable<?> jsonSerializable) {
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
        JsonWriter writer = DefaultJsonWriter.fromStream(outputStream);
        jsonSerializable.toJson(writer);

        return outputStream.toString(StandardCharsets.UTF_8);
    }

    private static String writeJson(List<? extends JsonSerializable<?>> jsonCapables) {
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
        JsonWriter writer = DefaultJsonWriter.fromStream(outputStream);

        writer.writeStartArray();
        jsonCapables.forEach(writer::writeJson);
        writer.writeEndArray().flush();

        return outputStream.toString(StandardCharsets.UTF_8);
    }

    private static <T> T readJson(String json, Function<JsonReader, T> reader) {
        return reader.apply(DefaultJsonReader.fromString(json));
    }

    private static <T> List<T> readJsons(String json, Function<JsonReader, T> reader) {
        return DefaultJsonReader.fromString(json).readArray(reader);
    }

    private static Stream<Arguments> emptyDanglingNodeJsonSupplier() {
        return Stream.of(
            Arguments.of("{\"a\":{}}", null),

            Arguments.of("{\"a\":{\"flattened\": {}}}", null),

            Arguments.of("{\"a\":{\"flattened\": {\"property\": null}}}", null),

            Arguments.of("{\"a\":{\"flattened\": {\"property\": \"value\"}}}", "value")
        );
    }

    private School prepareSchoolModel() {
        Teacher teacher = new Teacher();

        // Use LinkedHashMap for testing as it retains insertion order. This allows for a static, well-known JSON to
        // be produced instead of needing to inspect multiple potential outputs based on Map insertion ordering.
        Map<String, Student> students = new LinkedHashMap<>();
        students.put("af.B/C", new Student());
        students.put("af.B/D", new Student());

        teacher.setStudents(students);

        School school = new School().setName("school1");
        school.setTeacher(teacher);

        Map<String, String> schoolTags = new LinkedHashMap<>();
        schoolTags.put("foo.aa", "bar");
        schoolTags.put("x.y", "zz");

        school.setTags(schoolTags);

        return school;
    }
}

