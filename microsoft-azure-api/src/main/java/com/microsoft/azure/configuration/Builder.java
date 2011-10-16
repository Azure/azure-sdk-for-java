package com.microsoft.azure.configuration;

public interface Builder {

	public abstract <T> T build(Class<T> c) throws Exception;

}
