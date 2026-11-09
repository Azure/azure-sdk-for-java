/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.apachecommons.lang;

public class NotImplementedException extends UnsupportedOperationException {
    private static final long serialVersionUID = -6280014327960506025L;
    private final String code;

    /**
     * Constructs a NotImplementedException.
     *
     * @param message description of the exception
     */
    public NotImplementedException(final String message) {
        this(message, (String) null);
    }

    /**
     * Constructs a NotImplementedException.
     *
     * @param message description of the exception
     * @param code code indicating a resource for more information regarding the lack of implementation
     */
    public NotImplementedException(final String message, final String code) {
        super(message);
        this.code = code;
    }

    /**
     * Obtain the not implemented code. This is an unformatted piece of text intended to point to
     * further information regarding the lack of implementation. It might, for example, be an issue
     * tracker ID or a URL.
     *
     * @return a code indicating a resource for more information regarding the lack of implementation
     */
    public String getCode() {
        return this.code;
    }
}

