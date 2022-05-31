package com.azure.maps.route.models;

import java.util.List;

import com.azure.core.annotation.Fluent;
import com.azure.core.models.GeoCollection;
import com.azure.core.models.GeoPolygonCollection;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Post body parameters for Route directions. */
@Fluent
public final class RouteDirectionsParameters {
    /*
     * A GeoJSON Geometry collection representing sequence of coordinates used
     * as input for route reconstruction and for calculating zero or more
     * alternative routes to this reference route.
     * - The provided sequence of supporting points is used as input for route
     * reconstruction.
     * - The alternative routes are calculated between the origin and
     * destination points specified in the base path parameter locations.
     * - If both _minDeviationDistance_ and _minDeviationTime_ are set to zero,
     * then these origin and destination points are
     * expected to be at (or very near) the beginning and end of the reference
     * route, respectively.
     * - Intermediate locations (_waypoints_) are not supported when using
     * <_supportingPoints_>.
     * - The reference route may contain traffic incidents of type
     * _ROAD_CLOSURE_, which are
     * ignored for the calculation of the reference route's travel time and
     * traffic delay.
     * Please refer to [Supporting
     * Points](https://docs.microsoft.com/azure/azure-maps/how-to-use-best-practices-for-routing#calculate-and-bias-alternative-routes-using-supporting-points)
     * for details.
     */
    @JsonProperty(value = "supportingPoints")
    private GeoCollection supportingPoints;

    /*
     * This is a list of 3-character, ISO 3166-1, alpha-3 country codes of
     * countries in which all toll roads with vignettes are to be avoided, e.g.
     * "AUS,CHE". Toll roads with vignettes in countries not in the list are
     * unaffected. Note: It is an error to specify both **avoidVignette** and
     * **allowVignette**.
     */
    @JsonProperty(value = "avoidVignette")
    private List<String> avoidVignette;

    /*
     * This is a list of 3-character, ISO 3166-1, alpha-3 country codes of
     * countries in which toll roads with vignettes are allowed, e.g.
     * "AUS,CHE". Specifying **allowVignette** with some countries X is
     * equivalent to specifying **avoidVignette** with all countries but X.
     * Specifying **allowVignette** with an empty list is the same as avoiding
     * all toll roads with vignettes. Note: It is an error to specify both
     * **avoidVignette** and **allowVignette**.
     */
    @JsonProperty(value = "allowVignette")
    private List<String> allowVignette;

    /*
     * A GeoJSON MultiPolygon representing list of areas to avoid. Only
     * rectangle polygons are supported. The maximum size of a rectangle is
     * about 160x160 km. Maximum number of avoided areas is **10**. It cannot
     * cross the 180th meridian. It must be between -80 and +80 degrees of
     * latitude.
     */
    @JsonProperty(value = "avoidAreas")
    private GeoPolygonCollection avoidAreas;

    /**
     * Get the supportingPoints property: A GeoJSON Geometry collection representing sequence of coordinates used as
     * input for route reconstruction and for calculating zero or more alternative routes to this reference route. - The
     * provided sequence of supporting points is used as input for route reconstruction. - The alternative routes are
     * calculated between the origin and destination points specified in the base path parameter locations. - If both
     * _minDeviationDistance_ and _minDeviationTime_ are set to zero, then these origin and destination points are
     * expected to be at (or very near) the beginning and end of the reference route, respectively. - Intermediate
     * locations (_waypoints_) are not supported when using &lt;_supportingPoints_&gt;. - The reference route may
     * contain traffic incidents of type _ROAD_CLOSURE_, which are ignored for the calculation of the reference route's
     * travel time and traffic delay. Please refer to [Supporting
     * Points](https://docs.microsoft.com/azure/azure-maps/how-to-use-best-practices-for-routing#calculate-and-bias-alternative-routes-using-supporting-points)
     * for details.
     *
     * @return the supportingPoints value.
     */
    public GeoCollection getSupportingPoints() {
        return this.supportingPoints;
    }

    /**
     * Set the supportingPoints property: A GeoJSON Geometry collection representing sequence of coordinates used as
     * input for route reconstruction and for calculating zero or more alternative routes to this reference route. - The
     * provided sequence of supporting points is used as input for route reconstruction. - The alternative routes are
     * calculated between the origin and destination points specified in the base path parameter locations. - If both
     * _minDeviationDistance_ and _minDeviationTime_ are set to zero, then these origin and destination points are
     * expected to be at (or very near) the beginning and end of the reference route, respectively. - Intermediate
     * locations (_waypoints_) are not supported when using &lt;_supportingPoints_&gt;. - The reference route may
     * contain traffic incidents of type _ROAD_CLOSURE_, which are ignored for the calculation of the reference route's
     * travel time and traffic delay. Please refer to [Supporting
     * Points](https://docs.microsoft.com/azure/azure-maps/how-to-use-best-practices-for-routing#calculate-and-bias-alternative-routes-using-supporting-points)
     * for details.
     *
     * @param supportingPoints the supportingPoints value to set.
     * @return the RouteDirectionParameters object itself.
     */
    public RouteDirectionsParameters setSupportingPoints(GeoCollection supportingPoints) {
        this.supportingPoints = supportingPoints;
        return this;
    }

    /**
     * Get the avoidVignette property: This is a list of 3-character, ISO 3166-1, alpha-3 country codes of countries in
     * which all toll roads with vignettes are to be avoided, e.g. "AUS,CHE". Toll roads with vignettes in countries not
     * in the list are unaffected. Note: It is an error to specify both **avoidVignette** and **allowVignette**.
     *
     * @return the avoidVignette value.
     */
    public List<String> getAvoidVignette() {
        return this.avoidVignette;
    }

    /**
     * Set the avoidVignette property: This is a list of 3-character, ISO 3166-1, alpha-3 country codes of countries in
     * which all toll roads with vignettes are to be avoided, e.g. "AUS,CHE". Toll roads with vignettes in countries not
     * in the list are unaffected. Note: It is an error to specify both **avoidVignette** and **allowVignette**.
     *
     * @param avoidVignette the avoidVignette value to set.
     * @return the RouteDirectionParameters object itself.
     */
    public RouteDirectionsParameters setAvoidVignette(List<String> avoidVignette) {
        this.avoidVignette = avoidVignette;
        return this;
    }

    /**
     * Get the allowVignette property: This is a list of 3-character, ISO 3166-1, alpha-3 country codes of countries in
     * which toll roads with vignettes are allowed, e.g. "AUS,CHE". Specifying **allowVignette** with some countries X
     * is equivalent to specifying **avoidVignette** with all countries but X. Specifying **allowVignette** with an
     * empty list is the same as avoiding all toll roads with vignettes. Note: It is an error to specify both
     * **avoidVignette** and **allowVignette**.
     *
     * @return the allowVignette value.
     */
    public List<String> getAllowVignette() {
        return this.allowVignette;
    }

    /**
     * Set the allowVignette property: This is a list of 3-character, ISO 3166-1, alpha-3 country codes of countries in
     * which toll roads with vignettes are allowed, e.g. "AUS,CHE". Specifying **allowVignette** with some countries X
     * is equivalent to specifying **avoidVignette** with all countries but X. Specifying **allowVignette** with an
     * empty list is the same as avoiding all toll roads with vignettes. Note: It is an error to specify both
     * **avoidVignette** and **allowVignette**.
     *
     * @param allowVignette the allowVignette value to set.
     * @return the RouteDirectionParameters object itself.
     */
    public RouteDirectionsParameters setAllowVignette(List<String> allowVignette) {
        this.allowVignette = allowVignette;
        return this;
    }

    /**
     * Get the avoidAreas property: A GeoJSON MultiPolygon representing list of areas to avoid. Only rectangle polygons
     * are supported. The maximum size of a rectangle is about 160x160 km. Maximum number of avoided areas is **10**. It
     * cannot cross the 180th meridian. It must be between -80 and +80 degrees of latitude.
     *
     * @return the avoidAreas value.
     */
    public GeoPolygonCollection getAvoidAreas() {
        return this.avoidAreas;
    }

    /**
     * Set the avoidAreas property: A GeoJSON MultiPolygon representing list of areas to avoid. Only rectangle polygons
     * are supported. The maximum size of a rectangle is about 160x160 km. Maximum number of avoided areas is **10**. It
     * cannot cross the 180th meridian. It must be between -80 and +80 degrees of latitude.
     *
     * @param avoidAreas the avoidAreas value to set.
     * @return the RouteDirectionParameters object itself.
     */
    public RouteDirectionsParameters setAvoidAreas(GeoPolygonCollection avoidAreas) {
        this.avoidAreas = avoidAreas;
        return this;
    }
}
