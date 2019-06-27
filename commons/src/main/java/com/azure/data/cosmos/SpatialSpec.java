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

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpatialSpec extends JsonSerializable {

    private List<SpatialType> spatialTypes;

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
    SpatialSpec(String jsonString) {
        super(jsonString);
    }


    /**
     * Gets path.
     *
     * @return the path.
     */
    public String path() {
        return super.getString(Constants.Properties.PATH);
    }

    /**
     * Sets path.
     *
     * @param path the path.
     * @return the SpatialSpec.
     */
    public SpatialSpec path(String path) {
        super.set(Constants.Properties.PATH, path);
        return this;
    }

    /**
     * Gets the collection of spatial types.
     *
     * @return the collection of spatial types.
     */
    public List<SpatialType> spatialTypes() {
        if (this.spatialTypes == null) {
            this.spatialTypes = super.getList(Constants.Properties.TYPES, SpatialType.class, true);

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
     * @return the SpatialSpec.
     */
    public SpatialSpec spatialTypes(List<SpatialType> spatialTypes) {
        this.spatialTypes = spatialTypes;
        Collection<String> spatialTypeNames = new ArrayList<String>();
        for (SpatialType spatialType : this.spatialTypes) {
            spatialTypeNames.add(spatialType.toString());
        }
        super.set(Constants.Properties.TYPES, spatialTypeNames);
        return this;
    }
}
