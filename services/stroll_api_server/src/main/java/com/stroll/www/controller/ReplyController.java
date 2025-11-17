package com.stroll.www.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.stroll.www.vo.ReplyVO;

@RestController
@RequestMapping("/api")
public class ReplyController {
	@Autowired
	private ReplyService service;

	/**
	 * 리뷰 작성
	 */
	@PostMapping(value = "/place/{placeNo}/reviews", produces = "application/json;charset=UTF-8")
	public ResponseEntity<?> createReview(
			@PathVariable("placeNo") int placeNo,
			@RequestBody Map<String, Object> payload,
			HttpServletRequest req) {
		String userId = (String) req.getAttribute("auth.userId");
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "로그인이 필요합니다."));
		}

		String content = (String) payload.get("content");
		Integer star = (Integer) payload.get("star");

		if (content == null || star == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", "content와 star가 필요합니다."));
		}

		ReplyVO replyVO = new ReplyVO();
		replyVO.setPlaceNo(placeNo);
		replyVO.setUserId(userId);
		replyVO.setContent(content);
		replyVO.setStar(star);

		service.insertReply(replyVO);

		return ResponseEntity.ok(Map.of("message", "리뷰가 작성되었습니다."));
	}

	/**
	 * 리뷰 삭제
	 */
	@DeleteMapping(value = "/place/{placeNo}/reviews/{reviewNo}", produces = "application/json;charset=UTF-8")
	public ResponseEntity<?> deleteReview(
			@PathVariable("placeNo") int placeNo,
			@PathVariable("reviewNo") int reviewNo,
			HttpServletRequest req) {
		String userId = (String) req.getAttribute("auth.userId");
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "로그인이 필요합니다."));
		}

		ReplyVO replyVO = new ReplyVO();
		replyVO.setNo(reviewNo);
		replyVO.setPlaceNo(placeNo);
		replyVO.setUserId(userId);

		service.deleteReply(replyVO);

		return ResponseEntity.ok(Map.of("message", "리뷰가 삭제되었습니다."));
	}
}