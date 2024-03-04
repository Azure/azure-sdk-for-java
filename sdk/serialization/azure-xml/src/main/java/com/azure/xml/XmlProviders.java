// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import com.azure.xml.implementation.DefaultXmlReader;
import com.azure.xml.implementation.DefaultXmlWriter;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Handles loading an instance of {@link XmlProvider} found on the classpath.
 */
public final class XmlProviders {
    private static final String CANNOT_FIND_XML = "A request was made to load an XmlReader and XmlWriter provider "
        + "but one could not be found on the classpath. If you are using a dependency manager, consider including a "
        + "dependency that supplies a provider for XmlProvider or indicate to the loader to fallback to the default "
        + "implementation. Additionally, refer to https://aka.ms/azsdk/java/docs/custom-xml to learn about writing "
        + "your own implementation.";

    private static XmlProvider defaultProvider;

    static {
        // Use as classloader to load provider-configuration files and provider classes the classloader
        // that loaded this class. In most cases this will be the System classloader.
        // But this choice here provides additional flexibility in managed environments that control
        // classloading differently (OSGi, Spring and others) and don't depend on the
        // System classloader to load HttpClientProvider classes.
        ServiceLoader<XmlProvider> serviceLoader
            = ServiceLoader.load(XmlProvider.class, XmlProvider.class.getClassLoader());
        // Use the first provider found in the service loader iterator.
        Iterator<XmlProvider> it = serviceLoader.iterator();
        if (it.hasNext()) {
            defaultProvider = it.next();
        }

        while (it.hasNext()) {
            it.next();
        }
    }

    private XmlProviders() {
        // no-op
    }

    /**
     * Creates an instance of {@link XmlReader} that reads a {@code byte[]}.
     * <p>
     * If a provider could not be found on the classpath this will use the default implementation, effectively the
     * equivalent to {@link #createReader(byte[], boolean) createReader(xml, true)}.
     *
     * @param xml The XML represented as a {@code byte[]}.
     * @return A new instance of {@link XmlReader}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader createReader(byte[] xml) throws XMLStreamException {
        return createReader(xml, true);
    }

    /**
     * Creates an instance of {@link XmlReader} that reads a {@code byte[]}.
     *
     * @param xml The XML represented as a {@code byte[]}.
     * @param useDefault Whether the default implementation should be used if one could not be found on the classpath.
     * @return A new instance of {@link XmlReader}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws IllegalStateException If a provider could not be found on the classpath and {@code useDefault} is false.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader createReader(byte[] xml, boolean useDefault) throws XMLStreamException {
        if (defaultProvider == null) {
            if (useDefault) {
                return DefaultXmlReader.fromBytes(xml);
            } else {
                throw new IllegalStateException(CANNOT_FIND_XML);
            }
        } else {
            return defaultProvider.createReader(xml);
        }
    }

    /**
     * Creates an instance of {@link XmlReader} that reads a {@link String}.
     * <p>
     * If a provider could not be found on the classpath this will use the default implementation, effectively the
     * equivalent to {@link #createReader(String, boolean) createReader(xml, true)}.
     *
     * @param xml The XML represented as a {@link String}.
     * @return A new instance of {@link XmlReader}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader createReader(String xml) throws XMLStreamException {
        return createReader(xml, true);
    }

    /**
     * Creates an instance of {@link XmlReader} that reads a {@link String}.
     *
     * @param xml The XML represented as a {@link String}.
     * @param useDefault Whether the default implementation should be used if one could not be found on the classpath.
     * @return A new instance of {@link XmlReader}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws IllegalStateException If a provider could not be found on the classpath and {@code useDefault} is false.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader createReader(String xml, boolean useDefault) throws XMLStreamException {
        if (defaultProvider == null) {
            if (useDefault) {
                return DefaultXmlReader.fromString(xml);
            } else {
                throw new IllegalStateException(CANNOT_FIND_XML);
            }
        } else {
            return defaultProvider.createReader(xml);
        }
    }

    /**
     * Creates an instance of {@link XmlReader} that reads a {@link InputStream}.
     * <p>
     * If a provider could not be found on the classpath this will use the default implementation, effectively the
     * equivalent to {@link #createReader(InputStream, boolean) createReader(xml, true)}.
     *
     * @param xml The XML represented as a {@link InputStream}.
     * @return A new instance of {@link XmlReader}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader createReader(InputStream xml) throws XMLStreamException {
        return createReader(xml, true);
    }

    /**
     * Creates an instance of {@link XmlReader} that reads a {@link InputStream}.
     *
     * @param xml The XML represented as a {@link InputStream}.
     * @param useDefault Whether the default implementation should be used if one could not be found on the classpath.
     * @return A new instance of {@link XmlReader}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws IllegalStateException If a provider could not be found on the classpath and {@code useDefault} is false.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader createReader(InputStream xml, boolean useDefault) throws XMLStreamException {
        if (defaultProvider == null) {
            if (useDefault) {
                return DefaultXmlReader.fromStream(xml);
            } else {
                throw new IllegalStateException(CANNOT_FIND_XML);
            }
        } else {
            return defaultProvider.createReader(xml);
        }
    }

    /**
     * Creates an instance of {@link XmlReader} that reads a {@link Reader}.
     * <p>
     * If a provider could not be found on the classpath this will use the default implementation, effectively the
     * equivalent to {@link #createReader(Reader, boolean) createReader(xml, true)}.
     *
     * @param xml The XML represented as a {@link Reader}.
     * @return A new instance of {@link XmlReader}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader createReader(Reader xml) throws XMLStreamException {
        return createReader(xml, true);
    }

    /**
     * Creates an instance of {@link XmlReader} that reads a {@link Reader}.
     *
     * @param xml The XML represented as a {@link Reader}.
     * @param useDefault Whether the default implementation should be used if one could not be found on the classpath.
     * @return A new instance of {@link XmlReader}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws IllegalStateException If a provider could not be found on the classpath and {@code useDefault} is false.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader createReader(Reader xml, boolean useDefault) throws XMLStreamException {
        if (defaultProvider == null) {
            if (useDefault) {
                return DefaultXmlReader.fromReader(xml);
            } else {
                throw new IllegalStateException(CANNOT_FIND_XML);
            }
        } else {
            return defaultProvider.createReader(xml);
        }
    }

    /**
     * Creates an instance of {@link XmlWriter} that writes to an {@link OutputStream}.
     * <p>
     * If a provider could not be found on the classpath this will use the default implementation, effectively the
     * equivalent to {@link #createWriter(OutputStream, boolean) createWriter(xml, true)}.
     *
     * @param xml The XML represented as an {@link OutputStream}.
     * @return A new instance of {@link XmlWriter}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlWriter} cannot be instantiated.
     */
    public static XmlWriter createWriter(OutputStream xml) throws XMLStreamException {
        return createWriter(xml, true);
    }

    /**
     * Creates an instance of {@link XmlWriter} that writes to an {@link OutputStream}.
     *
     * @param xml The XML represented as an {@link OutputStream}.
     * @param useDefault Whether the default implementation should be used if one could not be found on the classpath.
     * @return A new instance of {@link XmlWriter}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws IllegalStateException If a provider could not be found on the classpath and {@code useDefault} is false.
     * @throws XMLStreamException If an {@link XmlWriter} cannot be instantiated.
     */
    public static XmlWriter createWriter(OutputStream xml, boolean useDefault) throws XMLStreamException {
        if (defaultProvider == null) {
            if (useDefault) {
                return DefaultXmlWriter.toStream(xml);
            } else {
                throw new IllegalStateException(CANNOT_FIND_XML);
            }
        } else {
            return defaultProvider.createWriter(xml);
        }
    }

    /**
     * Creates an instance of {@link XmlWriter} that writes to an {@link Writer}.
     * <p>
     * If a provider could not be found on the classpath this will use the default implementation, effectively the
     * equivalent to {@link #createWriter(Writer, boolean) createWriter(xml, true)}.
     *
     * @param xml The XML represented as an {@link Writer}.
     * @return A new instance of {@link XmlWriter}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlWriter} cannot be instantiated.
     */
    public static XmlWriter createWriter(Writer xml) throws XMLStreamException {
        return createWriter(xml, true);
    }

    /**
     * Creates an instance of {@link XmlWriter} that writes to an {@link Writer}.
     *
     * @param xml The XML represented as an {@link Writer}.
     * @param useDefault Whether the default implementation should be used if one could not be found on the classpath.
     * @return A new instance of {@link XmlWriter}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws IllegalStateException If a provider could not be found on the classpath and {@code useDefault} is false.
     * @throws XMLStreamException If an {@link XmlWriter} cannot be instantiated.
     */
    public static XmlWriter createWriter(Writer xml, boolean useDefault) throws XMLStreamException {
        if (defaultProvider == null) {
            if (useDefault) {
                return DefaultXmlWriter.toWriter(xml);
            } else {
                throw new IllegalStateException(CANNOT_FIND_XML);
            }
        } else {
            return defaultProvider.createWriter(xml);
        }
    }
}
