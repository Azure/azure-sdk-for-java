// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * Extension of {@link XMLInputFactory} that adds some convenience factory
 * methods as new standard properties that conforming stream
 * reader factory and instance implementations need to
 * recognize, and preferably support. There are also some profile-based
 * configuration methods which allow implementations to set proper goal-based
 * values for custom properties.
 *<br>
 * NOTE: although actual values for the property names are
 * visible, implementations should try to use the symbolic constants
 * defined here instead, to avoid typos.
 *
 * @version 3.0 01/21/2007
 * @author Tatu Saloranta (tatu.saloranta@iki.fi)
 */
public abstract class XMLInputFactory2 extends XMLInputFactory implements XMLStreamProperties {

    /*
    /**********************************************************************
    /* Additional standard configuration properties
    /**********************************************************************
     */

    // // // Parsing settings

    /**
     * Whether reader will generate 'ignorable white space' events during
     * prolog and epilog (before and after the main XML root element);
     * if true, will generate those events; if false,
     * will just ignore white space in these parts of the parsed document.
     *<p>
     * Turning this feature off may give slight performance improvement,
     * although usually effect should be negligible. This option is usually
     * only turned on when round-trip output should be as similar to input
     * as possible.
     *<p>
     * Default value for this setting is implementation dependant.
     */
    public final static String P_REPORT_PROLOG_WHITESPACE = "com.azure.xml.implementation.stax2.reportPrologWhitespace";

    /**
     * Whether cursor-based reader will ever generate CDATA events; if true,
     * CDATA events may be generated for non-coalesced CDATA sections. If
     * false, all CDATA sections are reported as CHARACTERS types. It may
     * still be possible for event methods to distinguish between underlying
     * type, but event type code will be reported as CHARACTERS.
     *<p>
     * State of property does not have any effect on performance.
     *<p>
     * Default value for this setting is implementation dependant.
     */
    public final static String P_REPORT_CDATA = "http://java.sun.com/xml/stream/properties/report-cdata-event";

    /**
     * Whether stream readers are allowed to do lazy parsing, meaning
     * to parse minimal part of the event when
     * {@link XMLStreamReader#next} is called, and only parse the rest
     * as needed (or skip remainder of no extra information is needed).
     * Alternative to lazy parsing is called "eager parsing", and is
     * what most xml parsers use by default.
     *<p>
     * Enabling lazy parsing can improve performance for tasks where
     * number of textual events are skipped. The downside is that
     * not all well-formedness problems are reported when
     * {@link XMLStreamReader#next} is called, but only when the
     * rest of event are read or skipped.
     *<p>
     * Default value for this setting is implementation dependant.
     */
    public final static String P_LAZY_PARSING = "com.ctc.wstx.lazyParsing";

    // // // Optimization settings

    /**
     * Whether name symbols (element, attribute, entity and notation names,
     * namespace prefixes)
     * stream reader returns are guaranteed to have been String.intern()ed.
     * Interning generally makes access faster (both internal and externally),
     * and saves memory, but can add some overhead for processing.
     * It may also be problematic for large symbol spaces; especially
     * if xml content has unbounded value space for names.
     *<p>
     * Default value for this setting is implementation dependant.
     * Additionally implementations may have use different default for
     * different types of stream readers.
     */
    public final static String P_INTERN_NAMES = "com.azure.xml.implementation.stax2.internNames";

    /**
     * Whether namespace URIs
     * stream reader returns are guaranteed to have been String.intern()ed.
     * Interning can make access by fully-qualified name faster as well
     * as save memory, but it can also add
     * some overhead when encountering a namespace URI for the first
     * time.
     *<p>
     * Default value for this setting is implementation dependant.
     */
    public final static String P_INTERN_NS_URIS = "com.azure.xml.implementation.stax2.internNsUris";

    /**
     * Property that determines whether stream reader instances are required
     * to try to keep track of the parser Location in the input documents.
     *<p>
     * When turned on, the stream reader should try to do its best to keep
     * track of the locations, to be able to properly create
     * <code>XMLEvent</code> objects with accurate Location information.
     * Similarly, implementation should keep track of the location for
     * error reporting purposes, and include this information within
     * <code>XMLStreamException</code> instances.
     *<p>
     * When turned off, implementations are allowed to optimize things,
     * and only keep/pass partial Location information, or even none at
     * all. Implementations are still encouraged to keep some location
     * information for error reporting purposes, even if they do not
     * maintain accurate
     * <code>XMLEvent</code> locations, or exact byte/character offsets.
     *<p>
     * Default value for this setting is true.
     */
    public final static String P_PRESERVE_LOCATION = "com.azure.xml.implementation.stax2.preserveLocation";

    // // // Input source settings

    /**
     * Whether stream reader is to close the underlying input source (input
     * stream, reader) when stream reader is closed. Basic StAX2
     * specification mandates this feature to be set to false by default
     * (for sources that are passed by the application).
     *<p>
     * Note: if set to true, readers are also allowed (but not required) to
     * close the underlying input source when they do not need it any more,
     * for example when encountering EOF, or when throwing an unrecoverable
     * parsing exception
     */
    public final static String P_AUTO_CLOSE_INPUT = "com.azure.xml.implementation.stax2.closeInputSource";

    // // // Validation settings

    /**
     * Property used to specify the source for DTD external subset to use
     * instead of DTD specified by the XML document itself (if any).
     */
    public final static String P_DTD_OVERRIDE = "com.azure.xml.implementation.stax2.propDtdOverride";

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected XMLInputFactory2() {
        super();
    }
}
