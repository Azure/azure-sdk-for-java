package com.azure.core.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class ThreadLocalContext extends Context implements AutoCloseable {

    private static final ThreadLocalContext NONE = new ThreadLocalContext(null, null, null);

    private static boolean reflectionInitiated = false;
    private static Field threadLocalsField;
    private static Field tableField;

    private final ThreadLocal<Context> threadLocal;

    /**
     * Constructs a new {@link Context} object.
     *
     * @param key the key
     * @param value the value
     */
    public ThreadLocalContext(Object key, Object value) {
        this(null, key, value);
    }

    ThreadLocalContext(Context parent, Object key, Object value) {
        super(parent, key, value);

        threadLocal = new ThreadLocal<>();
        if (key != null && value != null) {
            threadLocal.set(this);
        }
    }

    @Override
    public Context addData(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        // when we add data, the new ThreadLocalContext becomes the root element,
        // and so we remove this context out of thread local
        threadLocal.remove();

        return new ThreadLocalContext(this, key, value);
    }

    @Override
    public void close() {
        // Because of how we use context, we can not simply call threadLocal.remove() here, as
        // the context created at the start of a try-with-resources will not necessarily be the one
        // that needs to have close() called on it, and from that context we have no means on finding
        // the right context, except for diving into the thread reflectively.
        getThreadLocalContext().threadLocal.remove();
    }

    // FIXME temporary toString for debugging - delete before merging
    @Override
    public String toString() {
        if (key == null && value == null) {
            return "[ThreadLocalContext NONE, parent=" + parent + "]";
        }
        return "[ThreadLocalContext key='" + key + "', value='" + value + "', parent=" + parent + "]";
    }

    public static ThreadLocalContext getThreadLocalContext() {
        ThreadLocalContext result = ThreadLocalContext.NONE;

        try {
            if (!reflectionInitiated) {
                threadLocalsField = Thread.class.getDeclaredField("threadLocals");
                threadLocalsField.setAccessible(true);

                Class threadLocalMapClss = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
                tableField = threadLocalMapClss.getDeclaredField("table");
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

                    if (value instanceof ThreadLocalContext) {
                        result = (ThreadLocalContext) value;
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
