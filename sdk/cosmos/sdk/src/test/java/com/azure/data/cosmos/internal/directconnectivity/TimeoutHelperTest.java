/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
