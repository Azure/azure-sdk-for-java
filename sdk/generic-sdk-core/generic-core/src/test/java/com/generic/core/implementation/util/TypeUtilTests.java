// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.util;

import com.generic.core.implementation.TypeUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeUtilTests {

    @Test
    public void testGetClasses() {
        Puppy puppy = new Puppy();
        List<Class<?>> classes = TypeUtil.getAllClasses(puppy.getClass());

        assertEquals(4, classes.size());
        assertTrue(classes.contains(Puppy.class));
        assertTrue(classes.contains(Dog.class));
        assertTrue(classes.contains(Pet.class));
        assertTrue(classes.contains(Object.class));
    }

    @Test
    public void testGetTypeArguments() {
        Type[] puppyArgs = TypeUtil.getTypeArguments(Puppy.class);
        Type[] dogArgs = TypeUtil.getTypeArguments(Puppy.class.getGenericSuperclass());
        Type[] petArgs = TypeUtil.getTypeArguments(Dog.class.getGenericSuperclass());

        assertEquals(0, puppyArgs.length);
        assertEquals(1, dogArgs.length);
        assertEquals(2, petArgs.length);
    }

    @Test
    public void testGetTypeArgument() {
        Type dogArgs = TypeUtil.getTypeArgument(Puppy.class.getGenericSuperclass());

        assertEquals(Kid.class, dogArgs);
    }

    @Test
    public void testGetRawClass() {
        Type petType = Puppy.class.getSuperclass().getGenericSuperclass();

        assertEquals(Pet.class, TypeUtil.getRawClass(petType));
    }

    @Test
    public void testGetSuperType() {
        Type dogType = TypeUtil.getSuperType(Puppy.class);
        Type petType = TypeUtil.getSuperType(dogType);
        Type[] arguments = TypeUtil.getTypeArguments(petType);

        assertEquals(2, arguments.length);
        assertEquals(Kid.class, arguments[0]);
        assertEquals(String.class, arguments[1]);
    }

    @Test
    public void testGetTopSuperType() {
        Type petType = TypeUtil.getSuperType(Puppy.class, Pet.class);
        Type[] arguments = TypeUtil.getTypeArguments(petType);

        assertEquals(2, arguments.length);
        assertEquals(Kid.class, arguments[0]);
        assertEquals(String.class, arguments[1]);
    }

    @Test
    public void testIsTypeOrSubTypeOf() {
        Type dogType = TypeUtil.getSuperType(Puppy.class);
        Type petType = TypeUtil.getSuperType(dogType);

        assertTrue(TypeUtil.isTypeOrSubTypeOf(Puppy.class, dogType));
        assertTrue(TypeUtil.isTypeOrSubTypeOf(Puppy.class, Puppy.class));
        assertTrue(TypeUtil.isTypeOrSubTypeOf(Puppy.class, petType));
        assertTrue(TypeUtil.isTypeOrSubTypeOf(dogType, petType));
        assertTrue(TypeUtil.isTypeOrSubTypeOf(dogType, dogType));
        assertTrue(TypeUtil.isTypeOrSubTypeOf(petType, petType));
    }

    @Test
    public void testCreateParameterizedType() {
        Type dogType = TypeUtil.getSuperType(Puppy.class);
        Type petType = TypeUtil.getSuperType(dogType);
        Type createdType = TypeUtil.createParameterizedType(Pet.class, Kid.class, String.class);

        assertEquals(TypeUtil.getRawClass(petType), TypeUtil.getRawClass(createdType));
        assertArrayEquals(TypeUtil.getTypeArguments(petType), TypeUtil.getTypeArguments(createdType));
    }

    private abstract static class Pet<T extends Human, V> {
        abstract T owner();
    }

    private static class Human {
    }

    private static class Kid extends Human {
    }

    private static class Dog<T extends Human> extends Pet<T, String> {
        private T owner;

        @Override
        public T owner() {
            return owner;
        }
    }

    private static class Puppy extends Dog<Kid> {
    }
}
