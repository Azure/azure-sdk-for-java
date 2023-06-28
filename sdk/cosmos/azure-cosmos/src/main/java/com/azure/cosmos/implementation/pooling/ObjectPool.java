package com.azure.cosmos.implementation.pooling;

import java.util.concurrent.atomic.AtomicReference;
interface ObjectFactory<T> {
    T create(ObjectPool<T> parent);
}

public class ObjectPool<T> {
    class Element {
        T Value;
    }

    ObjectFactory<T> factory;
    AtomicReference<T> firstItem;
    AtomicReference<T>[] items;

    public ObjectPool(ObjectFactory<T> factory) throws IllegalArgumentException {
    }

    public ObjectPool(ObjectFactory<T> factory, int size) throws IllegalArgumentException {
        if (factory == null) {
            throw new IllegalArgumentException("factory");
        }
        this.factory = factory;
        this.firstItem = new AtomicReference<>(null);
        this.items = new AtomicReference[size];
        for (int i = 0; i < size; i++) {
            items[i] = new AtomicReference<>(null);
        }
    }

    final T createInstance() {
        return factory.create(this);
    }

    /**
     * Get object from the pool.
     */
    public T get()
    {
        if (firstItem == null) {
            return allocateSlow();
        }
        var inst = firstItem.get();
        if (inst == null) {
            inst = allocateSlow();
        }
        return inst;
    }

    final T allocateSlow()
    {
        for (var i = 0; i < items.length; i++)
        {
            // Note that the initial read is optimistically not synchronized. That is intentional.
            // We will interlock only when we have a candidate. in a worst case we may miss some
            // recently returned objects. Not a big deal.
            var inst = items[i].get();
            if (inst != null)
            {
                if (inst == items[i].compareAndExchange(items[i].get(), inst))
                {
                    return inst;
                }
            }
        }
        return createInstance();
    }

    /**
     * Returns object to the pool.
     */
    public final void free(T obj)
    {
        if (firstItem.get() == null)
        {
            // Intentionally not using interlocked here.
            // In a worst case scenario two objects may be stored into same slot.
            // It is very unlikely to happen and will only mean that one of the objects will get collected.
            firstItem.set(obj);
        }
        else
        {
            freeSlow(obj);
        }
    }

    final void freeSlow(T obj)
    {
        for (var i = 0; i < items.length; i++)
        {
            if (items[i].get() == null)
            {
                // Intentionally not using interlocked here.
                // In a worst case scenario two objects may be stored into same slot.
                // It is very unlikely to happen and will only mean that one of the objects will get collected.
                items[i].set(obj);
                break;
            }
        }
    }
}
