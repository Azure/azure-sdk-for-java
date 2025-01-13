// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.union;

// This is an example of a Model class that uses the Union type to allow for multiple types to be stored in a single
// property. This is useful when you have a property that can be one of a few types, but you want to ensure that the
// types are known at compile time, and that you can easily switch on the type of the value.
public class ModelType {
    private Union prop1 = Union.ofTypes(String.class, Integer.class, Double.class);

    @UnionTypes({String.class, Integer.class, Double.class})
    public Union getProp1() {
        return prop1;
    }

    // In this case, because all three values of the Union type are distinct, we can have three setter methods to
    // modify the Union in a type-safe way. If the types were not distinct, we would need to use a single setter method
    // that took an Object type, and then rely on the Union type to ensure that the value was of the correct type.
    // This would be the case (as we see in GenericModelType) where there are multiple types of the same class, such as
    // List<String>, List<Integer>, and List<Float>.
    public void setProp1(String str) {
        prop1.setValue(str);
    }
    public void setProp1(Integer integer) {
        prop1.setValue(integer);
    }
    public void setProp1(Double dbl) {
        prop1.setValue(dbl);
    }

    public static void main(String[] args) {
        ModelType modelType = new ModelType();
        modelType.setProp1(23);

        // we can just call the getValue(Class<T> cls) method to get the value as the type we expect it to be
        System.out.println(modelType.getProp1().getValue(Integer.class));

        // or we can use the tryConsume method
        modelType.getProp1().tryConsume(v -> System.out.println("Value from lambda: " + v), Integer.class);

        // or we can write a switch expression
        switch (modelType.getProp1().getValue()) {
            case String s -> System.out.println("String value from switch: " + s);
            case Integer i -> System.out.println("Integer value from switch: " + i);
            case Double d -> System.out.println("Double value from switch: " + d);
            default -> throw new IllegalArgumentException("Unknown type: " + modelType.getProp1().getType().getTypeName());
        }
    }
}
