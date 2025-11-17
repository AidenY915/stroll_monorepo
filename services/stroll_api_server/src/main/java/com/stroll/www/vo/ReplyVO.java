package com.stroll.www.vo;

import java.io.Serializable;
import java.sql.Timestamp;

public class ReplyVO implements Serializable{
	private static final long serialVersionUID = 6721634154947159221L;
	private int no;
	private String userId;
	private String userNickname;
	private int placeNo;
	private String content;
	private int star;
	private Timestamp writtenDate;
	private String placeTitle;
	public ReplyVO() {}
	
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
	public String getUserNickname() {
		return userNickname;
	}
	public void setUserNickname(String userNickname) {
		this.userNickname = userNickname;
	}
	public int getPlaceNo() {
		return placeNo;
	}
	public void setPlaceNo(int placeNo) {
		this.placeNo = placeNo;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getStar() {
		return star;
	}
	public void setStar(int star) {
		this.star = star;
	}
	public Timestamp getWrittenDate() {
		return writtenDate;
	}

	public void setWrittenDate(Timestamp writtenDate) {
		this.writtenDate = writtenDate;
	}
	
	public String getPlaceTitle() {
		return placeTitle;
	}

	public void setPlaceTitle(String placeTitle) {
		this.placeTitle = placeTitle;
	}

	@Override
	public String toString() {
		return "{ no:"+ no + ", userID:" + userId + ", userNickname:" + userNickname + ", placeNo:" + placeNo + ", content:" + content + ", star:" + star + ", writtenDate:" + writtenDate + " }" ;
	}
	
}
