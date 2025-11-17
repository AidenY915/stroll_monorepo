package com.stroll.www.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.stroll.www.vo.PlaceVO;
import com.stroll.www.vo.UserVO;
import com.stroll.www.vo.WishVO;

@Repository
public class UserDAO {
	@Autowired
	private SqlSession mybatis;

	public UserVO checkPassword(UserVO vo) {
		return mybatis.selectOne("user.checkPassword", vo);
	}

	public void registerUser(UserVO vo) {
		mybatis.insert("user.registerUser", vo);
	}

	public UserVO selectUser(UserVO vo) {
		return mybatis.selectOne("user.selectUser", vo);

	}
	public WishVO selectWish(WishVO vo) {
		return mybatis.selectOne("user.selectWish",vo);
	}
	public void insertToWishList(WishVO vo) {
		mybatis.insert("user.insertToWishList", vo);
	}
	public void deleteFromWishList(WishVO vo) {
		mybatis.insert("user.deleteFromWishList", vo);
	}

	public List<PlaceVO> selectWishedPlaces(WishVO vo) {
		return mybatis.selectList("user.selectWishedPlaces", vo);
	}

	public List<PlaceVO> selectUserPlaces(UserVO vo) {
		return mybatis.selectList("user.selectUserPlaces", vo);
	}

	public Object selectUserReivews(UserVO vo) {
		return mybatis.selectList("user.selectUserReviews", vo);
	}

	public int deleteUser(UserVO vo) {
		return mybatis.delete("user.deleteUser", vo);
	}

	public UserVO selectUserByNickname(UserVO vo) {
		return mybatis.selectOne("user.selectUserByNickname", vo);
	}

    public UserVO selectPetType(UserVO vo) {return mybatis.selectOne("user.selectPetType", vo); }
}
