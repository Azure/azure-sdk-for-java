// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.implementation.AccessibleByteArrayOutputStream;
import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;

import javax.xml.stream.XMLStreamException;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.invoke.MethodType.methodType;

/**
 * Utility class that handles creating and using {@code JsonSerializable} and {@code XmlSerializable} reflectively while
 * they are in beta.
 * <p>
 * Once {@code azure-json} and {@code azure-xml} GA this can be replaced with direct usage of the types. This is
 * separated out from what uses it to keep those code paths clean.
 */
public final class ReflectionSerializable {
    private static final ClientLogger LOGGER = new ClientLogger(ReflectionSerializable.class);

    private static final Class<?> JSON_SERIALIZABLE;
    private static final Class<?> JSON_READER;

    private static final CreateJsonReader JSON_READER_CREATOR;
    private static final CreateJsonWriter JSON_WRITER_CREATOR;
    private static final JsonWriterWriteJson JSON_WRITER_WRITE_JSON_SERIALIZABLE;
    private static final JsonWriterFlush JSON_WRITER_FLUSH;
    static final boolean JSON_SERIALIZABLE_SUPPORTED;
    private static final Map<Class<?>, MethodHandle> FROM_JSON_CACHE;

    private static final Class<?> XML_SERIALIZABLE;
    private static final Class<?> XML_READER;

    private static final CreateXmlReader XML_READER_CREATOR;
    private static final CreateXmlWriter XML_WRITER_CREATOR;
    private static final XmlWriterWriteStartDocument XML_WRITER_WRITE_XML_START_DOCUMENT;
    private static final XmlWriterWriteXml XML_WRITER_WRITE_XML_SERIALIZABLE;
    private static final XmlWriterFlush XML_WRITER_FLUSH;
    static final boolean XML_SERIALIZABLE_SUPPORTED;
    private static final Map<Class<?>, MethodHandle> FROM_XML_CACHE;

    static {
        MethodHandles.Lookup defaultLookup = MethodHandles.lookup();

        Class<?> jsonSerializable = null;
        Class<?> jsonReader = null;
        CreateJsonReader jsonReaderCreator = null;
        CreateJsonWriter jsonWriterCreator = null;
        JsonWriterWriteJson jsonWriterWriteJsonSerializable = null;
        JsonWriterFlush jsonWriterFlush = null;
        boolean jsonSerializableSupported = false;
        try {
            jsonSerializable = Class.forName("com.azure.json.JsonSerializable");
            jsonReader = Class.forName("com.azure.json.JsonReader");

            Class<?> jsonProviders = Class.forName("com.azure.json.JsonProviders");
            MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(jsonProviders);

            jsonReaderCreator = createMetaFactory(jsonProviders.getDeclaredMethod("createReader", byte[].class),
                lookup, CreateJsonReader.class, methodType(Closeable.class, byte[].class), defaultLookup);

            jsonWriterCreator = createMetaFactory(jsonProviders.getDeclaredMethod("createWriter", OutputStream.class),
                lookup, CreateJsonWriter.class, methodType(Closeable.class, OutputStream.class), defaultLookup);


            Class<?> jsonWriter = Class.forName("com.azure.json.JsonWriter");

            jsonWriterWriteJsonSerializable = createMetaFactory(
                jsonWriter.getDeclaredMethod("writeJson", jsonSerializable), lookup, JsonWriterWriteJson.class,
                methodType(Object.class, Object.class, Object.class), defaultLookup);

            jsonWriterFlush = createMetaFactory(jsonWriter.getDeclaredMethod("flush"), lookup, JsonWriterFlush.class,
                methodType(Object.class, Object.class), defaultLookup);

            jsonSerializableSupported = true;
        } catch (Throwable e) {
            if (e instanceof LinkageError || e instanceof Exception) {
                LOGGER.log(LogLevel.VERBOSE, () -> "JsonSerializable serialization and deserialization isn't "
                    + "supported. If it is required add a dependency of 'com.azure:azure-json', or another "
                    + "dependencies which include 'com.azure:azure-json' as a transitive dependency. If your "
                    + "application runs as expected this informational message can be ignored.");
            } else {
                throw (Error) e;
            }
        }

        JSON_SERIALIZABLE = jsonSerializable;
        JSON_READER = jsonReader;
        JSON_READER_CREATOR = jsonReaderCreator;
        JSON_WRITER_CREATOR = jsonWriterCreator;
        JSON_WRITER_WRITE_JSON_SERIALIZABLE = jsonWriterWriteJsonSerializable;
        JSON_WRITER_FLUSH = jsonWriterFlush;
        JSON_SERIALIZABLE_SUPPORTED = jsonSerializableSupported;
        FROM_JSON_CACHE = JSON_SERIALIZABLE_SUPPORTED ? new ConcurrentHashMap<>() : null;

        Class<?> xmlSerializable = null;
        Class<?> xmlReader = null;
        CreateXmlReader xmlReaderCreator = null;
        CreateXmlWriter xmlWriterCreator = null;
        XmlWriterWriteStartDocument xmlWriterWriteStartDocument = null;
        XmlWriterWriteXml xmlWriterWriteXmlSerializable = null;
        XmlWriterFlush xmlWriterFlush = null;
        boolean xmlSerializableSupported = false;
        try {
            xmlSerializable = Class.forName("com.azure.xml.XmlSerializable");
            xmlReader = Class.forName("com.azure.xml.XmlReader");

            Class<?> xmlProviders = Class.forName("com.azure.xml.XmlProviders");
            MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(xmlProviders);

            xmlReaderCreator = createMetaFactory(xmlProviders.getDeclaredMethod("createReader", byte[].class), lookup,
                CreateXmlReader.class, methodType(AutoCloseable.class, byte[].class), defaultLookup);

            xmlWriterCreator = createMetaFactory(xmlProviders.getDeclaredMethod("createWriter", OutputStream.class),
                lookup, CreateXmlWriter.class, methodType(AutoCloseable.class, OutputStream.class), defaultLookup);

            Class<?> xmlWriter = Class.forName("com.azure.xml.XmlWriter");

            xmlWriterWriteStartDocument = createMetaFactory(xmlWriter.getDeclaredMethod("writeStartDocument"), lookup,
                XmlWriterWriteStartDocument.class, methodType(Object.class, Object.class), defaultLookup);

            xmlWriterWriteXmlSerializable = createMetaFactory(xmlWriter.getDeclaredMethod("writeXml", xmlSerializable),
                lookup, XmlWriterWriteXml.class, methodType(Object.class, Object.class, Object.class), defaultLookup);

            xmlWriterFlush = createMetaFactory(xmlWriter.getDeclaredMethod("flush"), lookup, XmlWriterFlush.class,
                methodType(Object.class, Object.class), defaultLookup);

            xmlSerializableSupported = true;
        } catch (Throwable e) {
            if (e instanceof LinkageError || e instanceof Exception) {
                LOGGER.log(LogLevel.VERBOSE, () -> "XmlSerializable serialization and deserialization isn't supported. "
                    + "If it is required add a dependency of 'com.azure:azure-xml', or another dependencies which "
                    + "include 'com.azure:azure-xml' as a transitive dependency. If your application runs as expected "
                    + "this informational message can be ignored.");
            } else {
                throw (Error) e;
            }
        }

        XML_SERIALIZABLE = xmlSerializable;
        XML_READER = xmlReader;
        XML_READER_CREATOR = xmlReaderCreator;
        XML_WRITER_CREATOR = xmlWriterCreator;
        XML_WRITER_WRITE_XML_START_DOCUMENT = xmlWriterWriteStartDocument;
        XML_WRITER_WRITE_XML_SERIALIZABLE = xmlWriterWriteXmlSerializable;
        XML_WRITER_FLUSH = xmlWriterFlush;
        XML_SERIALIZABLE_SUPPORTED = xmlSerializableSupported;
        FROM_XML_CACHE = XML_SERIALIZABLE_SUPPORTED ? new ConcurrentHashMap<>() : null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T createMetaFactory(Method method, MethodHandles.Lookup unreflectLookup,
        Class<T> interfaceType, MethodType interfaceMethodType, MethodHandles.Lookup defaultLookup) throws Throwable {
        // Unreflect the method in azure-json or azure-xml using the Lookup for that module.
        MethodHandle handle = unreflectLookup.unreflect(method);

        // Get the method on the FunctionalInterface representing the call site.
        Method functionalMethod = interfaceType.getDeclaredMethods()[0];

        // Create the meta factory.
        return (T) LambdaMetafactory.metafactory(defaultLookup, functionalMethod.getName(), methodType(interfaceType),
            interfaceMethodType, handle, handle.type())
            .getTarget()
            .invoke();
    }

    /**
     * Whether {@code JsonSerializable} is supported and the {@code bodyContentClass} is an instance of it.
     *
     * @param bodyContentClass The body content class.
     * @return Whether {@code bodyContentClass} can be used as {@code JsonSerializable}.
     */
    public static boolean supportsJsonSerializable(Class<?> bodyContentClass) {
        return JSON_SERIALIZABLE_SUPPORTED && JSON_SERIALIZABLE.isAssignableFrom(bodyContentClass);
    }

    /**
     * Serializes the {@code jsonSerializable} as an instance of {@code JsonSerializable}.
     *
     * @param jsonSerializable The {@code JsonSerializable} body content.
     * @return The {@link ByteBuffer} representing the serialized {@code jsonSerializable}.
     * @throws IOException If an error occurs during serialization.
     */
    static ByteBuffer serializeAsJsonSerializable(Object jsonSerializable) throws IOException {
        try (AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
            Closeable jsonWriter = JSON_WRITER_CREATOR.createJsonWriter(outputStream)) {
            JSON_WRITER_WRITE_JSON_SERIALIZABLE.writeJson(jsonWriter, jsonSerializable);
            JSON_WRITER_FLUSH.flush(jsonWriter);

            return ByteBuffer.wrap(outputStream.toByteArray(), 0, outputStream.count());
        }
    }

    /**
     * Deserializes the {@code json} as an instance of {@code JsonSerializable}.
     *
     * @param jsonSerializable The {@code JsonSerializable} represented by the {@code json}.
     * @param json The JSON being deserialized.
     * @return An instance of {@code jsonSerializable} based on the {@code json}.
     * @throws IOException If an error occurs during deserialization.
     */
    public static Object deserializeAsJsonSerializable(Class<?> jsonSerializable, byte[] json) throws IOException {
        if (!JSON_SERIALIZABLE_SUPPORTED) {
            return null;
        }

        if (FROM_JSON_CACHE.size() >= 10000) {
            FROM_JSON_CACHE.clear();
        }

        MethodHandle readJson = FROM_JSON_CACHE.computeIfAbsent(jsonSerializable, clazz -> {
            try {
                MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(clazz);
                return lookup.unreflect(jsonSerializable.getDeclaredMethod("fromJson", JSON_READER));
            } catch (Exception e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(e));
            }
        });

        try (Closeable jsonReader = JSON_READER_CREATOR.createJsonReader(json)) {
            return readJson.invoke(jsonReader);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof Exception) {
                throw new IOException(e);
            } else {
                throw (Error) e;
            }
        }
    }

    @FunctionalInterface
    private interface CreateJsonWriter {
        Closeable createJsonWriter(OutputStream outputStream) throws IOException;
    }

    @FunctionalInterface
    private interface JsonWriterWriteJson {
        Object writeJson(Object jsonWriter, Object jsonSerializable) throws IOException;
    }

    @FunctionalInterface
    private interface  JsonWriterFlush {
        Object flush(Object jsonWriter) throws IOException;
    }

    @FunctionalInterface
    private interface CreateJsonReader {
        Closeable createJsonReader(byte[] bytes) throws IOException;
    }

    /**
     * Whether {@code XmlSerializable} is supported and the {@code bodyContentClass} is an instance of it.
     *
     * @param bodyContentClass The body content class.
     * @return Whether {@code bodyContentClass} can be used as {@code XmlSerializable}.
     */
    public static boolean supportsXmlSerializable(Class<?> bodyContentClass) {
        return XML_SERIALIZABLE_SUPPORTED && XML_SERIALIZABLE.isAssignableFrom(bodyContentClass);
    }

    /**
     * Serializes the {@code bodyContent} as an instance of {@code XmlSerializable}.
     *
     * @param bodyContent The {@code XmlSerializable} body content.
     * @return The {@link ByteBuffer} representing the serialized {@code bodyContent}.
     * @throws IOException If the XmlWriter fails to close properly.
     */
    static ByteBuffer serializeAsXmlSerializable(Object bodyContent) throws IOException {
        try (AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
            AutoCloseable xmlWriter = XML_WRITER_CREATOR.createXmlWriter(outputStream)) {
            XML_WRITER_WRITE_XML_START_DOCUMENT.writeStartDocument(xmlWriter);
            XML_WRITER_WRITE_XML_SERIALIZABLE.writeXml(xmlWriter, bodyContent);
            XML_WRITER_FLUSH.flush(xmlWriter);

            return ByteBuffer.wrap(outputStream.toByteArray(), 0, outputStream.count());
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Deserializes the {@code xml} as an instance of {@code XmlSerializable}.
     *
     * @param xmlSerializable The {@code XmlSerializable} represented by the {@code xml}.
     * @param xml The XML being deserialized.
     * @return An instance of {@code xmlSerializable} based on the {@code xml}.
     * @throws IOException If the XmlReader fails to close properly.
     */
    public static Object deserializeAsXmlSerializable(Class<?> xmlSerializable, byte[] xml) throws IOException {
        if (!XML_SERIALIZABLE_SUPPORTED) {
            return null;
        }

        if (FROM_XML_CACHE.size() >= 10000) {
            FROM_XML_CACHE.clear();
        }

        MethodHandle readXml = FROM_XML_CACHE.computeIfAbsent(xmlSerializable, clazz -> {
            try {
                MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(clazz);
                return lookup.unreflect(xmlSerializable.getMethod("fromXml", XML_READER));
            } catch (Exception e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(e));
            }
        });

        try (AutoCloseable xmlReader = XML_READER_CREATOR.createXmlReader(xml)) {
            return readXml.invoke(xmlReader);
        }  catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof Exception) {
                throw new IOException(e);
            } else {
                throw (Error) e;
            }
        }
    }

    @FunctionalInterface
    private interface CreateXmlWriter {
        AutoCloseable createXmlWriter(OutputStream outputStream) throws XMLStreamException;
    }

    @FunctionalInterface
    private interface XmlWriterWriteStartDocument {
        Object writeStartDocument(Object xmlWriter) throws XMLStreamException;
    }

    @FunctionalInterface
    private interface XmlWriterWriteXml {
        Object writeXml(Object xmlWriter, Object jsonSerializable) throws XMLStreamException;
    }

    @FunctionalInterface
    private interface XmlWriterFlush {
        Object flush(Object xmlWriter) throws XMLStreamException;
    }

    @FunctionalInterface
    private interface CreateXmlReader {
        AutoCloseable createXmlReader(byte[] bytes) throws XMLStreamException;
    }

    private ReflectionSerializable() {
    }
}
