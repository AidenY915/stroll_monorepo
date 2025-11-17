package com.stroll.www.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.stroll.www.dao.ImageDAO;
import com.stroll.www.dao.PlaceDAO;
import com.stroll.www.property.ApiKey;
import com.stroll.www.property.AwsProps;
import com.stroll.www.vo.ImageVO;
import com.stroll.www.vo.PlaceVO;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class PlaceService {
	@Autowired
	private PlaceDAO dao;
	@Autowired
	private ImageDAO imageDao;	
    @Autowired
    private AwsProps awsProps;
	@Autowired
    private ApiKey apiKey;
	private final static int PAGE_SIZE = 10;

	private S3Client s3Client;

    @PostConstruct
    void initializeS3Client() {
        Region region = awsProps.getS3Region() != null ? Region.of(awsProps.getS3Region()) : Region.AP_NORTHEAST_2;
        this.s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsProps.getS3AccessKeyId(), awsProps.getS3SecretAccessKey())
                ))
                .build();
    }

	public PlaceVO getPlace(PlaceVO vo) {
		vo = dao.getPlace(vo);
		vo.setStar(Math.round(vo.getStar() * 10) / 10.0f);
		return vo;
	}

	public List<PlaceVO> getPlaceList(PlaceVO vo, String keywords, String order, int page, HttpServletRequest request,
			int maxDistance, int minStar) {
        String petType = (String)request.getAttribute("petType");
        vo.setPetType(petType);
		List<PlaceVO> listFromDb = null;
		keywords = keywords.replaceAll(" ", "|");
		if (keywords.equals(""))
			keywords = ".*";
		vo.setTitle(keywords);
		vo.setDetailAddress(keywords);
		if (vo.getCategory() == null)
			vo.setCategory("");
		if (vo.getGuAddress() == null || vo.getGuAddress().equals("")) {		//내 위치 설정 -> 내 구에 있는 장소들만 검색
			vo.setGuAddress(keywords);
			listFromDb = dao.getPlaceList(vo);
		} else {
			String guAddressRegex = vo.getGuAddress().replaceAll("(특별시|광역시|시)", "[가-힣]{0,3}").replaceAll("^[가-힣]+도", ""); //!!수정 필!!클라이언트도 카카오이므로 단순히 바꾸면 gu_address만 자르면 됨.
			vo.setGuAddress(guAddressRegex);
			listFromDb = dao.getPlaceListByGuAddress(vo);
		}
        //petType으로 서버에서 정제 SQL을 수정하지 않았음. 생각해보니 SQL에서 바꿔야 함. ORDER BY 때문
//        petType = petType == null ? "" : petType;
//        System.out.println("유저 petType: "+petType);
//        Iterator<PlaceVO> itr = listFromDb.iterator();
//        while(itr.hasNext()){
//            PlaceVO place = itr.next();
//            String placePetType = place.getPetType();
//            System.out.println("장소 petType: "+placePetType);
//            if(placePetType == null || placePetType.equals("기타"))
//                continue;
//            if(!placePetType.contains(petType)){
//                itr.remove();
//            }
//        }
		for (PlaceVO place : listFromDb) {
			place.setDistance((int) Math.pow(((Math.pow(place.getX() * 1849 - vo.getX() * 1849, 2)
					+ Math.pow(place.getY() * 110940 - vo.getY() * 110940, 2))), 0.5));
			place.setStar(Math.round(place.getStar() * 10) / 10.0f);
		}
		sortPlaces(listFromDb, order);
		if (vo.getX() != 0)
			filterPlaces(listFromDb, maxDistance, minStar);

		List<PlaceVO> rsltList = new LinkedList<>();
		request.setAttribute("numOfPages", (int) Math.ceil(listFromDb.size() / (double) PAGE_SIZE));
		for (int i = (page - 1) * PAGE_SIZE; i < (page) * PAGE_SIZE && i < listFromDb.size(); i++) {
			rsltList.add(listFromDb.get(i));
		}
		return rsltList;
	}

	public int insertPlace(PlaceVO vo, MultipartFile[] imgs) {
		String jsonStr = getKakaoCoordinate(vo.getGuAddress() + vo.getAfterGuAddress());
		String x = jsonStr.split("\"x\":\"")[1].split("\"")[0];
		String y = jsonStr.split("\"y\":\"")[1].split("\"")[0];
		vo.setX(Double.parseDouble(x));
		vo.setY(Double.parseDouble(y));
		int rslt = dao.insertPlace(vo);
		if (rslt == 1)
			uploadImgs(imgs, vo);
		return vo.getNo();
	}

	private String getKakaoCoordinate(String address) {
		String kakaoApiKey = apiKey.getKakaoApiKey();
		String kakaoApiUrl = "https://dapi.kakao.com/v2/local/search/address.json";
		String jsonString = null;
		try {
			address = URLEncoder.encode(address, "UTF-8");

			String addr = kakaoApiUrl + "?query=" + address;

			URL url = new URL(addr);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Authorization", "KakaoAK " + kakaoApiKey);

			BufferedReader rd = null;
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			StringBuffer docJson = new StringBuffer();

			String line;

			while ((line = rd.readLine()) != null) {
				docJson.append(line);
			}

			jsonString = docJson.toString();
			rd.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonString;
	}

	private void uploadImgs(MultipartFile[] imgs, PlaceVO vo) {
		System.out.println(Arrays.toString(imgs));
		for (int i = 0; i < imgs.length; i++) {
			if (imgs[i].isEmpty())
				break;
			try {
				String imgPath = "image/" + vo.getNo() + "_" + (i + 1) + "." + "jpg";

				// S3에 업로드
				PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(awsProps.getS3Bucket()).key(imgPath)
						.contentType(imgs[i].getContentType()).build();

				s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imgs[i].getBytes()));
				// img 테이블에 추가
				ImageVO imgVo = new ImageVO();
				imgVo.setImagePath(imgPath);
				imgVo.setPlaceNo(vo.getNo());
				imageDao.insertImg(imgVo);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return;
	}

	public List<String> getImgs(PlaceVO vo) {
		List<ImageVO> imgs = imageDao.selectImgsByPlaceNo(vo.getNo());
		List<String> rslt = new LinkedList<String>();
		for (ImageVO img : imgs) {
			rslt.add(img.getImagePath().split("image/")[1]);
		}
		return rslt;
	}

	private void sortPlaces(List<PlaceVO> placeList, String order) {
		switch (order) {
		case "distance":
			Collections.sort(placeList, (p1, p2) -> (p1.getDistance() - p2.getDistance()));
			break;
		case "star":
			Collections.sort(placeList, (p1, p2) -> ((int) (p2.getStar() - p1.getStar() * 10)));
			break;
		}
	}

	private void filterPlaces(List<PlaceVO> placeList, int maxDistance, int minStar) {
		if (maxDistance >= 1 && maxDistance <= 50) {
			maxDistance *= 100;
			for (int i = 0; i < placeList.size(); i++) {
				if (placeList.get(i).getDistance() > maxDistance) {
					placeList.remove(i);
					i--;
				}
			}
		}
		if (minStar >= 1 && minStar <= 5) {
			for (int i = 0; i < placeList.size(); i++) {
				if (placeList.get(i).getStar() < minStar) {
					placeList.remove(i);
					i--;
				}
			}
		}
	}

	public boolean deletePlace(PlaceVO vo, String id) {
		vo = dao.getPlace(vo);
		if (vo.getUserId().equals(id)) {
			deleteImgs(vo);
			return dao.deletePlace(vo) == 1;
		}
		return false;
	}

	private void deleteImgs(PlaceVO vo) {
		List<ImageVO> imgs = imageDao.selectImgsByPlaceNo(vo.getNo());
		for(ImageVO img : imgs)
		{
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
	                .bucket(awsProps.getS3Bucket())
	                .key(img.getImagePath())
	                .build();
			try {
				s3Client.deleteObject(deleteObjectRequest);
				imageDao.deleteImg(img.getNo());
			}catch(S3Exception e) {
				e.printStackTrace();
				System.out.println("이미지 삭제 실패");
			}
		}
	}
}
