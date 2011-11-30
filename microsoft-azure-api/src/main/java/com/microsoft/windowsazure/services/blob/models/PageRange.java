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
package com.microsoft.windowsazure.services.blob.models;

import javax.xml.bind.annotation.XmlElement;

public class PageRange {
    private long start;
    private long end;

    public PageRange() {
    }

    public PageRange(long start, long end) {
        this.start = start;
        this.end = end;
    }

    @XmlElement(name = "Start")
    public long getStart() {
        return start;
    }

    public PageRange setStart(long start) {
        this.start = start;
        return this;
    }

    @XmlElement(name = "End")
    public long getEnd() {
        return end;
    }

    public PageRange setEnd(long end) {
        this.end = end;
        return this;
    }

    public long getLength() {
        return end - start + 1;
    }

    public PageRange setLength(long value) {
        this.end = this.start + value - 1;
        return this;
    }
}
