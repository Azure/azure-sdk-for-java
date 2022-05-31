package com.azure.maps.render.models;

/**
 * Lat Long organizes latitude longitude
 */
public final class LatLong {
    private double latitude;
    private double longitude;

    /**
     * Constructor
     */
    public LatLong() {
    }

    /**
     * Constructs a LatLong with a latitude and a longitude.
     * @param latitude
     * @param longitude
     */
    public LatLong(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Returns a LatLong from a comma-separated position string.
     * @param position
     * @return
     */
    public static LatLong fromCommaSeparatedString(String position) {
        if (position != null) {
            final String[] coords = position.split(",");

            if (coords != null && coords.length == 2) {
                return new LatLong(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
            }
        }

        return null;
    }

    /**
     * Returns the latitude.
     * @return
     */
    public double getLat() {
        return latitude;
    }

    /**
     * Returns the longitude.
     * @return
     */
    public double getLon() {
        return longitude;
    }

    /**
     * Returns the latitude.
     * @return
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude.
     * @return
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Returns a string representation.
     */
    @Override
    public String toString() {
        return latitude + "," + longitude;
    }
}
