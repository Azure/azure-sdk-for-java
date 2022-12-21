// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class XmlMapperFactory {
    private static final ClientLogger LOGGER = new ClientLogger(XmlMapperFactory.class);

    private static final String MUTABLE_COERCION_CONFIG = "com.fasterxml.jackson.databind.cfg.MutableCoercionConfig";
    private static final String COERCION_INPUT_SHAPE = "com.fasterxml.jackson.databind.cfg.CoercionInputShape";
    private static final String COERCION_ACTION = "com.fasterxml.jackson.databind.cfg.CoercionAction";

    private MethodHandle coercionConfigDefaults;
    private MethodHandle setCoercion;
    private Object coercionInputShapeEmptyString;
    private Object coercionActionAsNull;
    private boolean useReflectionToSetCoercion;

    public static final XmlMapperFactory INSTANCE = new XmlMapperFactory();

    private XmlMapperFactory() {
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

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
    }

    public ObjectMapper createXmlMapper() {
        ObjectMapper xmlMapper = ObjectMapperFactory.initializeMapperBuilder(XmlMapper.builder())
            .defaultUseWrapper(false)
            .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
            /*
             * In Jackson 2.12 the default value of this feature changed from true to false.
             * https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.12#xml-module
             */
            .enable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL)
            .build();

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
