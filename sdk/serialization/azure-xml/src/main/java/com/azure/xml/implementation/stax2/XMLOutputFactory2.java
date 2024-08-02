// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2;

import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter; // only for javadoc

import com.azure.xml.implementation.stax2.io.EscapingWriterFactory;

/**
 * Extension of {@link javax.xml.stream.XMLInputFactory} to add
 * missing functionality.
 *<p>
 * Also contains extended standard properties that conforming stream
 * writer factory and instance implementations should at least
 * recognize, and preferably support.
 *<br>
 * NOTE: although actual values for the property names are
 * visible, implementations should try to use the symbolic constants
 * defined here instead, to avoid typos.
 *<p>
 * Notes about properties that output factories should support:
 *<ul>
 * <li>{@link XMLStreamProperties#XSP_NAMESPACE_AWARE}: 
 * Whether output classes should keep track of and output namespace
 * information provided via write methods.
 * When enabled (set to Boolean.TRUE), will use all namespace information
 * provided, and does not allow colons in names (local name, prefix).<br>
 * What exactly is kept track
 * of depends on other settings, specifically whether
 * writer is in "repairing" mode or not.
 * When disabled, will only make use of local name part, which
 * may contain colons, and ignore prefix and namespace URI if any
 * are passed.<br>
 * Turning this option off may improve performance if no namespace
 * handling is needed.<br>
 * Default value for implementations should be 'true'; implementations
 * are not required to implement 'false'.
 *  </li>
 * <li>{@link XMLStreamProperties#XSP_PROBLEM_REPORTER}: 
 *  </li>
 * </ul>
 *
 * @version 3.0 01/21/2007
 * @author Tatu Saloranta (tatu.saloranta@iki.fi)
 */
public abstract class XMLOutputFactory2 extends XMLOutputFactory implements XMLStreamProperties {
    /*
    /**********************************************************************
    /* We share some options with other factories
    /**********************************************************************
     */

    //public final static String XSP_IMPLEMENTATION_NAME
    //public final static String XSP_IMPLEMENTATION_VERSION
    //public final static String XSP_NAMESPACE_AWARE
    //public final static String XSP_PROBLEM_REPORTER

    /*
    /**********************************************************************
    /* Additional standard configuration properties
    /**********************************************************************
     */

    // // General output options:

    /**
     * Whether stream writers are allowed to automatically output empty
     * elements, when a start element is immediately followed by matching
     * end element.
     * If true, will output empty elements; if false, will always create
     * separate end element (unless a specific method that produces empty
     * elements is called).
     *<p>
     * Default value for implementations should be 'true'; both values should
     * be recognized, and 'false' must be honored. However, 'true' value
     * is only a suggestion, and need not be implemented (since there is
     * the explicit 'writeEmptyElement()' method).
     */
    public final static String P_AUTOMATIC_EMPTY_ELEMENTS = "com.azure.xml.implementation.stax2.automaticEmptyElements";

    // // // Output stream/writer settings

    /**
     * Whether stream writer is to close the underlying output
     * destination (output stream, reader) when stream writer is closed.
     * Basic StAX2 specification mandates this feature to be set to
     * false by default
     * (for destinations that are passed by the application and for which
     * it has access to).
     *<p>
     * Note: if set to true, writers are also allowed (but not required) to
     * close the underlying destination when they do not need it any more,
     * for example when throwing an (unrecoverable) exception
     */
    public final static String P_AUTO_CLOSE_OUTPUT = "com.azure.xml.implementation.stax2.autoCloseOutput";

    // // Namespace options:

    /**
     * Prefix to use for automatically created namespace prefixes, when
     * namespace support is enabled, the writer is in "repairing" 
     * mode, and a new prefix name is needed. The value is a String,
     * and needs to be a valid namespace prefix in itself, as defined
     * by the namespace specification. Will be prepended by a trailing
     * part (often a sequence number), in order to make it unique to
     * be usable as a temporary non-colliding prefix.
     */
    public final static String P_AUTOMATIC_NS_PREFIX = "com.azure.xml.implementation.stax2.automaticNsPrefix";

    // // Text/attribute value escaping options:

    /**
     * Property that can be set if a custom output escaping for textual
     * content is needed.
     * The value set needs to be of type {@link EscapingWriterFactory}.
     * When set, the factory will be used to create a per-writer
     * instance used to escape all textual content written, both
     * via explicit {@link XMLStreamWriter#writeCharacters} methods,
     * and via copy methods ({@link XMLStreamWriter2#copyEventFromReader}).
     */
    public final static String P_TEXT_ESCAPER = "com.azure.xml.implementation.stax2.textEscaper";

    /**
     * Property that can be set if a custom output escaping for attribute
     * value content is needed.
     * The value set needs to be of type {@link EscapingWriterFactory}.
     * When set, the factory will be used to create a per-writer
     * instance used to escape all attribute values written, both
     * via explicit {@link XMLStreamWriter#writeAttribute} methods,
     * and via copy methods ({@link XMLStreamWriter2#copyEventFromReader}).
     */
    public final static String P_ATTR_VALUE_ESCAPER = "com.azure.xml.implementation.stax2.attrValueEscaper";

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected XMLOutputFactory2() {
        super();
    }

    public abstract XMLEventWriter createXMLEventWriter(Writer w, String encoding) throws XMLStreamException;

    public abstract XMLEventWriter createXMLEventWriter(XMLStreamWriter sw) throws XMLStreamException;

    public abstract XMLStreamWriter2 createXMLStreamWriter(Writer w, String encoding) throws XMLStreamException;

    /*
    /**********************************************************************
    /* Configuring using profiles
    /**********************************************************************
     */

    /**
     * Method call to make writer be as strict with output as possible,
     * ie maximize validation it does to try to catch any well-formedness
     * or validity problems.
     *<p>
     * This configuration does add some overhead to output process, since
     * it enables content checks that are overhead.
     *<p>
     * None of currently defined standard properties should be affected,
     * but implementations are likely to enable/disable custom
     * properties related to validation.
     */
    public abstract void configureForXmlConformance();

    /**
     * Method call to make writer be as robust as possible, that is, to
     * make it both check AND fix problems if it can.
     *<p>
     * Like {@link #configureForXmlConformance}, this configuration adds
     * some overhead to output process.
     *<p>
     * None of currently defined standard properties should be affected,
     * but implementations are likely to enable/disable custom
     * properties related to validation.
     */
    public abstract void configureForRobustness();

    /**
     * Method call to make writer optimize its operation for speed. This
     * generally disably additional checks (if any) writer does, and is
     * likely to disable many things that {@link #configureForXmlConformance}
     * (and {@link #configureForRobustness}) enables.
     *<p>
     * None of currently defined standard properties should be affected.
     */
    public abstract void configureForSpeed();
}
