package com.microsoft.azure.configuration;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;


public class DefaultBuilder implements Builder {
	public DefaultBuilder() {
		factories = new HashMap<Class<?>, Factory<?>>();
	}
	
	interface Factory<T> {
		T create() throws Exception;
	}
	
	Map<Class<?>, Factory<?>> factories;
	
	public Builder addFactory(Class<?> service, Factory<?> factory) {
		factories.put(service, factory);
		return this;
	}
	
	public <T> DefaultBuilder add(Class<?> service, final Class<T> implementation) {
		addFactory(service, new Factory<T>() {
			public T create() throws Exception {
				return implementation.newInstance();
			}});
		return this;
	}
	
	public <T> DefaultBuilder add(Class<?> service, final Class<T> implementation, final Class<?> parameterType1)  {
		addFactory(service, new Factory<T>() {
			public T create() throws Exception {
				final Constructor<T> ctor = implementation.getConstructor(parameterType1);
				return ctor.newInstance(build(parameterType1));
			}});
		return this;
	}
	
	
	public DefaultBuilder add(Class<?> service) {
		return add(service, service);		
	}
	

    
	public <T> T build(Class<T> c) throws Exception {
    	return (T) factories.get(c).create();
    }
}
