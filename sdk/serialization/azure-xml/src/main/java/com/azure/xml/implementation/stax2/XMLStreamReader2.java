// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* Stax2 extension for basic Stax API (JSR-173).
 *
 * Copyright (c) 2005- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in file LICENSE, included with
 * the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.xml.implementation.stax2;

import java.io.IOException;
import java.io.Writer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.azure.xml.implementation.stax2.typed.TypedXMLStreamReader;
import com.azure.xml.implementation.stax2.validation.Validatable;

/**
 * Extended interface that implements functionality that is necessary
 * to properly build event API on top of {@link XMLStreamReader}.
 * It also adds limited number of methods that are important for
 * efficient pass-through processing (such as one needed when routing
 * SOAP-messages).
 *<p>
 * The features supported via {@link #setFeature} are:
 *<dl>
 * <dt>FEATURE_DTD_OVERRIDE: (write-only)</dt>
 * <dd>Feature used to specify the source for DTD external subset to use
 *    instead of DTD specified by the XML document itself (if any).
 *    Setting the feature for a reader that supports DTD validation
 *    essentially allows for injecting an alternate DOCTYPE declaration.
 *    Note that setting this value to null is both legal, and sometimes
 *    useful: it is equivalent of removing the DOCTYPE declaration.
 *   <br>Feature is write-only, since storing it after loading the DTD
 *    in question does not have much use.
 *  </dd>
 *</dl>
 *<p>
 * Since version 3.0, stream writer will also implement "Typed Access API"
 * on output side.
 *
 * @author Tatu Saloranta (tatu.saloranta@iki.fi)
 */
public interface XMLStreamReader2 extends TypedXMLStreamReader, Validatable {
    /**
     * Feature used to specify the source for DTD external subset to use
     * instead of DTD specified by the XML document itself (if any).
     *
     * @deprecated Use {@link XMLInputFactory2#P_DTD_OVERRIDE} instead.
     */
    @Deprecated
    String FEATURE_DTD_OVERRIDE = XMLInputFactory2.P_DTD_OVERRIDE;

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /**
     * Method similar to {@link javax.xml.stream.XMLInputFactory#isPropertySupported}, used
     * to determine whether a property is supported by the Reader
     * <b>instance</b>. This means that this method may return false
     * for some properties that the input factory does support: specifically,
     * it should only return true if the value is mutable on per-instance
     * basis. False means that either the property is not recognized, or
     * is not mutable via reader instance.
     */
    boolean isPropertySupported(String name);

    /**
     * Method that can be used to set per-reader properties; a subset of
     * properties one can set via matching
     * {@link com.azure.xml.implementation.stax2.XMLInputFactory2}
     * instance. Exactly which methods are mutable is implementation
     * specific.
     *
     * @param name Name of the property to set
     * @param value Value to set property to.
     *
     * @return True, if the specified property was <b>succesfully</b>
     *    set to specified value; false if its value was not changed
     *
     * @throws IllegalArgumentException if the property is not supported
     *   (or recognized) by the stream reader implementation
     */
    boolean setProperty(String name, Object value);

    /**
     * Method that can be used to get per-reader values; both generic
     * ones (names for which are defined as constants in this class),
     * and implementation dependant ones.
     *<p>
     * Note: although some feature names are shared with
     * {@link #setFeature}, not all are: some features are read-only,
     * some write-only
     *
     * @deprecated Should use {@link #getProperty} instead
     *
     * @param name Name of the feature of which value to get
     *
     * @return Value of the feature (possibly null), if supported; null
     *     otherwise
     */
    @Deprecated
    Object getFeature(String name);

    /**
     * Method that can be used to set per-reader features such as configuration
     * settings; both generic
     * ones (names for which are defined as constants in this class),
     * and implementation dependant ones.
     *<p>
     * Note: although some feature names are shared with
     * {@link #getFeature}, not all are: some features are read-only,
     * some write-only
     *
     * @deprecated Should use {@link #setProperty} instead
     *
     * @param name Name of the feature to set
     * @param value Value to set feature to.
     */
    @Deprecated
    void setFeature(String name, Object value);

    /*
    /**********************************************************************
    /* Additional event traversing
    /**********************************************************************
     */

    /**
     * Method that will skip all the contents of the element that the
     * stream currently points to. Current event when calling the method
     * has to be START_ELEMENT (or otherwise {@link IllegalStateException}
     * is thrown); after the call the stream will point to the matching
     * END_ELEMENT event, having skipped zero or more intervening events
     * for the contents.
     */
    void skipElement() throws XMLStreamException;

    /*
    /**********************************************************************
    /* Additional DTD access
    /**********************************************************************
     */

    /**
     * Method that can be called to get information about DOCTYPE declaration
     * that the reader is currently pointing to, if the reader has parsed
     * it. Implementations can also choose to return null to indicate they
     * do not provide extra information; but they should not throw any
     * exceptions beyond normal parsing exceptions.
     *
     * @return Information object for accessing further DOCTYPE information,
     *   iff the reader currently points to DTD event, AND is operating
     *   in mode that parses such information (DTD-aware at least, and
     *   usually also validating)
     */
    DTDInfo getDTDInfo() throws XMLStreamException;

    /*
    /**********************************************************************
    /* Additional attribute accessors
    /**********************************************************************
     */

    /**
     * Method that can be called to get additional information about
     * attributes related to the current start element, as well as
     * related DTD-based information if available. Note that the
     * reader has to currently point to START_ELEMENT; if not,
     * a {@link IllegalStateException} will be thrown.
     */
    AttributeInfo getAttributeInfo() throws XMLStreamException;

    /*
    /**********************************************************************
    /* Extended location information access
    /**********************************************************************
     */

    LocationInfo getLocationInfo();

    /*
    /**********************************************************************
    /* Pass-through text accessors
    /**********************************************************************
     */

    /**
     * Method similar to {@link #getText()}, except
     * that it just uses provided Writer to write all textual content,
     * and that it works for wider range of event types.
     * For further optimization, it may also be allowed to do true
     * pass-through, thus possibly avoiding one temporary copy of the
     * data. Finally, note that this method is also guaranteed NOT
     * to return fragments, even when coalescing is not enabled and
     * a parser is otherwised allowed to return partial segments: this
     * requirement is due to there being little benefit in returning
     * such short chunks when streaming. Coalescing property is still
     * honored normally.
     *<p>
     * Method can only be called on states CDATA, CHARACTERS, COMMENT,
     * DTD, ENTITY_REFERENCE, SPACE and PROCESSING_INSTRUCTION; if called
     * when reader is in another state,
     * {@link IllegalStateException} will be thrown. Content written
     * for elements is same as with {@link #getText()}.
     *
     * @param w Writer to use for writing textual contents
     * @param preserveContents If true, reader has to preserve contents
     *   so that further calls to <code>getText</code> will return
     *   proper conntets. If false, reader is allowed to skip creation
     *   of such copies: this can improve performance, but it also means
     *   that further calls to <code>getText</code> is not guaranteed to
     *   return meaningful data.
     *
     * @return Number of characters written to the reader
     */
    int getText(Writer w, boolean preserveContents) throws IOException, XMLStreamException;

    /*
    /**********************************************************************
    /* Other accessors
    /**********************************************************************
     */

    /**
     * Method that can be used to check whether current START_ELEMENT
     * event was created for an empty element (xml short-hand notation
     * where one tag implies start and end, ending with "/&gt;"), or not.
     *<p>
     * Note: method may need to read more data to know if the element
     * is an empty one, and as such may throw an i/o or parsing exception
     * (as {@link XMLStreamException}); however, it won't throw exceptions
     * for non-START_ELEMENT event types.
     *
     * @return True, if current event is START_ELEMENT
     *   and is based on a parsed empty element; otherwise false
     */
    boolean isEmptyElement() throws XMLStreamException;

    /**
     * Method that returns the number of open elements in the stack; 0 when
     * the reader is in prolog/epilog, 1 inside root element (including
     * when pointing at the root element itself) and so on.
     * Depth is same for matching start/end elements, as well as for the
     * all children of an element.
     *
     * @return Number of open elements currently in the reader's stack,
     *  including current START_ELEMENT or END_ELEMENT (if pointing to one).
     */
    int getDepth();

    /**
     * This method returns a namespace context object that contains
     * information identical to that returned by
     * {@link javax.xml.stream.XMLStreamReader#getNamespaceContext()},
     * but one that is
     * not transient. That is, one that will remain valid and unchanged
     * after its creation. This allows the namespace context to be used
     * independent of its source documents life cycle. One possible use
     * case is to use this namespace context for 'initializing' writers
     * (especially ones that use repairing mode) with optimal/preferred name
     * space bindings.
     *
     * @return Non-transient namespace context as explained above.
     */
    NamespaceContext getNonTransientNamespaceContext();

    /**
     * This method returns "prefix-qualified" name of the current
     * element. In general, this means character-by-character exact
     * name of the element in XML content, and may be useful in informational
     * purposes, as well as when interacting with packages and APIs that
     * use such names (such as what SAX may use as qnames).
     *<p>
     * Note: implementations are encouraged to provide an implementation
     * that would be more efficient than calling <code>getLocalName</code>
     * and <code>getPrefix</code> separately, but are not required to do
     * so. Nonetheless it is usually at least as efficient (if not more)
     * to call this method as to do it fully in calling code.
     *
     * @return Prefix-qualified name of the current element; essentially
     *   'prefix:localName' if the element has a prefix, or 'localName'
     *   if it does not have one (belongs to the default namespace)
     */
    String getPrefixedName();

    /*
    /**********************************************************************
    /* Input handling
    /**********************************************************************
     */

    /**
     * Method similar to
     * {@link javax.xml.stream.XMLStreamReader#close},
     * except that this method also does close the underlying input
     * source if it has not yet been closed. It is generally preferable
     * to call this method if the parsing ends in an exception;
     * and for some input sources (when passing
     * a {@link java.io.File} or {@link java.net.URL} for factory
     * method) it has to be called as the application does not have
     * access to the actually input source ({@link java.io.InputStream}
     * opened from a {@link java.net.URL} and so on).
     */
    void closeCompletely() throws XMLStreamException;
}
