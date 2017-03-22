/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.documentdb.rx;

import com.microsoft.azure.documentdb.Database;

public class Utils {
    private static final String DATABASES_PATH_SEGMENT = "dbs";
    private static final String COLLECTIONS_PATH_SEGMENT = "colls";


    public static String getDatabaseLink(Database database, boolean isNameBased) {
        if (isNameBased) {
            return getDatabaseNameLink(database.getId());
        } else {
            return database.getSelfLink();
        }
    }

    public static String getDatabaseNameLink(String databaseId) {
        return DATABASES_PATH_SEGMENT + "/" + databaseId;
    }

    public static String getCollectionNameLink(String databaseId, String collectionId) {

        if (collectionId.equals("/")) {
            return databaseId + "/" + COLLECTIONS_PATH_SEGMENT + "/" + collectionId;

        } else {
            return DATABASES_PATH_SEGMENT + "/" + databaseId + "/" + COLLECTIONS_PATH_SEGMENT + "/" + collectionId;
        }
    }

    private Utils() {}
}
