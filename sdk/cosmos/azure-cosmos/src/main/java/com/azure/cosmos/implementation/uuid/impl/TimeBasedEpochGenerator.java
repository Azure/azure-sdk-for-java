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

package com.azure.cosmos.implementation.uuid.impl;

import com.azure.cosmos.implementation.uuid.NoArgGenerator;
import com.azure.cosmos.implementation.uuid.UUIDType;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

/**
 * Implementation of UUID generator that uses time/location based generation
 * method field from the Unix Epoch timestamp source - the number of 
 * milliseconds seconds since midnight 1 Jan 1970 UTC, leap seconds excluded.
 * This is usually referred to as "Variant 7".
 * <p>
 * As all JUG provided implementations, this generator is fully thread-safe.
 * Additionally it can also be made externally synchronized with other instances
 * (even ones running on other JVMs); to do this, use
 * {@link com.azure.cosmos.implementation.uuid.ext.FileBasedTimestampSynchronizer} (or
 * equivalent).
 *
 * @since 4.1
 */
public class TimeBasedEpochGenerator extends NoArgGenerator
{
    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /**
     * Random number generator that this generator uses.
     */
    protected final Random _random;

    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    /**
     * @param rnd Random number generator to use for generating UUIDs; if null,
     *   shared default generator is used. Note that it is strongly recommend to
     *   use a <b>good</b> (pseudo) random number generator; for example, JDK's
     *   {@link SecureRandom}.
     */
    
    public TimeBasedEpochGenerator(Random rnd)
    {
        if (rnd == null) {
            rnd = LazyRandom.sharedSecureRandom();
        }
        _random = rnd;
    }

    /*
    /**********************************************************************
    /* Access to config
    /**********************************************************************
     */

    @Override
    public UUIDType getType() { return UUIDType.TIME_BASED_EPOCH; }

    /*
    /**********************************************************************
    /* UUID generation
    /**********************************************************************
     */

    @Override
    public UUID generate()
    {
        final long rawTimestamp = System.currentTimeMillis();
        final byte[] rnd = new byte[10];
        _random.nextBytes(rnd);

        // Use only 48 lowest bits as per spec, next 16 bit from random
        // (note: UUIDUtil.constuctUUID will add "version" so it's only 12
        // actual random bits)
        long l1 = (rawTimestamp << 16) | _toShort(rnd, 8);

        // And then the other 64 bits of random; likewise UUIDUtil.constructUUID
        // will overwrite first 2 random bits so it's "only" 62 bits
        long l2 = _toLong(rnd, 0);

        // and as per above, this call fills in "variant" and "version" bits
        return UUIDUtil.constructUUID(UUIDType.TIME_BASED_EPOCH, l1, l2);
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected final static long _toLong(byte[] buffer, int offset)
    {
        long l1 = _toInt(buffer, offset);
        long l2 = _toInt(buffer, offset+4);
        long l = (l1 << 32) + ((l2 << 32) >>> 32);
        return l;
    }

    private final static long _toInt(byte[] buffer, int offset)
    {
        return (buffer[offset] << 24)
            + ((buffer[++offset] & 0xFF) << 16)
            + ((buffer[++offset] & 0xFF) << 8)
            + (buffer[++offset] & 0xFF);
    }

    private final static long _toShort(byte[] buffer, int offset)
    {
        return ((buffer[offset] & 0xFF) << 8)
            + (buffer[++offset] & 0xFF);
    }
}
