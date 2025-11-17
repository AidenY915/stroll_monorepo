## 공통

- Base URL: `http://localhost:8080`
- Auth 헤더(필요 시): `Authorization: Bearer {accessToken}`

---

## 1) 인증 (Auth)

### POST `/api/auth/login`

**Request (JSON)**

```json
{ "userId": "string", "password": "string" }
```

**Response**

- 200

```json
{ "accessToken": "string" }
```

- 401

```json
{ "message": "아이디 또는 비밀번호가 올바르지 않습니다." }
```

---

### GET `/api/auth/check-id/{userId}`

**Path**

- `userId: string`

**Response (200)**

```json
{ "userId": "string", "available": true }
```

---

### POST `/api/auth/register`

**Request (JSON)**

```json
{
  "userId": "string",
  "password": "string",
  "nickname": "string",
  "email": "string"
}
```

**Response**

- 200

```json
{ "message": "Register Done" }
```

- 400

```json
{ "message": "Wrong Access" }
```

---

## 2) 장소 (Place)

### GET `/api/places`

**Query (optional)**

- `address: string`
- `keywords: string`
- `order: "distance" | "star"` (default: distance)
- `page: integer` (default: 1)
- `maxDistance: integer` (1~50, _100m 단위_)
- `minStar: integer` (1~5)
- `x: double` (경도), `y: double` (위도)

**Response (200)**

```json
{
  "places": [{ "placeNo": 1, "name": "string", "star": 4.5, "distance": 1234 }],
  "lastPage": 5
}
```

---

### GET `/api/place/{placeNo}`

**Path**

- `placeNo: integer`

**Response (200)**

```json
{
  "placeNo": 1,
  "name": "string",
  "content": "string",
  "createdAt": "timestamp",
  "address": "string",
  "userId": "string",
  "distance": 1234.5,
  "star": 4.5,
  "isWished": true,
  "imgs": ["string"]
}
```

---

### GET `/api/place/{placeNo}/reviews`

**Path**

- `placeNo: integer`

**Response (200)**

```json
[
  {
    "userId": "string",
    "userNickname": "string",
    "content": "string",
    "star": 5,
    "createdAt": "timestamp"
  }
]
```

---

### POST `/api/place` (multipart/form-data)

**Form Data**

- `imgs: file[]` (optional)
- `placeName: string`
- `address: string`
- `detailAddress: string`
- `content: string`
- `category: string`

**Response**

- 200

```json
{ "message": "Place-posting Success", "placeNo": 123 }
```

- 401

```json
{ "message": "로그인이 필요합니다." }
```

---

### DELETE `/api/place/{placeNo}`

**Path**

- `placeNo: integer`

**Response**

- 200

```json
{ "message": "Delete Done" }
```

- 401

```json
{ "message": "로그인이 필요합니다." }
```

- 400

```json
{ "message": "잘못된 접근입니다." }
```

---

### GET `/api/image/{image_title}`

**Path**

- `image_title: string` (예: `123_1.jpg`)

**Response**

- 200: `image/jpeg` (바이너리)

---

## 3) 사용자 (User)

### GET `/api/users/{userId}/wishlist`

**Path**

- `userId: string`

**Response**

- 200

```json
{
  "wishlist": [
    { "placeNo": 1, "name": "string", "star": 4.5, "distance": 1234 }
  ]
}
```

- 401

```json
{ "message": "로그인이 필요합니다." }
```

- 403

```json
{ "message": "접근 권한이 없습니다." }
```

---

### POST `/api/users/{userId}/wishlist`

**Path**

- `userId: string`

**Request (JSON)**

```json
{ "placeNo": 123 }
```

**Response**

- 200

```json
{ "message": "찜 목록에 추가되었습니다." }
```

- 400

```json
{ "message": "placeNo가 필요합니다." }
```

- 401

```json
{ "message": "로그인이 필요합니다." }
```

- 403

```json
{ "message": "접근 권한이 없습니다." }
```

---

### DELETE `/api/users/{userId}/wishlist/{placeNo}`

**Path**

- `userId: string`
- `placeNo: integer`

**Response**

- 200

```json
{ "message": "찜 목록에서 삭제되었습니다." }
```

- 401

```json
{ "message": "로그인이 필요합니다." }
```

- 403

```json
{ "message": "접근 권한이 없습니다." }
```

---

### GET `/api/users/{userId}/places`

**Path**

- `userId: string`

**Response**

- 200

```json
{
  "places": [{ "placeNo": 1, "name": "string", "star": 4.5, "distance": 1234 }]
}
```

- 401

```json
{ "message": "로그인이 필요합니다." }
```

- 403

```json
{ "message": "접근 권한이 없습니다." }
```

---

### GET `/api/users/{userId}/reviews`

**Path**

- `userId: string`

**Response**

- 200

```json
{
  "reviews": [
    {
      "userId": "string",
      "userNickname": "string",
      "content": "string",
      "star": 5,
      "createdAt": "timestamp"
    }
  ]
}
```

- 401

```json
{ "message": "로그인이 필요합니다." }
```

- 403

```json
{ "message": "접근 권한이 없습니다." }
```

---

### DELETE `/api/users/{userId}`

**Path**

- `userId: string`

**Request (JSON)**

```json
{ "password": "string" }
```

**Response**

- 200

```json
{ "message": "회원 탈퇴가 완료되었습니다." }
```

- 400

```json
{ "message": "비밀번호가 필요합니다." }
```

- 401

```json
{ "message": "로그인이 필요합니다." }
```

- 401

```json
{ "message": "비밀번호가 올바르지 않습니다." }
```

- 403

```json
{ "message": "접근 권한이 없습니다." }
```

---

## 4) 리뷰 (Review/Reply)

### POST `/api/place/{placeNo}/reviews`

**Path**

- `placeNo: integer`

**Request (JSON)**

```json
{ "content": "string", "star": 1 }
```

**Response**

- 200

```json
{ "message": "리뷰가 작성되었습니다." }
```

- 400

```json
{ "message": "content와 star가 필요합니다." }
```

- 401

```json
{ "message": "로그인이 필요합니다." }
```

---

### DELETE `/api/place/{placeNo}/reviews/{reviewNo}`

**Path**

- `placeNo: integer`
- `reviewNo: integer`

**Response**

- 200

```json
{ "message": "리뷰가 삭제되었습니다." }
```

- 401

```json
{ "message": "로그인이 필요합니다." }
```
