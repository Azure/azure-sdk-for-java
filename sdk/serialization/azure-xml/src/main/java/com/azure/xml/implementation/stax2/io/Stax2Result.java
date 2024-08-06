// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.io;

import java.io.OutputStream;
import java.io.IOException;
import java.io.Writer;

import javax.xml.transform.Result;

/**
 * This is the base class for additional output results (implementations
 * of {@link javax.xml.transform.Result}) that Stax2
 * {@link com.azure.xml.implementation.stax2.XMLInputFactory2} implementations should support.
 *<p>
 * Note about usage by the parser factory implementations: the expectation
 * is that at least one of methods {@link #constructWriter} and
 * {@link #constructOutputStream} will succeed, but not necessarily both.
 * This generally depends on type of resource being represented: for example,
 * if the source is a StringBuffer, it is most naturally
 * represent via {@link Writer}. For File-backed results, on the other hand,
 * an {@link OutputStream} is the most natural access method.
 *<p>
 * Other things to note about using result {@link Writer}s and
 * {@link OutputStream}s:
 * <ul>
 *  <li>Caller is responsible for closing any {@link Writer} and
 *    {@link OutputStream} instances requested. That is, caller owns
 *    these accessor objects.
 *   </li>
 *  <li>Result objects are only required to return a non-null object
 *    <b>once</b>: after this, if new non-null instances are returned,
 *    they <b>must not</b> be the same objects as returned earlier.
 *    Implementations can choose to construct new instances to the same
 *    backing data structure or resource; if so, they should document
 *    this behavior.
 *   </li>
 *  </ul>
 */
public abstract class Stax2Result implements Result {
    protected String mSystemId;
    protected String mEncoding;

    protected Stax2Result() {
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

    public String getEncoding() {
        return mEncoding;
    }

    public void setEncoding(String enc) {
        mEncoding = enc;
    }

    /*
    ///////////////////////////////////////////
    // Public API, convenience factory methods
    ///////////////////////////////////////////
     */

    /**
     * This method creates a {@link Writer} via which underlying output
     * target can be written to. Note that caller is responsible for
     * closing that Writer when it is done reading it.
     */
    public abstract Writer constructWriter() throws IOException;

    /**
     * This method creates an {@link OutputStream} via which underlying output
     * target can be written to. Note that caller is responsible for
     * closing that OutputStream when it is done reading it
     */
    public abstract OutputStream constructOutputStream() throws IOException;
}
