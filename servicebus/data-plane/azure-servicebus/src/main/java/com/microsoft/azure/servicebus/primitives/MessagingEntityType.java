package com.microsoft.azure.servicebus.primitives;

// For internal use only. 
// Integers are to achieve parity with .net enums which are integer by default
public enum MessagingEntityType {
	QUEUE(0),
	TOPIC(1),
	SUBSCRIPTION(2),
	FILTER(3);
	
	private int enumValue;
	MessagingEntityType(int enumValue)
	{
		this.enumValue = enumValue;
	}
	
	public int getIntValue()
	{
		return this.enumValue;
	}
}