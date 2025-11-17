package com.stroll.www.response;

import java.util.List;

public class PlaceListResponse {
    private List<PlaceSummaryResponse> places;
    private int lastPage;

    public PlaceListResponse(List<PlaceSummaryResponse> places, int lastPage) {
        this.places = places;
        this.lastPage = lastPage;
    }

    public List<PlaceSummaryResponse> getPlaces() { return places; }
    public int getLastPage() { return lastPage; }
}

