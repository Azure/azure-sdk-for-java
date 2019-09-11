// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ModelComparer {
    public static <T> boolean collectionEquals(Collection<T> seq1, Collection<T> seq2) {
        if (seq1 == null) {
            return seq2 == null || seq2.isEmpty();
        } else {
            if (seq2 == null) {
                seq2 = new ArrayList<>();
            }
            return Objects.equals(seq1, seq2);
        }
    }
}
