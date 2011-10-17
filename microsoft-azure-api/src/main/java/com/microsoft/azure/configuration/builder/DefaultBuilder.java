package com.microsoft.azure.configuration.builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;


public class DefaultBuilder implements Builder, BuilderRegistry {
	public DefaultBuilder() {
		factories = new HashMap<Class<?>, Provider<?>>();
	}
	
	
	Map<Class<?>, Provider<?>> factories;
	
	public Builder addFactory(Class<?> service, Provider<?> factory) {
		factories.put(service, factory);
		return this;
	}
	

	public <T, TImpl> BuilderRegistry add(Class<T> service, final Class<TImpl> implementation) {
		Constructor<?>[] ctors = implementation.getConstructors();
		for(final Constructor<?> ctor : ctors) {
			if (ctor.getAnnotation(Inject.class) != null) {
				final Class<?>[] parameterTypes = ctor.getParameterTypes();
				addFactory(service, new Provider<T>() {
					@SuppressWarnings("unchecked")
					public T get() throws RuntimeException {
						Object[] initargs = new Object[parameterTypes.length];
						for(int i = 0; i != parameterTypes.length; ++i) {
							initargs[i] = build(parameterTypes[i]);
						}
						
						try {
							return (T) ctor.newInstance(initargs);
						} catch (InvocationTargetException e) {
							throw new RuntimeException(e);
						} catch (InstantiationException e) {
							throw new RuntimeException(e);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
				}});
			}
		}
		return this;
	}
		
	public <T> BuilderRegistry add(Class<T> service) {
		return add(service, service);		
	}
		
	public <T> BuilderRegistry add(Class<T> service, Provider<T> provider) {		
		addFactory(service, provider);
		return this;
	}
    
	public <T> T build(Class<T> c) throws RuntimeException {
		@SuppressWarnings("unchecked")
		Provider<T> factory = (Provider<T>) factories.get(c);
		if (factory == null) {
			return null;
		}
    	return factory.get();
    }


}
