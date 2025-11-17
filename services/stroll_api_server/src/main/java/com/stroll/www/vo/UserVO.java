package com.stroll.www.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
public class UserVO implements Serializable{
	private static final long serialVersionUID = -8799908554156269389L;
	private String id;
	private String password;
	private String nickname;
	private String email;
    private String petType;
	public UserVO() {}
    public UserVO(String id, String password) {
        this.id = id;
        this.password = password;
    }
	@Override
	public String toString() {
		return "{id = " + id + ", password = " + password + " }";
	}
}
