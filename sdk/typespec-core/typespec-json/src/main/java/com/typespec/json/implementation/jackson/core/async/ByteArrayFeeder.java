// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.async;

import java.io.IOException;

/**
 * {@link NonBlockingInputFeeder} implementation used when feeding data
 * as byte arrays.
 *
 * @since 2.9
 */
public interface ByteArrayFeeder extends NonBlockingInputFeeder
{
     /**
      * Method that can be called to feed more data, if (and only if)
      * {@link #needMoreInput} returns true.
      * 
      * @param data Byte array that contains data to feed: caller must ensure data remains
      *    stable until it is fully processed (which is true when {@link #needMoreInput}
      *    returns true)
      * @param offset Offset within array where input data to process starts
      * @param end Offset after last byte contained in the input array
      * 
      * @throws IOException if the state is such that this method should not be called
      *   (has not yet consumed existing input data, or has been marked as closed)
      */
     public void feedInput(byte[] data, int offset, int end) throws IOException;
}
