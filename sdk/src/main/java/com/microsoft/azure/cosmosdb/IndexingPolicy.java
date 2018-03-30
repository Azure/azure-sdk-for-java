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
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Represents the indexing policy configuration for a collection in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public final class IndexingPolicy extends JsonSerializable {

    private static final String DEFAULT_PATH = "/*";

    private Collection<IncludedPath> includedPaths;
    private Collection<ExcludedPath> excludedPaths;

    /**
     * Constructor.
     */
    public IndexingPolicy() {
        this.setAutomatic(true);
        this.setIndexingMode(IndexingMode.Consistent);
    }

    /**
     * Initializes a new instance of the IndexingPolicy class with the specified set of indexes as
     * default index specifications for the root path.
     * <p>
     * The following example shows how to override the default indexingPolicy for root path:
     * <pre>
     * {@code
     * HashIndex hashIndexOverride = Index.Hash(DataType.String, 5);
     * RangeIndex rangeIndexOverride = Index.Range(DataType.Number, 2);
     * SpatialIndex spatialIndexOverride = Index.Spatial(DataType.Point);
     *
     * IndexingPolicy indexingPolicy = new IndexingPolicy(hashIndexOverride, rangeIndexOverride, spatialIndexOverride);
     * }
     * </pre>
     * <p>
     * If you would like to just override the indexingPolicy for Numbers you can specify just that:
     * <pre>
     * {@code
     * RangeIndex rangeIndexOverride = Index.Range(DataType.Number, 2);
     *
     * IndexingPolicy indexingPolicy = new IndexingPolicy(rangeIndexOverride);
     * }
     * </pre>
     *
     * @param defaultIndexOverrides comma separated set of indexes that serve as default index specifications for the root path.
     */
    public IndexingPolicy(Index[] defaultIndexOverrides) {
        this();

        if (defaultIndexOverrides == null) {
            throw new IllegalArgumentException("defaultIndexOverrides is null.");
        }

        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath(IndexingPolicy.DEFAULT_PATH);
        includedPath.setIndexes(new ArrayList<Index>(Arrays.asList(defaultIndexOverrides)));
        this.getIncludedPaths().add(includedPath);
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the indexing policy.
     */
    public IndexingPolicy(String jsonString) {
        super(jsonString);
    }

    /**
     * Constructor.
     *
     * @param jsonObject the json object that represents the indexing policy.
     */
    public IndexingPolicy(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Gets whether automatic indexing is enabled for a collection.
     * <p>
     * In automatic indexing, documents can be explicitly excluded from indexing using RequestOptions. In manual
     * indexing, documents can be explicitly included.
     *
     * @return the automatic
     */
    public Boolean getAutomatic() {
        return super.getBoolean(Constants.Properties.AUTOMATIC);
    }

    /**
     * Sets whether automatic indexing is enabled for a collection.
     * <p>
     * In automatic indexing, documents can be explicitly excluded from indexing using RequestOptions. In manual
     * indexing, documents can be explicitly included.
     *
     * @param automatic the automatic
     */
    public void setAutomatic(boolean automatic) {
        super.set(Constants.Properties.AUTOMATIC, automatic);
    }

    /**
     * Gets the indexing mode (consistent or lazy).
     *
     * @return the indexing mode.
     */
    public IndexingMode getIndexingMode() {
        IndexingMode result = IndexingMode.Lazy;
        try {
            result = IndexingMode.valueOf(WordUtils.capitalize(super.getString(Constants.Properties.INDEXING_MODE)));
        } catch (IllegalArgumentException e) {
            this.getLogger().warn("Invalid indexingMode value {}.", super.getString(Constants.Properties.INDEXING_MODE));
        }
        return result;
    }

    /**
     * Sets the indexing mode (consistent or lazy).
     *
     * @param indexingMode the indexing mode.
     */
    public void setIndexingMode(IndexingMode indexingMode) {
        super.set(Constants.Properties.INDEXING_MODE, indexingMode.name());
    }

    /**
     * Gets the paths that are chosen to be indexed by the user.
     *
     * @return the included paths.
     */
    public Collection<IncludedPath> getIncludedPaths() {
        if (this.includedPaths == null) {
            this.includedPaths = super.getCollection(Constants.Properties.INCLUDED_PATHS, IncludedPath.class);

            if (this.includedPaths == null) {
                this.includedPaths = new ArrayList<IncludedPath>();
            }
        }

        return this.includedPaths;
    }

    public void setIncludedPaths(Collection<IncludedPath> includedPaths) {
        this.includedPaths = includedPaths;
    }

    /**
     * Gets the paths that are not indexed.
     *
     * @return the excluded paths.
     */
    public Collection<ExcludedPath> getExcludedPaths() {
        if (this.excludedPaths == null) {
            this.excludedPaths = super.getCollection(Constants.Properties.EXCLUDED_PATHS, ExcludedPath.class);

            if (this.excludedPaths == null) {
                this.excludedPaths = new ArrayList<ExcludedPath>();
            }
        }

        return this.excludedPaths;
    }

    public void setExcludedPaths(Collection<ExcludedPath> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }

    @Override
    void populatePropertyBag() {
        // If indexing mode is not 'none' and not paths are set, set them to the defaults
        if (this.getIndexingMode() != IndexingMode.None && this.getIncludedPaths().size() == 0 &&
                this.getExcludedPaths().size() == 0) {
            IncludedPath includedPath = new IncludedPath();
            includedPath.setPath(IndexingPolicy.DEFAULT_PATH);
            this.getIncludedPaths().add(includedPath);
        }

        if (this.includedPaths != null) {
            for (IncludedPath includedPath : this.includedPaths) {
                includedPath.populatePropertyBag();
            }
            super.set(Constants.Properties.INCLUDED_PATHS, this.includedPaths);
        }

        if (this.excludedPaths != null) {
            for (ExcludedPath excludedPath : this.excludedPaths) {
                excludedPath.populatePropertyBag();
            }
            super.set(Constants.Properties.EXCLUDED_PATHS, this.excludedPaths);
        }
    }
}
