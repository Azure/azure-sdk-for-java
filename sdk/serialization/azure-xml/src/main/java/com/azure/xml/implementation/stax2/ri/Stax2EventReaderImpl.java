// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* Woodstox XML processor
 *
 * Copyright (c) 2004- Tatu Saloranta, tatu.saloranta@iki.fi
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

package com.azure.xml.implementation.stax2.ri;

import java.util.NoSuchElementException;

import javax.xml.stream.*;
import javax.xml.stream.events.*;
import javax.xml.stream.util.XMLEventAllocator;

import com.azure.xml.implementation.stax2.XMLEventReader2;
import com.azure.xml.implementation.stax2.XMLStreamReader2;

/**
 * Almost complete implementation of {@link XMLEventReader2}, built on top of
 * a generic {@link XMLStreamReader} implementation (using aggregation).
 * Designed to be used by concrete Stax2 implementations to provide
 * full Event API implementation with minimal effort.
 *<p>
 * Since there is not much to optimize at this
 * level (API and underlying stream reader pretty much define functionality
 * and optimizations that can be done), implementation is fairly straight
 * forward, with not many surprises.
 *<p>
 * Implementation notes: the trickiest things to implement are:
 * <ul>
 *  <li>Peek() functionality! Geez, why did that have to be part of StAX
 *    specs???!
 *   </li>
 *  <li>Adding START_DOCUMENT event that cursor API does not return
 *    explicitly.
 *   </li>
 * </ul>
 */
public abstract class Stax2EventReaderImpl implements XMLEventReader2, XMLStreamConstants {
    // // // Enumerated state ids

    protected final static int STATE_INITIAL = 1;
    protected final static int STATE_END_OF_INPUT = 2;
    protected final static int STATE_CONTENT = 3;

    // // // Enumerated error case ids

    /**
     * Current state when getElementText() called not START_ELEMENT
     */
    protected final static int ERR_GETELEMTEXT_NOT_START_ELEM = 1;

    /**
     * Encountered non-textual event (other than closing END_ELEMENT)
     * when collecting text for getElementText()
     */
    protected final static int ERR_GETELEMTEXT_NON_TEXT_EVENT = 2;

    /**
     * Encountered CHARACTERS or CDATA that contains non-white space
     * char(s), when trying to locate tag with nextTag()
     */
    protected final static int ERR_NEXTTAG_NON_WS_TEXT = 3;

    /**
     * Encountered non-skippable non-text/element event with
     * nextTag()
     */
    protected final static int ERR_NEXTTAG_WRONG_TYPE = 4;

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    protected final XMLEventAllocator mAllocator;

    protected final XMLStreamReader2 mReader;

    /*
    /**********************************************************************
    /* State
    /**********************************************************************
     */

    // 22-Aug-2018, tatu: was `private` pre-4.2
    /**
     * Event that has been peeked, ie. loaded without call to
     * {@link #nextEvent}; will be returned and cleared by
     * call to {@link #nextEvent} (or, returned again if peeked
     * again)
     */
    protected XMLEvent mPeekedEvent = null;

    /**
     * High-level state indicator, with currently three values:
     * whether we are initializing (need to synthetize START_DOCUMENT),
     * at END_OF_INPUT (end-of-doc), or otherwise, normal operation.
     * Useful in simplifying some methods, as well as to make sure
     * that independent of how stream reader handles things, event reader
     * can reliably detect End-Of-Document.
     */
    protected int mState = STATE_INITIAL;

    /**
     * This variable keeps track of the type of the 'previous' event
     * when peeking for the next Event. It is needed for some functionality,
     * to remember state even when underlying parser has to move to peek
     * the next event.
     */
    protected int mPrePeekEvent = START_DOCUMENT;

    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    protected Stax2EventReaderImpl(XMLEventAllocator a, XMLStreamReader2 r) {
        mAllocator = a;
        mReader = r;
    }

    /*
    /**********************************************************************
    /* Abstract methods sub-classes have to implement
    /**********************************************************************
     */

    @Override
    public abstract boolean isPropertySupported(String name);

    @Override
    public abstract boolean setProperty(String name, Object value);

    /**
     * Method called upon encountering a problem that should result
     * in an exception being thrown. If non-null String is returned.
     * that will be used as the message of exception thrown; if null,
     * a standard message will be used instead.
     *
     * @param errorType Type of the problem, one of <code>ERR_</code>
     *    constants
     * @param eventType Type of the event that triggered the problem,
     *    if any; -1 if not available.
     */
    protected abstract String getErrorDesc(int errorType, int eventType);

    /*
    /**********************************************************************
    /* XMLEventReader API
    /**********************************************************************
     */

    @Override
    public void close() throws XMLStreamException {
        mReader.close();
    }

    @Override
    public String getElementText() throws XMLStreamException {
        /* Simple, if no peeking occured: can just forward this to the
         * underlying parser
         */
        if (mPeekedEvent == null) {
            return mReader.getElementText();
        }

        XMLEvent evt = mPeekedEvent;
        mPeekedEvent = null;

        /* Otherwise need to verify that we are currently over START_ELEMENT.
         * Problem is we have already went past it...
         */
        if (mPrePeekEvent != START_ELEMENT) {
            reportProblem(findErrorDesc(ERR_GETELEMTEXT_NOT_START_ELEM, mPrePeekEvent));
        }
        // ??? do we need to update mPrePeekEvent now

        String str = null;
        StringBuilder sb = null;

        /* Ok, fine, then just need to loop through and get all the
         * text...
         */
        for (; true; evt = nextEvent()) {
            if (evt.isEndElement()) {
                break;
            }
            int type = evt.getEventType();
            if (type == COMMENT || type == PROCESSING_INSTRUCTION) {
                // can/should just ignore them
                continue;
            }
            if (!evt.isCharacters()) {
                reportProblem(findErrorDesc(ERR_GETELEMTEXT_NON_TEXT_EVENT, type));
            }
            String curr = evt.asCharacters().getData();
            if (str == null) {
                str = curr;
            } else {
                if (sb == null) {
                    sb = new StringBuilder(str.length() + curr.length());
                    sb.append(str);
                }
                sb.append(curr);
            }
        }

        if (sb != null) {
            return sb.toString();
        }
        return (str == null) ? "" : str;
    }

    @Override
    public Object getProperty(String name) {
        return mReader.getProperty(name);
    }

    @Override
    public boolean hasNext() {
        return (mState != STATE_END_OF_INPUT);
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        if (mState == STATE_END_OF_INPUT) {
            throwEndOfInput();
        } else if (mState == STATE_INITIAL) {
            mState = STATE_CONTENT;
            return createStartDocumentEvent();
        }
        if (mPeekedEvent != null) {
            XMLEvent evt = mPeekedEvent;
            mPeekedEvent = null;
            if (evt.isEndDocument()) {
                updateStateEndDocument();
            }
            return evt;
        }
        return createNextEvent(true, mReader.next());
    }

    @Override
    public Object next() {
        try {
            return nextEvent();
        } catch (XMLStreamException sex) {
            throwUnchecked(sex);
            return null;
        }
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        // If we have peeked something, need to process it
        if (mPeekedEvent != null) {
            XMLEvent evt = mPeekedEvent;
            mPeekedEvent = null;
            int type = evt.getEventType();
            switch (type) {
                case END_DOCUMENT:
                    return null;

                case START_DOCUMENT:
                    // Need to skip START_DOCUMENT to get the root elem
                    break;

                case SPACE:
                    // Ignorable WS is just fine
                    break;

                /* !!! 07-Dec-2004, TSa: Specs are mum about Comments and PIs.
                 *  But why would they not be skipped just like what
                 *  the stream reader does?
                 */
                case COMMENT:
                case PROCESSING_INSTRUCTION:
                    break;

                case CDATA:
                case CHARACTERS:
                    if (((Characters) evt).isWhiteSpace()) {
                        break;
                    }
                    reportProblem(findErrorDesc(ERR_NEXTTAG_NON_WS_TEXT, type));
                    break; // never gets here, but some compilers whine without...

                case START_ELEMENT:
                case END_ELEMENT:
                    return evt;

                default:
                    reportProblem(findErrorDesc(ERR_NEXTTAG_WRONG_TYPE, type));
            }
        } else {
            /* 13-Sep-2005, TSa: As pointed out by Patrick, we may need to
             *   initialize the state here, too; otherwise peek() won't work
             *   correctly. The problem is that following loop's get method
             *   does not use event reader's method but underlying reader's.
             *   As such, it won't update state: most importantly, initial
             *   state may not be changed to non-initial.
             */
            if (mState == STATE_INITIAL) {
                mState = STATE_CONTENT;
            }
        }

        while (true) {
            int next = mReader.next();

            switch (next) {
                case END_DOCUMENT:
                    return null;

                case SPACE:
                case COMMENT:
                case PROCESSING_INSTRUCTION:
                    continue;

                case CDATA:
                case CHARACTERS:
                    if (mReader.isWhiteSpace()) {
                        continue;
                    }
                    reportProblem(findErrorDesc(ERR_NEXTTAG_NON_WS_TEXT, next));
                    break; // just to keep Jikes happy...

                case START_ELEMENT:
                case END_ELEMENT:
                    return createNextEvent(false, next);

                default:
                    reportProblem(findErrorDesc(ERR_NEXTTAG_WRONG_TYPE, next));
            }
        }
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
        if (mPeekedEvent == null) {
            if (mState == STATE_END_OF_INPUT) {
                // 06-Mar-2006, TSa: Fixed as per Arjen's suggestion:
                //throwEndOfInput();
                return null;
            }
            if (mState == STATE_INITIAL) {
                // Not sure what it should be... but this should do:
                mPrePeekEvent = START_DOCUMENT;
                mPeekedEvent = createStartDocumentEvent();
                mState = STATE_CONTENT;
            } else {
                mPrePeekEvent = mReader.getEventType();
                mPeekedEvent = createNextEvent(false, mReader.next());
            }
        }
        return mPeekedEvent;
    }

    /**
     * Note: only here because we implement Iterator interface. Will not
     * work, don't bother calling it.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Can not remove events from XMLEventReader.");
    }

    /**
     * Method called when we are about to return <code>END_DOCUMENT</code> event.
     * Usually this should change state to <code>STATE_END_OF_INPUT</code>, but
     * may vary for some alternative read modes (like multi-document)
     *
     * @since 4.2
     */
    protected void updateStateEndDocument() throws XMLStreamException {
        mState = STATE_END_OF_INPUT;
    }

    /*
    /**********************************************************************
    /* XMLEventReader2 API
    /**********************************************************************
     */

    /**
     *<p>
     * Note: although the interface allows implementations to
     * throw an {@link XMLStreamException}, the reference implementation
     * doesn't currently need to.
     * It's still declared, in case in future there is need to throw
     * such an exception.
     */
    @Override
    public boolean hasNextEvent() throws XMLStreamException {
        return (mState != STATE_END_OF_INPUT);
    }

    /*
    /**********************************************************************
    /* Overridable factory methods
    /**********************************************************************
     */

    protected XMLEvent createNextEvent(boolean checkEOD, int type) throws XMLStreamException {
        try {
            XMLEvent evt = mAllocator.allocate(mReader);
            if (checkEOD && type == END_DOCUMENT) {
                updateStateEndDocument();
            }
            return evt;
        } catch (RuntimeException rex) {
            throw _checkUnwrap(rex);
        }
    }

    protected XMLStreamException _checkUnwrap(RuntimeException rex) {
        /* 29-Mar-2008, TSa: Due to some problems with Stax API
         *  (lack of 'throws XMLStreamException' in signature of
         *  XMLStreamReader.getText(), for one) it is possible
         *  we will get a wrapped XMLStreamException. If so,
         *  we should be able to unwrap it.
         */
        Throwable t = rex.getCause();
        while (t != null) {
            if (t instanceof XMLStreamException) {
                return (XMLStreamException) t;
            }
            t = t.getCause();
        }
        // Nope, need to re-throw as is
        throw rex;
    }

    /**
     * Method called to create the very first event (START_DOCUMENT).
     */
    protected XMLEvent createStartDocumentEvent() throws XMLStreamException {
        XMLEvent start = mAllocator.allocate(mReader);
        return start;
    }

    /*
    /**********************************************************************
    /* Overridable error reporting methods
    /**********************************************************************
     */

    // note: `private` before 4.2
    protected void throwEndOfInput() {
        throw new NoSuchElementException();
    }

    protected void throwUnchecked(XMLStreamException sex) {
        // Wrapped root cause? Let's only unwrap one layer; one that
        // must have been used to expose the problem (if any)
        Throwable t = (sex.getNestedException() == null) ? sex : sex.getNestedException();
        // Unchecked? Can re-throw as is
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        // Otherwise, let's just wrap it
        throw new RuntimeException("[was " + t.getClass() + "] " + t.getMessage(), t);
    }

    protected void reportProblem(String msg) throws XMLStreamException {
        reportProblem(msg, mReader.getLocation());
    }

    protected void reportProblem(String msg, Location loc) throws XMLStreamException {
        if (loc == null) {
            throw new XMLStreamException(msg);
        }
        throw new XMLStreamException(msg, loc);
    }

    /*
    /**********************************************************************
    /* Package methods for sub-classes
    /**********************************************************************
     */

    protected XMLStreamReader getStreamReader() {
        return mReader;
    }

    /*
    /**********************************************************************
    /* Other internal methods
    /**********************************************************************
     */

    // note: `private` before 4.2
    /**
     * Method used to locate error message description to use.
     * Calls sub-classes <code>getErrorDesc()</code> first, and only
     * if no message found, uses default messages defined here.
     */
    protected final String findErrorDesc(int errorType, int currEvent) {
        String msg = getErrorDesc(errorType, currEvent);
        if (msg != null) {
            return msg;
        }
        switch (errorType) {
            case ERR_GETELEMTEXT_NOT_START_ELEM:
                return "Current state not START_ELEMENT when calling getElementText()";

            case ERR_GETELEMTEXT_NON_TEXT_EVENT:
                return "Expected a text token";

            case ERR_NEXTTAG_NON_WS_TEXT:
                return "Only all-whitespace CHARACTERS/CDATA (or SPACE) allowed for nextTag()";

            case ERR_NEXTTAG_WRONG_TYPE:
                return "Should only encounter START_ELEMENT/END_ELEMENT, SPACE, or all-white-space CHARACTERS";
        }

        // should never happen, but it'd be bad to throw another exception...
        return "Internal error (unrecognized error type: " + errorType + ")";
    }
}
