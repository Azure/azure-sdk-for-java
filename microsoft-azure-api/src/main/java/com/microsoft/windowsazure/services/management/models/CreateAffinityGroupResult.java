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

package com.microsoft.windowsazure.services.management.models;

import java.util.Date;

/**
 * The base result class for all the result of service management operation.
 * 
 */
public class CreateAffinityGroupResult extends OperationResult {

    private String location;
    private String region;
    private String server;
    private Date date;

    public CreateAffinityGroupResult(int statusCode, String requestId) {
        super(statusCode, requestId);
    }

    public CreateAffinityGroupResult setLocation(String location) {
        this.location = location;
        return this;

    }

    public CreateAffinityGroupResult setRegion(String region) {
        this.region = region;
        return this;
    }

    public CreateAffinityGroupResult setServer(String server) {
        this.server = server;
        return this;
    }

    public CreateAffinityGroupResult setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getLocation() {
        return this.location;
    }

    public String getRegion() {
        return this.region;
    }

    public String getServer() {
        return this.server;
    }

    public Date getDate() {
        return this.date;
    }

}
