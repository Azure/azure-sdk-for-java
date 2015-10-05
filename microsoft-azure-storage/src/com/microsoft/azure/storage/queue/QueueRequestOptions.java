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

package com.microsoft.azure.storage.queue;

import com.microsoft.azure.storage.RequestOptions;
import com.microsoft.azure.storage.ServiceClient;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents a set of options that may be specified on a queue request.
 */
public final class QueueRequestOptions extends RequestOptions {

    /**
     * The encryption policy to use for the request.
     */
    private QueueEncryptionPolicy encryptionPolicy;

    /**
     * Initializes a new instance of the QueueRequestOptions class.
     */
    public QueueRequestOptions() {
        // no op
    }

    /**
     * Initializes a new instance of the QueueRequestOptions class as a copy of
     * another QueueRequestOptions instance.
     * 
     * @param other
     *            The {@link QueueRequestOptions} object to copy the values
     *            from.
     */
    public QueueRequestOptions(final QueueRequestOptions other) {
        super(other);
        if (other != null) {
            this.setEncryptionPolicy(other.getEncryptionPolicy());
        }
    }

    /**
     * Populates the default timeout and retry policy from client if they are not set.
     * 
     * @param options
     *            The input options to copy from when applying defaults
     * @param client
     *            The {@link CloudQueueClient} service client to populate the
     *            default values from.
     */
    protected static final QueueRequestOptions populateAndApplyDefaults(QueueRequestOptions options, final CloudQueueClient client) {
        QueueRequestOptions modifiedOptions = new QueueRequestOptions(options);
        QueueRequestOptions.populateRequestOptions(modifiedOptions, client.getDefaultRequestOptions());
        QueueRequestOptions.applyDefaults(modifiedOptions);
        return modifiedOptions;
    }

    /**
     * Applies defaults to the options passed in.
     * 
     * @param modifiedOptions
     *          The options to apply defaults to.
     */
    protected static void applyDefaults(QueueRequestOptions modifiedOptions) {
        Utility.assertNotNull("modifiedOptions", modifiedOptions);
        RequestOptions.applyBaseDefaultsInternal(modifiedOptions);
    }

    /**
     * Populates any null fields in the first requestOptions object with values from the second requestOptions object.
     * 
     * @param modifiedOptions
     *            A {@link QueueRequestOptions} object from which to copy options.
     * @param clientOptions
     *            A {@link QueueRequestOptions} object where options will be copied.
     * 
     * @return A {@link RequestOptions} object.
     */
    private static void populateRequestOptions(QueueRequestOptions modifiedOptions,
            final QueueRequestOptions clientOptions) {
        RequestOptions.populateRequestOptions(modifiedOptions, clientOptions, true);
        if (modifiedOptions.getEncryptionPolicy() == null) {
            modifiedOptions.setEncryptionPolicy(clientOptions.getEncryptionPolicy());
        }
    }

    /**
     * Gets the encryption policy to use for this request. For more information about the encryption policy defaults,
     * see {@link #setEncryptionPolicy(QueueEncryptionPolicy)}.
     * 
     * @return An {@link QueueEncryptionPolicy} object that represents the current encryption policy.
     */
    public QueueEncryptionPolicy getEncryptionPolicy() {
        return this.encryptionPolicy;
    }

    /**
     * Sets the QueueEncryptionPolicy object to use for this request.
     * <p>
     * The default QueueEncryptionPolicy is set in the client and is by default null, indicating no encryption. You can
     * change the QueueEncryptionPolicy on this request by setting this property. You can also change the value on the
     * {@link ServiceClient#getDefaultRequestOptions()} object so that all subsequent requests made via the service
     * client will use that QueueEncryptionPolicy.
     * 
     * @param encryptionPolicy
     *            the QueueEncryptionPolicy object to use when making service requests.
     */
    public void setEncryptionPolicy(QueueEncryptionPolicy encryptionPolicy) {
        this.encryptionPolicy = encryptionPolicy;
    }
    
    /**
     * Assert that if strict mode is on, an encryption policy is specified.
     */
    protected void assertPolicyIfRequired()
    {
        if (this.requireEncryption() != null && this.requireEncryption() && this.getEncryptionPolicy() == null)
        {
            throw new IllegalArgumentException(SR.ENCRYPTION_POLICY_MISSING_IN_STRICT_MODE);
        }
    }
}
