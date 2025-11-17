package com.stroll.www.response;

import com.stroll.www.vo.ReplyVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private String userId;
    private String userNickname;
    private String content;
    private double star;
    private Timestamp createdAt;

    public static ReviewResponse from(ReplyVO vo) {
        return new ReviewResponse(vo.getUserId(), vo.getUserNickname(), vo.getContent(), vo.getStar(), vo.getWrittenDate());
    }
}
