package com.azure;

import com.azure.implementation.TheClass;
import com.azure.implementation.TheOtherClass;

public class AClass {
    static {
        TheClass theClass = new TheClass() {
            @Override
            public TheOtherClass getTheOtherClass() {
                return null;
            }
        };
    }
}