package com.azure.maps.route.models;

import java.util.List;

import com.azure.maps.route.implementation.helpers.RouteMatrixResultPropertiesHelper;
import com.azure.maps.route.implementation.models.RouteMatrixResultPrivate;

public class RouteMatrixResult {
    private String formatVersion;
    private List<List<RouteMatrix>> matrix;
    private RouteMatrixSummary summary;
    private String matrixId;

    static {
        RouteMatrixResultPropertiesHelper.setAccessor(
            new RouteMatrixResultPropertiesHelper.RouteMatrixResultAccessor() {
                @Override
                public void setFromRouteMatrixResultPrivate(RouteMatrixResult result,
                        RouteMatrixResultPrivate privateResult) {
                    result.setFromRouteMatrixResultPrivate(privateResult);
                }
        });
    }

    /**
     * Get the formatVersion property: Format Version property.
     *
     * @return the formatVersion value.
     */
    public String getFormatVersion() {
        return this.formatVersion;
    }

    /**
     * Get the matrix property: Results as a 2 dimensional array of route summaries.
     *
     * @return the matrix value.
     */
    public List<List<RouteMatrix>> getMatrix() {
        return this.matrix;
    }

    /**
     * Get the summary property: Summary object.
     *
     * @return the summary value.
     */
    public RouteMatrixSummary getSummary() {
        return this.summary;
    }

    /**
     * Get the matrix id of this request.
     */
    public String getMatrixId() {
        return matrixId;
    }

    /**
     * Sets the matrix id for this request.
     * @param matrixId
     */
    public void setMatrixId(String matrixId) {
        this.matrixId = matrixId;
    }

    // private setter
    private void setFromRouteMatrixResultPrivate(RouteMatrixResultPrivate privateResult) {
        this.formatVersion = privateResult.getFormatVersion();
        this.matrix = privateResult.getMatrix();
        this.summary = privateResult.getSummary();
    }
}
