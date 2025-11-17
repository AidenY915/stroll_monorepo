package com.stroll.www.controller;

import com.stroll.www.request.LoginRequest;
import com.stroll.www.request.RegisterRequest;
import com.stroll.www.response.CheckIdResponse;
import com.stroll.www.response.TokenResponse;
import com.stroll.www.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = "application/json", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        UserVO userVo = new UserVO(loginRequest.getUserId(), loginRequest.getPassword());
        String id = userService.login(userVo);
        if (id == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // 401
                    .body(Map.of(
                            "message", "아이디 또는 비밀번호가 올바르지 않습니다."
                    ));
        }
        String token = null;
        //JwtService로 토큰 발급
        String petType = userService.getPetType(userVo);
        try {
            token = jwtService.generateToken(id, petType);
        }catch (DecoderException e){
            e.printStackTrace(); //토큰 발급 실패
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // 401
                    .body(Map.of(
                            "message", "아이디 또는 비밀번호가 올바르지 않습니다."
                    ));
        }
        TokenResponse tokenResponse = new TokenResponse(token);
        return ResponseEntity.ok(tokenResponse);
    }
    @GetMapping(value = "check-id/{userId}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<CheckIdResponse> checkId(@PathVariable("userId") String userId) {
        UserVO userVo = new UserVO();
        userVo.setId(userId);
        CheckIdResponse checkIdResponse = new CheckIdResponse(userId, userService.checkId(userVo));
        return ResponseEntity.ok(checkIdResponse);
    }
    @PostMapping(value = "register", consumes = "application/json", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest, HttpServletRequest req){
        if(req.getAttribute("auth.userId") != null)
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 401
                .body(Map.of(
                        "message", "Wrong Access"
                ));

        UserVO userVo = registerRequest.to();
        userService.registerUser(userVo);
        return ResponseEntity.ok(Map.of(
                "message", "Register Done"
        ));
    }

/*
	@RequestMapping(value = "/logout")
	public String logout(HttpServletRequest request) {
		request.getSession().invalidate();
		return "redirect:/";
	}
*/
}