package com.microsoft.windowsazure.services.serviceBus.implementation;

public class EntryModel<T> {
    Entry entry;
    T model;

    public EntryModel(Entry entry, T model) {
        this.entry = entry;
        this.model = model;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
    }
}
