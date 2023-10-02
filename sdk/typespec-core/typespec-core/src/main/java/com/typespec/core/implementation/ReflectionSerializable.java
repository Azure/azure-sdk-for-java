// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation;

import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.logging.LogLevel;
import com.typespec.json.JsonProviders;
import com.typespec.json.JsonReader;
import com.typespec.json.JsonSerializable;
import com.typespec.json.JsonWriter;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Utility class that handles creating and using {@code JsonSerializable} and {@code XmlSerializable} reflectively while
 * they are in beta.
 * <p>
 * Once {@code azure-json} and {@code azure-xml} GA this can be replaced with direct usage of the types. This is
 * separated out from what uses it to keep those code paths clean.
 */
public final class ReflectionSerializable {
    private static final ClientLogger LOGGER = new ClientLogger(ReflectionSerializable.class);
    private static final Map<Class<?>, MethodHandle> FROM_JSON_CACHE;

    private static final Class<?> XML_SERIALIZABLE;
    private static final Class<?> XML_READER;

    private static final XmlStreamExceptionCallable<AutoCloseable> XML_READER_CREATOR;
    private static final XmlStreamExceptionCallable<AutoCloseable> XML_WRITER_CREATOR;
    private static final XmlStreamExceptionCallable<Object> XML_WRITER_WRITE_XML_START_DOCUMENT;
    private static final XmlStreamExceptionCallable<Object> XML_WRITER_WRITE_XML_SERIALIZABLE;
    private static final XmlStreamExceptionCallable<Object> XML_WRITER_FLUSH;
    static final boolean XML_SERIALIZABLE_SUPPORTED;
    private static final Map<Class<?>, MethodHandle> FROM_XML_CACHE;

    static {
        FROM_JSON_CACHE = new ConcurrentHashMap<>();

        Class<?> xmlSerializable = null;
        Class<?> xmlReader = null;
        XmlStreamExceptionCallable<AutoCloseable> xmlReaderCreator = null;
        XmlStreamExceptionCallable<AutoCloseable> xmlWriterCreator = null;
        XmlStreamExceptionCallable<Object> xmlWriterWriteStartDocument = null;
        XmlStreamExceptionCallable<Object> xmlWriterWriteXmlSerializable = null;
        XmlStreamExceptionCallable<Object> xmlWriterFlush = null;
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
        return JsonSerializable.class.isAssignableFrom(bodyContentClass);
    }

    /**
     * Serializes the {@code jsonSerializable} as an instance of {@code JsonSerializable}.
     *
     * @param jsonSerializable The {@code JsonSerializable} body content.
     * @return The {@link ByteBuffer} representing the serialized {@code jsonSerializable}.
     * @throws IOException If an error occurs during serialization.
     */
    public static ByteBuffer serializeJsonSerializableToByteBuffer(JsonSerializable<?> jsonSerializable)
        throws IOException {
        return serializeJsonSerializableWithReturn(jsonSerializable, AccessibleByteArrayOutputStream::toByteBuffer);
    }

    /**
     * Serializes the {@code jsonSerializable} as an instance of {@code JsonSerializable}.
     *
     * @param jsonSerializable The {@code JsonSerializable} content.
     * @return The {@code byte[]} representing the serialized {@code jsonSerializable}.
     * @throws IOException If an error occurs during serialization.
     */
    public static byte[] serializeJsonSerializableToBytes(JsonSerializable<?> jsonSerializable) throws IOException {
        return serializeJsonSerializableWithReturn(jsonSerializable, AccessibleByteArrayOutputStream::toByteArray);
    }

    /**
     * Serializes the {@code jsonSerializable} as an instance of {@code JsonSerializable}.
     *
     * @param jsonSerializable The {@code JsonSerializable} content.
     * @return The {@link String} representing the serialized {@code jsonSerializable}.
     * @throws IOException If an error occurs during serialization.
     */
    public static String serializeJsonSerializableToString(JsonSerializable<?> jsonSerializable) throws IOException {
        return serializeJsonSerializableWithReturn(jsonSerializable, aos -> aos.toString(StandardCharsets.UTF_8));
    }

    private static <T> T serializeJsonSerializableWithReturn(JsonSerializable<?> jsonSerializable,
        Function<AccessibleByteArrayOutputStream, T> returner) throws IOException {
        try (AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
             JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            jsonWriter.writeJson(jsonSerializable).flush();

            return returner.apply(outputStream);
        }
    }

    /**
     * Serializes the {@code jsonSerializable} as an instance of {@code JsonSerializable}.
     *
     * @param jsonSerializable The {@code JsonSerializable} content.
     * @param outputStream Where the serialized {@code JsonSerializable} will be written.
     * @throws IOException If an error occurs during serialization.
     */
    public static void serializeJsonSerializableIntoOutputStream(JsonSerializable<?> jsonSerializable,
        OutputStream outputStream) throws IOException {
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            jsonWriter.writeJson(jsonSerializable).flush();
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
        if (FROM_JSON_CACHE.size() >= 10000) {
            FROM_JSON_CACHE.clear();
        }

        MethodHandle readJson = FROM_JSON_CACHE.computeIfAbsent(jsonSerializable, clazz -> {
            try {
                MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(clazz);
                return lookup.unreflect(jsonSerializable.getDeclaredMethod("fromJson", JsonReader.class));
            } catch (Exception e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(e));
            }
        });

        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
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
     * @param xmlSerializable The {@code XmlSerializable} body content.
     * @return The {@link ByteBuffer} representing the serialized {@code bodyContent}.
     * @throws IOException If the XmlWriter fails to close properly.
     */
    public static ByteBuffer serializeXmlSerializableToByteBuffer(Object xmlSerializable) throws IOException {
        return serializeXmlSerializableWithReturn(xmlSerializable, AccessibleByteArrayOutputStream::toByteBuffer);
    }

    /**
     * Serializes the {@code bodyContent} as an instance of {@code XmlSerializable}.
     *
     * @param xmlSerializable The {@code XmlSerializable} body content.
     * @return The {@code byte[]} representing the serialized {@code bodyContent}.
     * @throws IOException If the XmlWriter fails to close properly.
     */
    public static byte[] serializeXmlSerializableToBytes(Object xmlSerializable) throws IOException {
        return serializeXmlSerializableWithReturn(xmlSerializable, AccessibleByteArrayOutputStream::toByteArray);
    }

    /**
     * Serializes the {@code bodyContent} as an instance of {@code XmlSerializable}.
     *
     * @param xmlSerializable The {@code XmlSerializable} body content.
     * @return The {@link String} representing the serialized {@code bodyContent}.
     * @throws IOException If the XmlWriter fails to close properly.
     */
    public static String serializeXmlSerializableToString(Object xmlSerializable) throws IOException {
        return serializeXmlSerializableWithReturn(xmlSerializable, aos -> aos.toString(StandardCharsets.UTF_8));
    }

    private static <T> T serializeXmlSerializableWithReturn(Object xmlSerializable,
        Function<AccessibleByteArrayOutputStream, T> returner) throws IOException {
        try (AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
             AutoCloseable xmlWriter = XML_WRITER_CREATOR.call(outputStream)) {
            XML_WRITER_WRITE_XML_START_DOCUMENT.call(xmlWriter);
            XML_WRITER_WRITE_XML_SERIALIZABLE.call(xmlWriter, xmlSerializable);
            XML_WRITER_FLUSH.call(xmlWriter);

            return returner.apply(outputStream);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Serializes the {@code xmlSerializable} as an instance of {@code XmlSerializable}.
     *
     * @param xmlSerializable The {@code XmlSerializable} content.
     * @param outputStream Where the serialized {@code XmlSerializable} will be written.
     * @throws IOException If an error occurs during serialization.
     */
    public static void serializeXmlSerializableIntoOutputStream(Object xmlSerializable, OutputStream outputStream)
        throws IOException {
        try (AutoCloseable xmlWriter = XML_WRITER_CREATOR.call(outputStream)) {
            XML_WRITER_WRITE_XML_START_DOCUMENT.call(xmlWriter);
            XML_WRITER_WRITE_XML_SERIALIZABLE.call(xmlWriter, xmlSerializable);
            XML_WRITER_FLUSH.call(xmlWriter);
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

    /**
     * Similar to {@link java.util.concurrent.Callable} except it's checked with an {@link XMLStreamException} and
     * accepts parameters.
     *
     * @param <T> Type returned by the callable.
     */
    private interface XmlStreamExceptionCallable<T> {
        /**
         * Computes a result or throws if it's unable to do so.
         *
         * @param parameters Parameters used to compute the result.
         * @return The result.
         * @throws XMLStreamException If a result is unable to be computed.
         */
        T call(Object... parameters) throws XMLStreamException;
    }

    private static <T> XmlStreamExceptionCallable<T> createXmlCallable(Class<T> returnType, MethodHandle methodHandle) {
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
