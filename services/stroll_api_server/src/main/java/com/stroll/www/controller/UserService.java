package com.stroll.www.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stroll.www.dao.UserDAO;
import com.stroll.www.vo.PlaceVO;
import com.stroll.www.vo.UserVO;
import com.stroll.www.vo.WishVO;

@Service
public class UserService {
	@Autowired
	private UserDAO dao;

	private String checkPassword(UserVO vo) {
		UserVO rsltVO = dao.checkPassword(vo);
		if (rsltVO == null)
			return null;
		return rsltVO.getId();
	}

	public String login(UserVO vo) {
		return checkPassword(vo);
	}

	public void registerUser(UserVO vo) {
		if(!checkId(vo))
            return;
        if(!vo.getId().matches("^[a-zA-Z0-9]+$"))
            return;
        dao.registerUser(vo);
	}

	public boolean checkId(UserVO vo) {
		if (vo.getId() != null)
			vo = dao.selectUser(vo);
		if (vo == null)
			return true;
		return false;
	}

	public void addToWishList(WishVO vo) {
		if (dao.selectWish(vo) == null)
			dao.insertToWishList(vo);
	}

	public void deleteFromWishList(WishVO vo) {
		dao.deleteFromWishList(vo);
	}

	public Boolean isWishedPlace(WishVO vo) {
		return dao.selectWish(vo) != null;
	}

	public List<PlaceVO> getWishedPlaces(WishVO vo) {
		return dao.selectWishedPlaces(vo);
	}

	public List<PlaceVO> getUserPlaces(UserVO vo) {
		return dao.selectUserPlaces(vo);
	}

	public Object getUserReivews(UserVO vo) {
		return dao.selectUserReivews(vo);
	}

	public boolean withdraw(UserVO vo) {
		if (dao.deleteUser(vo) != 1) {
			return false;
		}
		return true;
	}

    public String getPetType(UserVO vo){
        return dao.selectPetType(vo).getPetType();
    }
}
