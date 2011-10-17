package com.microsoft.azure.configuration.builder;

public interface Builder {

	public abstract <T> T build(Class<T> c) throws Exception;

}
