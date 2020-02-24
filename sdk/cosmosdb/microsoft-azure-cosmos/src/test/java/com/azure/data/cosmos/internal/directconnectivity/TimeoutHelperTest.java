// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.directconnectivity.TimeoutHelper;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeoutHelperTest {

    @Test(groups = "unit")
    public void isElapsed() throws InterruptedException {
        Duration duration1 = Duration.ofMillis(100);
        TimeoutHelper timeoutHelper1 = new TimeoutHelper(duration1);
        assertThat(timeoutHelper1.isElapsed()).isFalse();

        Duration duration2 = Duration.ofMillis(100);
        TimeoutHelper timeoutHelper2 = new TimeoutHelper(duration2);
        Thread.sleep(100);
        assertThat(timeoutHelper2.isElapsed()).isTrue();
    }

    @Test(groups = "unit")
    public void getRemainingTime() throws InterruptedException {
        for (int i = 1; i <= 5; i++) {
            Duration duration = Duration.ofMillis(100);
            TimeoutHelper timeoutHelper = new TimeoutHelper(duration);
            Thread.sleep((10*i));
            Duration remainingTime1 = timeoutHelper.getRemainingTime();
            //Giving 5 ms extra buffer in case thread sleep complete early
            assertThat(remainingTime1.toMillis()).isLessThanOrEqualTo(100-10*i+5);
        }
    }
}
