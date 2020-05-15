package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.converter.DefaultConverter;
import com.azure.search.documents.implementation.models.AutocompleteOptions;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@code AutocompleteOptions} and 
 * {@code com.azure.search.documents.implementation.models.internal.AutocompleteOptions} mismatch.
 */
public final class AutocompleteOptionsConverter {
  public static AutocompleteOptions convert(
      com.azure.search.documents.implementation.models.internal.AutocompleteOptions obj) {
    return DefaultConverter.convert(obj, com.azure.search.documents.implementation.models.internal.AutocompleteOptions.class);;
  }

  public static com.azure.search.documents.implementation.models.internal.AutocompleteOptions convert(
      AutocompleteOptions obj) {
    return DefaultConverter.convert(obj, AutocompleteOptions.class);;
  }
}
