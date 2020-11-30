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

package com.azure.cosmos.implementation.uuid.ext;

import com.azure.cosmos.implementation.uuid.TimestampSynchronizer;
import com.azure.cosmos.implementation.uuid.UUIDTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Implementation of {@link TimestampSynchronizer}, which uses file system
 * as the storage and locking mechanism.
 *<p>
 * Synchronization is achieved by obtaining an exclusive file locks on two
 * specified lock files, and by using the files to store first "safe" timestamp
 * value that the generator can use; alternating between one to use to ensure
 * one of them always contains a valid timestamp. Latter is needed to guard
 * against system clock moving backwards after UUID generator restart.
 *<p>
 * Note: this class will only work on JDK 1.4 and above, since it requires
 * NIO package to do proper file locking (as well as new opening mode for
 * {@link RandomAccessFile}).
 *<p>
 * Also note that it is assumed that the caller has taken care to synchronize
 * access to method to be single-threaded. As such, none of the methods
 * is explicitly synchronized here.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

public final class FileBasedTimestampSynchronizer
    extends TimestampSynchronizer
{

    private static final Logger logger = LoggerFactory.getLogger(FileBasedTimestampSynchronizer.class);
    
    // // // Constants:

    /**
     * The default update interval is 10 seconds, meaning that the
     * synchronizer "reserves" next 10 seconds for generation. This
     * also means that the lock files need to be accessed at most
     * once every ten second.
     */
    final static long DEFAULT_UPDATE_INTERVAL = 10L * 1000L;

    protected final static String DEFAULT_LOCK_FILE_NAME1 = "uuid1.lck";

    protected final static String DEFAULT_LOCK_FILE_NAME2 = "uuid2.lck";

    // // // Configuration:

    protected long mInterval = DEFAULT_UPDATE_INTERVAL;

    protected final LockedFile mLocked1, mLocked2;

    // // // State:

    /**
     * Flag used to indicate which of timestamp files has the most
     * recently succesfully updated timestamp value. True means that
     * <code>mFile1</code> is more recent; false that <code>mFile2</code>
     * is.
     */
    boolean mFirstActive = false;

    /**
     * Constructor that uses default values for names of files to use
     * (files will get created in the current working directory), as
     * well as for the update frequency value (10 seconds).
     */
    public FileBasedTimestampSynchronizer()
        throws IOException
    {
        this(new File(DEFAULT_LOCK_FILE_NAME1), new File(DEFAULT_LOCK_FILE_NAME2));
    }

    public FileBasedTimestampSynchronizer(File lockFile1, File lockFile2)
        throws IOException
    {
        this(lockFile1, lockFile2, DEFAULT_UPDATE_INTERVAL);
    }

    public FileBasedTimestampSynchronizer(File lockFile1, File lockFile2, long interval)
        throws IOException
    {
        mInterval = interval;
        mLocked1 = new LockedFile(lockFile1);

        boolean ok = false;
        try {
            mLocked2 = new LockedFile(lockFile2);
            ok = true;
        } finally {
            if (!ok) {
                mLocked1.deactivate();
            }
        }

        // But let's leave reading up to initialization
    }

    /*
    //////////////////////////////////////////////////////////////
    // Configuration
    //////////////////////////////////////////////////////////////
     */

    public void setUpdateInterval(long interval)
    {
        if (interval < 1L) {
            throw new IllegalArgumentException("Illegal value ("+interval+"); has to be a positive integer value");
        }
        mInterval = interval;
    }

    /*
    //////////////////////////////////////////////////////////////
    // Implementation of the API
    //////////////////////////////////////////////////////////////
     */

    /**
     * This method is to be called only once by
     * {@link UUIDTimer}. It
     * should fetch the persisted timestamp value, which indicates
     * first timestamp value that is guaranteed NOT to have used by
     * a previous incarnation. If it can not determine such value, it
     * is to return 0L as a marker.
     *
     * @return First timestamp value that was NOT locked by lock files;
     *   0L to indicate that no information was read.
     */
    @Override
    protected long initialize() throws IOException
    {
        long ts1 = mLocked1.readStamp();
        long ts2 = mLocked2.readStamp();
        long result;

        if (ts1 > ts2) {
            mFirstActive = true;
            result = ts1;
        } else {
            mFirstActive = false;
            result = ts2;
        }

        /* Hmmh. If we didn't get a time stamp (-> 0), or if written time is
         * ahead of current time, let's log something:
         */
        if (result <= 0L) {
            logger.warn("Could not determine safe timer starting point: assuming current system time is acceptable");
        } else {
            long now = System.currentTimeMillis();
            //long diff = now - result;

            /* It's more suspicious if old time was ahead... although with
             * longer iteration values, it can be ahead without errors. So
             * let's base check on current iteration value:
             */
            if ((now + mInterval) < result) {
                logger.warn("Safe timestamp read is {} milliseconds in future, and is greater than the inteval ({})",  (result - now), mInterval);
            }

            /* Hmmh. Is there any way a suspiciously old timestamp could be
             * harmful? It can obviously be useless but...
             */
        }

        return result;
    }

    @Override
    public void deactivate() throws IOException
    {
        doDeactivate(mLocked1, mLocked2);
    }

    /**
     * @return Timestamp value that the caller can NOT use. That is, all
     *   timestamp values prior to (less than) this value can be used
     *   ok, but this value and ones after can only be used by first
     *   calling update.
     */
    @Override
    public long update(long now)
        throws IOException
    {
        long nextAllowed = now + mInterval;

        /* We have to make sure to (over)write the one that is NOT
         * actively used, to ensure that we always have fully persisted
         * timestamp value, even if the write process gets interruped
         * half-way through.
         */

        if (mFirstActive) {
            mLocked2.writeStamp(nextAllowed);
        } else {
            mLocked1.writeStamp(nextAllowed);
        }

        mFirstActive = !mFirstActive;

        return nextAllowed;
    }

    /*
    //////////////////////////////////////////////////////////////
    // Internal methods
    //////////////////////////////////////////////////////////////
     */

    protected static void doDeactivate(LockedFile lf1, LockedFile lf2)
    {
        if (lf1 != null) {
            lf1.deactivate();
        }
        if (lf2 != null) {
            lf2.deactivate();
        }
    }
}
