/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.configuration.builder;

import com.microsoft.windowsazure.core.DefaultBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

public class DefaultBuilderTest {

    Map<String, Object> properties;
    DefaultBuilder builder;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
        properties = new HashMap<String, Object>();
        builder = new DefaultBuilder();
    }

    @Test
    public void namedAnnotationsComeFromBuildProperties() throws Exception {
        // Arrange
        builder.add(ClassWithNamedParameter.class);

        // Act
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("Foo", "world");
        ClassWithNamedParameter cwnp = builder.build("",
                ClassWithNamedParameter.class, ClassWithNamedParameter.class,
                properties);

        // Assert
        Assert.assertEquals("world", cwnp.getHello());
    }

    @Test
    public void namedAnnotationReportsMissingProperty() throws Exception {
        // Arrange
        thrown.expect(RuntimeException.class);

        builder.add(ClassWithNamedParameter.class);

        // Act
        ClassWithNamedParameter cwnp = builder.build("",
                ClassWithNamedParameter.class, ClassWithNamedParameter.class,
                properties);

        // Assert
        Assert.assertEquals("world", cwnp.getHello());
    }

    @Test
    public void singleCtorWithNoInjectShouldBeUsed() throws Exception {
        // Arrange
        builder.add(ClassWithSingleCtorNoInject.class);

        // Act
        ClassWithSingleCtorNoInject result = builder.build("",
                ClassWithSingleCtorNoInject.class,
                ClassWithSingleCtorNoInject.class, properties);

        // Assert
        Assert.assertNotNull(result);
    }

    @Test
    public void multipleCtorWithNoInjectShouldFail() throws Exception {
        // Arrange
        thrown.expect(RuntimeException.class);

        builder.add(ClassWithMultipleCtorNoInject.class);

        // Act
        ClassWithMultipleCtorNoInject result = builder.build("",
                ClassWithMultipleCtorNoInject.class,
                ClassWithMultipleCtorNoInject.class, properties);

        // Assert
        Assert.assertTrue("Exception must occur", false);
        Assert.assertNull("Result should be null", result);
    }

    @Test
    public void multipleCtorWithMultipleInjectShouldFail() throws Exception {
        // Arrange
        thrown.expect(RuntimeException.class);

        builder.add(ClassWithMultipleCtorMultipleInject.class);

        // Act
        ClassWithMultipleCtorMultipleInject result = builder.build("",
                ClassWithMultipleCtorMultipleInject.class,
                ClassWithMultipleCtorMultipleInject.class, properties);

        // Assert
        Assert.assertTrue("Exception must occur", false);
        Assert.assertNull("Result should be null", result);
    }

    @Test
    public void alterationExecutesWhenInstanceCreated() throws Exception {
        // Arrange
        builder.add(ClassWithProperties.class);
        builder.alter(ClassWithProperties.class, ClassWithProperties.class,
                new AlterClassWithProperties());

        // Act
        ClassWithProperties result = builder.build("",
                ClassWithProperties.class, ClassWithProperties.class,
                properties);

        // Assert
        Assert.assertEquals("one - changed", result.getFoo());
    }

    @Test
    public void namedParametersUseProfileBasedKeysFirst() throws Exception {
        // Arrange
        builder.add(ClassWithNamedParameter.class);
        properties.put("Foo", "fallback");
        properties.put("testing.Foo", "Profile foo value");

        // Act
        ClassWithNamedParameter result = builder.build("testing",
                ClassWithNamedParameter.class, ClassWithNamedParameter.class,
                properties);

        // Assert
        Assert.assertEquals("Profile foo value", result.getHello());
    }

    @Test
    public void namedParametersFallBackToNonProfileBasedKeys() throws Exception {
        // Arrange
        builder.add(ClassWithNamedParameter.class);
        properties.put("Foo", "fallback");
        properties.put("testing.Foo", "Profile foo value");

        // Act
        ClassWithNamedParameter result1 = builder.build("",
                ClassWithNamedParameter.class, ClassWithNamedParameter.class,
                properties);
        ClassWithNamedParameter result2 = builder.build("production",
                ClassWithNamedParameter.class, ClassWithNamedParameter.class,
                properties);
        ClassWithNamedParameter result3 = builder.build("testing.custom",
                ClassWithNamedParameter.class, ClassWithNamedParameter.class,
                properties);

        // Assert
        Assert.assertEquals("fallback", result1.getHello());
        Assert.assertEquals("fallback", result2.getHello());
        Assert.assertEquals("fallback", result3.getHello());
    }

    @Test
    public void namedParamatersFallBackFromLeftToRight() throws Exception {
        // Arrange
        builder.add(ClassWithNamedParameter.class);
        properties.put("Foo", "fallback");
        properties.put("custom.Foo", "custom.Foo value");
        properties.put("testing.custom.Foo", "testing.custom.Foo value");

        // Act
        ClassWithNamedParameter result1 = builder.build("custom",
                ClassWithNamedParameter.class, ClassWithNamedParameter.class,
                properties);
        ClassWithNamedParameter result2 = builder.build("production.custom",
                ClassWithNamedParameter.class, ClassWithNamedParameter.class,
                properties);
        ClassWithNamedParameter result3 = builder.build("testing.custom",
                ClassWithNamedParameter.class, ClassWithNamedParameter.class,
                properties);

        // Assert
        Assert.assertEquals("custom.Foo value", result1.getHello());
        Assert.assertEquals("custom.Foo value", result2.getHello());
        Assert.assertEquals("testing.custom.Foo value", result3.getHello());
    }
}
