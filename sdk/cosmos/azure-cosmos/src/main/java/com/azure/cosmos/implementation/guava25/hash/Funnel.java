package com.azure.cosmos.implementation.guava25.hash;

import com.azure.cosmos.implementation.guava25.annotations.Beta;

import java.io.Serializable;

/**
 * An object which can send data from an object of type {@code T} into a {@code PrimitiveSink}.
 * Implementations for common types can be found in {@link Funnels}.
 *
 * <p>Note that serialization of {@linkplain BloomFilter bloom filters} requires the proper
 * serialization of funnels. When possible, it is recommended that funnels be implemented as a
 * single-element enum to maintain serialization guarantees. See Effective Java (2nd Edition), Item
 * 3: "Enforce the singleton property with a private constructor or an enum type". For example:
 *
 * <pre>{@code
 * public enum PersonFunnel implements Funnel<Person> {
 *   INSTANCE;
 *   public void funnel(Person person, PrimitiveSink into) {
 *     into.putUnencodedChars(person.getFirstName())
 *         .putUnencodedChars(person.getLastName())
 *         .putInt(person.getAge());
 *   }
 * }
 * }</pre>
 *
 * @author Dimitris Andreou
 * @since 11.0
 */
@Beta
public interface Funnel<T> extends Serializable {

    /**
     * Sends a stream of data from the {@code from} object into the sink {@code into}. There is no
     * requirement that this data be complete enough to fully reconstitute the object later.
     *
     * @since 12.0 (in Guava 11.0, {@code PrimitiveSink} was named {@code Sink})
     */
    void funnel(T from, PrimitiveSink into);
}
