/**
 * Copyright 2011 Microsoft Corporation
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

package com.microsoft.windowsazure.services.table.client;

import java.util.HashMap;

/**
 * Reserved for internal use. A class that represents a given MIME Part.
 */
class MimePart {
    protected int httpStatusCode = -1;
    protected String httpStatusMessage;
    protected HashMap<String, String> headers = new HashMap<String, String>();
    protected String payload;
}
