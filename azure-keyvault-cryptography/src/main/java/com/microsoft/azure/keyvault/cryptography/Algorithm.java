/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.cryptography;

public abstract class Algorithm {

    private final String _name;

    protected Algorithm(String name) {
        if (Strings.isNullOrWhiteSpace(name)) {
            throw new IllegalArgumentException("name");
        }

        _name = name;
    }

    public String getName() {
        return _name;
    }
    
    /*
     * Takes the first count bytes from the source and
     * returns a new array containing those bytes.
     * 
     * @param count The number of bytes to take.
     * @param source The source of the bytes.
     * @return count bytes from the source as a new array.
     */
    public static byte[] Take(int count, byte[] source)
    {
    	if ( source == null ) {
    		throw new IllegalArgumentException("source");
    	}
    	
    	if ( count <= 0 || count > source.length ) {
    		throw new IllegalArgumentException("count");
    	}
    	
    	byte[] target = new byte[count];
    	
    	System.arraycopy(source, 0, target, 0, count);
    	
    	return target;
    }
}
