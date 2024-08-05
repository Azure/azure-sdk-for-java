// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

import javax.xml.stream.XMLStreamException;

/**
 * Class that defines interface that individual (possibly) stateful validator
 * instances have to implement, and that both
 * {@link javax.xml.stream.XMLStreamReader} and
 * {@link javax.xml.stream.XMLStreamWriter} instances can call to validate
 * xml documents.
 *<p>
 * Validator instances are created from and by non-stateful
 * {@link XMLValidationSchema} instances. A new validator instance has to
 * be created for each document read or written, ie. can not be shared
 * or reused, unlike schema instances which can be.
 */
public abstract class XMLValidator {
    // // // Shared constants

    /* First, constants used by validators to indicate kind of pre-validation
     * (with respect to text, and in some cases, other non-element events)
     * caller needs to take, before calling the validator. The idea is to
     * allow stream readers and writers to do parts of validity checks they
     * are in best position to do, while leaving the real structural and
     * content-based validation to validators.
     */

    /**
     * This value indicates that no content whatsoever
     * is legal within current context, that is, where the only legal content
     * to follow is the closing end tag -- not even comments or processing
     * instructions are allowed.  This is the case for example for
     * elements that DTD defines to have EMPTY content model.
     *<p>
     */
    public final static int CONTENT_ALLOW_NONE = 0;

    /**
     * This value indicates that only white space text content is allowed,
     * not other kinds of text. Other non-text events may be allowed;
     * validator will deal with element validation.
     * Value also indicates that if non-white space text content is
     * encountered, a validation problem is reported
     */
    public final static int CONTENT_ALLOW_WS = 1;

    /**
     * This value indicates that textual content is allowed, but that
     * the validator needs to be called to let it do actual content-based
     * validation. Other event types are ok, and elements will need to be
     * validated by the validator as well.
     */
    public final static int CONTENT_ALLOW_VALIDATABLE_TEXT = 3;

    /**
     * This value indicates that any textual content (plain PCTEXT) is
     * allowed, and that validator is not going to do any validation
     * for it. It will, however, need to be called with respect
     * to element events.
     */
    public final static int CONTENT_ALLOW_ANY_TEXT = 4;

    /*
    ///////////////////////////////////////////////////
    // Life-cycle
    ///////////////////////////////////////////////////
     */

    protected XMLValidator() {
    }

    /*
    ///////////////////////////////////////////////////
    // Configuration, properties
    ///////////////////////////////////////////////////
     */

    /**
     * Returns the schema instance that created this validator
     * object, if known (and applicable). May return null for
     * some instances: specifically, {@link ValidatorPair}
     * will return null since it 'contains' multiple validators
     * and generally does not have just one parent or owner schema.
     */
    public abstract XMLValidationSchema getSchema();

    /*
    ///////////////////////////////////////////////////
    // Actual validation interface
    ///////////////////////////////////////////////////
     */

    public abstract void validateElementStart(String localName, String uri, String prefix) throws XMLStreamException;

    /**
     * Callback method called on validator to give it a chance to validate
     * the value of an attribute, as well as to normalize its value if
     * appropriate (remove leading/trailing/intervening white space for
     * certain token types etc.).
     *
     * @return Null, if the passed value is fine as is; or a String, if
     *   it needs to be replaced. In latter case, caller will replace the
     *   value before passing it to other validators. Also, if the attribute
     *   value is accessible via caller (as is the case for stream readers),
     *   caller should return this value, instead of the original one.
     */
    public abstract String validateAttribute(String localName, String uri, String prefix, String value)
        throws XMLStreamException;

    /**
     * Callback method called on validator to give it a chance to validate
     * the value of an attribute, as well as to normalize its value if
     * appropriate (remove leading/trailing/intervening white space for
     * certain token types etc.).
     *
     * @param valueChars Character array that contains value (possibly
     *   along with some other text)
     * @param valueStart Index of the first character of the value in
     *   in <code>valueChars</code> array
     * @param valueEnd Index of the character AFTER the last character;
     *    so that the length of the value String is
     *    <code>valueEnd - valueStart</code>
     *
     * @return Null, if the passed value is fine as is; or a String, if
     *   it needs to be replaced. In latter case, caller will replace the
     *   value before passing it to other validators. Also, if the attribute
     *   value is accessible via caller (as is the case for stream readers),
     *   caller should return this value, instead of the original one.
     */
    public abstract String validateAttribute(String localName, String uri, String prefix, char[] valueChars,
        int valueStart, int valueEnd) throws XMLStreamException;

    /**
     * Method called after calling {@link #validateAttribute} on all
     * attributes (if any), but before starting to handle element
     * content.
     *
     * @return One of <code>CONTENT_ALLOW_</code> constants, to indicate
     *   what kind of textual content is allowed at the scope returned
     *   to after the element has closed.
     */
    public abstract int validateElementAndAttributes() throws XMLStreamException;

    /**
     * Method called right after encountering an element close tag.
     *
     * @return One of <code>CONTENT_ALLOW_</code> constants, to indicate
     *   what kind of textual content is allowed at the scope returned
     *   to after the element has closed.
     */
    public abstract int validateElementEnd(String localName, String uri, String prefix) throws XMLStreamException;

    /**
     * Method called to validate textual content.
     *<p>
     * Note: this method is only guaranteed to be called when
     * {@link #validateElementAndAttributes()} for the currently open
     * element returned {@link #CONTENT_ALLOW_VALIDATABLE_TEXT} (or,
     * in case of mixed content, {@link #validateElementEnd}, for the
     * last enclosed element). Otherwise, validator context may choose
     * not to call the method as an optimization.
     *
     * @param text Text content to validate
     * @param lastTextSegment Whether this text content is the last text
     *    segment before a close element; true if it is, false if it is not,
     *    or no determination can be made. Can be used for optimizing
     *    validation -- if this is true, no text needs to be buffered since
     *    no more will be sent before the current element closes.
     */
    public abstract void validateText(String text, boolean lastTextSegment) throws XMLStreamException;

    /**
     * Method called to validate textual content.
     *<p>
     * Note: this method is only guaranteed to be called when
     * {@link #validateElementAndAttributes()} for the currently open
     * element returned {@link #CONTENT_ALLOW_VALIDATABLE_TEXT} (or,
     * in case of mixed content, {@link #validateElementEnd}, for the
     * last enclosed element). Otherwise, validator context may choose
     * not to call the method as an optimization.
     *
     * @param cbuf Character array that contains text content to validate
     * @param textStart Index of the first character of the content to
     *   validate
     * @param textEnd Character following the last character of the
     *   content to validate (that is, length of content to validate is
     *   <code>textEnd - textStart</code>).
     * @param lastTextSegment Whether this text content is the last text
     *    segment before a close element; true if it is, false if it is not,
     *    or no determination can be made. Can be used for optimizing
     *    validation -- if this is true, no text needs to be buffered since
     *    no more will be sent before the current element closes.
     */
    public abstract void validateText(char[] cbuf, int textStart, int textEnd, boolean lastTextSegment)
        throws XMLStreamException;

    /**
     * Method called when the validation is completed; either due to the
     * input stream ending, or due to an explicit 'stop validation' request
     * by the application (via context object).
     *
     * @param eod Flag that indicates whether this method was called by the
     *    context due to the end of the stream (true); or by an application
     *    requesting end of validation (false).
     */
    public abstract void validationCompleted(boolean eod) throws XMLStreamException;

    /*
    ///////////////////////////////////////////////////
    // Access to post-validation data (type info)
    ///////////////////////////////////////////////////
     */

    /**
     * Method for getting schema-specified type of an attribute, if
     * information is available. If not, validators can return
     * null to explicitly indicate no information was available.
     */
    public abstract String getAttributeType(int index);

    /**
     * Method for finding out the index of the attribute that
     * is of type ID; derived from DTD, W4C Schema, or some other validation
     * source. Usually schemas explicitly specifies that at most one
     * attribute can have this type for any element.
     *
     * @return Index of the attribute with type ID, in the current
     *    element, if one exists: -1 otherwise
     */
    public abstract int getIdAttrIndex();

    /**
     * Method for finding out the index of the attribute (collected using
     * the attribute collector; having DTD/Schema-derived info in same order)
     * that is of type NOTATION. DTD explicitly specifies that at most one
     * attribute can have this type for any element.
     *
     * @return Index of the attribute with type NOTATION, in the current
     *    element, if one exists: -1 otherwise
     */
    public abstract int getNotationAttrIndex();
}
