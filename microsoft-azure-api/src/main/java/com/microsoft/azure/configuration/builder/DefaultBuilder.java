package com.microsoft.azure.configuration.builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;


public class DefaultBuilder implements Builder, Builder.Registry {
	public DefaultBuilder() {
		factories = new HashMap<Class<?>, Factory<?>>();
	}
	
	
	Map<Class<?>, Factory<?>> factories;
	
	public Builder addFactory(Class<?> service, Factory<?> factory) {
		factories.put(service, factory);
		return this;
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
		
	public <T> Builder.Registry add(Class<T> service) {
		return add(service, service);		
	}
		
	public <T> Builder.Registry add(Class<T> service, Factory<T> provider) {		
		addFactory(service, provider);
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
