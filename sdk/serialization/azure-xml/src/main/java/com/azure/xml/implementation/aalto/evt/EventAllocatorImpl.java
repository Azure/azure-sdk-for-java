// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Aalto XML processor
 *
 * Copyright (c) 2006- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in the file LICENSE which is
 * included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.azure.xml.implementation.aalto.evt;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventAllocator;

import com.azure.xml.implementation.stax2.ri.evt.Stax2EventAllocatorImpl;

import com.azure.xml.implementation.aalto.AsyncXMLStreamReader;

/**
 * Specialized event allocator implementation. Beyond additions needed
 * to support DTD entities, implements non-location-preserving optimization.
 *
 * @author Tatu Saloranta
 */
public final class EventAllocatorImpl extends Stax2EventAllocatorImpl {
    final static EventAllocatorImpl sStdInstance = new EventAllocatorImpl(true);

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    private final boolean _cfgPreserveLocation;

    /*
    /**********************************************************************
    /* Recycled objects
    /**********************************************************************
     */

    /**
     * Last used location info; only relevant to non-accurate-location
     * allocators.
     */
    private Location _lastLocation = null;

    /**
     * @param preserveLocation If true, allocator will construct instances
     *   that have accurate location information; if false, instances
     *   will only have some generic shared Location info. Latter option
     *   will reduce memory usage/thrashing a bit, and may improve speed.
     */
    private EventAllocatorImpl(boolean preserveLocation) {
        _cfgPreserveLocation = preserveLocation;
    }

    public static EventAllocatorImpl getDefaultInstance() {
        /* Standard allocator instance can be shared as it
         * has no state.
         */
        return sStdInstance;
    }

    public static EventAllocatorImpl getFastInstance() {
        /* Can not share instances, due to QName caching, as well as because
         * of Location object related state
         */
        return new EventAllocatorImpl(false);
    }

    /**
     * Default implementation assumes that the caller knows how to
     * share instances, and so need not create new copies.
     *<p>
     * Note: if this class is sub-classes, this method should be
     * redefined if assumptions about shareability do not hold.
     */
    @Override
    public XMLEventAllocator newInstance() {
        return new EventAllocatorImpl(_cfgPreserveLocation);
    }

    /*
    /**********************************************************************
    /* Overriden methods
    /**********************************************************************
     */

    @Override
    public XMLEvent allocate(XMLStreamReader r) throws XMLStreamException {
        if (r.getEventType() == AsyncXMLStreamReader.EVENT_INCOMPLETE) {
            return IncompleteEvent.instance();
        }
        return super.allocate(r);
    }

    /**
     * Method used to get the {@link Location} object to use for
     * an event to create. Base implementation just calls stream
     * reader's accessors, but sub-classes may choose to use other
     * methods (esp. when not in "preserve location" mode).
     */
    @Override
    protected Location getLocation(XMLStreamReader r) {
        if (_cfgPreserveLocation) {
            return r.getLocation();
        }
        Location loc = _lastLocation;
        /* And even if we can just share one instance, we need that
         * first instance...
         */
        if (loc == null) {
            loc = _lastLocation = r.getLocation();
        }
        return loc;
    }

    // Should redefine this one:
    /*
    protected EntityReference createEntityReference(XMLStreamReader r, Location loc)
        throws XMLStreamException
    */

    // As well as this:
    /*
    protected DTD createDTD(XMLStreamReader r, Location loc)
        throws XMLStreamException
    */

    // And probably this one too?
    /*
    protected StartElement createStartElement(XMLStreamReader r, Location loc)
        throws XMLStreamException
    {
        return super.createStartElement(r, loc);
    }
    */
}
