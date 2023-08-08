package com.azure.cosmos.implementation.uuid.impl;

import com.azure.cosmos.implementation.uuid.NoArgGenerator;
import com.azure.cosmos.implementation.uuid.UUIDType;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

/**
 * Implementation of UUID generator that uses generation method 4.
 *<p>
 * Note on random number generation when using {@link SecureRandom} for random number
 * generation: the first time {@link SecureRandom} object is used, there is noticeable delay between
 * calling the method and getting the reply. This is because SecureRandom
 * has to initialize itself to reasonably random state. Thus, if you
 * want to lessen delay, it may be be a good idea to either get the
 * first random UUID asynchronously from a separate thread, or to
 * use the other generateRandomBasedUUID passing a previously initialized
 * SecureRandom instance.
 *
 * @since 3.0
 */
public class RandomBasedGenerator extends NoArgGenerator
{
    /**
     * Default shared random number generator, used if no random number generator
     * is explicitly specified for instance
     */
    protected static Random _sharedRandom = null;

    /**
     * Random number generator that this generator uses.
     */
    protected final Random _random;

    /**
     * Looks like {@link SecureRandom} implementation is more efficient
     * using single call access (compared to basic {@link Random}),
     * so let's use that knowledge to our benefit.
     */
    protected final boolean _secureRandom;
    
    /**
     * @param rnd Random number generator to use for generating UUIDs; if null,
     *   shared default generator is used. Note that it is strongly recommend to
     *   use a <b>good</b> (pseudo) random number generator; for example, JDK's
     *   {@link SecureRandom}.
     */
    public RandomBasedGenerator(Random rnd)
    {
        if (rnd == null) {
            rnd = LazyRandom.sharedSecureRandom();
            _secureRandom = true;
        } else {
            _secureRandom = (rnd instanceof SecureRandom);
        }
        _random = rnd;
    }

    /*
    /**********************************************************************
    /* Access to config
    /**********************************************************************
     */

    @Override
    public UUIDType getType() { return UUIDType.RANDOM_BASED; }

    /*
    /**********************************************************************
    /* UUID generation
    /**********************************************************************
     */
    
    @Override
    public UUID generate()
    {
        /* 14-Oct-2010, tatu: Surprisingly, variant for reading byte array is
         *   tad faster for SecureRandom... so let's use that then
         */
        long r1, r2;

        if (_secureRandom) {
            final byte[] buffer = new byte[16];
            _random.nextBytes(buffer);
            r1 = _toLong(buffer, 0);
            r2 = _toLong(buffer, 1);
        } else {
            r1 = _random.nextLong();
            r2 = _random.nextLong();
        }
        return UUIDUtil.constructUUID(UUIDType.RANDOM_BASED, r1, r2);
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    private final static long _toLong(byte[] buffer, int offset)
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

    /*
    /**********************************************************************
    /* Helper classes
    /**********************************************************************
     */

    /**
     * Trivial helper class that uses class loading as synchronization
     * mechanism for lazy instantation of the shared secure random
     * instance.
     */
    private final static class LazyRandom
    {
        private final static SecureRandom shared = new SecureRandom();

        public static SecureRandom sharedSecureRandom() {
            return shared;
        }
    }
}
