// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.implementation.jackson.ObjectMapperShim;
import com.azure.core.util.serializer.MemberNameConverter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link JacksonJsonSerializer JacksonJsonSerializer's} {@link MemberNameConverter} functionality.
 */
@SuppressWarnings("checkstyle:VisibilityModifier")
public class JacksonMemberNameConverterTests {
    private static final String EXPECT_VALUE_IN_FIELD = "expectFieldName";
    private static final String EXPECT_VALUE_IN_METHOD = "expectMethodName";
    private static JacksonJsonSerializer jacksonJsonSerializer;

    @BeforeAll
    public static void setup() {
        ObjectMapper mapper = new ObjectMapper();

        // Configure the mapper with non-private field serialization.
        mapper.setVisibility(mapper.getVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.NON_PRIVATE));

        jacksonJsonSerializer = new JacksonJsonSerializerBuilder()
            .serializer(mapper)
            .build();
    }

    @Test
    public void testPropertyNameOnFieldName() throws NoSuchFieldException {
        class LocalHotel {
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertEquals("hotelName", jacksonJsonSerializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnIgnoredFieldName() throws NoSuchFieldException {
        class LocalHotel {
            @JsonIgnore
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertNull(jacksonJsonSerializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnTransientFieldName() throws NoSuchFieldException {
        class LocalHotel {
            transient String hotelName;

            public String getHotelName() {
                return hotelName;
            }
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertNull(jacksonJsonSerializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnFieldAnnotation() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty(value = EXPECT_VALUE_IN_FIELD)
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertEquals(EXPECT_VALUE_IN_FIELD, jacksonJsonSerializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithEmptyValue() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty(value = "")
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");

        assertEquals("hotelName", jacksonJsonSerializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithNullValue() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertEquals("hotelName", jacksonJsonSerializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnMethodName() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;
            boolean flag;

            public String getHotelName() {
                return hotelName;
            }
            public void setHotelName(String hotelName) {
                this.hotelName = hotelName;
            }

            public boolean isFlag() {
                return flag;
            }
            public void setFlag(boolean flag) {
                this.flag = flag;
            }
        }

        Method getterM = LocalHotel.class.getDeclaredMethod("getHotelName");
        assertEquals("hotelName", jacksonJsonSerializer.convertMemberName(getterM));
        Method setterM = LocalHotel.class.getDeclaredMethod("setHotelName", String.class);
        assertNull(jacksonJsonSerializer.convertMemberName(setterM));
        Method getterWithIs = LocalHotel.class.getDeclaredMethod("isFlag");
        assertEquals("flag", jacksonJsonSerializer.convertMemberName(getterWithIs));
        Method setterWithIs = LocalHotel.class.getDeclaredMethod("setFlag", boolean.class);
        assertNull(jacksonJsonSerializer.convertMemberName(setterWithIs));
    }

    @Test
    public void testPropertyNameOnMethodNameWithoutGetSet() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;

            public String hotelName1() {
                return hotelName;
            }

            public void hotelName2(String hotelName) {
                this.hotelName = hotelName;
            }
        }

        Method getterM = LocalHotel.class.getDeclaredMethod("hotelName1");
        assertNull(jacksonJsonSerializer.convertMemberName(getterM));
        Method setterM = LocalHotel.class.getDeclaredMethod("hotelName2", String.class);
        assertNull(jacksonJsonSerializer.convertMemberName(setterM));
    }

    @Test
    public void testPropertyNameOnMethodNameWithGetInMiddle() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;
            String hotelId;
            String flag;

            public String hotelgetName() {
                return hotelName;
            }

            public String getHotelGetId() {
                return hotelId;
            }

            public String isFlag() {
                return flag;
            }
        }

        Method getterM1 = LocalHotel.class.getDeclaredMethod("hotelgetName");
        Method getterM2 = LocalHotel.class.getDeclaredMethod("getHotelGetId");
        Method getterM3 = LocalHotel.class.getDeclaredMethod("isFlag");

        assertNull(jacksonJsonSerializer.convertMemberName(getterM1));
        assertEquals("hotelGetId", jacksonJsonSerializer.convertMemberName(getterM2));
        assertNull(jacksonJsonSerializer.convertMemberName(getterM3));
    }
    @Test
    public void testPropertyNameOnIgnoredMethodName() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;

            @JsonIgnore
            public String getHotelName() {
                return hotelName;
            }
        }
        Method m = LocalHotel.class.getDeclaredMethod("getHotelName");
        assertNull(jacksonJsonSerializer.convertMemberName(m));
    }

    @Test
    public void testPropertyNameOnMethodAnnotation() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;

            @JsonProperty(value = EXPECT_VALUE_IN_METHOD)
            public String getHotelName() {
                return hotelName;
            }
        }

        Method m = LocalHotel.class.getDeclaredMethod("getHotelName");
        assertEquals(EXPECT_VALUE_IN_METHOD, jacksonJsonSerializer.convertMemberName(m));
    }


    @Test
    public void testPropertyNameOnMethodAnnotationWithEmptyValue() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;

            @JsonProperty(value = "")
            public String getHotelName() {
                return hotelName;
            }
        }

        Method m = LocalHotel.class.getDeclaredMethod("getHotelName");
        assertEquals("hotelName", jacksonJsonSerializer.convertMemberName(m));
    }

    @Test
    public void testPropertyNameOnMethodAnnotationWithNullValue() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;

            @JsonProperty()
            public String getHotelName() {
                return hotelName;
            }
        }

        Method m = LocalHotel.class.getDeclaredMethod("getHotelName");

        assertEquals("hotelName", jacksonJsonSerializer.convertMemberName(m));
    }

    @Test
    public void testPropertyNameOnConstructor() {
        Constructor<?>[] constructors = Hotel.class.getConstructors();
        assertEquals(1, constructors.length);

        assertNull(jacksonJsonSerializer.convertMemberName(constructors[0]));
    }

    @SuppressWarnings("removal")
    @ParameterizedTest
    @MethodSource("classConversionSupplier")
    public <T> void classConversion(T object, JacksonJsonSerializer converter, Set<String> expected)
        throws Exception {
        Set<String> actual = getAllDeclaredMembers(object.getClass())
            .map(converter::convertMemberName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        assertEquals(expected, actual);

        Field field = JacksonJsonSerializer.class.getDeclaredField("mapper");
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            field.setAccessible(true);
            return null;
        });

        ObjectNode objectNode = ((ObjectMapperShim) field.get(converter)).valueToTree(object);

        for (String name : actual) {
            assertTrue(objectNode.has(name));
        }
    }

    private static Stream<Arguments> classConversionSupplier() {
        return Stream.of(
            Arguments.of(new NoAnnotationsPublicFields(50, "John Doe"), jacksonJsonSerializer,
                new HashSet<>(Arrays.asList("age", "name"))),

            Arguments.of(new NoAnnotationsGetters(50, "John Doe"), jacksonJsonSerializer,
                new HashSet<>(Arrays.asList("age", "name"))),

            Arguments.of(new GettersWithAnnotations().setAge(50).setName("John Doe"), jacksonJsonSerializer,
                new HashSet<>(Arrays.asList("_age", "_name")))
        );
    }

    private static Stream<Member> getAllDeclaredMembers(Class<?> clazz) {
        List<Member> members = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        members.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        members.addAll(Arrays.asList(clazz.getDeclaredConstructors()));

        return members.stream();
    }

    /**
     *
     */
    public static final class NoAnnotationsPublicFields {
        final int age;
        final String name;

        public NoAnnotationsPublicFields(int age, String name) {
            this.age = age;
            this.name = name;
        }

        public void notAGetter() {
        }

        public int alsoNotAGetter(String parameter) {
            return 0;
        }
    }

    public static final class NoAnnotationsGetters {
        private final int age;
        private final String name;

        public int getAge() {
            return age;
        }

        public String getName() {
            return name;
        }

        public NoAnnotationsGetters(int age, String name) {
            this.age = age;
            this.name = name;
        }

        public void notAGetter() {
        }

        public int alsoNotAGetter(String parameter) {
            return 0;
        }
    }

    public static final class GettersWithAnnotations {
        private int age;
        private String name;

        @JsonProperty("_age")
        public int getAge() {
            return age;
        }

        public GettersWithAnnotations setAge(int age) {
            this.age = age;
            return this;
        }

        @JsonProperty("_name")
        public String getName() {
            return name;
        }

        public GettersWithAnnotations setName(String name) {
            this.name = name;
            return this;
        }

        public void notAGetter() {
        }

        public int alsoNotAGetter(String parameter) {
            return 0;
        }
    }
}
