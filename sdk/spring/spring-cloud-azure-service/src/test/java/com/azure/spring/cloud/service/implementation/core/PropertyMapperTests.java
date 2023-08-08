// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.core;

import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class PropertyMapperTests {

    @Test
    void testTo() {
        PropertyMapper propertyMapper = new PropertyMapper();

        Foo source = new Foo(), target = new Foo();
        source.setA("foo.a");
        source.setB("foo.b");

        propertyMapper.from(source.getA()).to(target::setA);
        propertyMapper.from(source.getB()).to(target::setB);

        Assertions.assertEquals("foo.a", target.getA());
        Assertions.assertEquals("foo.b", target.getB());
    }

    @Test
    void testToWillNotThrowNPE() {
        PropertyMapper propertyMapper = new PropertyMapper();

        Foo source = new Foo(), target = new Foo();
        propertyMapper.from(() -> source.getBar().getA()).to(v -> target.getBar().setA(v));
        propertyMapper.from(() -> source.getBar().getB()).to(v -> target.getBar().setB(v));

        Assertions.assertNull(target.getBar());
    }

    @Test
    void testWillThrowNPE() {
        PropertyMapper propertyMapper = new PropertyMapper();

        Foo source = new Foo(), target = new Foo();

        Assertions.assertThrows(NullPointerException.class, () -> {
            propertyMapper.from(source.getBar().getA()).to(v -> target.getBar().setA(v));
            propertyMapper.from(source.getBar().getB()).to(v -> target.getBar().setB(v));
        });
    }

    @Test
    void testAlwaysApplyNonNullFalse() {
        PropertyMapper propertyMapper = new PropertyMapper(false);

        Foo source = new Foo(), target = new Foo();
        target.setA("foo.a");
        target.setB("foo.b");

        Assertions.assertNotNull(target.getA());
        Assertions.assertNotNull(target.getB());

        propertyMapper.from(source.getA()).to(target::setA);
        propertyMapper.from(source.getB()).to(target::setB);

        Assertions.assertNull(target.getA());
        Assertions.assertNull(target.getB());
    }

    @Test
    void testAlwaysApplyNonNullTrue() {
        PropertyMapper propertyMapper = new PropertyMapper(true);

        Foo source = new Foo(), target = new Foo();
        target.setA("foo.a");
        target.setB("foo.b");

        Assertions.assertNotNull(target.getA());
        Assertions.assertNotNull(target.getB());

        propertyMapper.from(source.getA()).to(target::setA);
        propertyMapper.from(source.getB()).to(target::setB);

        Assertions.assertEquals("foo.a", target.getA());
        Assertions.assertEquals("foo.b", target.getB());
    }

    @Test
    void testWhenTrue() {
        PropertyMapper propertyMapper = new PropertyMapper();

        Foo source = new Foo(), target = new Foo();

        propertyMapper.from(source.getFlag()).whenTrue().to(target::setFlag);
        Assertions.assertNull(target.getFlag());

        target = new Foo();
        source.setFlag(false);
        propertyMapper.from(source.getFlag()).whenTrue().to(target::setFlag);
        Assertions.assertNull(target.getFlag());

        target = new Foo();
        source.setFlag(true);
        propertyMapper.from(source.getFlag()).whenTrue().to(target::setFlag);
        Assertions.assertTrue(target.getFlag());
    }

    @Test
    void testWhenFalse() {
        PropertyMapper propertyMapper = new PropertyMapper();

        Foo source = new Foo(), target = new Foo();

        propertyMapper.from(source.getFlag()).whenFalse().to(target::setFlag);
        Assertions.assertNull(target.getFlag());

        target = new Foo();
        source.setFlag(true);
        propertyMapper.from(source.getFlag()).whenFalse().to(target::setFlag);
        Assertions.assertNull(target.getFlag());

        target = new Foo();
        source.setFlag(false);
        propertyMapper.from(source.getFlag()).whenFalse().to(target::setFlag);
        Assertions.assertFalse(target.getFlag());
    }

    @Test
    void testWhen() {
        PropertyMapper propertyMapper = new PropertyMapper();

        Foo source = new Foo(), target = new Foo();

        propertyMapper.from(source.getList()).when(l -> !l.isEmpty()).to(target::setList);
        Assertions.assertNull(target.getList());

        target = new Foo();
        source.setList(new ArrayList<>());
        propertyMapper.from(source.getList()).when(l -> !l.isEmpty()).to(target::setList);
        Assertions.assertNull(target.getList());

        target = new Foo();
        source.setList(Arrays.asList("String"));
        propertyMapper.from(source.getList()).when(l -> !l.isEmpty()).to(target::setList);
        Assertions.assertEquals(1, target.getList().size());
    }

    static class Foo {
        private String a;
        private String b;
        private Boolean flag;
        private List<String> list;
        private Bar bar;

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

        public Bar getBar() {
            return bar;
        }

        public void setBar(Bar bar) {
            this.bar = bar;
        }

        public Boolean getFlag() {
            return flag;
        }

        public void setFlag(Boolean flag) {
            this.flag = flag;
        }

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }

        static class Bar {
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
