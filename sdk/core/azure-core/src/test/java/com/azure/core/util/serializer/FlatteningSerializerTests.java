// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;


import com.azure.core.annotation.JsonFlatten;
import com.azure.core.implementation.TypeUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import wiremock.com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FlatteningSerializerTests {
    private static final JacksonAdapter ADAPTER = new JacksonAdapter();

    @Test
    public void canFlatten() throws Exception {
        Foo foo = new Foo();
        foo.bar("hello.world");
        //
        List<String> baz = new ArrayList<>();
        baz.add("hello");
        baz.add("hello.world");
        foo.baz(baz);

        HashMap<String, String> qux = new HashMap<>();
        qux.put("hello", "world");
        qux.put("a.b", "c.d");
        qux.put("bar.a", "ttyy");
        qux.put("bar.b", "uuzz");
        foo.qux(qux);

        // serialization
        String serialized = ADAPTER.serialize(foo, SerializerEncoding.JSON);
        assertEquals("{\"$type\":\"foo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}}}", serialized);

        // deserialization
        Foo deserialized = ADAPTER.deserialize(serialized, Foo.class, SerializerEncoding.JSON);
        assertEquals("hello.world", deserialized.bar());
        Assertions.assertArrayEquals(new String[]{"hello", "hello.world"}, deserialized.baz().toArray());
        assertNotNull(deserialized.qux());
        assertEquals("world", deserialized.qux().get("hello"));
        assertEquals("c.d", deserialized.qux().get("a.b"));
        assertEquals("ttyy", deserialized.qux().get("bar.a"));
        assertEquals("uuzz", deserialized.qux().get("bar.b"));
    }

    @Test
    public void canSerializeMapKeysWithDotAndSlash() throws Exception {
        String serialized = ADAPTER.serialize(prepareSchoolModel(), SerializerEncoding.JSON);
        assertEquals("{\"teacher\":{\"students\":{\"af.B/D\":{},\"af.B/C\":{}}},\"tags\":{\"foo.aa\":\"bar\",\"x.y\":\"zz\"},\"properties\":{\"name\":\"school1\"}}", serialized);
    }

    /**
     * Validates decoding and encoding of a type with type id containing dot and no additional properties For decoding
     * and encoding base type will be used.
     */
    @Test
    public void canHandleTypeWithTypeIdContainingDotAndNoProperties() throws IOException {
        String rabbitSerialized = "{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}";
        String shelterSerialized = "{\"properties\":{\"animalsInfo\":[{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}},{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}}]}}";

        AnimalWithTypeIdContainingDot rabbitDeserialized = ADAPTER.deserialize(rabbitSerialized,
            AnimalWithTypeIdContainingDot.class, SerializerEncoding.JSON);
        assertTrue(rabbitDeserialized instanceof RabbitWithTypeIdContainingDot);
        assertNotNull(rabbitDeserialized);

        AnimalShelter shelterDeserialized = ADAPTER.deserialize(shelterSerialized, AnimalShelter.class,
            SerializerEncoding.JSON);
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
    public void canHandleTypeWithTypeIdContainingDot0() throws IOException {
        // Serialize
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        //
        AnimalWithTypeIdContainingDot animalToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        String serialized = ADAPTER.serialize(animalToSerialize, SerializerEncoding.JSON);
        //
        String[] results = {
            "{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}",
            "{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}"
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // De-Serialize
        //
        AnimalWithTypeIdContainingDot animalDeserialized = ADAPTER.deserialize(serialized,
            AnimalWithTypeIdContainingDot.class, SerializerEncoding.JSON);
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
    public void canHandleTypeWithTypeIdContainingDot1() throws IOException {
        // Serialize
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        //
        RabbitWithTypeIdContainingDot rabbitToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        String serialized = ADAPTER.serialize(rabbitToSerialize, SerializerEncoding.JSON);
        //
        String[] results = {
            "{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}",
            "{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}"
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // De-Serialize
        //
        RabbitWithTypeIdContainingDot rabbitDeserialized = ADAPTER.deserialize(serialized,
            RabbitWithTypeIdContainingDot.class,
            SerializerEncoding.JSON);
        assertNotNull(rabbitDeserialized);
        assertNotNull(rabbitDeserialized.meals());
        assertEquals(rabbitDeserialized.meals().size(), 2);
    }


    /**
     * Validates that decoding and encoding of a type with flattenable property and type id containing dot and can be
     * done. For decoding and encoding base type will be used.
     */
    @Test
    public void canHandleTypeWithFlattenablePropertyAndTypeIdContainingDot0() throws IOException {
        AnimalWithTypeIdContainingDot animalToSerialize = new DogWithTypeIdContainingDot().withBreed("AKITA").withCuteLevel(10);

        // serialization
        String serialized = ADAPTER.serialize(animalToSerialize, SerializerEncoding.JSON);
        String[] results = {
            "{\"breed\":\"AKITA\",\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"properties\":{\"cuteLevel\":10}}",
            "{\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10},\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\"}",
            "{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10}}",
            "{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"properties\":{\"cuteLevel\":10},\"breed\":\"AKITA\"}",
            "{\"properties\":{\"cuteLevel\":10},\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\"}",
            "{\"properties\":{\"cuteLevel\":10},\"breed\":\"AKITA\",\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\"}",
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // de-serialization
        AnimalWithTypeIdContainingDot animalDeserialized = ADAPTER.deserialize(serialized,
            AnimalWithTypeIdContainingDot.class, SerializerEncoding.JSON);
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
    public void canHandleTypeWithFlattenablePropertyAndTypeIdContainingDot1() throws IOException {
        DogWithTypeIdContainingDot dogToSerialize = new DogWithTypeIdContainingDot().withBreed("AKITA").withCuteLevel(10);

        // serialization
        String serialized = ADAPTER.serialize(dogToSerialize, SerializerEncoding.JSON);
        String[] results = {
            "{\"breed\":\"AKITA\",\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"properties\":{\"cuteLevel\":10}}",
            "{\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10},\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\"}",
            "{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10}}",
            "{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"properties\":{\"cuteLevel\":10},\"breed\":\"AKITA\"}",
            "{\"properties\":{\"cuteLevel\":10},\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\"}",
            "{\"properties\":{\"cuteLevel\":10},\"breed\":\"AKITA\",\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\"}",
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // de-serialization
        DogWithTypeIdContainingDot dogDeserialized = ADAPTER.deserialize(serialized, DogWithTypeIdContainingDot.class,
            SerializerEncoding.JSON);
        assertNotNull(dogDeserialized);
        assertEquals(dogDeserialized.breed(), "AKITA");
        assertEquals(dogDeserialized.cuteLevel(), (Integer) 10);
    }

    /**
     * Validates that decoding and encoding of a array of type with type id containing dot and can be done. For decoding
     * and encoding base type will be used.
     */
    @Test
    public void canHandleArrayOfTypeWithTypeIdContainingDot0() throws IOException {
        // Serialize
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        //
        AnimalWithTypeIdContainingDot animalToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        List<AnimalWithTypeIdContainingDot> animalsToSerialize = new ArrayList<>();
        animalsToSerialize.add(animalToSerialize);
        String serialized = ADAPTER.serialize(animalsToSerialize, SerializerEncoding.JSON);
        String[] results = {
            "[{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}]",
            "[{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}]",
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // De-serialize
        //
        List<AnimalWithTypeIdContainingDot> animalsDeserialized = ADAPTER.deserialize(serialized,
            TypeUtil.createParameterizedType(List.class, AnimalWithTypeIdContainingDot.class), SerializerEncoding.JSON);
        assertNotNull(animalsDeserialized);
        assertEquals(1, animalsDeserialized.size());
        AnimalWithTypeIdContainingDot animalDeserialized = animalsDeserialized.get(0);
        assertTrue(animalDeserialized instanceof RabbitWithTypeIdContainingDot);
        RabbitWithTypeIdContainingDot rabbitDeserialized = (RabbitWithTypeIdContainingDot) animalDeserialized;
        assertNotNull(rabbitDeserialized.meals());
        assertEquals(rabbitDeserialized.meals().size(), 2);
    }

    /**
     * Validates that decoding and encoding of a array of type with type id containing dot and can be done. For decoding
     * and encoding concrete type will be used.
     */
    @Test
    public void canHandleArrayOfTypeWithTypeIdContainingDot1() throws IOException {
        // Serialize
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        //
        RabbitWithTypeIdContainingDot rabbitToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        List<RabbitWithTypeIdContainingDot> rabbitsToSerialize = new ArrayList<>();
        rabbitsToSerialize.add(rabbitToSerialize);
        String serialized = ADAPTER.serialize(rabbitsToSerialize, SerializerEncoding.JSON);
        String[] results = {
            "[{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}]",
            "[{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}]",
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // De-serialize
        //
        List<RabbitWithTypeIdContainingDot> rabbitsDeserialized = ADAPTER.deserialize(serialized,
            TypeUtil.createParameterizedType(List.class, RabbitWithTypeIdContainingDot.class), SerializerEncoding.JSON);
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
    public void canHandleComposedTypeWithTypeIdContainingDot0() throws IOException {
        // serialization
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        AnimalWithTypeIdContainingDot animalToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        FlattenableAnimalInfo animalInfoToSerialize = new FlattenableAnimalInfo().withAnimal(animalToSerialize);
        List<FlattenableAnimalInfo> animalsInfoSerialized = ImmutableList.of(animalInfoToSerialize);
        AnimalShelter animalShelterToSerialize = new AnimalShelter().withAnimalsInfo(animalsInfoSerialized);
        String serialized = ADAPTER.serialize(animalShelterToSerialize, SerializerEncoding.JSON);
        String[] results = {
            "{\"properties\":{\"animalsInfo\":[{\"animal\":{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}}]}}",
            "{\"properties\":{\"animalsInfo\":[{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}}]}}",
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // de-serialization
        //
        AnimalShelter shelterDeserialized = ADAPTER.deserialize(serialized, AnimalShelter.class,
            SerializerEncoding.JSON);
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
    public void canHandleComposedSpecificPolymorphicTypeWithTypeId() throws IOException {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = ADAPTER.deserialize(serializedCollectionWithTypeId,
            ComposeTurtles.class,
            SerializerEncoding.JSON);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1());
        assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        ADAPTER.serialize(composedTurtleDeserialized, SerializerEncoding.JSON);
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet1Lead\":{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}}";
        // de-serialization
        //
        composedTurtleDeserialized = ADAPTER.deserialize(serializedScalarWithTypeId, ComposeTurtles.class,
            SerializerEncoding.JSON);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1Lead());
        assertEquals(10, (long) composedTurtleDeserialized.turtlesSet1Lead().size());
        assertEquals(100, (long) composedTurtleDeserialized.turtlesSet1Lead().age());
        //
        ADAPTER.serialize(composedTurtleDeserialized, SerializerEncoding.JSON);
    }

    @Test
    public void canHandleComposedSpecificPolymorphicTypeWithoutTypeId() throws IOException {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10 },{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = ADAPTER.deserialize(serializedCollectionWithTypeId,
            ComposeTurtles.class, SerializerEncoding.JSON);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1());
        assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        ADAPTER.serialize(composedTurtleDeserialized, SerializerEncoding.JSON);
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet1Lead\":{\"age\":100,\"size\":10 }}";
        // de-serialization
        //
        composedTurtleDeserialized = ADAPTER.deserialize(serializedScalarWithTypeId, ComposeTurtles.class,
            SerializerEncoding.JSON);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1Lead());
        assertEquals(100, (long) composedTurtleDeserialized.turtlesSet1Lead().age());
        //
        ADAPTER.serialize(composedTurtleDeserialized, SerializerEncoding.JSON);
    }

    @Test
    public void canHandleComposedSpecificPolymorphicTypeWithAndWithoutTypeId() throws IOException {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = ADAPTER.deserialize(serializedCollectionWithTypeId,
            ComposeTurtles.class, SerializerEncoding.JSON);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1());
        assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        ADAPTER.serialize(composedTurtleDeserialized, SerializerEncoding.JSON);
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithTypeId() throws IOException {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = ADAPTER.deserialize(serializedCollectionWithTypeId,
            ComposeTurtles.class, SerializerEncoding.JSON);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2());
        assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        assertTrue(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        assertTrue(composedTurtleDeserialized.turtlesSet2().get(1) instanceof TurtleWithTypeIdContainingDot);
        //
        ADAPTER.serialize(composedTurtleDeserialized, SerializerEncoding.JSON);
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet2Lead\":{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}}";
        // de-serialization
        //
        composedTurtleDeserialized = ADAPTER.deserialize(serializedScalarWithTypeId, ComposeTurtles.class,
            SerializerEncoding.JSON);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2Lead());
        assertTrue(composedTurtleDeserialized.turtlesSet2Lead() instanceof TurtleWithTypeIdContainingDot);
        assertEquals(10, (long) ((TurtleWithTypeIdContainingDot) composedTurtleDeserialized.turtlesSet2Lead()).size());
        assertEquals(100, (long) composedTurtleDeserialized.turtlesSet2Lead().age());
        //
        ADAPTER.serialize(composedTurtleDeserialized, SerializerEncoding.JSON);
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithoutTypeId() throws IOException {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10 },{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = ADAPTER.deserialize(serializedCollectionWithTypeId,
            ComposeTurtles.class, SerializerEncoding.JSON);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2());
        assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        Assertions.assertFalse(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        Assertions.assertFalse(composedTurtleDeserialized.turtlesSet2().get(1) instanceof TurtleWithTypeIdContainingDot);
        //
        // -- Validate scalar property
        //
        ADAPTER.serialize(composedTurtleDeserialized, SerializerEncoding.JSON);
        //
        String serializedScalarWithTypeId = "{\"turtlesSet2Lead\":{\"age\":100,\"size\":10 }}";
        // de-serialization
        //
        composedTurtleDeserialized = ADAPTER.deserialize(serializedScalarWithTypeId, ComposeTurtles.class,
            SerializerEncoding.JSON);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2Lead());
        //
        ADAPTER.serialize(composedTurtleDeserialized, SerializerEncoding.JSON);
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithAndWithoutTypeId() throws IOException {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = ADAPTER.deserialize(serializedCollectionWithTypeId,
            ComposeTurtles.class, SerializerEncoding.JSON);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2());
        assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        assertTrue(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        assertNotNull(composedTurtleDeserialized.turtlesSet2().get(1));
        //
        ADAPTER.serialize(composedTurtleDeserialized, SerializerEncoding.JSON);
    }

    @Test
    public void canHandleEscapedProperties() throws IOException {
        FlattenedProduct productToSerialize = new FlattenedProduct();
        productToSerialize.setProductName("drink");
        productToSerialize.setProductType("chai");

        // serialization
        //
        String serialized = ADAPTER.serialize(productToSerialize, SerializerEncoding.JSON);
        String[] results = {
            "{\"properties\":{\"p.name\":\"drink\",\"type\":\"chai\"}}",
            "{\"properties\":{\"type\":\"chai\",\"p.name\":\"drink\"}}",
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // de-serialization
        //
        FlattenedProduct productDeserialized = ADAPTER.deserialize(serialized, FlattenedProduct.class,
            SerializerEncoding.JSON);
        assertNotNull(productDeserialized);
        assertEquals(productDeserialized.getProductName(), "drink");
        assertEquals(productDeserialized.productType, "chai");
    }

    @Test
    public void canHandleSinglePropertyBeingFlattened() throws IOException {
        ClassWithFlattenedProperties classWithFlattenedProperties = new ClassWithFlattenedProperties("random", "E24JJxztP");

        String serialized = ADAPTER.serialize(classWithFlattenedProperties, SerializerEncoding.JSON);
        String[] results = {
            "{\"@odata\":{\"type\":\"random\"},\"@odata.etag\":\"E24JJxztP\"}",
            "{\"@odata.etag\":\"E24JJxztP\",\"@odata\":{\"type\":\"random\"}}"
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        ClassWithFlattenedProperties deserialized = ADAPTER.deserialize(serialized, ClassWithFlattenedProperties.class,
            SerializerEncoding.JSON);
        assertNotNull(deserialized);
        assertEquals(classWithFlattenedProperties.getOdataType(), deserialized.getOdataType());
        assertEquals(classWithFlattenedProperties.getOdataETag(), deserialized.getOdataETag());
    }

    @SuppressWarnings({"unused", "UnusedReturnValue", "FieldCanBeLocal"})
    @JsonFlatten
    private class School {
        @JsonProperty(value = "teacher")
        private Teacher teacher;

        @JsonProperty(value = "properties.name")
        private String name;

        @JsonProperty(value = "tags")
        private Map<String, String> tags;

        public School setTeacher(Teacher teacher) {
            this.teacher = teacher;
            return this;
        }

        public School setName(String name) {
            this.name = name;
            return this;
        }

        public School setTags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    private class Student {

    }

    @SuppressWarnings({"unused", "UnusedReturnValue", "FieldCanBeLocal"})
    private class Teacher {
        @JsonProperty(value = "students")
        private Map<String, Student> students;

        public Teacher setStudents(Map<String, Student> students) {
            this.students = students;
            return this;
        }
    }

    private School prepareSchoolModel() {
        Teacher teacher = new Teacher();

        Map<String, Student> students = new HashMap<>();
        students.put("af.B/C", new Student());
        students.put("af.B/D", new Student());

        teacher.setStudents(students);

        School school = new School().setName("school1");
        school.setTeacher(teacher);

        Map<String, String> schoolTags = new HashMap<>();
        schoolTags.put("foo.aa", "bar");
        schoolTags.put("x.y", "zz");

        school.setTags(schoolTags);

        return school;
    }

    @SuppressWarnings({"unused", "UnusedReturnValue", "FieldCanBeLocal"})
    @JsonFlatten
    public static class FlattenedProduct {
        // Flattened and escaped property
        @JsonProperty(value = "properties.p\\.name")
        private String productName;

        @JsonProperty(value = "properties.type")
        private String productType;

        public String getProductName() {
            return this.productName;
        }

        public FlattenedProduct setProductName(String productName) {
            this.productName = productName;
            return this;
        }

        public String getProductType() {
            return this.productType;
        }

        public FlattenedProduct setProductType(String productType) {
            this.productType = productType;
            return this;
        }
    }

    @SuppressWarnings({"unused", "UnusedReturnValue", "FieldCanBeLocal", "FieldMayBeFinal"})
    public static final class ClassWithFlattenedProperties {
        @JsonFlatten
        @JsonProperty(value = "@odata.type")
        private String odataType;

        @JsonProperty(value = "@odata.etag")
        private String odataETag;

        @JsonCreator
        public ClassWithFlattenedProperties(@JsonProperty(value = "@odata.type") String odataType,
            @JsonProperty(value = "@odata.etag") String odataETag) {
            this.odataType = odataType;
            this.odataETag = odataETag;
        }

        public String getOdataType() {
            return odataType;
        }

        public String getOdataETag() {
            return odataETag;
        }
    }
}

