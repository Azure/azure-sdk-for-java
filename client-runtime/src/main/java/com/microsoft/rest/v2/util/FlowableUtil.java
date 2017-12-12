/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;

/**
 * Contains helper methods for dealing with Flowables.
 */
public class FlowableUtil {
    /**
     * Collects byte arrays emitted by a Flowable into a Single.
     * @param content A stream which emits byte arrays.
     * @return A Single which emits the concatenation of all the byte arrays given by the source Flowable.
     */
    public static Single<byte[]> collectBytes(Flowable<byte[]> content) {
        return content.collectInto(ByteStreams.newDataOutput(), new BiConsumer<ByteArrayDataOutput, byte[]>() {
            @Override
            public void accept(ByteArrayDataOutput out, byte[] chunk) throws Exception {
                out.write(chunk);
            }
        }).map(new Function<ByteArrayDataOutput, byte[]>() {
            @Override
            public byte[] apply(ByteArrayDataOutput out) throws Exception {
                return out.toByteArray();
            }
        });
    }
}
