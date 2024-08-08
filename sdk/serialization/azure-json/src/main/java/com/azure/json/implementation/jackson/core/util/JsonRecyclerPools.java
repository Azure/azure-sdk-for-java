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
     * {@link #threadLocalPool()} -- 2.17.0 temporarily had this call
     * {@code #newLockFreePool()} (but reverted due to problems reported).
     * Will likely be changed in 2.18.0 to something else.
     *
     * @return the default {@link RecyclerPool} implementation to use
     *   if no specific implementation desired.
     */
    public static RecyclerPool<BufferRecycler> defaultPool() {
        return threadLocalPool();
    }

    /**
     * Accessor for getting the shared/global {@link ThreadLocalPool} instance
     * (due to design only one instance ever needed)
     *
     * @return Globally shared instance of {@link ThreadLocalPool}
     */
    public static RecyclerPool<BufferRecycler> threadLocalPool() {
        return ThreadLocalPool.GLOBAL;
    }

    /**
     * Accessor for getting the shared/global {@link NonRecyclingPool} instance
     * (due to design only one instance ever needed)
     *
     * @return Globally shared instance of {@link NonRecyclingPool}.
     */
    public static RecyclerPool<BufferRecycler> nonRecyclingPool() {
        return NonRecyclingPool.GLOBAL;
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

    /**
     * Dummy {@link RecyclerPool} implementation that does not recycle
     * anything but simply creates new instances when asked to acquire items.
     */
    public static class NonRecyclingPool extends RecyclerPool.NonRecyclingPoolBase<BufferRecycler> {
        private static final long serialVersionUID = 1L;

        protected static final NonRecyclingPool GLOBAL = new NonRecyclingPool();

        protected NonRecyclingPool() {
        }

        @Override
        public BufferRecycler acquirePooled() {
            return new BufferRecycler();
        }

        // // // JDK serialization support

        protected Object readResolve() {
            return GLOBAL;
        }
    }

}
