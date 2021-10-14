// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class FastBeanSerializerAccessor extends BeanSerializerModifier {
    private static final Map<Member, BeanPropertyWriter> FAST_ACCESSOR_CACHE = new ConcurrentHashMap<>();

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
        List<BeanPropertyWriter> beanProperties) {
        final Class<?> beanClass = beanDesc.getBeanClass();

        final MethodHandles.Lookup lookup = ObjectMapperFactory.getLookupFor(beanClass);
        if (lookup == null) {
            return beanProperties;
        }

        // Check if the lookup being used can access private Classes and Members.
        boolean lookupCanAccessPrivate = (lookup.lookupModes() & MethodHandles.Lookup.PRIVATE) != 0;
        if (!lookupCanAccessPrivate && Modifier.isPrivate(beanClass.getModifiers())) {
            return beanProperties;
        }

        findProperties(beanProperties, lookup);
        return beanProperties;
    }

    protected void findProperties(List<BeanPropertyWriter> beanProperties,
        MethodHandles.Lookup lookup) {

        ListIterator<BeanPropertyWriter> it = beanProperties.listIterator();
        while (it.hasNext()) {
            BeanPropertyWriter bpw = it.next();
            AnnotatedMember member = bpw.getMember();

            Member jdkMember = member.getMember();
            if (jdkMember == null) {
                return;
            }

            if (bpw.isUnwrapping()) {
                return;
            }

            if (!bpw.getClass().isAnnotationPresent(JacksonStdImpl.class)) {
                return;
            }

            it.set(FAST_ACCESSOR_CACHE.computeIfAbsent(jdkMember, jMember -> createFastAccessor(lookup, member, bpw)));


        }
    }

    private BeanPropertyWriter createFastAccessor(MethodHandles.Lookup lookup, AnnotatedMember member,
        BeanPropertyWriter bpw) {
        try {
            MethodHandle getter;
            Function<Object, Object> fastAccessor;
            if (member instanceof AnnotatedMethod) {
                getter = lookup.unreflect((Method) member.getMember());
                fastAccessor = obj -> {
                    try {
                        return getter.invokeExact(obj);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                };
            } else {
                getter = lookup.unreflectGetter((Field) member.getMember());
                fastAccessor = obj -> {
                    try {
                        return getter.invokeWithArguments(obj);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            return new FastBeanPropertyWriter(fastAccessor, bpw);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
