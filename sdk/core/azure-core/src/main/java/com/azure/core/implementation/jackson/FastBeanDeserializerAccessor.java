// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableAnyProperty;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.util.ClassUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class FastBeanDeserializerAccessor extends BeanDeserializerModifier {
    private static final Map<Member, SettableBeanProperty> FAST_ACCESSOR_CACHE = new ConcurrentHashMap<>();

    @Override
    public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc,
        BeanDeserializerBuilder builder) {
        final Class<?> beanClass = beanDesc.getBeanClass();

        final MethodHandles.Lookup lookup = ObjectMapperFactory.getLookupFor(beanClass);
        if (lookup == null) {
            return builder;
        }

        // Check if the lookup being used can access private Classes and Members.
        boolean lookupCanAccessPrivate = (lookup.lookupModes() & MethodHandles.Lookup.PRIVATE) != 0;
        if (!lookupCanAccessPrivate && Modifier.isPrivate(beanClass.getModifiers())) {
            return builder;
        }

        List<SettableBeanProperty> optimizedSetters = createOptimizedSetters(builder.getProperties(), lookup);
        if (!optimizedSetters.isEmpty()) {
            for (SettableBeanProperty prop : optimizedSetters) {
                builder.addOrReplaceProperty(prop, true);
            }
        }

        SettableAnyProperty anySetter = builder.getAnySetter();
        if (anySetter != null) {
            //config.get
            // builder.setAnySetter(createOptimizedAnySetter(anySetter, lookup));
        }

        ValueInstantiator valueInstantiator = builder.getValueInstantiator();
        if (valueInstantiator.getClass() == StdValueInstantiator.class) {
            // also, only override if using default creator (no-arg ctor, no-arg static factory)
            if (valueInstantiator.canCreateUsingDefault() || valueInstantiator.canCreateFromObjectWith()) {
                ValueInstantiator optimizedInstantiator =
                    createOptimizedConstructor((StdValueInstantiator) valueInstantiator, lookup);
                if (optimizedInstantiator != null) {
                    builder.setValueInstantiator(optimizedInstantiator);
                }
            }
        }

        return super.updateBuilder(config, beanDesc, builder);
    }

    private static List<SettableBeanProperty> createOptimizedSetters(Iterator<SettableBeanProperty> currentSetters,
        MethodHandles.Lookup lookup) {
        List<SettableBeanProperty> optimizedSetters = new ArrayList<>();

        while (currentSetters.hasNext()) {
            SettableBeanProperty currentSetter = currentSetters.next();

            AnnotatedMember member = currentSetter.getMember();
            Member jdkMember = member.getMember();

            if (jdkMember == null) {
                continue;
            }

            if (currentSetter.hasValueDeserializer()) {
                if (!ClassUtil.isJacksonStdImpl(currentSetter.getValueDeserializer())) {
                    continue;
                }
            }

            optimizedSetters.add(FAST_ACCESSOR_CACHE.computeIfAbsent(jdkMember, m ->
                createOptimizedSetter(currentSetter, lookup, member)));
        }

        return optimizedSetters;
    }

    private static SettableBeanProperty createOptimizedSetter(SettableBeanProperty currentSetter,
        MethodHandles.Lookup lookup, AnnotatedMember member) {
        try {
            final MethodHandle setter;
            BiFunction<Object, Object, Object> optimizedSetter;
            if (member instanceof AnnotatedMethod) {
                setter = lookup.unreflect((Method) member.getMember());
                optimizedSetter = (instance, value) -> {
                    try {
                        return setter.invokeExact(instance, value);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                };
            } else {
                setter = lookup.unreflectSetter((Field) member.getMember());
                optimizedSetter = (instance, value) -> {
                    try {
                        return setter.invokeWithArguments(instance, value);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            return new FastBeanPropertyReader(member.getRawType(), optimizedSetter, currentSetter);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private static SettableAnyProperty createOptimizedAnySetter(SettableAnyProperty currentAnySetter,
        MethodHandles.Lookup lookup) {
        return currentAnySetter;
    }

    private static ValueInstantiator createOptimizedConstructor(StdValueInstantiator currentConstructor,
        MethodHandles.Lookup lookup) {
        if (currentConstructor.canCreateUsingDelegate()) {
            return null;
        }

        // Attempt to optimize the args constructor.
        Function<Object[], Object> optimizedArgsCreator = null;
        AnnotatedWithParams argsCreator = currentConstructor.getWithArgsCreator();
        if (argsCreator != null) {
            MethodHandle argsCreatorHandle = getCreatorHandle(argsCreator.getAnnotated(), lookup);
            if (argsCreatorHandle != null) {
                optimizedArgsCreator = args -> {
                    try {
                        return argsCreatorHandle.invokeWithArguments(args);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                };
            }
        }

        // Attempt to optimize the default constructor.
        Supplier<?> optimizedFactory = null;
        AnnotatedWithParams defaultCreator = currentConstructor.getDefaultCreator();
        if (defaultCreator != null) {
            MethodHandle defaultCreatorHandle = getCreatorHandle(defaultCreator.getAnnotated(), lookup);
            if (defaultCreatorHandle != null) {
                optimizedFactory = () -> {
                    try {
                        return defaultCreatorHandle.invoke();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                };
            }
        }
        if (optimizedArgsCreator != null || optimizedFactory != null) {
            return new FastBeanConstructor(currentConstructor, optimizedFactory, optimizedArgsCreator);
        }
        return null;
    }

    private static MethodHandle getCreatorHandle(AnnotatedElement jsonCreatorElement, MethodHandles.Lookup lookup) {
        // JsonCreator can go on either a constructor or static method.
        try {
            if (jsonCreatorElement instanceof Constructor) {
                return lookup.unreflectConstructor((Constructor<?>) jsonCreatorElement);
            } else if (jsonCreatorElement instanceof Method) {
                Method jsonCreatorMethod = (Method) jsonCreatorElement;
                if (Modifier.isStatic(jsonCreatorMethod.getModifiers())) {
                    return lookup.unreflect(jsonCreatorMethod);
                }
            }

            return null;
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
