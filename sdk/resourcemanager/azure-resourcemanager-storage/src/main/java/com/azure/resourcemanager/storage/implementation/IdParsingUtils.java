// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import java.util.Arrays;
import java.util.Iterator;

class IdParsingUtils {

    /**
     * Parses out the name of resource groups, storage accounts etc from the id string.
     *
     * @param id the id string.
     * @param name the name of the value you would like to parse out from the id string
     * @return the value you would like parsed out of the id string.
     */
    public static String getValueFromIdByName(String id, String name) {
        if (id == null) {
            return null;
        }
        Iterable<String> iterable = Arrays.asList(id.split("/"));
        Iterator<String> itr = iterable.iterator();
        while (itr.hasNext()) {
            String part = itr.next();
            if (part != null && !part.trim().isEmpty()) {
                if (part.equalsIgnoreCase(name)) {
                    if (itr.hasNext()) {
                        return itr.next();
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns a value from an id string based on the given position.
     *
     * @param id the id string.
     * @param pos the position of the value you would like to retrieve.
     * @return the value from the id string based on the given position.
     */
    public static String getValueFromIdByPosition(String id, int pos) {
        if (id == null) {
            return null;
        }
        Iterable<String> iterable = Arrays.asList(id.split("/"));
        Iterator<String> itr = iterable.iterator();
        int index = 0;
        while (itr.hasNext()) {
            String part = itr.next();
            if (part != null && !part.trim().isEmpty()) {
                if (index == pos) {
                    if (itr.hasNext()) {
                        return itr.next();
                    } else {
                        return null;
                    }
                }
            }
            index++;
        }
        return null;
    }
}
