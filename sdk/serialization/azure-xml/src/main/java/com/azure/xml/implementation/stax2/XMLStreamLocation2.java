// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2;

import javax.xml.stream.Location;

/**
 * Extension of {@link Location} that adds accessor to retrieve nested
 * location information.
 */
public interface XMLStreamLocation2 extends Location {
    /**
     * "Dummy" Location implementation and instance that can be return
     * instead of null, when no location information is available.
     */
    XMLStreamLocation2 NOT_AVAILABLE = new XMLStreamLocation2() {
        @Override
        public XMLStreamLocation2 getContext() {
            return null;
        }

        @Override
        public int getCharacterOffset() {
            return -1;
        }

        @Override
        public int getColumnNumber() {
            return -1;
        }

        @Override
        public int getLineNumber() {
            return -1;
        }

        @Override
        public String getPublicId() {
            return null;
        }

        @Override
        public String getSystemId() {
            return null;
        }
    };

    /**
     * Method that can be used to traverse nested locations, like ones
     * created when expanding entities (especially external entities).
     * If so, single location object only contains information about
     * specific offsets and ids, and a link to its context. Outermost
     * location will return null to indicate there is no more information
     * to retrieve.
     *
     * @return Location in the context (parent input source), if any;
     *    null for locations in the outermost known context
     */
    XMLStreamLocation2 getContext();
}
