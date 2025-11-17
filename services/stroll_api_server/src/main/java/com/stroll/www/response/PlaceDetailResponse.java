package com.stroll.www.response;

import com.stroll.www.vo.PlaceVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDetailResponse {
    private int placeNo;
    private String name;
    private String content;
    private Timestamp createdAt;
    private String address;
    private String userId;
    private double distance;
    private double star;
    @Setter
    private boolean isWished;
    @Setter
    private List<String> imgs;

    public static PlaceDetailResponse from(PlaceVO vo) {
        String address = vo.getGuAddress() + " " + vo.getAfterGuAddress() + " " + vo.getDetailAddress();
        return new PlaceDetailResponse(vo.getNo(), vo.getTitle(), vo.getContent(), vo.getWrittenDate(),address, vo.getUserId(), vo.getDistance(), vo.getStar(), false, new ArrayList<String>());
    }
}
