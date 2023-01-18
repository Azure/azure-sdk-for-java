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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final IOExceptionCallable<Closeable> JSON_READER_CREATOR;
    private static final IOExceptionCallable<Closeable> JSON_WRITER_CREATOR;
    private static final IOExceptionCallable<Object> JSON_WRITER_WRITE_JSON_SERIALIZABLE;
    private static final IOExceptionCallable<Object> JSON_WRITER_FLUSH;
    static final boolean JSON_SERIALIZABLE_SUPPORTED;
    private static final Map<Class<?>, MethodHandle> FROM_JSON_CACHE;

    private static final Class<?> XML_SERIALIZABLE;
    private static final Class<?> XML_READER;

    private static final XMLStreamExceptionCallable<AutoCloseable> XML_READER_CREATOR;
    private static final XMLStreamExceptionCallable<AutoCloseable> XML_WRITER_CREATOR;
    private static final XMLStreamExceptionCallable<Object> XML_WRITER_WRITE_XML_START_DOCUMENT;
    private static final XMLStreamExceptionCallable<Object> XML_WRITER_WRITE_XML_SERIALIZABLE;
    private static final XMLStreamExceptionCallable<Object> XML_WRITER_FLUSH;
    static final boolean XML_SERIALIZABLE_SUPPORTED;
    private static final Map<Class<?>, MethodHandle> FROM_XML_CACHE;

    static {
        Class<?> jsonSerializable = null;
        Class<?> jsonReader = null;
        IOExceptionCallable<Closeable> jsonReaderCreator = null;
        IOExceptionCallable<Closeable> jsonWriterCreator = null;
        IOExceptionCallable<Object> jsonWriterWriteJsonSerializable = null;
        IOExceptionCallable<Object> jsonWriterFlush = null;
        boolean jsonSerializableSupported = false;
        try {
            jsonSerializable = Class.forName("com.azure.json.JsonSerializable");
            jsonReader = Class.forName("com.azure.json.JsonReader");

            Class<?> jsonProviders = Class.forName("com.azure.json.JsonProviders");
            MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(jsonProviders);

            MethodHandle handle = lookup.unreflect(jsonProviders.getDeclaredMethod("createReader", byte[].class));
            jsonReaderCreator = createJsonCallable(Closeable.class, handle);

            handle = lookup.unreflect(jsonProviders.getDeclaredMethod("createWriter", OutputStream.class));
            jsonWriterCreator = createJsonCallable(Closeable.class, handle);


            Class<?> jsonWriter = Class.forName("com.azure.json.JsonWriter");

            handle = lookup.unreflect(jsonWriter.getDeclaredMethod("writeJson", jsonSerializable));
            jsonWriterWriteJsonSerializable = createJsonCallable(Object.class, handle);

            handle = lookup.unreflect(jsonWriter.getDeclaredMethod("flush"));
            jsonWriterFlush = createJsonCallable(Object.class, handle);

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
        XMLStreamExceptionCallable<AutoCloseable> xmlReaderCreator = null;
        XMLStreamExceptionCallable<AutoCloseable> xmlWriterCreator = null;
        XMLStreamExceptionCallable<Object> xmlWriterWriteStartDocument = null;
        XMLStreamExceptionCallable<Object> xmlWriterWriteXmlSerializable = null;
        XMLStreamExceptionCallable<Object> xmlWriterFlush = null;
        boolean xmlSerializableSupported = false;
        try {
            xmlSerializable = Class.forName("com.azure.xml.XmlSerializable");
            xmlReader = Class.forName("com.azure.xml.XmlReader");

            Class<?> xmlProviders = Class.forName("com.azure.xml.XmlProviders");
            MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(xmlProviders);

            MethodHandle handle = lookup.unreflect(xmlProviders.getDeclaredMethod("createReader", byte[].class));
            xmlReaderCreator = createXmlCallable(AutoCloseable.class, handle);

            handle = lookup.unreflect(xmlProviders.getDeclaredMethod("createWriter", OutputStream.class));
            xmlWriterCreator = createXmlCallable(AutoCloseable.class, handle);

            Class<?> xmlWriter = Class.forName("com.azure.xml.XmlWriter");

            handle = lookup.unreflect(xmlWriter.getDeclaredMethod("writeStartDocument"));
            xmlWriterWriteStartDocument = createXmlCallable(Object.class, handle);

            handle = lookup.unreflect(xmlWriter.getDeclaredMethod("writeXml", xmlSerializable));
            xmlWriterWriteXmlSerializable = createXmlCallable(Object.class, handle);

            handle = lookup.unreflect(xmlWriter.getDeclaredMethod("flush"));
            xmlWriterFlush = createXmlCallable(Object.class, handle);

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
            Closeable jsonWriter = JSON_WRITER_CREATOR.call(outputStream)) {
            JSON_WRITER_WRITE_JSON_SERIALIZABLE.call(jsonWriter, jsonSerializable);
            JSON_WRITER_FLUSH.call(jsonWriter);

            return outputStream.toByteBuffer();
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

        try (Closeable jsonReader = JSON_READER_CREATOR.call((Object) json)) {
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

    private interface IOExceptionCallable<T> {
        T call(Object... parameters) throws IOException;
    }

    private static <T> IOExceptionCallable<T> createJsonCallable(Class<T> returnType, MethodHandle methodHandle) {
        return parameters -> {
            try {
                return returnType.cast(methodHandle.invokeWithArguments(parameters));
            } catch (Throwable throwable) {
                if (throwable instanceof Error) {
                    throw (Error) throwable;
                } else if (throwable instanceof IOException) {
                    throw (IOException) throwable;
                } else {
                    throw new IOException(throwable);
                }
            }
        };
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
            AutoCloseable xmlWriter = XML_WRITER_CREATOR.call(outputStream)) {
            XML_WRITER_WRITE_XML_START_DOCUMENT.call(xmlWriter);
            XML_WRITER_WRITE_XML_SERIALIZABLE.call(xmlWriter, bodyContent);
            XML_WRITER_FLUSH.call(xmlWriter);

            return outputStream.toByteBuffer();
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

        try (AutoCloseable xmlReader = XML_READER_CREATOR.call((Object) xml)) {
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

    private interface XMLStreamExceptionCallable<T> {
        T call(Object... parameters) throws XMLStreamException;
    }

    private static <T> XMLStreamExceptionCallable<T> createXmlCallable(Class<T> returnType, MethodHandle methodHandle) {
        return parameters -> {
            try {
                return returnType.cast(methodHandle.invokeWithArguments(parameters));
            } catch (Throwable throwable) {
                if (throwable instanceof Error) {
                    throw (Error) throwable;
                } else if (throwable instanceof XMLStreamException) {
                    throw (XMLStreamException) throwable;
                } else {
                    throw new XMLStreamException(throwable);
                }
            }
        };
    }

    private ReflectionSerializable() {
    }
}
