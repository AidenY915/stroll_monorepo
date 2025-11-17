package com.stroll.www.vo;

import java.io.Serializable;

public class WishVO implements Serializable{
	private static final long serialVersionUID = -468619732720108382L;
	private int no;
	private String userId;
	private int placeNo;
	
	public WishVO() {}
	
	public int getNo() {
		return no;
	}
	public void setNo(int no) {
		this.no = no;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getPlaceNo() {
		return placeNo;
	}
	public void setPlaceNo(int placeNo) {
		this.placeNo = placeNo;
	}
	
}
