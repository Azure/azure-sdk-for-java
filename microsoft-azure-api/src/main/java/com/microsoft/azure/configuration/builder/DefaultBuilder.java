package com.microsoft.azure.configuration.builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.inject.Inject;



public class DefaultBuilder implements Builder, Builder.Registry {
	Map<Class<?>, Factory<?>> factories;

	public DefaultBuilder() {
		factories = new HashMap<Class<?>, Factory<?>>();
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

	

	public <T, TImpl> Builder.Registry add(Class<T> service, final Class<TImpl> implementation) {
		Constructor<?>[] ctors = implementation.getConstructors();
		for(final Constructor<?> ctor : ctors) {
			if (ctor.getAnnotation(Inject.class) != null) {
				final Class<?>[] parameterTypes = ctor.getParameterTypes();
				addFactory(service, new Builder.Factory<T>() {
					@SuppressWarnings("unchecked")
					public T create(Builder builder, Map<String,Object> properties) throws Exception {
						Object[] initargs = new Object[parameterTypes.length];
						for(int i = 0; i != parameterTypes.length; ++i) {
							initargs[i] = builder.build(parameterTypes[i], properties);
						}
						
						return (T) ctor.newInstance(initargs);
				}});
			}
		}
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
		
    
	public <T> T build(Class<T> service, Map<String,Object> properties) throws Exception {
		@SuppressWarnings("unchecked")
		Factory<T> factory = (Factory<T>) factories.get(service);
		if (factory == null) {
			return null;
		}
    	return factory.create(this, properties);
    }



}
