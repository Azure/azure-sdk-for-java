package com.microsoft.azure.configuration.builder;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DefaultBuilderTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void namedAnnotationsComeFromBuildProperties() throws Exception {
		// Arrange
		DefaultBuilder builder = new DefaultBuilder();
		builder.add(ClassWithNamedParameter.class);

		// Act
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("Foo", "world");
		ClassWithNamedParameter cwnp = builder.build(
				ClassWithNamedParameter.class, properties);

		// Assert
		Assert.assertEquals("world", cwnp.getHello());
	}

	@Test
	public void namedAnnotationReportsMissingProperty() throws Exception {
		// Arrange
		thrown.expect(RuntimeException.class);

		Map<String, Object> properties = new HashMap<String, Object>();
		DefaultBuilder builder = new DefaultBuilder();
		builder.add(ClassWithNamedParameter.class);

		// Act
		ClassWithNamedParameter cwnp = builder.build(
				ClassWithNamedParameter.class, properties);

		// Assert
		Assert.assertEquals("world", cwnp.getHello());
	}

	@Test
	public void singleCtorWithNoInjectShouldBeUsed() throws Exception {
		// Arrange
		Map<String, Object> properties = new HashMap<String, Object>();
		DefaultBuilder builder = new DefaultBuilder();
		builder.add(ClassWithSingleCtorNoInject.class);

		// Act
		ClassWithSingleCtorNoInject result = builder.build(
				ClassWithSingleCtorNoInject.class, properties);

		// Assert
		Assert.assertNotNull(result);
	}

	@Test
	public void multipleCtorWithNoInjectShouldFail() throws Exception {
		// Arrange
		thrown.expect(RuntimeException.class);

		Map<String, Object> properties = new HashMap<String, Object>();
		DefaultBuilder builder = new DefaultBuilder();
		builder.add(ClassWithMultipleCtorNoInject.class);

		// Act
		ClassWithMultipleCtorNoInject result = builder.build(
				ClassWithMultipleCtorNoInject.class, properties);

		// Assert
		Assert.assertTrue("Exception must occur", false);
	}

	@Test
	public void multipleCtorWithMultipleInjectShouldFail() throws Exception {
		// Arrange
		thrown.expect(RuntimeException.class);

		Map<String, Object> properties = new HashMap<String, Object>();
		DefaultBuilder builder = new DefaultBuilder();
		builder.add(ClassWithMultipleCtorMultipleInject.class);

		// Act
		ClassWithMultipleCtorMultipleInject result = builder.build(
				ClassWithMultipleCtorMultipleInject.class, properties);

		// Assert
		Assert.assertTrue("Exception must occur", false);
	}
	
	@Test 
	public void alterationExecutesWhenInstanceCreated() throws Exception {
		// Arrange
		Map<String, Object> properties = new HashMap<String, Object>();
		DefaultBuilder builder = new DefaultBuilder();
		builder.add(ClassWithProperties.class);
		builder.alter(ClassWithProperties.class, new AlterClassWithProperties());

		// Act
		ClassWithProperties result = builder.build(ClassWithProperties.class, properties);
		
		// Assert
		Assert.assertEquals("one - changed", result.getFoo());
	}
}