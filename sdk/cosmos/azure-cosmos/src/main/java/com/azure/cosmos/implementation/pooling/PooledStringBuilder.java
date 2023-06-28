package com.azure.cosmos.implementation.pooling;

public class PooledStringBuilder {
    final int MAX_SIZE = 1024;
    StringBuilder builder = new StringBuilder();

    ObjectPool<PooledStringBuilder> pool;

    static ObjectPool<PooledStringBuilder> s_poolInstance = createPool();

    PooledStringBuilder(ObjectPool<PooledStringBuilder> pool) {
        this.pool = pool;
    }

    public static PooledStringBuilder createInstance() {
        return s_poolInstance.get();
    }
    public static ObjectPool<PooledStringBuilder> createPool() {
        return createPool(32);
    }

    public static ObjectPool<PooledStringBuilder> createPool(int size) {
        return new ObjectPool<PooledStringBuilder>((pool) -> new PooledStringBuilder(pool), size);
    }

    public int length() { return builder.length(); }

    @Override
    public String toString() {
        return builder.toString();
    }

    public String toStringAndFree() {
        String result = builder.toString();
        free();
        return result;
    }

    public void free() {
        if (builder.capacity() < MAX_SIZE) {
            builder.setLength(0);
            pool.free(this);
        }
    }

    // region Delegation

    public PooledStringBuilder append(String str) {
        builder.append(str);
        return this;
    }

    public PooledStringBuilder append(StringBuffer stringBuffer) {
        builder.append(stringBuffer);
        return this;
    }

    public PooledStringBuilder append(char[] data) {
        builder.append(data);
        return this;
    }

    public PooledStringBuilder append(char[] data, int offset, int count) {
        builder.append(data, offset, count);
        return this;
    }
    public PooledStringBuilder append(boolean bool) {
        builder.append(bool);
        return this;
    }
    public PooledStringBuilder append(char ch) {
        builder.append(ch);
        return this;
    }

    public PooledStringBuilder append(CharSequence seq) {
        builder.append(seq);
        return this;
    }
    public PooledStringBuilder append(CharSequence seq, int start, int end) {
        builder.append(seq, start, end);
        return this;
    }
    public PooledStringBuilder append(int i) {
        builder.append(i);
        return this;
    }
    public PooledStringBuilder append(long lng) {
        builder.append(lng);
        return this;
    }

    public PooledStringBuilder append(float f) {
        builder.append(f);
        return this;
    }

    public PooledStringBuilder append(Object o) {
        builder.append(o);
        return this;
    }

    public int capacity() { return builder.capacity(); }
    public char charAt(int index) { return builder.charAt(index); }

    public PooledStringBuilder delete(int start, int end) {
        builder.delete(start, end);
        return this;
    }

    public PooledStringBuilder deleteCharAt(int index) {
        builder.deleteCharAt(index);
        return this;
    }

    public void ensureCapacity(int minimumCapacity) { builder.ensureCapacity(minimumCapacity); }

    public int indexOf(String str) { return builder.indexOf(str); }
    public int indexOf(String str, int fromIndex) { return builder.indexOf(str, fromIndex); }

    public PooledStringBuilder insert(int offset, String str) {
        builder.insert(offset, str);
        return this;
    }

    public PooledStringBuilder insert(int offset, char[] str) {
        builder.insert(offset, str);
        return this;
    }
    public PooledStringBuilder insert(int index, char[] str, int offset, int len) {
        builder.insert(index, str, offset, len);
        return this;
    }
    public PooledStringBuilder insert(int offset, CharSequence seq) {
        builder.insert(offset, seq);
        return this;
    }

    public PooledStringBuilder insert(int offset, CharSequence seq, int start, int end) {
        builder.insert(offset, seq, start, end);
        return this;
    }

    public PooledStringBuilder insert(int offset, int i) {
        builder.insert(offset, i);
        return this;
    }
    public PooledStringBuilder insert(int offset, long l) {
        builder.insert(offset, l);
        return this;
    }

    public PooledStringBuilder insert(int offset, float f) {
        builder.insert(offset, f);
        return this;
    }

    public PooledStringBuilder insert(int offset, double d) {
        builder.insert(offset, d);
        return this;
    }

    public PooledStringBuilder insert(int offset, Object obj) {
        builder.insert(offset, obj);
        return this;
    }

    public int lastIndexOf(String str) { return builder.lastIndexOf(str); }
    public int lastIndexOf(String str, int fromIndex) { return builder.lastIndexOf(str, fromIndex); }

    public PooledStringBuilder reverse() {
        builder.reverse();
        return this;
    }

    public void setCharAt(int index, char ch) { builder.setCharAt(index, ch); }
    public void setLength(int newLength) { builder.setLength(newLength); }

    public CharSequence subSequence(int start, int end) { return builder.subSequence(start, end); }
    public String substring(int start) { return builder.substring(start); }
    public String substring(int start, int end) { return builder.substring(start, end); }

    public void trimToSize() { builder.trimToSize(); }
}
