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

import com.azure.cosmos.implementation.uuid.ext.FileBasedTimestampSynchronizer;

import java.io.IOException;

/**
 * This is the API for utility classes optionally used by {@link UUIDTimer} to
 * ensure that timestamp values used for generating time/location-based UUIDs
 * are monotonically increasing, as well as that only one such generator
 * is ever used on a single system, even in presence of multiple JVMs.
 *<p>
 * The default implementation used by JUG is
 * {@link FileBasedTimestampSynchronizer}.
 */
public abstract class TimestampSynchronizer
{
    protected TimestampSynchronizer() { }

    /**
     * Initialization method is will be called on an instance by
     * {@link UUIDTimer} right after it's been configured with one.
     * At this point the implementation instance should have been properly
     * configured, and should be able to determine the first legal timestamp
     * value (return value). Method should also initialize any locking that
     * it does (if any), such as locking files it needs.
     *<p>
     * Return value thus indicates the lowest initial time value that can
     * be used by the caller that can not have been used by previous
     * incarnations of the UUID generator (assuming instance was able to
     * find properly persisted data to figure that out).
     * However, caller also needs to make sure that it will
     * call {@link #update} when it actually needs the time stamp for the
     * first time,
     * since this method can not allocate timestamps beyond this initial
     * value at this point.
     *
     * @return First (and last) legal timestamp to use; <code>0L</code> if it
     *  can not
     *   determine it and caller can use whatever value (current timestamp)
     *   it has access to.
     */
    protected abstract long initialize()
        throws IOException;

    /**
     * Method {@link UUIDTimer} will call if this synchronizer object is
     * being replaced by another synchronizer (or removed, that is, no
     * more synchronization is to be done). It will not be called if JVM
     * terminates.
     */
    protected abstract void deactivate()
        throws IOException;

    /**
     * Method called by {@link UUIDTimer} to indicate that it has generated
     * a timestamp value that is beyond last legal timestamp value.
     * The method should only return once it has "locked" specified timestamp
     * value (and possible additional ones).
     *
     * @param now Timestamp value caller wants to use, and that the
     *   synchronizer is asked to protect.
     *
     * @return First timestamp value that can NOT be used by the caller;
     *    has to be higher than the input timestamp value
     */
    protected abstract long update(long now)
        throws IOException;
}
