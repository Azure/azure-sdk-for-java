/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.core.storage;

/**
 * Represents the result of a retry policy evaluation.
 */
public final class RetryResult
{
    /**
     * Represents the backoff interval in milliseconds.
     */
    private int backOffIntervalInMs;

    /**
     * @return the backOffIntervalInMs
     */
    public int getBackOffIntervalInMs()
    {
        return this.backOffIntervalInMs;
    }

    /**
     * Indicates whether to retry the operation. Set to <code>true</code> to
     * retry; otherwise, <code>false</code>.
     */
    private boolean shouldRetry;

    /**
     * Creates an instance of the <code>RetryResult</code> class.
     * 
     * @param backOff
     *            The backoff interval, in milliseconds, to wait before
     *            attempting the retry.
     * @param shouldRetry
     *            <code>true</code> if the operation should be retried,
     *            otherwise, <code>false</code>.
     * 
     */
    public RetryResult(final int backOff, final boolean shouldRetry)
    {
        this.backOffIntervalInMs = backOff;
        this.setShouldRetry(shouldRetry);
    }

    /**
     * Sleeps the amount of time specified by the backoff interval, if the retry
     * policy indicates the operation should be retried.
     */
    public void doSleep()
    {
        if (this.isShouldRetry())
        {
            try
            {
                Thread.sleep(this.backOffIntervalInMs);
            } catch (final InterruptedException e)
            {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * @return the shouldRetry
     */
    public boolean isShouldRetry()
    {
        return this.shouldRetry;
    }

    /**
     * @param shouldRetry
     *            the shouldRetry to set
     */
    public void setShouldRetry(final boolean shouldRetry)
    {
        this.shouldRetry = shouldRetry;
    }
}
