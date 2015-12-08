package com.microsoft.azure.eventhubs.common;

public final class StringUtil {
	
	public static boolean isNullOrEmpty(String string) {
		return (string == null || string.isEmpty());
	}
	
	public static boolean isNullOrWhiteSpace(String string) {
		if (string == null)
			return true;
		
		for (int index=0; index < string.length(); index++)
			if (!Character.isWhitespace(string.charAt(index))) return false;
		
		return true;
	}
}
