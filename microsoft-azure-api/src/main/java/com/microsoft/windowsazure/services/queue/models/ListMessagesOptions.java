/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.queue.models;

public class ListMessagesOptions extends QueueServiceOptions {
    private Integer numberOfMessages;
    private Integer visibilityTimeoutInSeconds;

    @Override
    public ListMessagesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public Integer getNumberOfMessages() {
        return numberOfMessages;
    }

    public ListMessagesOptions setNumberOfMessages(Integer numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
        return this;
    }

    public Integer getVisibilityTimeoutInSeconds() {
        return visibilityTimeoutInSeconds;
    }

    public ListMessagesOptions setVisibilityTimeoutInSeconds(Integer visibilityTimeoutInSeconds) {
        this.visibilityTimeoutInSeconds = visibilityTimeoutInSeconds;
        return this;
    }
}
