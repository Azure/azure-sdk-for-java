/*
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
package com.microsoft.azure.storage.blob;

/**
 * An {@code IProgressReceiver} is an object that can be used to report progress on network transfers. When specified on
 * transfer operations, the {@code reportProgress} method will be called periodically with the total number of bytes
 * transferred. The user may configure this method to report progress in whatever format desired.
 */
public interface IProgressReceiver {

    /**
     * The callback function invoked as progress is reported.
     *
     * @param bytesTransferred
     *      The total number of bytes transferred during this transaction.
     */
    public void reportProgress(long bytesTransferred);
}
