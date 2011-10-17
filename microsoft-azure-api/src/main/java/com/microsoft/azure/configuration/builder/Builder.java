package com.microsoft.azure.configuration.builder;

import javax.inject.Provider;

public interface Builder {

	public abstract <T> T build(Class<T> c) throws Exception;

	public interface Exports {
		void register(Registry registry);
	}
	
	public interface Registry {
		<T> Registry add(Class<T> service);
		<T, TImpl> Registry add(Class<T> service, Class<TImpl> implementation);
		<T> Registry add(Class<T> service, Provider<T> provider);
	}
}
