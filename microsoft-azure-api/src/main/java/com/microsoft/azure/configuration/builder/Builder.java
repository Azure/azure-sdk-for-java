package com.microsoft.azure.configuration.builder;

import java.util.Map;


/**
 * Builder interface used internally by Configuration to create component instances
 *
 */
public interface Builder {

	/**
	 * Called by configuration when a component needs to be allocated
	 * @param service 
	 * @param properties
	 * @return
	 * @throws Exception
	 */
	public abstract <T> T build(Class<T> service, Map<String,Object> properties) throws Exception;

	/**
	 * @author lodejard
	 *
	 * @param <T>
	 */
	public interface Factory<T> {
		/**
		 * @param builder
		 * @param properties
		 * @return
		 * @throws Exception
		 */
		T create(Builder builder, Map<String,Object> properties) throws Exception;
	}
	
	public interface Registry {
		<T> Registry add(Class<T> service);
		<T, TImpl> Registry add(Class<T> service, Class<TImpl> implementation);
		<T> Registry add(Class<T> service, Factory<T> factory);
	}
	
	public interface Exports {
		void register(Registry registry);
	}
}
