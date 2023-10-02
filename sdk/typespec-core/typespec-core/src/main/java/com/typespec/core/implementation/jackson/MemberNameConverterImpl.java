// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.logging.LogLevel;
import com.typespec.core.util.serializer.MemberNameConverter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.cfg.PackageVersion;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.util.BeanUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Retrieves the JSON serialized property name from {@link Member}.
 */
final class MemberNameConverterImpl implements MemberNameConverter {
    private static final ClientLogger LOGGER = new ClientLogger(MemberNameConverterImpl.class);

    private final ObjectMapper mapper;
    final boolean useJackson212;
    private boolean jackson212IsSafe = true;

    MemberNameConverterImpl(ObjectMapper mapper) {
        this.mapper = mapper;
        this.useJackson212 = PackageVersion.VERSION.getMinorVersion() >= 12;
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
                    LOGGER.atInfo()
                        .addKeyValue("field", f.getName())
                        .log("Field is annotated with JsonProperty but isn't accessible to JacksonJsonSerializer.");
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
                    LOGGER.atInfo()
                        .addKeyValue("method", m.getName())
                        .log("Method is annotated with either JsonGetter or JsonProperty but isn't accessible  to JacksonJsonSerializer.");
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

        AnnotatedMethod annotatedMethod = annotatedClass.findMethod(method.getName(), method.getParameterTypes());
        String annotatedMethodName = annotatedMethod.getName();

        String name = null;
        if (useJackson212 && jackson212IsSafe) {
            try {
                name = JacksonDatabind212.removePrefix(config, annotatedClass, annotatedMethod, annotatedMethodName);
            } catch (Throwable ex) {
                if (ex instanceof LinkageError) {
                    jackson212IsSafe = false;
                    LOGGER.log(LogLevel.VERBOSE, JacksonVersion::getHelpInfo, ex);
                }

                throw ex;
            }
        }

        if (name == null) {
            name = removePrefixWithBeanUtils(annotatedMethod);
        }

        return name;
    }

    @SuppressWarnings("deprecation")
    private static String removePrefixWithBeanUtils(AnnotatedMethod annotatedMethod) {
        return BeanUtil.okNameForGetter(annotatedMethod, false);
    }
}
