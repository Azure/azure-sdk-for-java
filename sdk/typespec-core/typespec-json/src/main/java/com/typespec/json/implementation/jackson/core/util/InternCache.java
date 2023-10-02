// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton class that adds a simple first-level cache in front of
 * regular String.intern() functionality. This is done as a minor
 * performance optimization, to avoid calling native intern() method
 * in cases where same String is being interned multiple times.
 *<p>
 * Note: that this class extends {@link java.util.LinkedHashMap} is an implementation
 * detail -- no code should ever directly call Map methods.
 */
public final class InternCache
    extends ConcurrentHashMap<String,String> // since 2.3
{
    private static final long serialVersionUID = 1L;

    /**
     * Size to use is somewhat arbitrary, so let's choose something that's
     * neither too small (low hit ratio) nor too large (waste of memory).
     *<p>
     * One consideration is possible attack via colliding {@link String#hashCode};
     * because of this, limit to reasonably low setting.
     */
    private final static int MAX_ENTRIES = 180;

    public final static InternCache instance = new InternCache();

    /**
     * As minor optimization let's try to avoid "flush storms",
     * cases where multiple threads might try to concurrently
     * flush the map.
     */
    private final Object lock = new Object();
    
    private InternCache() { super(MAX_ENTRIES, 0.8f, 4); }

    public String intern(String input) {
        String result = get(input);
        if (result != null) { return result; }

        /* 18-Sep-2013, tatu: We used to use LinkedHashMap, which has simple LRU
         *   method. No such functionality exists with CHM; and let's use simplest
         *   possible limitation: just clear all contents. This because otherwise
         *   we are simply likely to keep on clearing same, commonly used entries.
         */
        if (size() >= MAX_ENTRIES) {
            /* Not incorrect wrt well-known double-locking anti-pattern because underlying
             * storage gives close enough answer to real one here; and we are
             * more concerned with flooding than starvation.
             */
            synchronized (lock) {
                if (size() >= MAX_ENTRIES) {
                    clear();
                }
            }
        }
        result = input.intern();
        put(result, result);
        return result;
    }
}

