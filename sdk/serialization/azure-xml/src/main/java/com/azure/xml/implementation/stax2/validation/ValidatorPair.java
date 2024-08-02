// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

import javax.xml.stream.XMLStreamException;

/**
 * Simple utility class that allows chaining of {@link XMLValidator}
 * instances. Since the class itself implements {@link XMLValidator},
 * multiple validators can be added by chaining these pairs; ordering
 * of validator calls depends on ordering of the pairs.
 *<p>
 * Default semantics are quite simple: first validator of the pair is
 * always called first, and results as/if modified by that validator
 * are passed on to the second validator.
 *<p>
 * It is expected that this class is mostly used by actual stream reader
 * and writer implementations; not so much by validator implementations.
 */
public class ValidatorPair extends XMLValidator {
    public final static String ATTR_TYPE_DEFAULT = "CDATA";

    protected XMLValidator mFirst, mSecond;

    /*
    ////////////////////////////////////////////////////
    // Life-cycle
    ////////////////////////////////////////////////////
     */

    public ValidatorPair(XMLValidator first, XMLValidator second) {
        mFirst = first;
        mSecond = second;
    }

    /*
    ////////////////////////////////////////////////////
    // XMLValidator implementation
    ////////////////////////////////////////////////////
     */

    /**
     * Two choices here; could either return schema of the first child,
     * or return null. Let's do latter, do avoid accidental matches.
     */
    @Override
    public XMLValidationSchema getSchema() {
        return null;
    }

    @Override
    public void validateElementStart(String localName, String uri, String prefix) throws XMLStreamException {
        mFirst.validateElementStart(localName, uri, prefix);
        mSecond.validateElementStart(localName, uri, prefix);
    }

    @Override
    public String validateAttribute(String localName, String uri, String prefix, String value)
        throws XMLStreamException {
        String retVal = mFirst.validateAttribute(localName, uri, prefix, value);
        if (retVal != null) {
            value = retVal;
        }
        return mSecond.validateAttribute(localName, uri, prefix, value);
    }

    @Override
    public String validateAttribute(String localName, String uri, String prefix, char[] valueChars, int valueStart,
        int valueEnd) throws XMLStreamException {
        String retVal = mFirst.validateAttribute(localName, uri, prefix, valueChars, valueStart, valueEnd);
        /* If it got normalized, let's pass normalized value to the second
         * validator
         */
        if (retVal != null) {
            return mSecond.validateAttribute(localName, uri, prefix, retVal);
        }
        // Otherwise the original
        return mSecond.validateAttribute(localName, uri, prefix, valueChars, valueStart, valueEnd);
    }

    @Override
    public int validateElementAndAttributes() throws XMLStreamException {
        int textType1 = mFirst.validateElementAndAttributes();
        int textType2 = mSecond.validateElementAndAttributes();

        /* Here, let's choose the stricter (more restrictive) of the two.
         * Since constants are order from strictest to most lenient,
         * we'll just take smaller of values
         */
        return Math.min(textType1, textType2);
    }

    @Override
    public int validateElementEnd(String localName, String uri, String prefix) throws XMLStreamException {
        int textType1 = mFirst.validateElementEnd(localName, uri, prefix);
        int textType2 = mSecond.validateElementEnd(localName, uri, prefix);

        // As with earlier, let's return stricter of the two
        return Math.min(textType1, textType2);
    }

    @Override
    public void validateText(String text, boolean lastTextSegment) throws XMLStreamException {
        mFirst.validateText(text, lastTextSegment);
        mSecond.validateText(text, lastTextSegment);
    }

    @Override
    public void validateText(char[] cbuf, int textStart, int textEnd, boolean lastTextSegment)
        throws XMLStreamException {
        mFirst.validateText(cbuf, textStart, textEnd, lastTextSegment);
        mSecond.validateText(cbuf, textStart, textEnd, lastTextSegment);
    }

    @Override
    public void validationCompleted(boolean eod) throws XMLStreamException {
        mFirst.validationCompleted(eod);
        mSecond.validationCompleted(eod);
    }

    /*
    ///////////////////////////////////////////////////
    // Access to post-validation data (type info)
    ///////////////////////////////////////////////////
     */

    @Override
    public String getAttributeType(int index) {
        String type = mFirst.getAttributeType(index);
        /* Hmmh. Which heuristic to use here: obviously no answer (null or
         * empty) is not useful. But what about the default type (CDATA)?
         * We can probably find a more explicit type?
         */
        if (type == null || type.isEmpty() || type.equals(ATTR_TYPE_DEFAULT)) {
            String type2 = mSecond.getAttributeType(index);
            if (type2 != null && !type2.isEmpty()) {
                return type2;
            }

        }
        return type;
    }

    @Override
    public int getIdAttrIndex() {
        int index = mFirst.getIdAttrIndex();
        if (index < 0) {
            return mSecond.getIdAttrIndex();
        }
        return index;
    }

    @Override
    public int getNotationAttrIndex() {
        int index = mFirst.getNotationAttrIndex();
        if (index < 0) {
            return mSecond.getNotationAttrIndex();
        }
        return index;
    }

    /*
    ////////////////////////////////////////////////////
    // Additional API used by Woodstox core
    ////////////////////////////////////////////////////
     */

    public static boolean removeValidator(XMLValidator root, XMLValidationSchema schema, XMLValidator[] results) {
        if (root instanceof ValidatorPair) {
            return ((ValidatorPair) root).doRemoveValidator(schema, results);
        } else {
            if (root.getSchema() == schema) {
                results[0] = root;
                results[1] = null;
                return true;
            }
        }
        return false;
    }

    public static boolean removeValidator(XMLValidator root, XMLValidator vld, XMLValidator[] results) {
        if (root == vld) { // single validator?
            results[0] = root;
            results[1] = null;
            return true;
        } else if (root instanceof ValidatorPair) {
            return ((ValidatorPair) root).doRemoveValidator(vld, results);
        }
        return false;
    }

    private boolean doRemoveValidator(XMLValidationSchema schema, XMLValidator[] results) {
        if (removeValidator(mFirst, schema, results)) {
            XMLValidator newFirst = results[1];
            if (newFirst == null) { // removed first (was leaf) -> remove this pair
                results[1] = mSecond;
            } else {
                mFirst = newFirst; // two may be the same, need not be
                results[1] = this;
            }
            return true;
        }
        if (removeValidator(mSecond, schema, results)) {
            XMLValidator newSecond = results[1];
            if (newSecond == null) { // removed second (was leaf) -> remove this pair
                results[1] = mFirst;
            } else {
                mSecond = newSecond; // two may be the same, need not be
                results[1] = this; // will still have this pair
            }
            return true;
        }
        return false;
    }

    private boolean doRemoveValidator(XMLValidator vld, XMLValidator[] results) {
        if (removeValidator(mFirst, vld, results)) {
            XMLValidator newFirst = results[1];
            if (newFirst == null) { // removed first (was leaf) -> remove this pair
                results[1] = mSecond;
            } else {
                mFirst = newFirst; // two may be the same, need not be
                results[1] = this;
            }
            return true;
        }
        if (removeValidator(mSecond, vld, results)) {
            XMLValidator newSecond = results[1];
            if (newSecond == null) { // removed second (was leaf) -> remove this pair
                results[1] = mFirst;
            } else {
                mSecond = newSecond; // two may be the same, need not be
                results[1] = this; // will still have this pair
            }
            return true;
        }
        return false;
    }

    /*
    ////////////////////////////////////////////////////
    // Internal methods
    ////////////////////////////////////////////////////
     */
}
