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