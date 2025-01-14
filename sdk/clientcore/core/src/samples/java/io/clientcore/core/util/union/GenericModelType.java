// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.union;

import io.clientcore.core.util.ParameterizedTypeImpl;
import io.clientcore.core.util.Union;

import java.util.List;

// This is an example of a Model class that uses the Union type to allow for multiple types to be stored in a single
// property, with the additional complexity that the types are all generic types (in this case, List<String>, List<Integer>,
// and List<Float>).
public class GenericModelType {
    // We specify that the Union type can be one of three types: List<String>, List<Integer>, or List<Float>.
    private Union prop1 = Union.ofTypes(
        new ParameterizedTypeImpl(List.class, String.class),
        new ParameterizedTypeImpl(List.class, Integer.class),
        new ParameterizedTypeImpl(List.class, Float.class));

    // we give access to the Union type, so that the value can be modified and retrieved.
    public Union getProp1() {
        return prop1;
    }

    // but our setter methods need to have more complex names, to differentiate them at runtime.
    public void setProp1AsStrings(List<String> strValues) {
        prop1.setValue(strValues);
    }
    public void setProp1AsIntegers(List<Integer> intValues) {
        prop1.setValue(intValues);
    }
    public void setProp1AsFloats(List<Float> floatValues) {
        prop1.setValue(floatValues);
    }

    public static void main(String[] args) {
        GenericModelType model = new GenericModelType();
        model.setProp1AsStrings(List.of("Hello", "World"));

        // in this case, it isn't possible to switch over the values easily (as we could in the ModelType class), as the
        // types are all List types (and we would need to inspect the values inside the list to be sure). Instead, we
        // can use the tryConsume method to consume the value if it is of the expected type.
        model.getProp1().tryConsume(strings -> System.out.println("Strings: " + strings), List.class, String.class);

        model.setProp1AsIntegers(List.of(1, 2, 3));
        model.getProp1().tryConsume(integers -> System.out.println("Integers: " + integers), List.class, Integer.class);

        model.setProp1AsFloats(List.of(1.0f, 2.0f, 3.0f));
        model.getProp1().tryConsume(floats -> System.out.println("Floats: " + floats), List.class, Float.class);
    }
}
