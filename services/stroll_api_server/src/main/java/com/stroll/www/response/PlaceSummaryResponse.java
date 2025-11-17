package com.stroll.www.response;

import com.stroll.www.vo.PlaceVO;

public class PlaceSummaryResponse {
    private int placeNo;
    private String name;
    private double star;
    private int distance;
    private String address;
    public PlaceSummaryResponse() {}

    public PlaceSummaryResponse(int placeNo, String name, double star, int distance, String address) {
        this.placeNo = placeNo;
        this.name = name;
        this.star = star;
        this.distance = distance;
        this.address = address;
    }

    // VO → Response 변환
    public static PlaceSummaryResponse from(PlaceVO vo) {
        return new PlaceSummaryResponse(
                vo.getNo(),
                vo.getTitle(),
                vo.getStar(),
                vo.getDistance(),
                vo.getGuAddress() + " " + vo.getAfterGuAddress() + " " + vo.getDetailAddress()
        );
    }

    public int getPlaceNo() { return placeNo; }
    public String getName() { return name; }
    public double getStar() { return star; }
    public int getDistance() { return distance; }
    public String getAddress() {return address;}
}
