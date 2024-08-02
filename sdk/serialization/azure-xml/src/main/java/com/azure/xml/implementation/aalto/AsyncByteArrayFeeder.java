// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto;

import javax.xml.stream.XMLStreamException;

public interface AsyncByteArrayFeeder extends AsyncInputFeeder {
    /**
     * Method that can be called to feed more data, if (and only if)
     * {@link #needMoreInput} returns true.
     * 
     * @param data Byte array that containts data to feed: caller must ensure data remains
     *    stable until it is fully processed (which is true when {@link #needMoreInput}
     *    returns true)
     * @param offset Offset within array where input data to process starts
     * @param len Length of input data within array to process.
     * 
     * @throws XMLStreamException if the state is such that this method should not be called
     *   (has not yet consumed existing input data, or has been marked as closed)
     */
    public void feedInput(byte[] data, int offset, int len) throws XMLStreamException;

}
