// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;
import java.util.Arrays;

public class TestHelper {
    static int editDistance(String s1, String s2) {
        int n1 = s1.length();
        int n2 = s2.length();
        return distance(s1, s2, n1, n2);
    }

    static int distance(String s1, String s2, int n1, int n2) {
        if (n1 == 0) {
            return n2;
        }

        if (n2 == 0) {
            return n1;
        }

        if (s1.charAt(n1 - 1) == s2.charAt(n2 - 1)) {
            return distance(s1, s2, n1 - 1, n2 - 1);
        }

        int[] nums = new int[] { distance(s1, s2, n1, n2 - 1), distance(s1, s2, n1 - 1, n2), distance(s1, s2, n1 - 1, n2 - 1) };
        return 1 + Arrays.stream(nums).min().getAsInt();
    }		
}
