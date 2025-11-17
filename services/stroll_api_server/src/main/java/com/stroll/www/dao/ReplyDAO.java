package com.stroll.www.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.stroll.www.vo.PlaceVO;
import com.stroll.www.vo.ReplyVO;

@Repository
public class ReplyDAO {
	@Autowired
	private SqlSession mybatis;

	public void insertReply(ReplyVO vo) {
		mybatis.insert("reply.insertReply", vo);
	}

	public List<ReplyVO> selectReplies(PlaceVO vo) {
		return mybatis.selectList("reply.selectReplies", vo);		
	}

	public void deleteReply(ReplyVO vo) {
		mybatis.delete("reply.deleteReply", vo);
	}
}
