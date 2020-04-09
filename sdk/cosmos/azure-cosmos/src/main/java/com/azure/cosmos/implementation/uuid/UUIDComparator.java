/* JUG Java Uuid Generator
 *
 * Copyright (c) 2002- Tatu Saloranta, tatu.saloranta@iki.fi
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

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.uuid;

import java.util.Comparator;
import java.util.UUID;

/**
 * Default {@link UUID} comparator is not very useful, since
 * it just does blind byte-by-byte comparison which does not work well
 * for time+location - based UUIDs. Additionally it also uses signed
 * comparisons for longs which can lead to unexpected behavior
 * This comparator does implement proper lexical ordering: starting with
 * type (different types are collated
 * separately), followed by time and location (for time/location based),
 * and simple lexical (byte-by-byte) ordering for name/hash and random
 * variants.
 * 
 * @author tatu
 */
public class UUIDComparator implements Comparator<UUID>
{
    @Override
    public int compare(UUID u1, UUID u2)
    {
        return staticCompare(u1, u2);
    }

    /**
     * Static helper method that can be used instead of instantiating comparator
     * (used by unit tests, can be used by code too)
     */
    public static int staticCompare(UUID u1, UUID u2)
    {
        // First: major sorting by types
        int type = u1.version();
        int diff = type - u2.version();
        if (diff != 0) {
            return diff;
        }
        // Second: for time-based variant, order by time stamp:
        if (type == UUIDType.TIME_BASED.raw()) {
            diff = compareULongs(u1.timestamp(), u2.timestamp());
            if (diff == 0) {
                // or if that won't work, by other bits lexically
                diff = compareULongs(u1.getLeastSignificantBits(), u2.getLeastSignificantBits());
            }
        } else {
            // note: java.util.UUIDs compares with sign extension, IMO that's wrong, so:
            diff = compareULongs(u1.getMostSignificantBits(),
                    u2.getMostSignificantBits());
            if (diff == 0) {
                diff = compareULongs(u1.getLeastSignificantBits(),
                        u2.getLeastSignificantBits());
            }
        }
        return diff;
    }
    
    protected final static int compareULongs(long l1, long l2) {
        int diff = compareUInts((int) (l1 >> 32), (int) (l2 >> 32));
        if (diff == 0) {
            diff = compareUInts((int) l1, (int) l2);
        }
        return diff;
    }

    protected final static int compareUInts(int i1, int i2)
    {
        /* bit messier due to java's insistence on signed values: if both
         * have same sign, normal comparison (by subtraction) works fine;
         * but if signs don't agree need to resolve differently
         */
        if (i1 < 0) {
            return (i2 < 0) ? (i1 - i2) : 1;
        }
        return (i2 < 0) ? -1 : (i1 - i2);
    }
}
