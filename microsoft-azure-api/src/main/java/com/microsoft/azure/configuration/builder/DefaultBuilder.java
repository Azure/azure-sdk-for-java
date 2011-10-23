package com.microsoft.azure.configuration.builder;

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
		
		for(Builder.Exports exports : ServiceLoader.load(Builder.Exports.class)) {
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
				if (withInject != null){
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
			public T create(Builder builder, Map<String,Object> properties) throws Exception {
				Object[] initargs = new Object[parameterTypes.length];
				for(int i = 0; i != parameterTypes.length; ++i) {
					boolean located = false;
					
					Annotation[] annotations = parameterAnnotations[i];
					for(int ii = 0; ii != annotations.length && !located; ++ii){
						if (Named.class.isAssignableFrom(annotations[ii].getClass())) {
							located = true;

							Named named = (Named)annotations[ii];
							if (!properties.containsKey(named.value())) {
								throw new RuntimeException("Configuration missing required property: " + named.value());
							}
							initargs[i] = properties.get(named.value());
						}
					}
					
					if (!located) {
						initargs[i] = builder.build(parameterTypes[i], properties);
					}
				}
				
				return (T) ctor.newInstance(initargs);
			}
		});
		return this;
	}

	
	public <T> Registry add(Factory<T> factory) {
		for(Type genericInterface : factory.getClass().getGenericInterfaces())
		{
			ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
			if (parameterizedType.getRawType().equals(Builder.Factory.class)) {
				Type typeArgument = parameterizedType.getActualTypeArguments()[0];
				addFactory((Class<?>)typeArgument, factory);
			}
		}
		return this;
	}
		
    
	@SuppressWarnings("unchecked")
	public <T> T build(Class<T> service, Map<String,Object> properties) throws Exception {
		Factory<T> factory = (Factory<T>) factories.get(service);
		if (factory == null) {
			throw new RuntimeException("Service not registered: " + service.getName());
		}
    	T instance = factory.create(this, properties);
    	List<Alteration<?>> alterationList = alterations.get(service);
    	if (alterationList != null){
    		for(Alteration<?> alteration : alterationList){
    			instance = ((Alteration<T>)alteration).alter(instance, this, properties);
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
