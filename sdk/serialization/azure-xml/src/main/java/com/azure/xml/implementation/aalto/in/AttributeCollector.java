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

package com.azure.xml.implementation.aalto.in;

import java.text.MessageFormat;

import javax.xml.namespace.QName;

import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.util.DataUtil;

/**
 * Object used by the tokenizer to collect and store information
 * about attributes, specifically, names and values.
 *<p>
 */
public final class AttributeCollector {

    /**
     * Let's guess that most of the time there won't be more than
     * 12 attributes. Since the underlying buffer will be expanded
     * as necessary, exact value is chosen to minimize overhead
     * rather than eliminate any resizing.
     */
    private final static int DEFAULT_ENTRY_COUNT = 12;

    /**
     * The default length of the value buffer is also chosen more
     * to minimize overhead than to eliminate all need for resizing.
     */
    private final static int DEFAULT_BUFFER_LENGTH = 120;

    // // // State: actual collected attributes

    /**
     * Number of attributes currently held by this collector.
     */
    private int _attrCount;

    private PName[] _names = null;

    /**
     * Consequtive character array, in which attribute values are
     * concatenated in
     */
    private char[] _valueBuffer = null;

    // // // State: hash table (-like structure) for attributes

    /**
     * Int-based compact data structure that contains mapping from
     * attribute names to attribute indexes in the main attribute name array.
     *<p>
     * Data structure contains two separate areas; main hash area (with
     * size <code>_hashAreaSize</code>), and remaining spillover area
     * that follows hash area up until (but not including)
     * <code>_spillAreaEnd</code> index.
     * Main hash area only contains indexes (index+1; 0 signifying empty slot)
     * to actual attributes; spillover area has both hash and index for
     * any spilled entry. Spilled entries are simply stored in order
     * added, and need to be searched using linear search. In case of both
     * primary hash hits and spills, eventual comparison with the local
     * name needs to be done with actual name array.
     */
    private int[] _attrMap = null;

    /**
     * Size of hash area in <code>_attrMap</code>; generally at least 20%
     * more than number of attributes (<code>_attrCount</code>).
     */
    private int _hashAreaSize;

    /**
     * Pointer to int slot right after last spill entry, in
     * <code>_attrMap</code> array.
     */
    private int _spillAreaEnd;

    // // // State: work-in-progress:

    /**
     * Array that contains ending offsets of the values in the shared
     * buffer. Entries contain character offset after the end of
     * the matching offset; so entry 0 for example contains starting
     * offset of the entry 1.
     */
    private int[] _valueOffsets = null;

    /**
     * Flag used to indicate that all attribute values for an element
     * have been parsed, and that next call to <code>startNewValue</code>
     * should reset the value structures
     */
    private boolean _needToResetValues = true;

    /**
     * For some errors, we'll have to temporarily store error message,
     * to be thrown at a later point.
     */
    private String _errorMsg = null;

    // // // Temporary storage for optimizations

    /**
     * Concatenated String that contains all the attribute values
     * for the element. Allows some buffer reuse, and should result
     * in slight speed optimization, for elements with lots of
     * attributes that are usually all (or none) accessed.
     */
    private String _allAttrValues = null;

    /*
    /**********************************************************************
    /* Life-cycle methods (creation, further construction)
    /**********************************************************************
     */

    AttributeCollector() {
        _attrCount = 0;
    }

    /**
     * Method called by the parser right after attribute name has been
     * parsed, but before value has been parsed.
     *
     * @return Underlying character buffer to use for storing attribute
     *   value characters
     */
    public char[] startNewValue(PName attrName, int currOffset) {
        int count;

        if (_needToResetValues) {
            _needToResetValues = false;
            _attrCount = count = 0;
            _allAttrValues = null;
            if (_valueBuffer == null) { // first time for this instance
                _names = new PName[DEFAULT_ENTRY_COUNT];
                _valueBuffer = new char[DEFAULT_BUFFER_LENGTH];
                _valueOffsets = new int[DEFAULT_ENTRY_COUNT];
            }
        } else {
            // Not enough room for a new entry?
            count = _attrCount;
            if (count >= _valueOffsets.length) {
                int[] oldVal = _valueOffsets;
                PName[] oldNames = _names;
                int oldLen = oldVal.length;
                int newLen = oldLen + oldLen;
                _valueOffsets = new int[newLen];
                _names = new PName[newLen];
                for (int i = 0; i < oldLen; ++i) {
                    _valueOffsets[i] = oldVal[i];
                    _names[i] = oldNames[i];
                }
            }
            if (count > 0) { // no predecessor for the first entry
                _valueOffsets[count - 1] = currOffset;
            }
        }
        _names[count] = attrName;
        ++_attrCount;
        return _valueBuffer;
    }

    /**
     * Method called after all attribute entries have been parsed,
     * and thus the end of the last value in the buffer is known.
     *
     * @return Number of attributes collected
     */
    public int finishLastValue(int endingOffset) {
        // Did we get any values?
        if (_needToResetValues) { // nope
            return 0;
        }
        _needToResetValues = true; // so it'll get reset next time a value is started

        // Since a previous startNewValue checked buffers, no check needed
        int count = _attrCount;
        _valueOffsets[count - 1] = endingOffset;

        /* So far so good. But now, also need to ensure there are no
         * duplicates. This also allows us to create a hash for efficient
         * access by name as a side effect. Since hash table building
         * overhead is somewhat significant, let's only use it for 3 or
         * more attributes.
         */
        if (count < 3) {
            _hashAreaSize = 0;
            if (count == 2) {
                PName[] names = _names;
                if (names[0].boundEquals(names[1])) {
                    noteDupAttr(0, 1);
                    return -1;
                }
            }
            return count;
        }
        return finishLastValue2();
    }

    public int finishLastValue2() {
        int count = _attrCount;
        PName[] names = _names;

        // Ok, nope, better use a hash:
        /* Ok, finally, let's create attribute map, to allow efficient
         * access by prefix+localname combination. Could do it on-demand,
         * but this way we can check for duplicates right away.
         */
        int[] map = _attrMap;

        /* What's minimum size to contain at most 80% full hash area,
         * plus 1/8 spill area (12.5% spilled entries, two ints each)?
         * Since we'll need 8 for 4 entries and up, and minimum to get
         * here is 3 entries, let's just skip 4 entry map...
         */
        int hashCount = 8;
        {
            int min = count + (count >> 2); // == 80% fill rate
            /* Need to get 2^N size that can contain all elements, with
             * 80% fill rate
             */
            while (hashCount < min) {
                hashCount += hashCount; // 2x
            }
            // And then add the spill area
            _hashAreaSize = hashCount;
            min = hashCount + (hashCount >> 4); // 12.5 x 2 ints
            if (map == null || map.length < min) {
                map = new int[min];
            } else {
                /* Need to clear old hash entries (if any). But note that
                 * spilled entries we can leave alone -- they are just ints,
                 * and get overwritten if and as needed
                 */
                map[0] = map[1] = map[2] = map[3] = map[4] = map[5] = map[6] = map[7] = 0;
                for (int i = 8; i < hashCount; ++i) {
                    map[i] = 0;
                }
            }
        }

        {
            int mask = hashCount - 1;
            int spillIndex = hashCount;

            // Ok, array's fine, let's hash 'em in!
            for (int i = 0; i < count; ++i) {
                PName newName = names[i];
                int hash = newName.boundHashCode();
                int index = hash & mask;
                // Hash slot available?
                int oldNameIndex = map[index];
                if (oldNameIndex == 0) { // yup
                    map[index] = i + 1; // since 0 is marker
                } else { // nope, collision, need to spill
                    --oldNameIndex; // to unmask 0 etc
                    // But first, is it a dup?
                    if (names[oldNameIndex].boundEquals(newName)) {
                        // Only first collision needs to be reported
                        if (_errorMsg == null) {
                            noteDupAttr(oldNameIndex, i);
                        }
                        /* let's still continue to build hash, even if there's
                         * collision; to keep data as consistent (and accessible)
                         * as possible
                         */
                    }
                    /* Is there room to spill into? (need to 2 int spaces;
                     * one for hash, the other for index)
                     */
                    if ((spillIndex + 1) >= map.length) {
                        // Let's just add room for 4 spills...
                        map = DataUtil.growArrayBy(map, 8);
                    }
                    // Let's first ensure we aren't adding a dup:
                    for (int j = hashCount; j < spillIndex; j += 2) {
                        if (map[j] == hash) {
                            oldNameIndex = map[j + 1];
                            if (names[oldNameIndex].boundEquals(newName)) {
                                if (_errorMsg == null) {
                                    noteDupAttr(oldNameIndex, i);
                                }
                                break;
                            }
                        }
                    }
                    map[spillIndex++] = hash;
                    map[spillIndex++] = i; // no need to mask 0
                }
            }
            _spillAreaEnd = spillIndex;
        }
        _attrMap = map;

        return (_errorMsg == null) ? count : -1;
    }

    /**
     * Method called by the owner, when the
     */
    public char[] valueBufferFull() {
        /* Let's just double the size as necessary? Could also grow
         * by less (50%?)... but shouldn't greatly matter
         */
        _valueBuffer = DataUtil.growArrayBy(_valueBuffer, _valueBuffer.length);
        return _valueBuffer;
    }

    /*
    /**********************************************************************
    /* Accessors
    /**********************************************************************
     */

    public int getCount() {
        return _attrCount;
    }

    public PName getName(int index) {
        return _names[index];
    }

    public QName getQName(int index) {
        return _names[index].constructQName();
    }

    public String getValue(int index) {
        int count = _attrCount;

        // Note: no checks, caller is to ensure index is ok. Acceptable
        // since it's not externally exposed
        if (_allAttrValues == null) {
            int len = _valueOffsets[count - 1];
            _allAttrValues = (len == 0) ? "" : new String(_valueBuffer, 0, len);
        }
        if (index == 0) {
            if (count == 1) { // Degenerate case; only one substring?
                return _allAttrValues;
            }
            int len = _valueOffsets[0];
            return (len == 0) ? "" : _allAttrValues.substring(0, len);
        }
        // !!! 11-Nov-2006, tatus: Should we cache constructed value?
        //   Might be worth the trouble
        int start = _valueOffsets[index - 1];
        int end = _valueOffsets[index];
        return (start == end) ? "" : _allAttrValues.substring(start, end);
    }

    public String getValue(String nsUri, String localName) {
        int ix = findIndex(nsUri, localName);
        return (ix >= 0) ? getValue(ix) : null;
    }

    public int findIndex(String nsUri, String localName) {
        int hashSize = _hashAreaSize;

        // No hash? Linear search, then:
        if (hashSize < 1) {
            for (int i = 0, len = _attrCount; i < len; ++i) {
                PName curr = _names[i];
                if (curr.boundEquals(nsUri, localName)) {
                    return i;
                }
            }
            return -1;
        }

        // Need to/can use hash... primary hit?
        int hash = PName.boundHashCode(localName);
        int ix = _attrMap[hash & (hashSize - 1)];

        if (ix > 0) { // has primary entry, does it match?
            --ix;
            // Is primary candidate match?
            if (_names[ix].boundEquals(nsUri, localName)) {
                return ix;
            }
            // Nope, need to traverse spill list, which has 2 entries for
            // each spilled attribute id; first for hash value, second index.
            for (int i = hashSize, len = _spillAreaEnd; i < len; i += 2) {
                if (_attrMap[i] != hash) {
                    continue;
                }
                // Note: spill indexes are not off-by-one, since there's
                // no need to mask 0
                ix = _attrMap[i + 1];
                if (_names[ix].boundEquals(nsUri, localName)) {
                    return ix;
                }
            }
        }

        return -1;
    }

    public String getErrorMsg() {
        return _errorMsg;
    }

    /*
    /**********************************************************************
    /* Type-safe accessors to support TypedXMLStreamReader
    /**********************************************************************
     */

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    private void noteDupAttr(int ix1, int ix2) {
        _errorMsg = MessageFormat.format(ErrorConsts.ERR_WF_DUP_ATTRS, _names[ix1].toString(), ix1,
            _names[ix2].toString(), ix2);

    }
}
