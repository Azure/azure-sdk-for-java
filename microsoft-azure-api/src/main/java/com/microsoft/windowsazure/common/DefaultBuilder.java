package com.microsoft.windowsazure.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.inject.Inject;
import javax.inject.Named;


public class DefaultBuilder implements Builder, Builder.Registry {
    Map<Class<?>, Factory<?>> factories;
    Map<Class<?>, List<Alteration<?>>> alterations;

    public DefaultBuilder() {
        factories = new HashMap<Class<?>, Factory<?>>();
        alterations = new HashMap<Class<?>, List<Alteration<?>>>();
    }

    public static DefaultBuilder create() {
        DefaultBuilder builder = new DefaultBuilder();

        for (Builder.Exports exports : ServiceLoader.load(Builder.Exports.class)) {
            exports.register(builder);
        }

        return builder;
    }

    void addFactory(Class<?> service, Factory<?> factory) {
        factories.put(service, factory);
    }

    public <T> Builder.Registry add(Class<T> service) {
        return add(service, service);
    }

    Constructor<?> findInjectConstructor(Class<?> implementation) {

        Constructor<?> withInject = null;
        Constructor<?> withoutInject = null;
        int count = 0;

        for (Constructor<?> ctor : implementation.getConstructors()) {
            if (ctor.getAnnotation(Inject.class) != null) {
                if (withInject != null) {
                    throw new RuntimeException("Class must not have multple @Inject annotations: " + implementation.getName());
                }
                withInject = ctor;
            }
            else {
                ++count;
                withoutInject = ctor;
            }
        }
        if (withInject != null) {
            return withInject;
        }
        if (count != 1) {
            throw new RuntimeException("Class without @Inject annotation must have one constructor: " + implementation.getName());
        }
        return withoutInject;
    }

    public <T, TImpl> Builder.Registry add(Class<T> service, final Class<TImpl> implementation) {
        final Constructor<?> ctor = findInjectConstructor(implementation);
        final Class<?>[] parameterTypes = ctor.getParameterTypes();
        final Annotation[][] parameterAnnotations = ctor.getParameterAnnotations();

        addFactory(service, new Builder.Factory<T>() {
            @SuppressWarnings("unchecked")
            public T create(String profile, Builder builder, Map<String, Object> properties) throws Exception {
                Object[] initargs = new Object[parameterTypes.length];
                for (int i = 0; i != parameterTypes.length; ++i) {

                    boolean located = false;

                    String named = findNamedAnnotation(parameterAnnotations[i]);
                    String fullName = dotCombine(profile, named);

                    boolean probeProperties = fullName != null && fullName != "";
                    int startingIndex = 0;
                    while (!located && probeProperties) {
                        String probeName = fullName.substring(startingIndex);
                        if (!located && named != null && properties.containsKey(probeName)) {
                            located = true;
                            initargs[i] = properties.get(probeName);
                        }
                        else {
                            startingIndex = fullName.indexOf('.', startingIndex) + 1;
                            if (startingIndex == 0) {
                                probeProperties = false;
                            }
                        }
                    }

                    if (!located) {
                        located = true;
                        initargs[i] = builder.build(fullName, parameterTypes[i], properties);
                    }
                }

                return (T) ctor.newInstance(initargs);
            }
        });
        return this;
    }

    protected String dotCombine(String profile, String named) {
        boolean noProfile = profile == null || profile == "";
        boolean noName = named == null || named == "";
        if (noName)
            return profile;
        if (noProfile)
            return named;
        return profile + "." + named;
    }

    protected String findNamedAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (Named.class.isAssignableFrom(annotation.getClass())) {
                return ((Named) annotation).value();
            }
        }
        return null;
    }

    public <T> Registry add(Factory<T> factory) {
        for (Type genericInterface : factory.getClass().getGenericInterfaces()) {
            ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
            if (parameterizedType.getRawType().equals(Builder.Factory.class)) {
                Type typeArgument = parameterizedType.getActualTypeArguments()[0];
                addFactory((Class<?>) typeArgument, factory);
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T build(String profile, Class<T> service, Map<String, Object> properties) throws Exception {
        Factory<T> factory = (Factory<T>) factories.get(service);
        if (factory == null) {
            throw new RuntimeException("Service or property not registered: " + profile + " " + service.getName());
        }
        T instance = factory.create(profile, this, properties);
        List<Alteration<?>> alterationList = alterations.get(service);
        if (alterationList != null) {
            for (Alteration<?> alteration : alterationList) {
                instance = ((Alteration<T>) alteration).alter(instance, this, properties);
            }
        }
        return instance;
    }

    public <T> void alter(Class<T> service, Alteration<T> alteration) {
        if (!this.alterations.containsKey(service)) {
            this.alterations.put(service, new ArrayList<Alteration<?>>());
        }
        this.alterations.get(service).add(alteration);
    }

}
