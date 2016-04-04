package com.microsoft.azure.management.resources.models.fluent.common;

import java.util.Map;

public interface Taggable<T> {
	T withTags(Map<String, String> tags);
	T withTag(String key, String value);
	T withoutTag(String key);
}
