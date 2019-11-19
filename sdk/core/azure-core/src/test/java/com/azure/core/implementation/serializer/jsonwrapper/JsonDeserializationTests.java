// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer.jsonwrapper;

import com.azure.core.implementation.serializer.jsonwrapper.api.Config;
import com.azure.core.implementation.serializer.jsonwrapper.api.Deserializer;
import com.azure.core.implementation.serializer.jsonwrapper.api.JsonApi;
import com.azure.core.implementation.serializer.jsonwrapper.api.Node;
import com.azure.core.implementation.serializer.jsonwrapper.api.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Some tests from the following websites:
 * - https://www.baeldung.com/gson-deserialization-guide
 */
public abstract class JsonDeserializationTests {

    protected JsonApi jsonApi;

    @Test
    public void deserializeString() {
        String json = "{ \"color\" : \"Black\", \"type\" : \"BMW\" }";
        Car car = jsonApi.readString(json, Car.class);
        Assertions.assertNotNull(car);
        Assertions.assertEquals("Black", car.getColor());
        Assertions.assertEquals("BMW", car.getType());
    }

    @Test
    public void deserializeListString() {
        String json = "[{ \"color\" : \"Black\", \"type\" : \"BMW\" }, { \"color\" : \"Red\", \"type\" : \"FIAT\" }]";
        List<Car> cars = jsonApi.readStringToList(json, new Type<List<Car>>() { });
        Assertions.assertNotNull(cars);
        Assertions.assertEquals(2, cars.size());
        Assertions.assertEquals("Black", cars.get(0).getColor());
        Assertions.assertEquals("BMW", cars.get(0).getType());
        Assertions.assertEquals("Red", cars.get(1).getColor());
        Assertions.assertEquals("FIAT", cars.get(1).getType());
    }

    @Test
    public void whenDeserializingToSimpleObjectThenCorrect() {
        String json = "{\"intValue\":1,\"stringValue\":\"one\"}";

        Foo targetObject = jsonApi.readString(json, Foo.class);

        Assertions.assertEquals(targetObject.getIntValue(), 1);
        Assertions.assertEquals(targetObject.getStringValue(), "one");
    }

    @Test
    public void whenDeserializingToGenericObjectThenCorrect() {
        String json = "{\"theValue\":1}";

        GenericFoo<Integer> targetObject = jsonApi.readString(json, new Type<GenericFoo<Integer>>() { });

        Assertions.assertEquals(targetObject.getTheValue(), new Integer(1));
    }

    @Test
    public void givenJsonHasExtraValuesWhenDeserializingThenCorrect() {
        jsonApi.configure(Config.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String json = "{\"intValue\":1,\"stringValue\":\"one\",\"extraString\":\"two\",\"extraFloat\":2.2}";
        Foo targetObject = jsonApi.readString(json, Foo.class);

        Assertions.assertEquals(targetObject.getIntValue(), 1);
        Assertions.assertEquals(targetObject.getStringValue(), "one");
    }

    @Test
    public void givenJsonHasNonMatchingFieldsWhenDeserializingWithCustomDeserializerThenCorrect() {
        String json = "{\"valueInt\":7,\"valueString\":\"seven\"}";

        jsonApi.registerCustomDeserializer(new Deserializer<Foo>(Foo.class) {
            @Override public Foo deserialize(Node node) {
                int intValue = node.get("valueInt").asInt();
                String stringValue = node.get("valueString").asString();
                return new Foo(intValue, stringValue);
            }
        });
        Foo targetObject = jsonApi.readString(json, Foo.class);

        Assertions.assertEquals(targetObject.getIntValue(), 7);
        Assertions.assertEquals(targetObject.getStringValue(), "seven");
    }

    @Test
    public void customDeserializeCar() {
        String json = "{ \"color\" : \"Black\", \"type\" : \"BMW\" }";

        jsonApi.registerCustomDeserializer(new Deserializer<Car>(Car.class) {
            @Override public Car deserialize(Node node) {
                Car car = new Car();
                Node colorNode = node.get("color");
                String color = colorNode.asString();
                car.setColor(color);
                return car;
            }
        });

        Car car = jsonApi.readString(json, Car.class);
        Assertions.assertEquals(car.getColor(), "Black");
        Assertions.assertNull(car.getType());
    }

    @Test
    public void givenJsonArrayOfFoosWhenDeserializingToArrayThenCorrect() {
        String json = "[{\"intValue\":1,\"stringValue\":\"one\"}," + "{\"intValue\":2,\"stringValue\":\"two\"}]";
        Foo[] targetArray = jsonApi.readString(json, Foo[].class);

        Assertions.assertEquals(2, targetArray.length);
        Assertions.assertEquals(new Foo(1, "one"), targetArray[0]);
        Assertions.assertEquals(new Foo(2, "two"), targetArray[1]);
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void givenJsonArrayOfFoosWhenDeserializingCollectionThenCorrect() {
        String json =
            "[{\"intValue\":1,\"stringValue\":\"one\"},{\"intValue\":2,\"stringValue\":\"two\"}]";
        Type targetClassType = new Type<List<Foo>>() { };

        List<Foo> targetList = jsonApi.readStringToList(json, targetClassType);
        Assertions.assertEquals(2, targetList.size());
        Assertions.assertEquals(new Foo(1, "one"), targetList.get(0));
        Assertions.assertEquals(new Foo(2, "two"), targetList.get(1));
    }

    @Test
    public void whenDeserializingToNestedObjectsThenCorrect() {
        String json = "{\"intValue\":1,\"stringValue\":\"one\",\"innerFoo\":{\"name\":\"inner\"}}";

        FooWithInner targetObject = jsonApi.readString(json, FooWithInner.class);

        Assertions.assertEquals(targetObject.getIntValue(), 1);
        Assertions.assertEquals(targetObject.getStringValue(), "one");
        Assertions.assertEquals(targetObject.getInnerFoo().getName(), "inner");
    }
}
