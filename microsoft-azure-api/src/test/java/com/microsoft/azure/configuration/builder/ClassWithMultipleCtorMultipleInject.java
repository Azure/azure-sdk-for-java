package com.microsoft.azure.configuration.builder;

import javax.inject.Inject;

public class ClassWithMultipleCtorMultipleInject {
	@Inject
	public ClassWithMultipleCtorMultipleInject(){}
	@Inject
	public ClassWithMultipleCtorMultipleInject(String x){}
}
