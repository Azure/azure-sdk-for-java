// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2;

import javax.xml.stream.XMLEventReader;

/**
 * Extended interface that implements functionality that is missing
 * from {@link XMLEventReader}, based on findings on trying to
 * implement Stax API v1.0.
 */
public interface XMLEventReader2 extends XMLEventReader {

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
     *
     * @since 3.0
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
     *
     * @since 3.0
     */
    boolean setProperty(String name, Object value);
}
