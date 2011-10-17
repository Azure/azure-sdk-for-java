package com.microsoft.azure.services.serviceBus.contract;

import com.sun.syndication.feed.atom.Entry;

public class EntryModel<T> {
	Entry entry;
	T model;
	
	public EntryModel(){
		
	}
	
	public EntryModel(Entry entry, T model) {
		setEntry(entry);
		setModel(model);
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

