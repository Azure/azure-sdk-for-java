// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlWriter;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
    private static final Map<Class<?>, ReflectiveInvoker> FROM_JSON_CACHE;
    private static final Map<Class<?>, ReflectiveInvoker> FROM_XML_CACHE;

    static {
        FROM_JSON_CACHE = new ConcurrentHashMap<>();
        FROM_XML_CACHE = new ConcurrentHashMap<>();
    }

    /**
     * Whether {@code JsonSerializable} is supported and the {@code bodyContentClass} is an instance of it.
     *
     * @param bodyContentClass The body content class.
     * @return Whether {@code bodyContentClass} can be used as {@code JsonSerializable}.
     */
    public static boolean supportsJsonSerializable(Class<?> bodyContentClass) {
        if (FROM_JSON_CACHE.containsKey(bodyContentClass)) {
            return true;
        }

        if (!JsonSerializable.class.isAssignableFrom(bodyContentClass)) {
            return false;
        }

        boolean hasFromJson = false;
        boolean hasToJson = false;
        for (Method method : bodyContentClass.getDeclaredMethods()) {
            if (method.getName().equals("fromJson")
                && (method.getModifiers() & Modifier.STATIC) != 0
                && method.getParameterCount() == 1
                && method.getParameterTypes()[0].equals(JsonReader.class)) {
                hasFromJson = true;
            } else if (method.getName().equals("toJson")
                && method.getParameterCount() == 1
                && method.getParameterTypes()[0].equals(JsonWriter.class)) {
                hasToJson = true;
            }

            if (hasFromJson && hasToJson) {
                return true;
            }
        }

        return false;
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
     * @throws IllegalStateException If the {@code jsonSerializable} does not have a static {@code fromJson} method
     * @throws Error If an error occurs during deserialization.
     */
    public static Object deserializeAsJsonSerializable(Class<?> jsonSerializable, byte[] json) throws IOException {
        if (FROM_JSON_CACHE.size() >= 10000) {
            FROM_JSON_CACHE.clear();
        }

        ReflectiveInvoker readJson = FROM_JSON_CACHE.computeIfAbsent(jsonSerializable, clazz -> {
            try {
                return ReflectionUtils.getMethodInvoker(clazz,
                    jsonSerializable.getDeclaredMethod("fromJson", JsonReader.class));
            } catch (Exception e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(e));
            }
        });

        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return readJson.invokeStatic(jsonReader);
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
        if (FROM_XML_CACHE.containsKey(bodyContentClass)) {
            return true;
        }

        if (!XmlSerializable.class.isAssignableFrom(bodyContentClass)) {
            return false;
        }

        boolean hasFromXml = false;
        boolean hasToXml = false;
        for (Method method : bodyContentClass.getDeclaredMethods()) {
            if (method.getName().equals("fromXml")
                && (method.getModifiers() & Modifier.STATIC) != 0
                && method.getParameterCount() == 2
                && method.getParameterTypes()[0].equals(XmlReader.class)
                && method.getParameterTypes()[1].equals(String.class)) {
                hasFromXml = true;
            } else if (method.getName().equals("toXml")
                && method.getParameterCount() == 2
                && method.getParameterTypes()[0].equals(XmlWriter.class)
                && method.getParameterTypes()[1].equals(String.class)) {
                hasToXml = true;
            }

            if (hasFromXml && hasToXml) {
                return true;
            }
        }

        return false;
    }

    /**
     * Serializes the {@code bodyContent} as an instance of {@code XmlSerializable}.
     *
     * @param xmlSerializable The {@code XmlSerializable} body content.
     * @return The {@link ByteBuffer} representing the serialized {@code bodyContent}.
     * @throws IOException If the XmlWriter fails to close properly.
     */
    public static ByteBuffer serializeXmlSerializableToByteBuffer(XmlSerializable<?> xmlSerializable)
        throws IOException {
        return serializeXmlSerializableWithReturn(xmlSerializable, AccessibleByteArrayOutputStream::toByteBuffer);
    }

    /**
     * Serializes the {@code bodyContent} as an instance of {@code XmlSerializable}.
     *
     * @param xmlSerializable The {@code XmlSerializable} body content.
     * @return The {@code byte[]} representing the serialized {@code bodyContent}.
     * @throws IOException If the XmlWriter fails to close properly.
     */
    public static byte[] serializeXmlSerializableToBytes(XmlSerializable<?> xmlSerializable) throws IOException {
        return serializeXmlSerializableWithReturn(xmlSerializable, AccessibleByteArrayOutputStream::toByteArray);
    }

    /**
     * Serializes the {@code bodyContent} as an instance of {@code XmlSerializable}.
     *
     * @param xmlSerializable The {@code XmlSerializable} body content.
     * @return The {@link String} representing the serialized {@code bodyContent}.
     * @throws IOException If the XmlWriter fails to close properly.
     */
    public static String serializeXmlSerializableToString(XmlSerializable<?> xmlSerializable) throws IOException {
        return serializeXmlSerializableWithReturn(xmlSerializable, aos -> aos.toString(StandardCharsets.UTF_8));
    }

    private static <T> T serializeXmlSerializableWithReturn(XmlSerializable<?> xmlSerializable,
        Function<AccessibleByteArrayOutputStream, T> returner) throws IOException {
        try (AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
            XmlWriter xmlWriter = XmlWriter.toStream(outputStream)) {
            xmlWriter.writeStartDocument();
            xmlWriter.writeXml(xmlSerializable);
            xmlWriter.flush();

            return returner.apply(outputStream);
        } catch (XMLStreamException ex) {
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
    public static void serializeXmlSerializableIntoOutputStream(XmlSerializable<?> xmlSerializable,
        OutputStream outputStream) throws IOException {
        try (XmlWriter xmlWriter = XmlWriter.toStream(outputStream)) {
            xmlWriter.writeStartDocument();
            xmlWriter.writeXml(xmlSerializable);
            xmlWriter.flush();
        } catch (XMLStreamException ex) {
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
     * @throws IllegalStateException If the {@code xmlSerializable} does not have a static {@code fromXml} method
     * @throws Error If an error occurs during deserialization.
     */
    public static Object deserializeAsXmlSerializable(Class<?> xmlSerializable, byte[] xml) throws IOException {
        if (FROM_XML_CACHE.size() >= 10000) {
            FROM_XML_CACHE.clear();
        }

        ReflectiveInvoker readXml = FROM_XML_CACHE.computeIfAbsent(xmlSerializable, clazz -> {
            try {
                return ReflectionUtils.getMethodInvoker(xmlSerializable,
                    xmlSerializable.getDeclaredMethod("fromXml", XmlReader.class));
            } catch (Exception e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(e));
            }
        });

        try (XmlReader xmlReader = XmlReader.fromBytes(xml)) {
            return readXml.invokeStatic(xmlReader);
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

    private ReflectionSerializable() {
    }
}
