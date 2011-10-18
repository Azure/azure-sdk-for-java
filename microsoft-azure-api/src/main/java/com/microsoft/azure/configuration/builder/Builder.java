package com.microsoft.azure.configuration.builder;

import java.util.Map;


public interface Builder {

	public abstract <T> T build(Class<T> service, Map<String,Object> properties) throws Exception;

	public interface Factory<T> {
		T create(Builder builder, Map<String,Object> properties) throws Exception;
	}
	
	public interface Registry {
		<T> Registry add(Class<T> service);
		<T, TImpl> Registry add(Class<T> service, Class<TImpl> implementation);
		<T> Registry add(Factory<T> factory);
	}
	
	public interface Exports {
		void register(Registry registry);
	}
}
