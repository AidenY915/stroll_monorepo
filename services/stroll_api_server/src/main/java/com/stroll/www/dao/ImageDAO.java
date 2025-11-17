package com.stroll.www.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.stroll.www.vo.ImageVO;

@Repository
public class ImageDAO{
	@Autowired
	private SqlSession mybatis;

	public int insertImg(ImageVO img) {
		return mybatis.insert("image.insertImg", img);
	}
	
	public List<ImageVO> selectImgsByPlaceNo(@Param("placeNo") int placeNo){
		return mybatis.selectList("image.selectImgsByPlaceNo", placeNo);
	}

	public int deleteImg(int no) {
		return mybatis.delete("image.deleteImg", no);
	}
}
