// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.PackageVersion;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemberNameConverterImplTests {
    @Test
    public void usesReflectionProperly() {
        if (PackageVersion.VERSION.getMinorVersion() >= 12) {
            assertTrue(new MemberNameConverterImpl(null).useJackson212,
                "Jackson 2.12 or later was found on the classpath, expected reflection to be used for member name.");
        } else {
            assertFalse(new MemberNameConverterImpl(null).useJackson212,
                "Jackson 2.11 or earlier was found on the classpath, expected reflection to not be used for member "
                    + "name.");
        }
    }

    @Test
    public void fieldsWithJsonProperty() throws NoSuchFieldException {
        final Field publicFieldWithAnnotationAndValue = Foo.class.getDeclaredField("publicFieldWithAnnotationAndValue");
        final Field publicFieldWithAnnotationNoValue = Foo.class.getDeclaredField("publicFieldWithAnnotationNoValue");
        final Field privateFieldWithAnnotation = Foo.class.getDeclaredField("privateFieldWithAnnotation");

        MemberNameConverterImpl memberNameConverter = new MemberNameConverterImpl(new ObjectMapper());

        assertEquals("public-field-with-annotation-and-value",
            memberNameConverter.convertMemberName(publicFieldWithAnnotationAndValue));
        assertEquals("publicFieldWithAnnotationNoValue",
            memberNameConverter.convertMemberName(publicFieldWithAnnotationNoValue));
        assertNull(memberNameConverter.convertMemberName(privateFieldWithAnnotation));
    }

    @Test
    public void methodsWithJsonGetter() throws NoSuchMethodException {
        final Method getPublicWithAnnotationAndValue = Foo.class.getDeclaredMethod("getPublicWithAnnotationAndValue");
        final Method getPublicWithAnnotationNoValue = Foo.class.getDeclaredMethod("getPublicWithAnnotationNoValue");
        final Method publicWithAnnotationNoPrefix = Foo.class.getDeclaredMethod("publicWithAnnotationNoPrefix");
        final Method isPublicWithAnnotationString = Foo.class.getDeclaredMethod("isPublicWithAnnotationString");
        final Method isPublicWithAnnotationBoolean = Foo.class.getDeclaredMethod("isPublicWithAnnotationBoolean");
        final Method getPrivateWithAnnotation = Foo.class.getDeclaredMethod("getPrivateWithAnnotation");
        final Method getVoidWithAnnotation = Foo.class.getDeclaredMethod("getVoidWithAnnotation");

        MemberNameConverterImpl memberNameConverter = new MemberNameConverterImpl(new ObjectMapper());

        assertEquals("public-getter-with-annotation-and-value",
            memberNameConverter.convertMemberName(getPublicWithAnnotationAndValue));
        assertEquals("publicWithAnnotationNoValue",
            memberNameConverter.convertMemberName(getPublicWithAnnotationNoValue));
        assertEquals("publicWithAnnotationBoolean",
            memberNameConverter.convertMemberName(isPublicWithAnnotationBoolean));
        assertNull(memberNameConverter.convertMemberName(publicWithAnnotationNoPrefix));
        assertNull(memberNameConverter.convertMemberName(isPublicWithAnnotationString));
        assertNull(memberNameConverter.convertMemberName(getPrivateWithAnnotation));
        assertNull(memberNameConverter.convertMemberName(getVoidWithAnnotation));
    }

    @Test
    public void fieldNoJsonProperty() throws NoSuchFieldException {
        final Field publicIgnoredField = Foo.class.getDeclaredField("publicIgnoredField");
        final Field publicNoAnnotationField = Foo.class.getDeclaredField("publicNoAnnotationField");
        final Field privateField = Foo.class.getDeclaredField("privateField");

        MemberNameConverterImpl memberNameConverter = new MemberNameConverterImpl(new ObjectMapper());

        assertEquals("publicNoAnnotationField", memberNameConverter.convertMemberName(publicNoAnnotationField));
        assertNull(memberNameConverter.convertMemberName(publicIgnoredField));
        assertNull(memberNameConverter.convertMemberName(privateField));
    }

    @Test
    public void methodsNoJsonGetter() throws NoSuchMethodException {
        final Method getPublicIgnored = Foo.class.getDeclaredMethod("getPublicIgnored");
        final Method getPublicNoAnnotation = Foo.class.getDeclaredMethod("getPublicNoAnnotation");
        final Method publicNoAnnotationNoPrefix = Foo.class.getDeclaredMethod("publicNoAnnotationNoPrefix");
        final Method isPublicNoAnnotationString = Foo.class.getDeclaredMethod("isPublicNoAnnotationString");
        final Method isPublicNoAnnotationBoolean = Foo.class.getDeclaredMethod("isPublicNoAnnotationBoolean");
        final Method getNoAnnotationVoid = Foo.class.getDeclaredMethod("getNoAnnotationVoid");
        final Method getNoAnnotationPrivate = Foo.class.getDeclaredMethod("getNoAnnotationPrivate");

        MemberNameConverterImpl memberNameConverter = new MemberNameConverterImpl(new ObjectMapper());

        assertEquals("publicNoAnnotation", memberNameConverter.convertMemberName(getPublicNoAnnotation));
        assertEquals("publicNoAnnotationBoolean", memberNameConverter.convertMemberName(isPublicNoAnnotationBoolean));
        assertNull(memberNameConverter.convertMemberName(getPublicIgnored));
        assertNull(memberNameConverter.convertMemberName(publicNoAnnotationNoPrefix));
        assertNull(memberNameConverter.convertMemberName(isPublicNoAnnotationString));
        assertNull(memberNameConverter.convertMemberName(getNoAnnotationVoid));
        assertNull(memberNameConverter.convertMemberName(getNoAnnotationPrivate));
    }

    private static class Foo {
        @JsonProperty(value = "public-field-with-annotation-and-value")
        public String publicFieldWithAnnotationAndValue;

        @JsonProperty
        public String publicFieldWithAnnotationNoValue;

        @JsonIgnore
        public String publicIgnoredField;

        public String publicNoAnnotationField;

        @JsonProperty
        private String privateFieldWithAnnotation = null;

        private String privateField = null;

        @JsonGetter(value = "public-getter-with-annotation-and-value")
        public String getPublicWithAnnotationAndValue() {
            return "foo";
        }

        @JsonGetter
        public String getPublicWithAnnotationNoValue() {
            return "foo";
        }

        @JsonGetter
        public void getVoidWithAnnotation() {
        }

        @JsonGetter
        public String publicWithAnnotationNoPrefix() {
            return "foo";
        }

        @JsonGetter
        public String isPublicWithAnnotationString() {
            return "foo";
        }

        @JsonGetter
        public boolean isPublicWithAnnotationBoolean() {
            return true;
        }

        @JsonIgnore
        public String getPublicIgnored() {
            return "foo";
        }

        public String getPublicNoAnnotation() {
            return "foo";
        }

        public String publicNoAnnotationNoPrefix() {
            return "foo";
        }

        public String isPublicNoAnnotationString() {
            return "foo";
        }

        public boolean isPublicNoAnnotationBoolean() {
            return true;
        }

        public void getNoAnnotationVoid() {
        }

        @JsonGetter
        private String getPrivateWithAnnotation() {
            return "foo";
        }

        private String getNoAnnotationPrivate() {
            return "foo";
        }
    }
}
