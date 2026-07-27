// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.deviceupdate;

import com.azure.core.util.BinaryData;
import com.azure.core.util.ExpandableStringEnum;
import com.azure.iot.deviceupdate.implementation.JsonMergePatchHelper;
import com.azure.iot.deviceupdate.models.PatchBody;
import com.azure.json.JsonSerializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class DeviceUpdateModelTests {
    private static final String MODELS_PACKAGE = "com.azure.iot.deviceupdate.models.";
    private static final String MODELS_RESOURCE = "com/azure/iot/deviceupdate/models";

    @Test
    public void modelAccessorsAndJsonSerializationAreCovered() throws Exception {
        Map<Class<?>, Object> samples = new HashMap<>();

        for (Class<?> modelClass : modelClasses()) {
            if (ExpandableStringEnum.class.isAssignableFrom(modelClass)) {
                coverExpandableStringEnum(modelClass);
                continue;
            }

            Object model = instantiateModel(modelClass, samples, new HashSet<Class<?>>());
            assertNotNull(model);
            invokeModelMutators(model, samples);
            invokeModelAccessors(model);

            if (JsonSerializable.class.isAssignableFrom(modelClass)) {
                String json = BinaryData.fromObject(model).toString();
                assertNotNull(json);
                Object deserialized = toObject(json, modelClass);
                assertNotNull(deserialized);
                invokeModelAccessors(deserialized);
            }
        }
    }

    @Test
    public void patchBodyCanSerializeAsJsonMergePatch() {
        PatchBody patchBody = new PatchBody().setFriendlyName(null);
        JsonMergePatchHelper.PatchBodyAccessor accessor = JsonMergePatchHelper.getPatchBodyAccessor();

        PatchBody mergePatch = accessor.prepareModelForJsonMergePatch(patchBody, true);

        assertSame(patchBody, mergePatch);
        assertFalse(BinaryData.fromObject(mergePatch).toString().isEmpty());
    }

    private static List<Class<?>> modelClasses() throws Exception {
        List<Class<?>> modelClasses = new ArrayList<>();
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(MODELS_RESOURCE);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if ("file".equals(resource.getProtocol())) {
                try (Stream<Path> paths = Files.list(Paths.get(resource.toURI()))) {
                    paths.forEach(path -> addModelClass(modelClasses, path.getFileName().toString()));
                }
            }
        }

        modelClasses.sort((left, right) -> left.getSimpleName().compareTo(right.getSimpleName()));
        assertFalse(modelClasses.isEmpty());
        return modelClasses;
    }

    private static void addModelClass(List<Class<?>> modelClasses, String fileName) {
        if (!fileName.endsWith(".class") || fileName.contains("$") || "package-info.class".equals(fileName)) {
            return;
        }

        String modelClassName = fileName.substring(0, fileName.length() - ".class".length());
        modelClasses.add(loadModelClass(modelClassName));
    }

    private static Class<?> loadModelClass(String modelClassName) {
        try {
            return Class.forName(MODELS_PACKAGE + modelClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object toObject(String json, Class<?> modelClass) {
        return BinaryData.fromString(json).toObject((Class<Object>) modelClass);
    }

    private static void coverExpandableStringEnum(Class<?> enumClass) throws Exception {
        Constructor<?> constructor = enumClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());

        Method fromString = enumClass.getMethod("fromString", String.class);
        Object value = fromString.invoke(null, "custom");
        assertNotNull(value);

        Method values = enumClass.getMethod("values");
        Collection<?> knownValues = (Collection<?>) values.invoke(null);
        assertFalse(knownValues.isEmpty());
    }

    private static Object instantiateModel(Class<?> modelClass, Map<Class<?>, Object> samples, Set<Class<?>> visiting)
        throws Exception {
        if (samples.containsKey(modelClass)) {
            return samples.get(modelClass);
        }

        if (!visiting.add(modelClass)) {
            return null;
        }

        Constructor<?> constructor = getPreferredConstructor(modelClass);
        constructor.setAccessible(true);
        Type[] genericParameterTypes = constructor.getGenericParameterTypes();
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            arguments[i] = sampleValue(parameterTypes[i], genericParameterTypes[i], samples, visiting);
        }

        Object model = constructor.newInstance(arguments);
        samples.put(modelClass, model);
        visiting.remove(modelClass);
        return model;
    }

    private static Constructor<?> getPreferredConstructor(Class<?> modelClass) {
        Constructor<?>[] constructors = modelClass.getDeclaredConstructors();
        Arrays.sort(constructors, DeviceUpdateModelTests::compareConstructorParameterCount);
        for (Constructor<?> constructor : constructors) {
            if (!constructor.isSynthetic()) {
                return constructor;
            }
        }
        throw new IllegalStateException("No constructor found for " + modelClass.getName());
    }

    private static int compareConstructorParameterCount(Constructor<?> left, Constructor<?> right) {
        return Integer.compare(right.getParameterCount(), left.getParameterCount());
    }

    private static void invokeModelMutators(Object model, Map<Class<?>, Object> samples) throws Exception {
        Class<?> currentClass = model.getClass();
        while (currentClass != null && currentClass != Object.class) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (isMutator(method)) {
                    method.setAccessible(true);
                    Class<?> parameterType = method.getParameterTypes()[0];
                    Type genericParameterType = method.getGenericParameterTypes()[0];
                    Set<Class<?>> visiting = new HashSet<>();
                    Object argument = sampleValue(parameterType, genericParameterType, samples, visiting);
                    method.invoke(model, argument);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    private static boolean isMutator(Method method) {
        return !Modifier.isStatic(method.getModifiers())
            && method.getName().startsWith("set")
            && method.getParameterCount() == 1;
    }

    private static void invokeModelAccessors(Object model) throws Exception {
        Class<?> currentClass = model.getClass();
        while (currentClass != null && currentClass != Object.class) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (isAccessor(method)) {
                    method.setAccessible(true);
                    method.invoke(model);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    private static boolean isAccessor(Method method) {
        return !Modifier.isStatic(method.getModifiers())
            && method.getParameterCount() == 0
            && (method.getName().startsWith("get") || method.getName().startsWith("is"));
    }

    private static Object sampleValue(Class<?> parameterType, Type genericType, Map<Class<?>, Object> samples,
        Set<Class<?>> visiting) throws Exception {
        if (parameterType == String.class) {
            return "value";
        } else if (parameterType == int.class || parameterType == Integer.class) {
            return 1;
        } else if (parameterType == long.class || parameterType == Long.class) {
            return 1L;
        } else if (parameterType == boolean.class || parameterType == Boolean.class) {
            return true;
        } else if (parameterType == OffsetDateTime.class) {
            return OffsetDateTime.parse("2026-07-27T00:00:00Z");
        } else if (List.class.isAssignableFrom(parameterType)) {
            return Collections.singletonList(sampleListElement(genericType, samples, visiting));
        } else if (Map.class.isAssignableFrom(parameterType)) {
            return Collections.singletonMap("key", "value");
        } else if (ExpandableStringEnum.class.isAssignableFrom(parameterType)) {
            Method fromString = parameterType.getMethod("fromString", String.class);
            return fromString.invoke(null, "custom");
        } else if (parameterType.getName().startsWith(MODELS_PACKAGE)) {
            return instantiateModel(parameterType, samples, visiting);
        }

        return null;
    }

    private static Object sampleListElement(Type genericType, Map<Class<?>, Object> samples, Set<Class<?>> visiting)
        throws Exception {
        if (genericType instanceof ParameterizedType) {
            Type elementType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            if (elementType instanceof Class<?>) {
                return sampleValue((Class<?>) elementType, elementType, samples, visiting);
            }
        }

        return "value";
    }
}
