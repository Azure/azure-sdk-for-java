// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

/**
 * Represents class with four different generic objects.
 */
public class Quadruple<A, B, C, D> {

    private final A val0;
    private final B val1;
    private final C val2;
    private final D val3;

    public static <A, B, C, D> Quadruple<A, B, C, D> with(final A value0, final B value1, final C value2,
            final D value3) {
        return new Quadruple<A, B, C, D>(value0, value1, value2, value3);
    }

    public Quadruple(final A value0, final B value1, final C value2, final D value3) {
        this.val0 = value0;
        this.val1 = value1;
        this.val2 = value2;
        this.val3 = value3;
    }

    public A getValue0() {
        return this.val0;
    }

    public B getValue1() {
        return this.val1;
    }

    public C getValue2() {
        return this.val2;
    }

    public D getValue3() {
        return this.val3;
    }

}
