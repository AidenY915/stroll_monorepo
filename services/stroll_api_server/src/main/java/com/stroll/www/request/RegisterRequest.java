package com.stroll.www.request;

import com.stroll.www.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String userId;
    private String password;
    private String nickname;
    private String email;
    private String petType;

    public UserVO to(){
        return new UserVO(userId, password, nickname, email, petType);
    }
}
