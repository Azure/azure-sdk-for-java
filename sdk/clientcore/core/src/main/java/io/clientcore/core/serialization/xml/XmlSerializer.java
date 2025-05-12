// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.serialization.xml;

import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.serialization.SerializationFormat;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * Class providing basic XML serialization and deserialization methods.
 * <p>
 * The implementation of this class is based on the usage of {@link XmlReader} and {@link XmlWriter}.
 * <p>
 * The deserialization methods only work with models implementing {@link XmlSerializable}. Or, in code terms, types that
 * provide a static factory method {@code fromXml(XmlReader)}.
 * <p>
 * The serialization methods only work with complex types that implement {@link XmlSerializable}.
 */
public class XmlSerializer implements ObjectSerializer {
    private static final ClientLogger LOGGER = new ClientLogger(XmlSerializer.class);

    private static final XmlSerializer INSTANCE = new XmlSerializer();

    /**
     * Get an instance of the {@link XmlSerializer}
     * @return An instance of {@link XmlSerializer}
     */
    public static XmlSerializer getInstance() {
        return INSTANCE;
    }

    /**
     * Creates an instance of the {@link XmlSerializer}.
     */
    public XmlSerializer() {
    }

    /**
     * Reads an XML byte array into its object representation.
     *
     * @param bytes The XML byte array.
     * @param type {@link Type} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized XML byte array.
     * @throws IOException If the deserialization fails.
     */
    @Override
    public <T> T deserializeFromBytes(byte[] bytes, Type type) throws IOException {
        try (XmlReader xmlReader = XmlReader.fromBytes(bytes)) {
            return deserializeShared(xmlReader, type);
        } catch (XMLStreamException ex) {
            throw LOGGER.throwableAtError().log(ex, IOException::new);
        }
    }

    /**
     * Reads an XML stream into its object representation.
     *
     * @param stream XML stream.
     * @param type {@link Type} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized XML stream.
     * @throws IOException If the deserialization fails.
     */
    @Override
    public <T> T deserializeFromStream(InputStream stream, Type type) throws IOException {
        try (XmlReader xmlReader = XmlReader.fromStream(stream)) {
            return deserializeShared(xmlReader, type);
        } catch (XMLStreamException ex) {
            throw LOGGER.throwableAtError().log(ex, IOException::new);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T deserializeShared(XmlReader xmlReader, Type type) {
        try {
            if (XmlSerializable.class.isAssignableFrom(TypeUtil.getRawClass(type))) {
                Class<T> clazz = (Class<T>) type;

                return (T) clazz.getMethod("fromXml", XmlReader.class).invoke(null, xmlReader);
            } else {
                // TODO (alzimmer): XML needs untyped support.

                throw LOGGER.throwableAtError()
                    .log("XmlSerializer does not have support for untyped deserialization.",
                        UnsupportedOperationException::new);
            }
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw LOGGER.throwableAtError().log(e, RuntimeException::new);
        }
    }

    /**
     * Converts the object into an XML byte array.
     * <p>
     * This method writes both the XML declaration and contents of the object.
     *
     * @param value The object.
     * @return The XML binary representation of the serialized object.
     * @throws IOException If the serialization fails.
     */
    @Override
    public byte[] serializeToBytes(Object value) throws IOException {
        if (value == null) {
            return null;
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            XmlWriter xmlWriter = XmlWriter.toStream(byteArrayOutputStream)) {
            serializeShared(xmlWriter, value);

            return byteArrayOutputStream.toByteArray();
        } catch (XMLStreamException ex) {
            throw LOGGER.throwableAtError().log(ex, IOException::new);
        }
    }

    /**
     * Writes an object's XML representation into a stream.
     * <p>
     * This method writes both the XML declaration and contents of the object.
     *
     * @param stream {@link OutputStream} where the object's XML representation will be written.
     * @param value The object to serialize.
     * @throws IOException If the serialization fails.
     */
    @Override
    public void serializeToStream(OutputStream stream, Object value) throws IOException {
        if (value == null) {
            return;
        }

        try (XmlWriter xmlWriter = XmlWriter.toStream(stream)) {
            serializeShared(xmlWriter, value);
        } catch (XMLStreamException ex) {
            throw LOGGER.throwableAtError().log(ex, IOException::new);
        }
    }

    private static void serializeShared(XmlWriter xmlWriter, Object value) {
        try {
            if (value instanceof XmlSerializable) {
                xmlWriter.writeStartDocument();
                ((XmlSerializable<?>) value).toXml(xmlWriter).flush();
            } else {
                // TODO (alzimmer): XML needs untyped support.
                throw LOGGER.throwableAtError()
                    .log("XmlSerializer does not have support for untyped serialization.",
                        UnsupportedOperationException::new);
            }
        } catch (XMLStreamException e) {
            throw LOGGER.throwableAtError().log(e, RuntimeException::new);
        }
    }

    @Override
    public final boolean supportsFormat(SerializationFormat format) {
        return format == SerializationFormat.XML;
    }
}
