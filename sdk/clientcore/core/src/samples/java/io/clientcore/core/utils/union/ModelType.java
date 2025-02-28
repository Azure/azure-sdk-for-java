// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils.union;

import io.clientcore.core.utils.Union;

// This is an example of a Model class that uses the Union type to allow for multiple types to be stored in a single
// property. This is useful when you have a property that can be one of a few types, but you want to ensure that the
// types are known at compile time, and that you can easily switch on the type of the value.
public class ModelType {
    private Union prop = Union.ofTypes(String.class, Integer.class, Double.class);

    public Union getProp() {
        return prop;
    }

    // In this case, because all three values of the Union type are distinct, we can have three setter methods to
    // modify the Union in a type-safe way. If the types were not distinct, we would need to use a single setter method
    // that took an Object type, and then rely on the Union type to ensure that the value was of the correct type.
    // This would be the case (as we see in GenericModelType) where there are multiple types of the same class, such as
    // List<String>, List<Integer>, and List<Float>.
    public ModelType setProp(String str) {
        prop.setValue(str);
        return this;
    }
    public ModelType setProp(Integer integer) {
        prop.setValue(integer);
        return this;
    }
    public ModelType setProp(Double dbl) {
        prop.setValue(dbl);
        return this;
    }

    public static void main(String[] args) {
        ModelType modelType = new ModelType();
        modelType.setProp(23);

        // we can just call the getValue(Class<T> cls) method to get the value as the type we expect it to be
        System.out.println(modelType.getProp().getValue(Integer.class));

        // or we can use the tryConsume method
        modelType.getProp().tryConsume(v -> System.out.println("Value from lambda: " + v), Integer.class);

        // or we can write an if-else block to consume the value in Java 8+, or switch pattern match in Java 17+
        Object value = modelType.getProp().getValue();
        if (value instanceof String) {
            String s = (String) value;
            System.out.println("String value from if-else: " + s);
        } else if (value instanceof Integer) {
            Integer i = (Integer) value;
            System.out.println("Integer value from if-else: " + i);
        } else if (value instanceof Double) {
            Double d = (Double) value;
            System.out.println("Double value from if-else: " + d);
        } else {
            throw new IllegalArgumentException("Unknown type: " + modelType.getProp().getCurrentType().getTypeName());
        }
    }
}
