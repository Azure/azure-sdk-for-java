// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * {@code Context} offers a means of passing arbitrary data (key-value pairs) to pipeline policies.
 * Most applications do not need to pass arbitrary data to the pipeline and can pass {@code Context.NONE} or
 * {@code null}. Each context object is immutable. The {@code addData(Object, Object)} method creates a new
 * {@code Context} object that refers to its parent, forming a linked list.
 */
public class Context implements AutoCloseable {
    // All fields must be immutable.
    //
    /**
     * Signifies that no data need be passed to the pipeline.
     */
    public static final Context NONE = new Context(null, null, null);

    private final Context parent;
    private final Object key;
    private final Object value;

    private ThreadLocal<Context> threadLocal;
    private static boolean reflectionInitiated = false;
    private static Field threadLocalsField;
    private static Field tableField;

    /**
     * Constructs a new {@link Context} object.
     *
     * @param key the key
     * @param value the value
     */
    public Context(Object key, Object value) {
        this(null, key, value);
    }

    Context(Context parent, Object key, Object value) {
        this(parent, key, value, new ThreadLocal<>());
    }

    Context(Context parent, Object key, Object value, ThreadLocal<Context> threadLocal) {
        this.parent = parent;
        this.key = key;
        this.value = value;
        this.threadLocal = threadLocal;

        if (threadLocal != null && key != null && value != null) {
            threadLocal.set(this);
        }
    }

    /**
     * Adds a new immutable {@link Context} object with the specified key-value pair to
     * the existing {@link Context} chain.
     *
     * @param key the key
     * @param value the value
     * @return the new {@link Context} object containing the specified pair added to the set of pairs
     */
    public Context addData(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        // when we add data, the new ThreadLocalContext becomes the root element,
        // and so we remove this context out of thread local
        if (threadLocal != null) {
            threadLocal.remove();
        }

        return new Context(this, key, value, threadLocal);
    }

    /**
     * Scans the linked-list of {@link Context} objects looking for one with the specified key.
     * Note that the first key found, i.e. the most recently added, will be returned.
     *
     * @param key the key to search for
     * @return the value of the key if it exists
     */
    public Optional<Object> getData(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        for (Context c = this; c != null; c = c.parent) {
            if (key.equals(c.key)) {
                return Optional.of(c.value);
            }
        }
        return Optional.empty();
    }

    @Override
    public void close() {
        // Because of how we use context, we can not simply call threadLocal.remove() here, as
        // the context created at the start of a try-with-resources will not necessarily be the one
        // that needs to have close() called on it, and from that context we have no means on finding
        // the right context, except for diving into the thread reflectively.
        Context ctx = getThreadLocalContext();
        if (ctx != null && ctx.threadLocal != null) {
            ctx.threadLocal.remove();
        }
    }

    // FIXME temporary toString for debugging - delete before merging
    @Override
    public String toString() {
        if (key == null && value == null) {
            return "[Context NONE, parent=" + parent + "]";
        }
        return "[Context key='" + key + "', value='" + value + "', parent=" + parent + "]";
    }

    public static Context getThreadLocalContext() {
        Context result = Context.NONE;

        try {
            if (!reflectionInitiated) {
                threadLocalsField = Thread.class.getDeclaredField("threadLocals");
                threadLocalsField.setAccessible(true);

                Class threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
                tableField = threadLocalMapClass.getDeclaredField("table");
                tableField.setAccessible(true);

                reflectionInitiated = true;
            }

            Object table = tableField.get(threadLocalsField.get(Thread.currentThread()));

            for (int i = 0, max = Array.getLength(table); i < max; i++) {
                Object entry = Array.get(table, i);
                if (entry != null) {
                    Field valueField = entry.getClass().getDeclaredField("value");
                    valueField.setAccessible(true);
                    Object value = valueField.get(entry);

                    if (value instanceof Context) {
                        result = (Context) value;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
