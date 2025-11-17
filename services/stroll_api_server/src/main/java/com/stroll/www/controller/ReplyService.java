package com.stroll.www.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stroll.www.dao.ReplyDAO;
import com.stroll.www.vo.PlaceVO;
import com.stroll.www.vo.ReplyVO;

@Service
public class ReplyService {
	@Autowired
	private ReplyDAO dao;

	public void insertReply(ReplyVO vo) {
		System.out.println(vo);
		dao.insertReply(vo);
	}

	public List<ReplyVO> selectReplies(PlaceVO vo) {
		return dao.selectReplies(vo);
	}

	public void deleteReply(ReplyVO vo) {
		dao.deleteReply(vo);
		
	}
}
