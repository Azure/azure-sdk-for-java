// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.internal.Constants;

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
    public String getPath() {
        return super.getString(Constants.Properties.PATH);
    }

    /**
     * Sets path.
     *
     * @param path the path.
     * @return the SpatialSpec.
     */
    public SpatialSpec setPath(String path) {
        super.set(Constants.Properties.PATH, path);
        return this;
    }

    /**
     * Gets the collection of spatial types.
     *
     * @return the collection of spatial types.
     */
    public List<SpatialType> getSpatialTypes() {
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
    public SpatialSpec setSpatialTypes(List<SpatialType> spatialTypes) {
        this.spatialTypes = spatialTypes;
        Collection<String> spatialTypeNames = new ArrayList<String>();
        for (SpatialType spatialType : this.spatialTypes) {
            spatialTypeNames.add(spatialType.toString());
        }
        super.set(Constants.Properties.TYPES, spatialTypeNames);
        return this;
    }
}
