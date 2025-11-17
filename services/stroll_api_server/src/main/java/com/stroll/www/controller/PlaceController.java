package com.stroll.www.controller;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.stroll.www.response.PlaceDetailResponse;
import com.stroll.www.response.PlaceListResponse;
import com.stroll.www.response.PlaceSummaryResponse;
import com.stroll.www.response.ReviewResponse;
import com.stroll.www.vo.ReplyVO;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.stroll.www.property.AwsProps;
import com.stroll.www.vo.PlaceVO;
import com.stroll.www.vo.WishVO;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequestMapping("/api")
public class PlaceController {
	@Autowired
	private PlaceService placeService;
	@Autowired
	private ReplyService replyService;
	@Autowired
	private UserService userService;
    @Autowired
    private AwsProps awsProps;
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
	
	private String extractGuAddress(String fullAddress) {
		if (fullAddress == null || fullAddress.isEmpty()) {
	        return "";
	    }
	    Pattern pattern = Pattern.compile("^(.+?(구|군))");
	    Matcher matcher = pattern.matcher(fullAddress);
	    if (matcher.find()) {
	        return matcher.group(1).trim(); // 전체 매칭된 부분 리턴
	    }
	    return ""; // 구나 군이 없을 경우 빈 문자열
	}

    @GetMapping(value = "/places", produces = "application/json;charset=UTF-8")
    public ResponseEntity<PlaceListResponse> showAroundme(
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "keywords", defaultValue = "") String keywords,
            @RequestParam(value = "order", defaultValue = "distance") String order,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "maxDistance", defaultValue = "-1") int maxDistance,
            @RequestParam(value = "minStar", defaultValue = "-1") int minStar,
            @RequestParam(value = "x", required = false) Double x,
            @RequestParam(value = "y", required = false) Double y,
            HttpServletRequest request
    ) {
        System.out.println("address: " + address);
        System.out.println("keywords: " + keywords);
        System.out.println("order: " + order);
        System.out.println("page: " + page);
        System.out.println("maxDistance: " + maxDistance);
        System.out.println("minStar: " + minStar);
        System.out.println("x: " + x);
        System.out.println("y: " + y);
        // 기존 vo 사용
        PlaceVO vo = new PlaceVO();
        if(x != null && y != null) {
            vo.setX(x);
            vo.setY(y);
        }

        // 서비스 호출
        List<PlaceVO> searchedPlaces =
                placeService.getPlaceList(vo, keywords, order, page, request, maxDistance, minStar);

        // 총 페이지 수 (기존에 request attribute로 넣던 값 활용)
        int numOfPages = (Integer) request.getAttribute("numOfPages");

        // VO -> DTO 변환
        List<PlaceSummaryResponse> places = searchedPlaces.stream()
                .map(PlaceSummaryResponse::from)
                .toList();

        // 응답 래퍼 구성
        PlaceListResponse body = new PlaceListResponse(places, numOfPages);

        return ResponseEntity.ok(body);
    }

	@GetMapping(value = "/place/{placeNo}", produces = "application/json;charset=UTF-8")
	public ResponseEntity<PlaceDetailResponse> showDetail(@PathVariable(value = "placeNo") int placeNo, HttpSession session) {
        PlaceVO place = new PlaceVO();
        System.out.println(placeNo);
        place.setNo(placeNo);
        place = placeService.getPlace(place);
        PlaceDetailResponse placeDetailResponse = PlaceDetailResponse.from(place);

		List<String> imgs =  placeService.getImgs(place);
        placeDetailResponse.setImgs(imgs);

        //찜한 곳인지 표시
		String id = (String) session.getAttribute("id");
		if (id != null) {
            WishVO wishVO = new WishVO();
			wishVO.setUserId(id);
			wishVO.setPlaceNo(place.getNo());
            placeDetailResponse.setWished(userService.isWishedPlace(wishVO));
		}
        return ResponseEntity.ok(placeDetailResponse);
	}

    @RequestMapping(value = "/place/{placeNo}/reviews", produces = "application/json;charset=UTF-8")
    public ResponseEntity<List<ReviewResponse>> getReviewsOfPlace(@PathVariable(value = "placeNo") int placeNo) {
        PlaceVO place = new PlaceVO();
        place.setNo(placeNo);
        List<ReplyVO> replies =  replyService.selectReplies(place);
        List<ReviewResponse> reviewListResponse  = new LinkedList<>(replies.stream().map(ReviewResponse::from).toList());
        return ResponseEntity.ok(reviewListResponse);
    }
    @Transactional
	@PostMapping(value = "/place")
	public ResponseEntity<?> insertPlace(@RequestParam(value = "imgs", required = false) MultipartFile[] imgs,@RequestParam("placeName") String placeName, @RequestParam("address") String address, @RequestParam("detailAddress") String detailAddress, @RequestParam("content") String content, @RequestParam("category") String category, HttpServletRequest req) {
		String id = (String)req.getAttribute("auth.userId");
		if(id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401
                .body(Map.of(
                        "message", "로그인이 필요합니다."
                ));
        PlaceVO vo = new PlaceVO();
		vo.setUserId(id);
        vo.setTitle(placeName);
        vo.setCategory(category);
        vo.setContent(content);
		vo.setGuAddress(extractGuAddress(address));
		vo.setAfterGuAddress(address.replace(vo.getGuAddress(),"").trim());
        vo.setDetailAddress(detailAddress);
        if(imgs == null) imgs = new MultipartFile[0];
        int placeNo = placeService.insertPlace(vo, imgs);

		return ResponseEntity.ok(Map.of("message", "Place-posting Success", "placeNo", placeNo));
	}

	@DeleteMapping(value = "/place/{placeNo}")
	public ResponseEntity<?> deletePlace(@PathVariable("placeNo") int placeNo, HttpServletRequest req) {
        PlaceVO vo = new PlaceVO();
        vo.setNo(placeNo);
		String id = (String) req.getAttribute("auth.userId");
		if(id==null)
			return ResponseEntity.status(Response.SC_UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        else if(!placeService.deletePlace(vo, id))
            return ResponseEntity.status(Response.SC_BAD_REQUEST).body(Map.of("message", "잘못된 접근입니다."));
		return ResponseEntity.ok(Map.of("message", "Delete Done"));
	}

	@RequestMapping(value = "/image/{image_title:.+}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getImageFromS3(@PathVariable String image_title) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(awsProps.getS3Bucket())
                .key("image/" + image_title)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(objectBytes.asByteArray());
    }

}
