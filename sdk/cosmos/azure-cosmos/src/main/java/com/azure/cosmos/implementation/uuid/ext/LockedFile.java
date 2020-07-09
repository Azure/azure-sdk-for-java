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

package com.azure.cosmos.implementation.uuid.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Utility class used by {@link FileBasedTimestampSynchronizer} to do
 * actual file access and locking.
 *<p>
 * Class stores simple timestamp values based on system time accessed
 * using <code>System.currentTimeMillis()</code>. A single timestamp
 * is stored into a file using {@link RandomAccessFile} in fully
 * synchronized mode. Value is written in ISO-Latin (ISO-8859-1)
 * encoding (superset of Ascii, 1 byte per char) as 16-digit hexadecimal
 * number, surrounded by brackets. As such, file produced should
 * always have exact size of 18 bytes. For extra robustness, slight
 * variations in number of digits are accepeted, as are white space
 * chars before and after bracketed value.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

class LockedFile
{

    private static final Logger logger = LoggerFactory.getLogger(LockedFile.class);
    
    /**
     * Expected file length comes from hex-timestamp (16 digits),
     * preamble "[0x",(3 chars) and trailer "]\r\n" (2 chars, linefeed
     * to help debugging -- in some environments, missing trailing linefeed
     * causes problems: also, 2-char linefeed to be compatible with all
     * standard linefeeds on MacOS, Unix and Windows).
     */
    final static int DEFAULT_LENGTH = 22;

    final static long READ_ERROR = 0L;

    // // // Configuration:

    final File mFile;

    // // // File state

    RandomAccessFile mRAFile;

    FileChannel mChannel;

    FileLock mLock;

    ByteBuffer mWriteBuffer = null;

    /**
     * Flag set if the original file (created before this instance was
     * created) had size other than default size and needs to be
     * truncated
     */
    boolean mWeirdSize;

    /**
     * Marker used to ensure that the timestamps stored are monotonously
     * increasing. Shouldn't really be needed, since caller should take
     * care of it, but let's be bit paranoid here.
     */
    long mLastTimestamp = 0L;

    LockedFile(File f)
        throws IOException
    {
        mFile = f;

        RandomAccessFile raf = null;
        FileChannel channel = null;
        FileLock lock = null;
        boolean ok = false;

        try { // let's just use a single block to share cleanup code
            raf = new RandomAccessFile(f, "rwd");
            
            // Then lock them, if possible; if not, let's err out
            channel = raf.getChannel();
            if (channel == null) {
                throw new IOException("Failed to access channel for '"+f+"'");
            }
            lock = channel.tryLock();
            if (lock == null) {
                throw new IOException("Failed to lock '"+f+"' (another JVM running UUIDGenerator?)");
            }
            ok = true;
        } finally {
            if (!ok) {
                doDeactivate(f, raf, lock);
            }
        }

        mRAFile = raf;
        mChannel = channel;
        mLock = lock;
    }

    public void deactivate()
    {
        RandomAccessFile raf = mRAFile;
        mRAFile = null;
        FileLock lock = mLock;
        mLock = null;
        doDeactivate(mFile, raf, lock);
    }

    public long readStamp()
    {
        int size;

        try {
            size = (int) mChannel.size();
        } catch (IOException ioe) {
            logger.error("Failed to read file size", ioe);
            return READ_ERROR;
        }

        mWeirdSize = (size != DEFAULT_LENGTH);

        // Let's check specifically empty files though
        if (size == 0) {
            logger.warn("Missing or empty file, can not read timestamp value");
            return READ_ERROR;
        }

        // Let's also allow some slack... but just a bit
        if (size > 100) {
            size = 100;
        }
        byte[] data = new byte[size];
        try {
            mRAFile.readFully(data);
        } catch (IOException ie) {
            logger.error("(file '{}') Failed to read {} bytes", mFile, size, ie);
            return READ_ERROR;
        }

        /* Ok, got data. Now, we could just directly parse the bytes (since
         * it is single-byte encoding)... but for convenience, let's create
         * the String (this is only called once per JVM session)
         */
        char[] cdata = new char[size];
        for (int i = 0; i < size; ++i) {
            cdata[i] = (char) (data[i] & 0xFF);
        }
        String dataStr = new String(cdata);
        // And let's trim leading (and trailing, who cares)
        dataStr = dataStr.trim();

        long result = -1;
        String err = null;

        if (!dataStr.startsWith("[0")
            || dataStr.length() < 3
            || Character.toLowerCase(dataStr.charAt(2)) != 'x') {
            err = "does not start with '[0x' prefix";
        } else {
            int ix = dataStr.indexOf(']', 3);
            if (ix <= 0) {
                err = "does not end with ']' marker";
            } else {
                String hex = dataStr.substring(3, ix);
                if (hex.length() > 16) {
                    err = "length of the (hex) timestamp too long; expected 16, had "+hex.length()+" ('"+hex+"')";
                } else {
                    try {
                        result = Long.parseLong(hex, 16);
                    } catch (NumberFormatException nex) {
                        err = "does not contain a valid hex timestamp; got '"
                            +hex+"' (parse error: "+nex+")";
                    }
                }
            }
        }

        // Unsuccesful?
        if (result < 0L) {
            logger.error("(file '{}') Malformed timestamp file contents: {}", mFile, err);
            return READ_ERROR;
        }

	mLastTimestamp = result;
        return result;
    }

    final static String HEX_DIGITS = "0123456789abcdef";

    public void writeStamp(long stamp)
        throws IOException
    {
	// Let's do sanity check first:
	if (stamp <= mLastTimestamp) {
	    /* same stamp is not dangerous, but pointless... so warning,
	     * not an error:
	     */
	    if (stamp == mLastTimestamp) {
	        logger.warn("(file '{}') Trying to re-write existing timestamp ({})", mFile, stamp);
		return;
	    }
	    throw new IOException(""+mFile+" trying to overwrite existing value ("+mLastTimestamp+") with an earlier timestamp ("+stamp+")");
	}

//System.err.println("!!!! Syncing ["+mFile+"] with "+stamp+" !!!");

        // Need to initialize the buffer?
        if (mWriteBuffer == null) {
            mWriteBuffer = ByteBuffer.allocate(DEFAULT_LENGTH);
            mWriteBuffer.put(0, (byte) '[');
            mWriteBuffer.put(1, (byte) '0');
            mWriteBuffer.put(2, (byte) 'x');
            mWriteBuffer.put(19, (byte) ']');
            mWriteBuffer.put(20, (byte) '\r');
            mWriteBuffer.put(21, (byte) '\n');
        }

        // Converting to hex is simple
        for (int i = 18; i >= 3; --i) {
            int val = (((int) stamp) & 0x0F);
            mWriteBuffer.put(i, (byte) HEX_DIGITS.charAt(val));
            stamp = (stamp >> 4);
        }
        // and off we go:
        mWriteBuffer.position(0); // to make sure we always write it all
        mChannel.write(mWriteBuffer, 0L);
        if (mWeirdSize) {
            mRAFile.setLength(DEFAULT_LENGTH);
            mWeirdSize = false;
        }

        // This is probably not needed (as the random access file is supposedly synced)... but let's be safe:
        mChannel.force(false);

        // And that's it!
    }

    /*
    //////////////////////////////////////////////////////////////
    // Internal methods
    //////////////////////////////////////////////////////////////
     */

    protected static void doDeactivate(File f, RandomAccessFile raf,
                                       FileLock lock)
    {
        if (lock != null) {
            try {
                lock.release();
            } catch (Throwable t) {
                logger.error("Failed to release lock (for file '{}')", f, t);
            }
        }
        if (raf != null) {
            try {
                raf.close();
            } catch (Throwable t) {
                logger.error("Failed to close file '{}'", f, t);
            }
        }
    }
}


