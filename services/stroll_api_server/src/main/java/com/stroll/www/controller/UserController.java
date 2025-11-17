package com.stroll.www.controller;

import java.util.List;
import java.util.Map;

import com.stroll.www.response.PlaceSummaryResponse;
import com.stroll.www.response.ReviewResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.stroll.www.vo.PlaceVO;
import com.stroll.www.vo.ReplyVO;
import com.stroll.www.vo.UserVO;
import com.stroll.www.vo.WishVO;

@RestController
@RequestMapping("/api/users")
public class UserController {
	@Autowired
	private UserService service;

	/**
	 * 사용자의 찜 목록 조회
	 */
	@GetMapping(value = "/{userId}/wishlist", produces = "application/json;charset=UTF-8")
	public ResponseEntity<?> getWishlist(@PathVariable("userId") String userId, HttpServletRequest req) {
		String authUserId = (String) req.getAttribute("auth.userId");
		if (authUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "로그인이 필요합니다."));
		}
		if (!authUserId.equals(userId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "접근 권한이 없습니다."));
		}

		WishVO wishVO = new WishVO();
		wishVO.setUserId(userId);
		List<PlaceVO> places = service.getWishedPlaces(wishVO);
		List<PlaceSummaryResponse> placeList = places.stream()
				.map(PlaceSummaryResponse::from)
				.toList();

		return ResponseEntity.ok(Map.of("wishlist", placeList));
	}

	/**
	 * 찜 목록에 장소 추가
	 */
	@PostMapping(value = "/{userId}/wishlist", produces = "application/json;charset=UTF-8")
	public ResponseEntity<?> addToWishlist(
			@PathVariable("userId") String userId,
			@RequestBody Map<String, Integer> payload,
			HttpServletRequest req) {
		String authUserId = (String) req.getAttribute("auth.userId");
		if (authUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "로그인이 필요합니다."));
		}
		if (!authUserId.equals(userId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "접근 권한이 없습니다."));
		}

		Integer placeNo = payload.get("placeNo");
		if (placeNo == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", "placeNo가 필요합니다."));
		}

		WishVO wishVO = new WishVO();
		wishVO.setUserId(userId);
		wishVO.setPlaceNo(placeNo);
		service.addToWishList(wishVO);

		return ResponseEntity.ok(Map.of("message", "찜 목록에 추가되었습니다."));
	}

	/**
	 * 찜 목록에서 장소 삭제
	 */
	@DeleteMapping(value = "/{userId}/wishlist/{placeNo}", produces = "application/json;charset=UTF-8")
	public ResponseEntity<?> deleteFromWishlist(
			@PathVariable("userId") String userId,
			@PathVariable("placeNo") int placeNo,
			HttpServletRequest req) {
		String authUserId = (String) req.getAttribute("auth.userId");
		if (authUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "로그인이 필요합니다."));
		}
		if (!authUserId.equals(userId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "접근 권한이 없습니다."));
		}

		WishVO wishVO = new WishVO();
		wishVO.setUserId(userId);
		wishVO.setPlaceNo(placeNo);
		service.deleteFromWishList(wishVO);

		return ResponseEntity.ok(Map.of("message", "찜 목록에서 삭제되었습니다."));
	}

	/**
	 * 사용자가 작성한 장소 목록 조회
	 */
	@GetMapping(value = "/{userId}/places", produces = "application/json;charset=UTF-8")
	public ResponseEntity<?> getUserPlaces(@PathVariable("userId") String userId, HttpServletRequest req) {
		String authUserId = (String) req.getAttribute("auth.userId");
		if (authUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "로그인이 필요합니다."));
		}
		if (!authUserId.equals(userId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "접근 권한이 없습니다."));
		}

		UserVO userVO = new UserVO();
		userVO.setId(userId);
		List<PlaceVO> places = service.getUserPlaces(userVO);
		List<PlaceSummaryResponse> placeList = places.stream()
				.map(PlaceSummaryResponse::from)
				.toList();

		return ResponseEntity.ok(Map.of("places", placeList));
	}

	/**
	 * 사용자가 작성한 리뷰 목록 조회
	 */
	@GetMapping(value = "/{userId}/reviews", produces = "application/json;charset=UTF-8")
	public ResponseEntity<?> getUserReviews(@PathVariable("userId") String userId, HttpServletRequest req) {
		String authUserId = (String) req.getAttribute("auth.userId");
		if (authUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "로그인이 필요합니다."));
		}
		if (!authUserId.equals(userId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "접근 권한이 없습니다."));
		}

		UserVO userVO = new UserVO();
		userVO.setId(userId);
		@SuppressWarnings("unchecked")
		List<ReplyVO> reviews = (List<ReplyVO>) service.getUserReivews(userVO);
		List<ReviewResponse> reviewList = reviews.stream()
				.map(ReviewResponse::from)
				.toList();

		return ResponseEntity.ok(Map.of("reviews", reviewList));
	}

	/**
	 * 회원 탈퇴
	 */
	@DeleteMapping(value = "/{userId}", produces = "application/json;charset=UTF-8")
	public ResponseEntity<?> withdraw(
			@PathVariable("userId") String userId,
			@RequestBody Map<String, String> payload,
			HttpServletRequest req) {
		String authUserId = (String) req.getAttribute("auth.userId");
		if (authUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "로그인이 필요합니다."));
		}
		if (!authUserId.equals(userId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "접근 권한이 없습니다."));
		}

		String password = payload.get("password");
		if (password == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", "비밀번호가 필요합니다."));
		}

		UserVO userVO = new UserVO();
		userVO.setId(userId);
		userVO.setPassword(password);

		if (!service.withdraw(userVO)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "비밀번호가 올바르지 않습니다."));
		}

		return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
	}
}
