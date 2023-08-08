// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.utils;

import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class AzureServicePropertiesUtilsTests {

    @Test
    void nestedPropertiesShouldNotMerge() {
        TestServiceProperties target = new TestServiceProperties();
        target.getSomething().setA("a");

        TestServiceProperties source = new TestServiceProperties();
        source.getSomething().setB("b");

        TestServiceProperties result = AzureServicePropertiesUtils.loadServiceCommonProperties(source, target);
        assertNull(result.getSomething().getA());
        Assertions.assertEquals("b", result.getSomething().getB());
    }

    private static class TestServiceProperties extends AzureHttpSdkProperties {

        private Something something = new Something();

        public Something getSomething() {
            return something;
        }

        public void setSomething(Something something) {
            this.something = something;
        }

        private static class Something {
            private String a;
            private String b;

            public String getA() {
                return a;
            }

            public void setA(String a) {
                this.a = a;
            }

            public String getB() {
                return b;
            }

            public void setB(String b) {
                this.b = b;
            }
        }
    }

}
