// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.util;

import java.io.Serializable;
import java.util.Deque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

/**
 * API for object pools that control creation and possible reuse of
 * objects that are costly to create (often things like encoding/decoding buffers).
 *<p>
 * Also contains partial (base) implementations for pools that use different
 * strategies on retaining objects for reuse.
 * Following implementations are included:
 *<ul>
 * <li>{@link NonRecyclingPoolBase} which does not retain or recycle anything and
 * will always simply construct and return new instance when
 * {@code acquireBufferRecycler} is called
 *  </li>
 * <li>{@link ThreadLocalPoolBase} which uses {@link ThreadLocal} to retain at most
 *   1 object per {@link Thread}.
 * </li>
 * <li>{@link BoundedPoolBase} is "bounded pool" and retains at most N objects (default value being
 *  {@link BoundedPoolBase#DEFAULT_CAPACITY}) at any given time.
 *  </li>
 * <li>Two implementations -- {@link ConcurrentDequePoolBase}, {@link LockFreePoolBase}
 *   -- are "unbounded" and retain any number of objects released: in practice
 *   it is at most the highest number of concurrently used {@link BufferRecycler}s.
 *  </li>
 *</ul>
 *
 *<p>
 * Default implementations are also included as nested classes.
 *
 * @param <P> Type of Objects pool recycles
 *
 * @since 2.16
 */
public interface RecyclerPool<P extends RecyclerPool.WithPool<P>> extends Serializable {
    /**
     * Simple add-on interface that poolable entities must implement.
     *
     * @param <P> Self type
     */
    interface WithPool<P extends WithPool<P>> {
        /**
         * Method to call to add link from pooled item back to pool
         * that handles it
         *
         * @param pool Pool that "owns" pooled item
         *
         * @return This item (for call chaining)
         */
        P withPool(RecyclerPool<P> pool);

        /**
         * Method called when this item is to be released back to the
         * pool that owns it (if any)
         */
        void releaseToPool();
    }

    /**
     * Method called to acquire a Pooled value from this pool
     * AND make sure it is linked back to this
     * {@link RecyclerPool} as necessary for it to be
     * released (see {@link #releasePooled}) later after usage ends.
     * Actual acquisition is done by a call to {@link #acquirePooled()}.
     *<p>
     * Default implementation calls {@link #acquirePooled()} followed by
     * a call to {@link WithPool#withPool}.
     *
     * @return Pooled instance for caller to use; caller expected
     *   to call {@link #releasePooled} after it is done using instance.
     */
    default P acquireAndLinkPooled() {
        return acquirePooled().withPool(this);
    }

    /**
     * Method for sub-classes to implement for actual acquire logic; called
     * by {@link #acquireAndLinkPooled()}.
     *
     * @return Instance acquired (pooled or just constructed)
     */
    P acquirePooled();

    /**
     * Method that should be called when previously acquired (see {@link #acquireAndLinkPooled})
     * pooled value that is no longer needed; this lets pool to take ownership
     * for possible reuse.
     *
     * @param pooled Pooled instance to release back to pool
     */
    void releasePooled(P pooled);

    /**
     * Optional method that may allow dropping of all pooled Objects; mostly
     * useful for unbounded pool implementations that may retain significant
     * memory and that may then be cleared regularly.
     *
     * @since 2.17
     *
     * @return {@code true} If pool supports operation and dropped all pooled
     *    Objects; {@code false} otherwise.
     */
    default boolean clear() {
        return false;
    }

    /*
    /**********************************************************************
    /* Partial/base RecyclerPool implementations
    /**********************************************************************
     */

    /**
     * Default {@link RecyclerPool} implementation that uses
     * {@link ThreadLocal} for recycling instances.
     * Instances are stored using {@link java.lang.ref.SoftReference}s so that
     * they may be Garbage Collected as needed by JVM.
     *<p>
     * Note that this implementation may not work well on platforms where
     * {@link java.lang.ref.SoftReference}s are not well supported (like
     * Android), or on platforms where {@link java.lang.Thread}s are not
     * long-living or reused (like Project Loom).
     */
    abstract class ThreadLocalPoolBase<P extends WithPool<P>> implements RecyclerPool<P> {
        private static final long serialVersionUID = 1L;

        protected ThreadLocalPoolBase() {
        }

        // // // Actual API implementation

        @Override
        public P acquireAndLinkPooled() {
            // since this pool doesn't do anything on release it doesn't need to be registered on the BufferRecycler
            return acquirePooled();
        }

        @Override
        public abstract P acquirePooled();

        @Override
        public void releasePooled(P pooled) {
            // nothing to do, relies on ThreadLocal
        }

        // Due to use of ThreadLocal no tracking available; cannot clear
        @Override
        public boolean clear() {
            return false;
        }
    }

    /**
     * {@link RecyclerPool} implementation that does not use
     * any pool but simply creates new instances when necessary.
     */
    abstract class NonRecyclingPoolBase<P extends WithPool<P>> implements RecyclerPool<P> {
        private static final long serialVersionUID = 1L;

        // // // Actual API implementation

        @Override
        public P acquireAndLinkPooled() {
            // since this pool doesn't do anything on release it doesn't need to be registered on the BufferRecycler
            return acquirePooled();
        }

        @Override
        public abstract P acquirePooled();

        @Override
        public void releasePooled(P pooled) {
            // nothing to do, there is no underlying pool
        }

        /**
         * Although no pooling occurs, we consider clearing to succeed,
         * so returns always {@code true}.
         *
         * @return Always returns {@code true}
         */
        @Override
        public boolean clear() {
            return true;
        }
    }

    /**
     * Intermediate base class for instances that are stateful and require
     * special handling with respect to JDK serialization, to retain
     * "global" reference distinct from non-shared ones.
     */
    abstract class StatefulImplBase<P extends WithPool<P>> implements RecyclerPool<P> {
        private static final long serialVersionUID = 1L;

        /**
         * Value that indicates basic aspects of pool for JDK serialization;
         * either marker for shared/non-shared, or possibly bounded size;
         * depends on sub-class.
         */
        protected final int _serialization;

        protected StatefulImplBase(int serialization) {
            _serialization = serialization;
        }

        public abstract P createPooled();
    }

    /**
     * {@link RecyclerPool} implementation that uses
     * {@link ConcurrentLinkedDeque} for recycling instances.
     *<p>
     * Pool is unbounded: see {@link RecyclerPool} what this means.
     */
    abstract class ConcurrentDequePoolBase<P extends WithPool<P>> extends StatefulImplBase<P> {
        private static final long serialVersionUID = 1L;

        protected final transient Deque<P> pool;

        protected ConcurrentDequePoolBase(int serialization) {
            super(serialization);
            pool = new ConcurrentLinkedDeque<>();
        }

        // // // Actual API implementation

        @Override
        public P acquirePooled() {
            P pooled = pool.pollFirst();
            if (pooled == null) {
                pooled = createPooled();
            }
            return pooled;
        }

        @Override
        public void releasePooled(P pooled) {
            pool.offerLast(pooled);
        }

        @Override
        public boolean clear() {
            pool.clear();
            return true;
        }
    }

    /**
     * {@link RecyclerPool} implementation that uses
     * a lock free linked list for recycling instances.
     * Pool is unbounded: see {@link RecyclerPool} for
     * details on what this means.
     */
    abstract class LockFreePoolBase<P extends WithPool<P>> extends StatefulImplBase<P> {
        private static final long serialVersionUID = 1L;

        // Needs to be transient to avoid JDK serialization from writing it out
        private final transient AtomicReference<Node<P>> head;

        // // // Life-cycle (constructors, factory methods)

        protected LockFreePoolBase(int serialization) {
            super(serialization);
            head = new AtomicReference<>();
        }

        // // // Actual API implementation

        @Override
        public P acquirePooled() {
            // This simple lock free algorithm uses an optimistic compareAndSet strategy to
            // populate the underlying linked list in a thread-safe way. However, under very
            // heavy contention, the compareAndSet could fail multiple times, so it seems a
            // reasonable heuristic to limit the number of retries in this situation.
            for (int i = 0; i < 3; i++) {
                Node<P> currentHead = head.get();
                if (currentHead == null) {
                    return createPooled();
                }
                if (head.compareAndSet(currentHead, currentHead.next)) {
                    currentHead.next = null;
                    return currentHead.value;
                }
            }
            return createPooled();
        }

        @Override
        public void releasePooled(P pooled) {
            Node<P> newHead = new Node<>(pooled);
            for (int i = 0; i < 3; i++) {
                newHead.next = head.get();
                if (head.compareAndSet(newHead.next, newHead)) {
                    return;
                }
            }
        }

        // Yes, we can clear it
        @Override
        public boolean clear() {
            head.set(null);
            return true;
        }

        protected static class Node<P> {
            final P value;
            Node<P> next;

            Node(P value) {
                this.value = value;
            }
        }
    }

    /**
     * {@link RecyclerPool} implementation that uses
     * a bounded queue ({@link ArrayBlockingQueue} for recycling instances.
     * This is "bounded" pool since it will never hold on to more
     * pooled instances than its size configuration:
     * the default size is {@link BoundedPoolBase#DEFAULT_CAPACITY}.
     */
    abstract class BoundedPoolBase<P extends WithPool<P>> extends StatefulImplBase<P> {
        private static final long serialVersionUID = 1L;

        /**
         * Default capacity which limits number of items that are ever
         * retained for reuse.
         */
        public final static int DEFAULT_CAPACITY = 100;

        private final transient ArrayBlockingQueue<P> pool;

        private final transient int capacity;

        // // // Life-cycle (constructors, factory methods)

        protected BoundedPoolBase(int capacityAsId) {
            super(capacityAsId);
            capacity = (capacityAsId <= 0) ? DEFAULT_CAPACITY : capacityAsId;
            pool = new ArrayBlockingQueue<>(capacity);
        }

        // // // Actual API implementation

        @Override
        public P acquirePooled() {
            P pooled = pool.poll();
            if (pooled == null) {
                pooled = createPooled();
            }
            return pooled;
        }

        @Override
        public void releasePooled(P pooled) {
            pool.offer(pooled);
        }

        @Override
        public boolean clear() {
            pool.clear();
            return true;
        }

        // // // Other methods

        public int capacity() {
            return capacity;
        }
    }
}
