// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.logging.LogLevel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.cfg.PackageVersion;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
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

    final boolean useJackson212;
    private boolean jackson212IsSafe = true;

    public static final XmlMapperFactory INSTANCE = new XmlMapperFactory();

    private XmlMapperFactory() {
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
        ClassLoader thisClassLoader = XmlMapperFactory.class.getClassLoader();

        MethodHandle createXmlMapperBuilder;
        MethodHandle defaultUseWrapper;
        MethodHandle enableWriteXmlDeclaration;
        Object writeXmlDeclaration;
        MethodHandle enableEmptyElementAsNull;
        Object emptyElementAsNull;
        try {
            Class<?> xmlMapper = Class.forName(XML_MAPPER, true, thisClassLoader);
            Class<?> xmlMapperBuilder = Class.forName(XML_MAPPER_BUILDER, true, thisClassLoader);
            Class<?> fromXmlParser = Class.forName(FROM_XML_PARSER, true, thisClassLoader);
            Class<?> toXmlGenerator = Class.forName(TO_XML_GENERATOR, true, thisClassLoader);

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

        this.useJackson212 = PackageVersion.VERSION.getMinorVersion() >= 12;
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

        if (useJackson212 && jackson212IsSafe) {
            try {
                return JacksonDatabind212.mutateXmlCoercions(xmlMapper);
            } catch (Throwable ex) {
                if (ex instanceof LinkageError) {
                    jackson212IsSafe = false;
                    LOGGER.log(LogLevel.VERBOSE, JacksonVersion::getHelpInfo, ex);
                }

                throw ex;
            }
        }

        return xmlMapper;
    }
}
