// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2;

/**
 * Interface that specifies additional access functionality to parsed DTD
 * information (if any); these methods are encapsulated on a separate
 * interface to keep the main reader interface from exploding.
 *<p>
 * Note: instances of DTDInfo are not guaranteed to persist when the reader
 * that returned it is asked to provide the next event. Some implementations
 * may let it persist, others might not.
 */
public interface DTDInfo {
    // // // Stax2, v1.0

    /**
     * @return If current event is DTD, DTD support is enabled,
     *   and reader supports DTD processing, returns an internal
     *   Object implementation uses for storing/processing DTD;
     *   otherwise returns null.
     */
    Object getProcessedDTD();

    /**
     * @return If current event is DTD, returns the full root name
     *   (including prefix, if any); otherwise returns null
     */
    String getDTDRootName();

    /**
     * @return If current event is DTD, and has a system id, returns the
     *   system id; otherwise returns null.
     */
    String getDTDSystemId();

    /**
     * @return If current event is DTD, and has a public id, returns the
     *   public id; otherwise returns null.
     */
    String getDTDPublicId();

    /**
     * @return If current event is DTD, and has an internal subset,
     *   returns the internal subset; otherwise returns null.
     */
    String getDTDInternalSubset();

    // // // Stax2, v2.0

}
