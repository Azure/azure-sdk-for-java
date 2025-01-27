// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.union;

import io.clientcore.core.implementation.GenericParameterizedType;
import io.clientcore.core.util.Union;

import java.util.Arrays;
import java.util.List;

// This is an example of a Model class that uses the Union type to allow for multiple types to be stored in a single
// property, with the additional complexity that the types are all generic types (in this case, List<String>, List<Integer>,
// and List<Float>).
public class GenericModelType {
    // We specify that the Union type can be one of three types: List<String>, List<Integer>, or List<Float>.
    private Union prop = Union.ofTypes(
        // GenericParameterizedType is a non-public helper class that allows us to specify a generic type with
        // a class and a type. User can define any similar class to achieve the same functionality.
        new GenericParameterizedType(List.class, String.class),
        new GenericParameterizedType(List.class, Integer.class),
        new GenericParameterizedType(List.class, Float.class));

    // we give access to the Union type, so that the value can be modified and retrieved.
    public Union getProp() {
        return prop;
    }

    // but our setter methods need to have more complex names, to differentiate them at runtime.
    public GenericModelType setPropAsStrings(List<String> strValues) {
        prop = prop.setValue(strValues);
        return this;
    }
    public GenericModelType setPropAsIntegers(List<Integer> intValues) {
        prop = prop.setValue(intValues);
        return this;
    }
    public GenericModelType setPropAsFloats(List<Float> floatValues) {
        prop = prop.setValue(floatValues);
        return this;
    }

    public static void main(String[] args) {
        GenericModelType model = new GenericModelType();
        model.setPropAsStrings(Arrays.asList("Hello", "World"));

        // in this case, it isn't possible to switch over the values easily (as we could in the ModelType class), as the
        // types are all List types (and we would need to inspect the values inside the list to be sure). Instead, we
        // can use the tryConsume method to consume the value if it is of the expected type.
        model.getProp().tryConsume(strings -> System.out.println("Strings: " + strings), List.class, String.class);

        model.setPropAsIntegers(Arrays.asList(1, 2, 3));
        model.getProp().tryConsume(integers -> System.out.println("Integers: " + integers), List.class, Integer.class);

        model.setPropAsFloats(Arrays.asList(1.0f, 2.0f, 3.0f));
        model.getProp().tryConsume(floats -> System.out.println("Floats: " + floats), List.class, Float.class);
    }
}
