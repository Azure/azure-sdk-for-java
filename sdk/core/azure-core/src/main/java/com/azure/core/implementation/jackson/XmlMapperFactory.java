// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;

public final class XmlMapperFactory {
    private static final ClientLogger LOGGER = new ClientLogger(XmlMapperFactory.class);

    private static final String XML_MAPPER = "com.fasterxml.jackson.dataformat.xml.XmlMapper";
    private static final String XML_MAPPER_BUILDER = "com.fasterxml.jackson.dataformat.xml.XmlMapper$Builder";
    private static final String FROM_XML_PARSER = "com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser$Feature";
    private static final String TO_XML_GENERATOR = "com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator$Feature";
    private final MethodHandle createXmlMapperBuilder;
    private final MethodHandle defaultUseWrapper;
    private final MethodHandle enableWriteXmlDeclaration;
    private final Object writeXmlDeclaration;
    private final MethodHandle enableEmptyElementAsNull;
    private final Object emptyElementAsNull;

    private static final String MUTABLE_COERCION_CONFIG = "com.fasterxml.jackson.databind.cfg.MutableCoercionConfig";
    private static final String COERCION_INPUT_SHAPE = "com.fasterxml.jackson.databind.cfg.CoercionInputShape";
    private static final String COERCION_ACTION = "com.fasterxml.jackson.databind.cfg.CoercionAction";

    private final MethodHandle coercionConfigDefaults;
    private final MethodHandle setCoercion;
    private final Object coercionInputShapeEmptyString;
    private final Object coercionActionAsNull;
    private final boolean useReflectionToSetCoercion;

    public static final XmlMapperFactory INSTANCE = new XmlMapperFactory();

    private XmlMapperFactory() {
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

        MethodHandle createXmlMapperBuilder;
        MethodHandle defaultUseWrapper;
        MethodHandle enableWriteXmlDeclaration;
        Object writeXmlDeclaration;
        MethodHandle enableEmptyElementAsNull;
        Object emptyElementAsNull;
        try {
            Class<?> xmlMapper = Class.forName(XML_MAPPER);
            Class<?> xmlMapperBuilder = Class.forName(XML_MAPPER_BUILDER);
            Class<?> fromXmlParser = Class.forName(FROM_XML_PARSER);
            Class<?> toXmlGenerator = Class.forName(TO_XML_GENERATOR);

            createXmlMapperBuilder = publicLookup.unreflect(xmlMapper.getDeclaredMethod("builder"));
            defaultUseWrapper = publicLookup.unreflect(xmlMapperBuilder.getDeclaredMethod("defaultUseWrapper",
                boolean.class));

            enableWriteXmlDeclaration = publicLookup.unreflect(xmlMapperBuilder.getDeclaredMethod("enable",
                Array.newInstance(toXmlGenerator, 0).getClass()));
            writeXmlDeclaration = toXmlGenerator.getDeclaredField("WRITE_XML_DECLARATION").get(null);
            enableEmptyElementAsNull = publicLookup.unreflect(xmlMapperBuilder.getDeclaredMethod("enable",
                Array.newInstance(fromXmlParser, 0).getClass()));
            emptyElementAsNull = fromXmlParser.getDeclaredField("EMPTY_ELEMENT_AS_NULL").get(null);
        } catch (Throwable ex) {
            // Throw the Error only if it isn't a LinkageError.
            // This initialization is attempting to use classes that may not exist.
            if (ex instanceof Error && !(ex instanceof LinkageError)) {
                throw (Error) ex;
            }

            throw LOGGER.logExceptionAsError(new IllegalStateException("Failed to retrieve MethodHandles used to "
                + "create XmlMapper. XML serialization won't be supported until "
                + "'com.fasterxml.jackson.dataformat:jackson-dataformat-xml' is added to the classpath or updated to a "
                + "supported version. " + JacksonVersion.getHelpInfo(), ex));
        }

        this.createXmlMapperBuilder = createXmlMapperBuilder;
        this.defaultUseWrapper = defaultUseWrapper;
        this.enableWriteXmlDeclaration = enableWriteXmlDeclaration;
        this.writeXmlDeclaration = writeXmlDeclaration;
        this.enableEmptyElementAsNull = enableEmptyElementAsNull;
        this.emptyElementAsNull = emptyElementAsNull;

        MethodHandle coercionConfigDefaults = null;
        MethodHandle setCoercion = null;
        Object coercionInputShapeEmptyString = null;
        Object coercionActionAsNull = null;
        boolean useReflectionToSetCoercion = false;
        try {
            Class<?> mutableCoercionConfig = Class.forName(MUTABLE_COERCION_CONFIG);
            Class<?> coercionInputShapeClass = Class.forName(COERCION_INPUT_SHAPE);
            Class<?> coercionActionClass = Class.forName(COERCION_ACTION);

            coercionConfigDefaults = publicLookup.findVirtual(ObjectMapper.class, "coercionConfigDefaults",
                MethodType.methodType(mutableCoercionConfig));
            setCoercion = publicLookup.findVirtual(mutableCoercionConfig, "setCoercion",
                MethodType.methodType(mutableCoercionConfig, coercionInputShapeClass, coercionActionClass));
            coercionInputShapeEmptyString = publicLookup.findStaticGetter(coercionInputShapeClass, "EmptyString",
                coercionInputShapeClass).invoke();
            coercionActionAsNull = publicLookup.findStaticGetter(coercionActionClass, "AsNull", coercionActionClass)
                .invoke();
            useReflectionToSetCoercion = true;
        } catch (Throwable ex) {
            // Throw the Error only if it isn't a LinkageError.
            // This initialization is attempting to use classes that may not exist.
            if (ex instanceof Error && !(ex instanceof LinkageError)) {
                throw (Error) ex;
            }

            LOGGER.verbose("Failed to retrieve MethodHandles used to set coercion configurations. "
                + "Setting coercion configurations will be skipped. "
                + "Please update your Jackson dependencies to at least version 2.12", ex);
        }

        this.coercionConfigDefaults = coercionConfigDefaults;
        this.setCoercion = setCoercion;
        this.coercionInputShapeEmptyString = coercionInputShapeEmptyString;
        this.coercionActionAsNull = coercionActionAsNull;
        this.useReflectionToSetCoercion = useReflectionToSetCoercion;
    }

    public ObjectMapper createXmlMapper() {
        ObjectMapper xmlMapper;
        try {
            MapperBuilder<?, ?> xmlMapperBuilder = ObjectMapperFactory
                .initializeMapperBuilder((MapperBuilder<?, ?>) createXmlMapperBuilder.invoke());

            defaultUseWrapper.invokeWithArguments(xmlMapperBuilder, false);
            enableWriteXmlDeclaration.invokeWithArguments(xmlMapperBuilder, writeXmlDeclaration);

            /*
             * In Jackson 2.12 the default value of this feature changed from true to false.
             * https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.12#xml-module
             */
            enableEmptyElementAsNull.invokeWithArguments(xmlMapperBuilder, emptyElementAsNull);

            xmlMapper = xmlMapperBuilder.build();
        }  catch (Throwable e) {
            if (e instanceof Error) {
                throw (Error) e;
            }

            throw LOGGER.logExceptionAsError(new IllegalStateException("Unable to create XmlMapper instance.", e));
        }

        if (useReflectionToSetCoercion) {
            try {
                Object object = coercionConfigDefaults.invoke(xmlMapper);
                setCoercion.invoke(object, coercionInputShapeEmptyString, coercionActionAsNull);
            } catch (Throwable e) {
                if (e instanceof Error) {
                    throw (Error) e;
                }

                LOGGER.verbose("Failed to set coercion actions.", e);
            }
        } else {
            LOGGER.verbose("Didn't set coercion defaults as it wasn't found on the classpath.");
        }

        return xmlMapper;
    }
}
