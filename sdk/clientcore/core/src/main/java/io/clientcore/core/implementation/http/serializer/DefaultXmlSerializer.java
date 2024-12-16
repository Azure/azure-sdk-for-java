// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.serializer;

import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.serialization.xml.XmlReader;
import io.clientcore.core.serialization.xml.XmlSerializable;
import io.clientcore.core.serialization.xml.XmlWriter;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.serializer.ObjectSerializer;
import io.clientcore.core.util.serializer.XmlSerializer;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * Default implementation of the {@link XmlSerializer}.
 */
public class DefaultXmlSerializer extends XmlSerializer {
    // DefaultXmlSerializer is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(DefaultXmlSerializer.class);

    /**
     * Creates an instance of the {@link DefaultXmlSerializer}.
     */
    public DefaultXmlSerializer() {
    }

    @Override
    public <T> T deserializeFromBytes(byte[] bytes, Type type, ObjectSerializer.Format format) throws IOException {
        verifyFormat(format);

        try (XmlReader xmlReader = XmlReader.fromBytes(bytes)) {
            return deserializeShared(xmlReader, type);
        } catch (XMLStreamException ex) {
            throw LOGGER.logThrowableAsError(new IOException(ex));
        }
    }

    @Override
    public <T> T deserializeFromStream(InputStream stream, Type type, ObjectSerializer.Format format)
        throws IOException {
        verifyFormat(format);

        try (XmlReader xmlReader = XmlReader.fromStream(stream)) {
            return deserializeShared(xmlReader, type);
        } catch (XMLStreamException ex) {
            throw LOGGER.logThrowableAsError(new IOException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T deserializeShared(XmlReader xmlReader, Type type) {
        try {
            if (type instanceof Class<?> && XmlSerializer.class.isAssignableFrom(TypeUtil.getRawClass(type))) {
                Class<T> clazz = (Class<T>) type;

                return (T) clazz.getMethod("fromXml", XmlReader.class).invoke(null, xmlReader);
            } else {
                // TODO (alzimmer): XML needs untyped support.
                throw LOGGER.logThrowableAsError(new UnsupportedOperationException(
                    "DefaultXmlSerializer does not have support for untyped deserialization."));
            }
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    @Override
    public byte[] serializeToBytes(Object value, ObjectSerializer.Format format) throws IOException {
        verifyFormat(format);

        if (value == null) {
            return null;
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            XmlWriter xmlWriter = XmlWriter.toStream(byteArrayOutputStream)) {
            serializeShared(xmlWriter, value);

            return byteArrayOutputStream.toByteArray();
        } catch (XMLStreamException ex) {
            throw LOGGER.logThrowableAsError(new IOException(ex));
        }
    }

    @Override
    public void serializeToStream(OutputStream stream, Object value, ObjectSerializer.Format format)
        throws IOException {
        verifyFormat(format);

        if (value == null) {
            return;
        }

        try (XmlWriter xmlWriter = XmlWriter.toStream(stream)) {
            serializeShared(xmlWriter, value);
        } catch (XMLStreamException ex) {
            throw LOGGER.logThrowableAsError(new IOException(ex));
        }
    }

    private static void serializeShared(XmlWriter xmlWriter, Object value) {
        try {
            if (value instanceof XmlSerializable) {
                ((XmlSerializable<?>) value).toXml(xmlWriter).flush();
            } else {
                // TODO (alzimmer): XML needs untyped support.
                throw LOGGER.logThrowableAsError(new UnsupportedOperationException(
                    "DefaultXmlSerializer does not have support for untyped serialization."));
            }
        } catch (XMLStreamException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    private static void verifyFormat(ObjectSerializer.Format format) {
        if (format != ObjectSerializer.Format.XML) {
            throw LOGGER.logThrowableAsError(new UnsupportedOperationException("Only XML format is supported."));
        }
    }
}
