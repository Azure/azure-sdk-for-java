package com.microsoft.azure.configuration.builder;

import javax.inject.Provider;


public interface BuilderRegistry {
	<T> BuilderRegistry add(Class<T> service);
	<T, TImpl> BuilderRegistry add(Class<T> service, Class<TImpl> implementation);
	<T> BuilderRegistry add(Class<T> service, Provider<T> provider);
}
