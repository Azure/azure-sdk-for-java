package io.clientcore.core.util.union;

import io.clientcore.core.implementation.GenericParameterizedType;
import io.clientcore.core.util.Union;

import java.util.List;

/**
 * This is an example of a model class A that uses the Union type to allow for nested union type to be stored in a single
 * property.
 */
public class NestedUnion {
    public static void main(String[] args) {
        NestedClassB nestedClassB = new NestedClassB();
        nestedClassB.setProp(List.of(1, 2, 3));
        System.out.println("Current Type of Nested Class B: " + nestedClassB.getProp().getCurrentType());
        System.out.println("Value from Nested Class B: " +
            nestedClassB.getProp().getValue(new GenericParameterizedType(List.class, Integer.class)));

        ClassA outerClassA = new ClassA();
        outerClassA.setProp(nestedClassB);
        NestedClassB nestedClassBFromA = outerClassA.getProp().getValue(NestedClassB.class);
        System.out.println("Current Type of Nested Class B from Class A: " + nestedClassBFromA.getProp().getCurrentType());
        System.out.println("Value of Nested Class B from Class A: " +
            nestedClassBFromA.getProp().getValue(new GenericParameterizedType(List.class, Integer.class)));
    }

    private static class ClassA {
        Union prop = Union.ofTypes(String.class, NestedClassB.class);

        public Union getProp() {
            return prop;
        }

        public ClassA setProp(String str) {
            prop = prop.setValue(str);
            return this;
        }

        // Nested Class B contains a Union type property.
        public ClassA setProp(NestedClassB b) {
            prop = prop.setValue(b);
            return this;
        }
    }

    private static class NestedClassB {
        Union prop = Union.ofTypes(String.class, new GenericParameterizedType(List.class, Integer.class));

        public Union getProp() {
            return prop;
        }

        public NestedClassB setProp(String str) {
            prop = prop.setValue(str);
            return this;
        }

        public NestedClassB setProp(List<Integer> intList) {
            prop = prop.setValue(intList);
            return this;
        }
    }
}
