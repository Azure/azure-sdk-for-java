// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.io;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import javax.xml.transform.Source;

/**
 * This is the base class for additional input sources (implementations
 * of {@link javax.xml.transform.Source}) that Stax2
 * {@link com.azure.xml.implementation.stax2.XMLInputFactory2} implementations should support.
 *<p>
 * Note about usage by the parser factory implementations: the expectation
 * is that at least one of methods {@link #constructReader} and
 * {@link #constructInputStream} will succeed, but not necessarily both.
 * This generally depends on type of resource being represented: for example,
 * if the source is a String or character array, it is most naturally
 * represent via {@link Reader}. For a byte array, on the other hand,
 * an {@link InputStream} is the most natural access method.
 *<p>
 * Other things to note about using result {@link Reader}s and
 * {@link InputStream}s:
 * <ul>
 *  <li>Caller is responsible for closing any {@link Reader} and
 *    {@link InputStream} instances requested. That is, caller owns
 *    these accessor objects.
 *   </li>
 *  <li>Source objects are only required to return a non-null object
 *    <b>once</b>: after this, if new non-null instances are returned,
 *    they <b>must not</b> be the same objects as returned earlier.
 *    Implementations can choose to construct new instances to the same
 *    backing data structure or resource; if so, they should document
 *    this behavior.
 *   </li>
 *  </ul>
 */
public abstract class Stax2Source implements Source {
    protected String mSystemId;
    protected String mPublicId;
    protected String mEncoding;

    protected Stax2Source() {
    }

    /*
    /////////////////////////////////////////
    // Public API, simple accessors/mutators
    /////////////////////////////////////////
     */

    @Override
    public String getSystemId() {
        return mSystemId;
    }

    @Override
    public void setSystemId(String id) {
        mSystemId = id;
    }

    public String getPublicId() {
        return mPublicId;
    }

    public String getEncoding() {
        return mEncoding;
    }

    public void setEncoding(String enc) {
        mEncoding = enc;
    }

    /**
     * @return URL that can be used to resolve references
     *   originating from the content read via this source; may be
     *   null if not known (which is the case for most non-referential
     *   sources)
     */
    public abstract URL getReference();

    /*
    ///////////////////////////////////////////
    // Public API, convenience factory methods
    ///////////////////////////////////////////
     */

    /**
     * This method creates a {@link Reader} via which underlying input
     * source can be accessed. Note that caller is responsible for
     * closing that Reader when it is done reading it.
     */
    public abstract Reader constructReader() throws IOException;

    /**
     * This method creates an {@link InputStream} via which underlying input
     * source can be accessed. Note that caller is responsible for
     * closing that InputSource when it is done reading it
     */
    public abstract InputStream constructInputStream() throws IOException;
}
