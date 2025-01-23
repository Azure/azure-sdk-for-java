package io.clientcore.core.util.union;

import io.clientcore.core.util.GenericParameterizedType;
import io.clientcore.core.util.Union;

import java.util.List;

/**
 * This is an example of a model class A that uses the Union type to allow for nested union type to be stored in a single
 * property.
 */
public class NestedUnion {
    public static void main(String[] args) {
        NestedClassB nestedClassB = new NestedClassB();
        nestedClassB.setProp2(List.of(1, 2, 3));
        System.out.println("Current Type of Nested Class B: " + nestedClassB.getProp2().getCurrentType());
        System.out.println("Value from Nested Class B: " +
            nestedClassB.getProp2().getValue(new GenericParameterizedType(List.class, Integer.class)));

        ClassA outerClassA = new ClassA();
        outerClassA.setProp1(nestedClassB);
        NestedClassB nestedClassBFromA = outerClassA.getProp1().getValue(NestedClassB.class);
        System.out.println("Current Type of Nested Class B from Class A: " + nestedClassBFromA.getProp2().getCurrentType());
        System.out.println("Value of Nested Class B from Class A: " +
            nestedClassBFromA.getProp2().getValue(new GenericParameterizedType(List.class, Integer.class)));
    }

    private static class ClassA {
        Union prop1 = Union.ofTypes(String.class, NestedClassB.class);

        public Union getProp1() {
            return prop1;
        }

        public void setProp1(String str) {
            this.prop1.setValue(str);
        }

        // Nested Class B contains a Union type property.
        public void setProp1(NestedClassB b) {
            this.prop1.setValue(b);
        }
    }

    private static class NestedClassB {
        Union prop2 = Union.ofTypes(String.class, new GenericParameterizedType(List.class, Integer.class));;

        public Union getProp2() {
            return prop2;
        }

        public void setProp2(String str) {
            this.prop2.setValue(str);
        }

        public void setProp2(List<Integer> intList) {
            this.prop2.setValue(intList);
        }
    }
}
