package com.microsoft.azure.configuration.builder;

public class ClassWithProperties {
	String foo;
	String bar;
	
	public ClassWithProperties(){
		foo = "one";
		bar = "two";
	}

	/**
	 * @return the foo
	 */
	public String getFoo() {
		return foo;
	}
	/**
	 * @param foo the foo to set
	 */
	public void setFoo(String foo) {
		this.foo = foo;
	}
	/**
	 * @return the bar
	 */
	public String getBar() {
		return bar;
	}
	/**
	 * @param bar the bar to set
	 */
	public void setBar(String bar) {
		this.bar = bar;
	}
}
