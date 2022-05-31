package com.azure.maps.render.implementation.helpers;

import java.util.Locale;

import com.azure.maps.render.implementation.models.IncludeText;

public class Utility {
    public static IncludeText toIncludeTextPrivate(boolean includeText) {
        return IncludeText.fromString(String.valueOf(includeText));
    }
}