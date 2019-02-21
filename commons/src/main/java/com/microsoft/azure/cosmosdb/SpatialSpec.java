/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
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

package com.microsoft.azure.cosmosdb;

import java.util.ArrayList;
import java.util.Collection;

import com.microsoft.azure.cosmosdb.internal.Constants;

public class SpatialSpec extends JsonSerializable {

    private Collection<SpatialType> spatialTypes;

    /**
     * Constructor.
     */
    public SpatialSpec() {
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the included path.
     */
    public SpatialSpec(String jsonString) {
        super(jsonString);
    }


    /**
     * Gets path.
     *
     * @return the path.
     */
    public String getPath() {
        return super.getString(Constants.Properties.PATH);
    }

    /**
     * Sets path.
     *
     * @param path the path.
     */
    public void setPath(String path) {
        super.set(Constants.Properties.PATH, path);
    }

    /**
     * Gets the collection of spatial types.
     *
     * @return the collection of spatial types.
     */
    public Collection<SpatialType> getSpatialTypes() {
        if (this.spatialTypes == null) {
            this.spatialTypes = super.getCollection(Constants.Properties.TYPES, SpatialType.class);

            if (this.spatialTypes == null) {
                this.spatialTypes = new ArrayList<SpatialType>();
            }
        }

        return this.spatialTypes;
    }

    /**
     * Sets the collection of spatial types.
     *
     * @param spatialTypes the collection of spatial types.
     */
    public void setSpatialTypes(Collection<SpatialType> spatialTypes) {
        this.spatialTypes = spatialTypes;
        Collection<String> spatialTypeNames = new ArrayList<String>();
        for (SpatialType spatialType : this.spatialTypes) {
            spatialTypeNames.add(spatialType.name());
        }
        super.set(Constants.Properties.TYPES, spatialTypeNames);
    }
}
