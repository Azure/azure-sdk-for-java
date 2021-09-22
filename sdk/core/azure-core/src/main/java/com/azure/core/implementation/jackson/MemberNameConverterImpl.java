// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.MemberNameConverter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.util.BeanUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 *  Retrieves the JSON serialized property name from {@link Member}.
 */
final class MemberNameConverterImpl implements MemberNameConverter {
    private static final ClientLogger LOGGER = new ClientLogger(MemberNameConverterImpl.class);

    private static final String ACCESSOR_NAMING_STRATEGY =
        "com.fasterxml.jackson.databind.introspect.AccessorNamingStrategy";
    private static final String ACCESSOR_NAMING_STRATEGY_PROVIDER = ACCESSOR_NAMING_STRATEGY + ".Provider";
    private static final MethodHandle GET_ACCESSOR_NAMING;
    private static final MethodHandle FOR_POJO;
    private static final MethodHandle FIND_NAME_FOR_IS_GETTER;
    private static final MethodHandle FIND_NAME_FOR_REGULAR_GETTER;
    private static final boolean USE_REFLECTION_FOR_MEMBER_NAME;

    private final ObjectMapper mapper;

    static {
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

        MethodHandle getAccessorNaming = null;
        MethodHandle forPojo = null;
        MethodHandle findNameForIsGetter = null;
        MethodHandle findNameForRegularGetter = null;
        boolean useReflectionForMemberName = false;

        try {
            Class<?> accessorNamingStrategyProviderClass = Class.forName(ACCESSOR_NAMING_STRATEGY_PROVIDER);
            Class<?> accessorNamingStrategyClass = Class.forName(ACCESSOR_NAMING_STRATEGY);
            getAccessorNaming = publicLookup.findVirtual(MapperConfig.class, "getAccessorNaming",
                MethodType.methodType(accessorNamingStrategyProviderClass));
            forPojo = publicLookup.findVirtual(accessorNamingStrategyProviderClass, "forPOJO",
                MethodType.methodType(accessorNamingStrategyClass, MapperConfig.class, AnnotatedClass.class));
            findNameForIsGetter = publicLookup.findVirtual(accessorNamingStrategyClass, "findNameForIsGetter",
                MethodType.methodType(String.class, AnnotatedMethod.class, String.class));
            findNameForRegularGetter = publicLookup.findVirtual(accessorNamingStrategyClass, "findNameForRegularGetter",
                MethodType.methodType(String.class, AnnotatedMethod.class, String.class));
            useReflectionForMemberName = true;
        } catch (Throwable ex) {
            LOGGER.verbose("Failed to retrieve MethodHandles used to get naming strategy. Falling back to BeanUtils.",
                    ex);
        }

        GET_ACCESSOR_NAMING = getAccessorNaming;
        FOR_POJO = forPojo;
        FIND_NAME_FOR_IS_GETTER = findNameForIsGetter;
        FIND_NAME_FOR_REGULAR_GETTER = findNameForRegularGetter;
        USE_REFLECTION_FOR_MEMBER_NAME = useReflectionForMemberName;
    }

    MemberNameConverterImpl(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String convertMemberName(Member member) {
        if (Modifier.isTransient(member.getModifiers())) {
            return null;
        }

        VisibilityChecker<?> visibilityChecker = mapper.getVisibilityChecker();
        if (member instanceof Field) {
            Field f = (Field) member;

            if (f.isAnnotationPresent(JsonIgnore.class) || !visibilityChecker.isFieldVisible(f)) {
                if (f.isAnnotationPresent(JsonProperty.class)) {
                    LOGGER.info("Field {} is annotated with JsonProperty but isn't accessible to "
                        + "JacksonJsonSerializer.", f.getName());
                }
                return null;
            }

            if (f.isAnnotationPresent(JsonProperty.class)) {
                String propertyName = f.getDeclaredAnnotation(JsonProperty.class).value();
                return CoreUtils.isNullOrEmpty(propertyName) ? f.getName() : propertyName;
            }

            return f.getName();
        }

        if (member instanceof Method) {
            Method m = (Method) member;

            /*
             * If the method isn't a getter, is annotated with JsonIgnore, or isn't visible to the ObjectMapper ignore
             * it.
             */
            if (!verifyGetter(m)
                || m.isAnnotationPresent(JsonIgnore.class)
                || !visibilityChecker.isGetterVisible(m)) {
                if (m.isAnnotationPresent(JsonGetter.class) || m.isAnnotationPresent(JsonProperty.class)) {
                    LOGGER.info("Method {} is annotated with either JsonGetter or JsonProperty but isn't accessible "
                        + "to JacksonJsonSerializer.", m.getName());
                }
                return null;
            }

            String methodNameWithoutJavaBeans = removePrefix(m);

            /*
             * Prefer JsonGetter over JsonProperty as it is the more targeted annotation.
             */
            if (m.isAnnotationPresent(JsonGetter.class)) {
                String propertyName = m.getDeclaredAnnotation(JsonGetter.class).value();
                return CoreUtils.isNullOrEmpty(propertyName) ? methodNameWithoutJavaBeans : propertyName;
            }

            if (m.isAnnotationPresent(JsonProperty.class)) {
                String propertyName = m.getDeclaredAnnotation(JsonProperty.class).value();
                return CoreUtils.isNullOrEmpty(propertyName) ? methodNameWithoutJavaBeans : propertyName;
            }

            // If no annotation is present default to the inferred name.
            return methodNameWithoutJavaBeans;
        }

        return null;
    }

    /*
     * Only consider methods that don't have parameters and aren't void as valid getter methods.
     */
    private static boolean verifyGetter(Method method) {
        Class<?> returnType = method.getReturnType();

        return method.getParameterCount() == 0
            && returnType != void.class
            && returnType != Void.class;
    }

    private String removePrefix(Method method) {
        MapperConfig<?> config = mapper.getSerializationConfig();

        AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(config,
            mapper.constructType(method.getDeclaringClass()), null);

        AnnotatedMethod annotatedMethod = new AnnotatedMethod(null, method, null, null);
        String annotatedMethodName = annotatedMethod.getName();

        String name = null;
        if (USE_REFLECTION_FOR_MEMBER_NAME) {
            name = removePrefixWithReflection(config, annotatedClass, annotatedMethod, annotatedMethodName);
        }

        if (name == null) {
            name = removePrefixWithBeanUtils(annotatedMethod);
        }

        return name;
    }

    private static String removePrefixWithReflection(MapperConfig<?> config, AnnotatedClass annotatedClass,
        AnnotatedMethod method, String methodName) {
        try {
            Object accessorNamingStrategy = FOR_POJO.invoke(GET_ACCESSOR_NAMING.invoke(config), config, annotatedClass);
            String name = (String) FIND_NAME_FOR_IS_GETTER.invoke(accessorNamingStrategy, method, methodName);
            if (name == null) {
                name = (String) FIND_NAME_FOR_REGULAR_GETTER.invoke(accessorNamingStrategy, method, methodName);
            }

            return name;
        } catch (Throwable ex) {
            LOGGER.verbose("Failed to find member name with AccessorNamingStrategy, returning null.", ex);
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private static String removePrefixWithBeanUtils(AnnotatedMethod annotatedMethod) {
        return BeanUtil.okNameForGetter(annotatedMethod, false);
    }
}
