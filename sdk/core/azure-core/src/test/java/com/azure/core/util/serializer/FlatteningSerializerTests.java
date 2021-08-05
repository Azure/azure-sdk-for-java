// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;


import com.azure.core.implementation.TypeUtil;
import com.azure.core.implementation.models.jsonflatten.ClassWithFlattenedProperties;
import com.azure.core.implementation.models.jsonflatten.FlattenedProduct;
import com.azure.core.implementation.models.jsonflatten.FlattenedPropertiesAndJsonAnyGetter;
import com.azure.core.implementation.models.jsonflatten.JsonFlattenNestedInner;
import com.azure.core.implementation.models.jsonflatten.JsonFlattenOnArrayType;
import com.azure.core.implementation.models.jsonflatten.JsonFlattenOnCollectionType;
import com.azure.core.implementation.models.jsonflatten.JsonFlattenOnJsonIgnoredProperty;
import com.azure.core.implementation.models.jsonflatten.JsonFlattenOnPrimitiveType;
import com.azure.core.implementation.models.jsonflatten.JsonFlattenWithJsonInfoDiscriminator;
import com.azure.core.implementation.models.jsonflatten.School;
import com.azure.core.implementation.models.jsonflatten.Student;
import com.azure.core.implementation.models.jsonflatten.Teacher;
import com.azure.core.implementation.models.jsonflatten.VirtualMachineIdentity;
import com.azure.core.implementation.models.jsonflatten.VirtualMachineScaleSet;
import com.azure.core.implementation.models.jsonflatten.VirtualMachineScaleSetNetworkConfiguration;
import com.azure.core.implementation.models.jsonflatten.VirtualMachineScaleSetNetworkProfile;
import com.azure.core.implementation.models.jsonflatten.VirtualMachineScaleSetVMProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import wiremock.com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FlatteningSerializerTests {
    private static final JacksonAdapter ADAPTER = new JacksonAdapter();

    @Test
    public void canFlatten() {
        Foo foo = new Foo();
        foo.bar("hello.world");
        //
        List<String> baz = Arrays.asList("hello", "hello.world");
        foo.baz(baz);

        HashMap<String, String> qux = new HashMap<>();
        qux.put("hello", "world");
        qux.put("a.b", "c.d");
        qux.put("bar.a", "ttyy");
        qux.put("bar.b", "uuzz");
        foo.qux(qux);

        // serialization
        String serialized = serialize(foo);
        assertEquals("{\"$type\":\"foo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}}}", serialized);

        // deserialization
        Foo deserialized = deserialize(serialized, Foo.class);
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
        String serialized = serialize(prepareSchoolModel());
        assertEquals("{\"teacher\":{\"students\":{\"af.B/D\":{},\"af.B/C\":{}}},\"tags\":{\"foo.aa\":\"bar\",\"x.y\":\"zz\"},\"properties\":{\"name\":\"school1\"}}", serialized);
    }

    /**
     * Validates decoding and encoding of a type with type id containing dot and no additional properties For decoding
     * and encoding base type will be used.
     */
    @Test
    public void canHandleTypeWithTypeIdContainingDotAndNoProperties() {
        String rabbitSerialized = "{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}";
        String shelterSerialized = "{\"properties\":{\"animalsInfo\":[{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}},{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}}]}}";

        AnimalWithTypeIdContainingDot rabbitDeserialized = deserialize(rabbitSerialized,
            AnimalWithTypeIdContainingDot.class);
        assertTrue(rabbitDeserialized instanceof RabbitWithTypeIdContainingDot);
        assertNotNull(rabbitDeserialized);

        AnimalShelter shelterDeserialized = deserialize(shelterSerialized, AnimalShelter.class);
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
        String serialized = serialize(animalToSerialize);
        //
        String[] results = {
            "{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}",
            "{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}"
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // De-Serialize
        //
        AnimalWithTypeIdContainingDot animalDeserialized = deserialize(serialized, AnimalWithTypeIdContainingDot.class);
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
        String serialized = serialize(rabbitToSerialize);
        //
        String[] results = {
            "{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}",
            "{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}"
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // De-Serialize
        //
        RabbitWithTypeIdContainingDot rabbitDeserialized = deserialize(serialized, RabbitWithTypeIdContainingDot.class);
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
        String serialized = serialize(animalToSerialize);
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
        AnimalWithTypeIdContainingDot animalDeserialized = deserialize(serialized, AnimalWithTypeIdContainingDot.class);
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
        String serialized = serialize(dogToSerialize);
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
        DogWithTypeIdContainingDot dogDeserialized = deserialize(serialized, DogWithTypeIdContainingDot.class);
        assertNotNull(dogDeserialized);
        assertEquals(dogDeserialized.breed(), "AKITA");
        assertEquals(dogDeserialized.cuteLevel(), (Integer) 10);
    }

    /**
     * Validates that decoding and encoding of a array of type with type id containing dot and can be done. For decoding
     * and encoding base type will be used.
     */
    @Test
    public void canHandleArrayOfTypeWithTypeIdContainingDot0() {
        List<String> meals = Arrays.asList("carrot", "apple");
        //
        AnimalWithTypeIdContainingDot animalToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        List<AnimalWithTypeIdContainingDot> animalsToSerialize = new ArrayList<>();
        animalsToSerialize.add(animalToSerialize);
        String serialized = serialize(animalsToSerialize);
        String[] results = {
            "[{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}]",
            "[{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}]",
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // De-serialize
        //
        List<AnimalWithTypeIdContainingDot> animalsDeserialized = deserialize(serialized,
            TypeUtil.createParameterizedType(List.class, AnimalWithTypeIdContainingDot.class));
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
    public void canHandleArrayOfTypeWithTypeIdContainingDot1() {
        List<String> meals = Arrays.asList("carrot", "apple");
        //
        RabbitWithTypeIdContainingDot rabbitToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        List<RabbitWithTypeIdContainingDot> rabbitsToSerialize = new ArrayList<>();
        rabbitsToSerialize.add(rabbitToSerialize);
        String serialized = serialize(rabbitsToSerialize);
        String[] results = {
            "[{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}]",
            "[{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}]",
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // De-serialize
        //
        List<RabbitWithTypeIdContainingDot> rabbitsDeserialized = deserialize(serialized,
            TypeUtil.createParameterizedType(List.class, RabbitWithTypeIdContainingDot.class));
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
        String serialized = serialize(animalShelterToSerialize);
        String[] results = {
            "{\"properties\":{\"animalsInfo\":[{\"animal\":{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}}]}}",
            "{\"properties\":{\"animalsInfo\":[{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}}]}}",
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // de-serialization
        //
        AnimalShelter shelterDeserialized = deserialize(serialized, AnimalShelter.class);
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
        ComposeTurtles composedTurtleDeserialized = deserialize(serializedCollectionWithTypeId, ComposeTurtles.class);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1());
        assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        serialize(composedTurtleDeserialized);
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet1Lead\":{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}}";
        // de-serialization
        //
        composedTurtleDeserialized = deserialize(serializedScalarWithTypeId, ComposeTurtles.class);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1Lead());
        assertEquals(10, (long) composedTurtleDeserialized.turtlesSet1Lead().size());
        assertEquals(100, (long) composedTurtleDeserialized.turtlesSet1Lead().age());
        //
        serialize(composedTurtleDeserialized);
    }

    @Test
    public void canHandleComposedSpecificPolymorphicTypeWithoutTypeId() {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10 },{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = deserialize(serializedCollectionWithTypeId, ComposeTurtles.class);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1());
        assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        serialize(composedTurtleDeserialized);
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet1Lead\":{\"age\":100,\"size\":10 }}";
        // de-serialization
        //
        composedTurtleDeserialized = deserialize(serializedScalarWithTypeId, ComposeTurtles.class);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1Lead());
        assertEquals(100, (long) composedTurtleDeserialized.turtlesSet1Lead().age());
        //
        serialize(composedTurtleDeserialized);
    }

    @Test
    public void canHandleComposedSpecificPolymorphicTypeWithAndWithoutTypeId() {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = deserialize(serializedCollectionWithTypeId, ComposeTurtles.class);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet1());
        assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        serialize(composedTurtleDeserialized);
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithTypeId() {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = deserialize(serializedCollectionWithTypeId, ComposeTurtles.class);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2());
        assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        assertTrue(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        assertTrue(composedTurtleDeserialized.turtlesSet2().get(1) instanceof TurtleWithTypeIdContainingDot);
        //
        serialize(composedTurtleDeserialized);
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet2Lead\":{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}}";
        // de-serialization
        //
        composedTurtleDeserialized = deserialize(serializedScalarWithTypeId, ComposeTurtles.class);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2Lead());
        assertTrue(composedTurtleDeserialized.turtlesSet2Lead() instanceof TurtleWithTypeIdContainingDot);
        assertEquals(10, (long) ((TurtleWithTypeIdContainingDot) composedTurtleDeserialized.turtlesSet2Lead()).size());
        assertEquals(100, (long) composedTurtleDeserialized.turtlesSet2Lead().age());
        //
        serialize(composedTurtleDeserialized);
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithoutTypeId() {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10 },{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = deserialize(serializedCollectionWithTypeId, ComposeTurtles.class);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2());
        assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        Assertions.assertFalse(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        Assertions.assertFalse(composedTurtleDeserialized.turtlesSet2().get(1) instanceof TurtleWithTypeIdContainingDot);
        //
        // -- Validate scalar property
        //
        serialize(composedTurtleDeserialized);
        //
        String serializedScalarWithTypeId = "{\"turtlesSet2Lead\":{\"age\":100,\"size\":10 }}";
        // de-serialization
        //
        composedTurtleDeserialized = deserialize(serializedScalarWithTypeId, ComposeTurtles.class);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2Lead());
        //
        serialize(composedTurtleDeserialized);
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithAndWithoutTypeId() {
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = deserialize(serializedCollectionWithTypeId, ComposeTurtles.class);
        assertNotNull(composedTurtleDeserialized);
        assertNotNull(composedTurtleDeserialized.turtlesSet2());
        assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        assertTrue(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        assertNotNull(composedTurtleDeserialized.turtlesSet2().get(1));
        //
        serialize(composedTurtleDeserialized);
    }

    @Test
    public void canHandleEscapedProperties() {
        FlattenedProduct productToSerialize = new FlattenedProduct()
            .setProductName("drink")
            .setProductType("chai");

        // serialization
        //
        String serialized = serialize(productToSerialize);
        String[] results = {
            "{\"properties\":{\"p.name\":\"drink\",\"type\":\"chai\"}}",
            "{\"properties\":{\"type\":\"chai\",\"p.name\":\"drink\"}}",
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        // de-serialization
        //
        FlattenedProduct productDeserialized = deserialize(serialized, FlattenedProduct.class);
        assertNotNull(productDeserialized);
        assertEquals(productDeserialized.getProductName(), "drink");
        assertEquals(productDeserialized.getProductType(), "chai");
    }

    @Test
    public void canHandleSinglePropertyBeingFlattened() {
        ClassWithFlattenedProperties classWithFlattenedProperties = new ClassWithFlattenedProperties("random", "E24JJxztP");

        String serialized = serialize(classWithFlattenedProperties);
        String[] results = {
            "{\"@odata\":{\"type\":\"random\"},\"@odata.etag\":\"E24JJxztP\"}",
            "{\"@odata.etag\":\"E24JJxztP\",\"@odata\":{\"type\":\"random\"}}"
        };

        assertTrue(Arrays.asList(results).contains(serialized));

        ClassWithFlattenedProperties deserialized = deserialize(serialized, ClassWithFlattenedProperties.class);
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

        String serialized = serialize(virtualMachineScaleSet);
        String expected = "{\"properties\":{\"virtualMachineProfile\":{\"networkProfile\":{\"networkInterfaceConfigurations\":[{\"name\":\"name\",\"properties\":{\"primary\":true}}]}}}}";
        assertEquals(expected, serialized);

        VirtualMachineScaleSet deserialized = deserialize(serialized, VirtualMachineScaleSet.class);
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
        String actualSerialization = serialize(expected);

        assertEquals(expectedSerialization, actualSerialization);

        JsonFlattenOnArrayType deserialized = deserialize(actualSerialization, JsonFlattenOnArrayType.class);
        assertArrayEquals(expected.getJsonFlattenArray(), deserialized.getJsonFlattenArray());
    }

    @Test
    public void jsonFlattenOnCollectionTypeList() {
        final List<String> listCollection = Arrays.asList("hello", "goodbye", null);
        JsonFlattenOnCollectionType expected = new JsonFlattenOnCollectionType()
            .setJsonFlattenCollection(Collections.unmodifiableList(listCollection));

        String expectedSerialization = "{\"jsonflatten\":{\"collection\":[\"hello\",\"goodbye\",null]}}";
        String actualSerialization = serialize(expected);

        assertEquals(expectedSerialization, actualSerialization);

        JsonFlattenOnCollectionType deserialized = deserialize(actualSerialization, JsonFlattenOnCollectionType.class);
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
        String actualSerialization = serialize(expected);

        assertEquals(expectedSerialization, actualSerialization);

        JsonFlattenOnJsonIgnoredProperty deserialized = deserialize(actualSerialization,
            JsonFlattenOnJsonIgnoredProperty.class);
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
        String actualSerialization = serialize(expected);

        assertEquals(expectedSerialization, actualSerialization);

        JsonFlattenOnPrimitiveType deserialized = deserialize(actualSerialization, JsonFlattenOnPrimitiveType.class);
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
        String actualSerialization = serialize(expected);

        assertEquals(expectedSerialization, actualSerialization);

        JsonFlattenWithJsonInfoDiscriminator deserialized = deserialize(actualSerialization,
            JsonFlattenWithJsonInfoDiscriminator.class);
        assertEquals(expected.getJsonFlattenDiscriminator(), deserialized.getJsonFlattenDiscriminator());
    }

    @Test
    public void flattenedPropertiesAndJsonAnyGetter() {
        FlattenedPropertiesAndJsonAnyGetter expected = new FlattenedPropertiesAndJsonAnyGetter()
            .setString("string")
            .addAdditionalProperty("key1", "value1")
            .addAdditionalProperty("key2", "value2");

        String expectedSerialization = "{\"flattened\":{\"string\":\"string\"},\"key1\":\"value1\",\"key2\":\"value2\"}";
        String actualSerialization = serialize(expected);

        assertEquals(expectedSerialization, actualSerialization);

        FlattenedPropertiesAndJsonAnyGetter deserialized = deserialize(actualSerialization,
            FlattenedPropertiesAndJsonAnyGetter.class);
        assertEquals(expected.getString(), deserialized.getString());
        assertEquals(expected.additionalProperties().size(), deserialized.additionalProperties().size());
        for (String key : expected.additionalProperties().keySet()) {
            assertEquals(expected.additionalProperties().get(key), deserialized.additionalProperties().get(key));
        }
    }

    @Test
    public void jsonFlattenFinalMap() {
        final HashMap<String, String> mapProperties = new HashMap<String, String>() {{
                put("/subscriptions/0-0-0-0-0/resourcegroups/0/providers/Microsoft.ManagedIdentity/0", "value");
                }};
        School school = new School().setTags(mapProperties);

        String actualSerialization = serialize(school);
        String expectedSerialization = "{\"tags\":{\"/subscriptions/0-0-0-0-0/resourcegroups"
            + "/0/providers/Microsoft.ManagedIdentity/0\":\"value\"}}";
        Assertions.assertEquals(expectedSerialization, actualSerialization);
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
        String actualSerialization = serialize(expected);

        Assertions.assertEquals(expectedSerialization, actualSerialization);
    }

    private static String serialize(Object object) {
        try {
            return ADAPTER.serialize(object, SerializerEncoding.JSON);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static <T> T deserialize(String json, Type type) {
        try {
            return ADAPTER.deserialize(json, type, SerializerEncoding.JSON);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
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
}

