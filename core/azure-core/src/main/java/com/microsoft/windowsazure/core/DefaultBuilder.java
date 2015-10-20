/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.core;

import com.microsoft.windowsazure.ConfigurationException;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class DefaultBuilder implements Builder, Builder.Registry {
    private Map<Class<?>, Factory<?>> factories;
    private Map<Class<?>, Map<Class<?>, List<Alteration<?>>>> alterations;

    public DefaultBuilder() {
        factories = new HashMap<Class<?>, Factory<?>>();
        alterations = new HashMap<Class<?>, Map<Class<?>, List<Alteration<?>>>>();
    }

    public static DefaultBuilder create() {
        DefaultBuilder builder = new DefaultBuilder();

        for (Builder.Exports exports : ServiceLoader
                .load(Builder.Exports.class)) {
            exports.register(builder);
        }

        return builder;
    }

    void addFactory(Class<?> service, Factory<?> factory) {
        factories.put(service, factory);
    }

    @Override
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
                    throw new RuntimeException(
                            "Class must not have multple @Inject annotations: "
                                    + implementation.getName());
                }
                withInject = ctor;
            } else {
                ++count;
                withoutInject = ctor;
            }
        }
        if (withInject != null) {
            return withInject;
        }
        if (count != 1) {
            throw new RuntimeException(
                    "Class without @Inject annotation must have one constructor: "
                            + implementation.getName());
        }
        return withoutInject;
    }

    @Override
    public <T, TImpl> Builder.Registry add(Class<T> service,
            final Class<TImpl> implementation) {
        final Constructor<?> ctor = findInjectConstructor(implementation);
        final Class<?>[] parameterTypes = ctor.getParameterTypes();
        final Annotation[][] parameterAnnotations = ctor
                .getParameterAnnotations();

        addFactory(service, new Builder.Factory<T>() {
            @Override
            @SuppressWarnings("unchecked")
            public <S> T create(String profile, Class<S> service,
                    Builder builder, Map<String, Object> properties) {
                Object[] initializationArguments = new Object[parameterTypes.length];
                for (int i = 0; i != parameterTypes.length; ++i) {

                    boolean located = false;

                    String named = findNamedAnnotation(parameterAnnotations[i]);
                    String fullName = dotCombine(profile, named);

                    boolean probeProperties = fullName != null
                            && fullName != "";
                    int startingIndex = 0;
                    while (!located && probeProperties) {
                        String nameProbe = fullName.substring(startingIndex);
                        if (!located && named != null
                                && properties.containsKey(nameProbe)) {
                            located = true;
                            if (parameterTypes[i] == String.class) {
                                initializationArguments[i] = properties
                                    .get(nameProbe);
                            } else {
                                initializationArguments[i] = builder.build(fullName,
                                        service, parameterTypes[i], properties);
                            }
                        } else {
                            startingIndex = fullName
                                    .indexOf('.', startingIndex) + 1;
                            if (startingIndex == 0) {
                                probeProperties = false;
                            }
                        }
                    }

                    if (!located) {
                        located = true;
                        initializationArguments[i] = builder.build(fullName,
                                service, parameterTypes[i], properties);
                    }
                }

                try {
                    return (T) ctor.newInstance(initializationArguments);
                } catch (InstantiationException e) {
                    throw new ConfigurationException(e);
                } catch (IllegalAccessException e) {
                    throw new ConfigurationException(e);
                } catch (InvocationTargetException e) {
                    throw new ConfigurationException(e);
                }
            }
        });
        return this;
    }

    protected String dotCombine(String profile, String named) {
        boolean noProfile = profile == null || profile.isEmpty();
        boolean noName = named == null || named.isEmpty();
        if (noName) {
            return profile;
        }
        if (noProfile) {
            return named;
        }

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

    @Override
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

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> T build(String profile, Class<S> service,
            Class<T> instanceClass, Map<String, Object> properties) {
        Factory<T> factory = (Factory<T>) factories.get(instanceClass);
        if (factory == null) {
            throw new RuntimeException("Service or property not registered: "
                    + profile + " " + service.getName() + " " + instanceClass);
        }
        T instance = factory.create(profile, service, this, properties);
        Map<Class<?>, List<Alteration<?>>> alterationMap = alterations
                .get(service);
        if (alterationMap != null) {
            List<Alteration<?>> alterationList = alterationMap
                    .get(instanceClass);
            if (alterationList != null) {
                for (Alteration<?> alteration : alterationList) {
                    instance = ((Alteration<T>) alteration).alter(profile,
                            instance, this, properties);
                }
            }
        }
        return instance;
    }

    @Override
    public <S, T> void alter(Class<S> service, Class<T> instance,
            Alteration<T> alteration) {
        if (!this.alterations.containsKey(service)) {
            this.alterations.put(service,
                    new HashMap<Class<?>, List<Alteration<?>>>());
        }
        if (!this.alterations.get(service).containsKey(instance)) {
            this.alterations.get(service).put(instance,
                    new ArrayList<Alteration<?>>());
        }
        this.alterations.get(service).get(instance).add(alteration);
    }

}
