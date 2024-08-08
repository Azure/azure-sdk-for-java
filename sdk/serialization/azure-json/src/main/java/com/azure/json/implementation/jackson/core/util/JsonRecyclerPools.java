// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.util;

import com.azure.json.implementation.jackson.core.JsonFactory;

/**
 * Set of {@link RecyclerPool} implementations to be used by the default
 * JSON-backed {@link JsonFactory} for recycling {@link BufferRecycler}
 * containers.
 *
 * @since 2.16
 */
public final class JsonRecyclerPools {
    /**
     * Method to call to get the default recycler pool instance:
     * as of Jackson 2.17.x and earlier (except for 2.17.0) this is same as calling
     * {@code #threadLocalPool()} -- 2.17.0 temporarily had this call
     * {@code #newLockFreePool()} (but reverted due to problems reported).
     * Will likely be changed in 2.18.0 to something else.
     *
     * @return the default {@link RecyclerPool} implementation to use
     *   if no specific implementation desired.
     */
    public static RecyclerPool<BufferRecycler> defaultPool() {
        return ThreadLocalPool.GLOBAL;
    }

    /*
    /**********************************************************************
    /* Concrete RecyclerPool implementations for recycling BufferRecyclers
    /**********************************************************************
     */

    /**
     * {@link ThreadLocal}-based {@link RecyclerPool} implementation used for
     * recycling {@link BufferRecycler} instances:
     * see {@link RecyclerPool.ThreadLocalPoolBase} for full explanation
     * of functioning.
     */
    public static class ThreadLocalPool extends RecyclerPool.ThreadLocalPoolBase<BufferRecycler> {
        private static final long serialVersionUID = 1L;

        protected static final ThreadLocalPool GLOBAL = new ThreadLocalPool();

        private ThreadLocalPool() {
        }

        @SuppressWarnings("deprecation")
        @Override
        public BufferRecycler acquirePooled() {
            return BufferRecyclers.getBufferRecycler();
        }

        // // // JDK serialization support

        protected Object readResolve() {
            return GLOBAL;
        }
    }

}
