package com.stroll.www.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceVO implements Serializable {
	private static final long serialVersionUID = 7766196491054550097L;

	private int no;
	private String title;
	private String content;
	private String category;
	private Timestamp writtenDate;
	private String guAddress;
	private String afterGuAddress;
	private String detailAddress;
	private double x;
	private double y;
	private String userId;
	private int distance;
	private float star;
    private String petType;

	public String toString() {
		return "{no:" + no +", title:" + title + ", content:" + content + ", category:" + category + ", writtenDate:" + writtenDate + ", address:" + guAddress + ", afterGuAddress:" + afterGuAddress + ", detailAddress:" + detailAddress + ", x:" + x + ", y:" + y + "}";
	}

}
