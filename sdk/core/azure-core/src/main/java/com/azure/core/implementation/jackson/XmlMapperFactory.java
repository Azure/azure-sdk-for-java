// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.implementation.ReflectiveInvoker;
import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.cfg.PackageVersion;

import java.lang.reflect.Array;

public final class XmlMapperFactory {
    private static final ClientLogger LOGGER = new ClientLogger(XmlMapperFactory.class);

    private static final String XML_MAPPER = "com.fasterxml.jackson.dataformat.xml.XmlMapper";
    private static final String XML_MAPPER_BUILDER = "com.fasterxml.jackson.dataformat.xml.XmlMapper$Builder";
    private static final String FROM_XML_PARSER = "com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser$Feature";
    private static final String TO_XML_GENERATOR = "com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator$Feature";
    private final ReflectiveInvoker createXmlMapperBuilder;
    private final ReflectiveInvoker defaultUseWrapper;
    private final ReflectiveInvoker enableWriteXmlDeclaration;
    private final Object writeXmlDeclaration;
    private final ReflectiveInvoker enableEmptyElementAsNull;
    private final Object emptyElementAsNull;

    final boolean useJackson212;
    private boolean jackson212IsSafe = true;

    public static final XmlMapperFactory INSTANCE = new XmlMapperFactory();

    private XmlMapperFactory() {
        ClassLoader thisClassLoader = XmlMapperFactory.class.getClassLoader();

        ReflectiveInvoker createXmlMapperBuilder;
        ReflectiveInvoker defaultUseWrapper;
        ReflectiveInvoker enableWriteXmlDeclaration;
        Object writeXmlDeclaration;
        ReflectiveInvoker enableEmptyElementAsNull;
        Object emptyElementAsNull;
        try {
            Class<?> xmlMapper = Class.forName(XML_MAPPER, true, thisClassLoader);
            Class<?> xmlMapperBuilder = Class.forName(XML_MAPPER_BUILDER, true, thisClassLoader);
            Class<?> fromXmlParser = Class.forName(FROM_XML_PARSER, true, thisClassLoader);
            Class<?> toXmlGenerator = Class.forName(TO_XML_GENERATOR, true, thisClassLoader);

            createXmlMapperBuilder = ReflectionUtils.getMethodInvoker(xmlMapper, xmlMapper.getDeclaredMethod("builder"),
                false);
            defaultUseWrapper = ReflectionUtils.getMethodInvoker(xmlMapperBuilder,
                xmlMapperBuilder.getDeclaredMethod("defaultUseWrapper", boolean.class), false);

            enableWriteXmlDeclaration = ReflectionUtils.getMethodInvoker(xmlMapperBuilder,
                xmlMapperBuilder.getDeclaredMethod("enable", Array.newInstance(toXmlGenerator, 0).getClass()), false);
            writeXmlDeclaration = toXmlGenerator.getDeclaredField("WRITE_XML_DECLARATION").get(null);
            enableEmptyElementAsNull = ReflectionUtils.getMethodInvoker(xmlMapperBuilder,
                xmlMapperBuilder.getDeclaredMethod("enable", Array.newInstance(fromXmlParser, 0).getClass()), false);
            emptyElementAsNull = fromXmlParser.getDeclaredField("EMPTY_ELEMENT_AS_NULL").get(null);
        } catch (Throwable ex) {
            // Throw the Error only if it isn't a LinkageError.
            // This initialization is attempting to use classes that may not exist.
            if (ex instanceof Error && !(ex instanceof LinkageError)) {
                throw (Error) ex;
            }

            throw LOGGER.logExceptionAsError(new IllegalStateException("Failed to retrieve invoker used to "
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
                .initializeMapperBuilder((MapperBuilder<?, ?>) createXmlMapperBuilder.invokeStatic());

            defaultUseWrapper.invokeWithArguments(xmlMapperBuilder, false);
            enableWriteXmlDeclaration.invokeWithArguments(xmlMapperBuilder, writeXmlDeclaration);

            /*
             * In Jackson 2.12 the default value of this feature changed from true to false.
             * https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.12#xml-module
             */
            enableEmptyElementAsNull.invokeWithArguments(xmlMapperBuilder, emptyElementAsNull);

            xmlMapper = xmlMapperBuilder.build();
        }  catch (Exception exception) {
            if (exception instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) exception);
            }

            throw LOGGER.logExceptionAsError(new IllegalStateException("Unable to create XmlMapper instance.",
                exception));
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
