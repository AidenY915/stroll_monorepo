from transformers import pipeline
import os
import json
import pymysql

clf = pipeline("zero-shot-classification",
    model = "MoritzLaurer/mDeBERTa-v3-base-xnli-multilingual-nli-2mil7",
    device=-1, # -1 : CPU만 사용
    truncation=True, # 문장 길이 초과 시 자르기
    max_length=512, # 문장 길이 최대 512
)

def classify_pet_type(text : str) -> list[str]:
    pet_type_result = clf(text, candidate_labels=["강아지", "고양이", "토끼", "미어캣", "페럿"])
    if(pet_type_result["scores"][1] /pet_type_result["scores"][0] > 0.8):
        return [pet_type_result["labels"][0], pet_type_result["labels"][1]]
    return [pet_type_result["labels"][0]]

def classify_category(text : str) -> str:
    category_result = clf(text, candidate_labels=["카페", "음식점", "반려동물동반 숙박", "놀이터", "미용", "동물병원", "반려견 호텔", "반려견 유치원"])
    return category_result["labels"][0]



def classify_category_and_pet_type(tmp_path: str):
    filled_contents_dir = os.path.join(tmp_path, "filled-contents")
    categories_and_pet_types_ndjson = os.path.join(tmp_path,"categories-and-pet-types.ndjson")    
    files = os.listdir(filled_contents_dir)
    with open(categories_and_pet_types_ndjson, "w", encoding="utf-8") as f:
        f.write("")
    place_info_list = []
    try:
        for file in files:
            with open(os.path.join(filled_contents_dir, file), "r", encoding="utf-8") as f:
                record = json.loads(f.read())
                if(record == {}):
                    continue
                place_no = record["place_no"]
                place_name = ""# record["place_name"]
                content = record["content"]
                place_desc = f"장소 이름: {place_name}\n\n장소 설명: {content}"
                category = classify_category(place_desc)
                pet_type = classify_pet_type(place_desc)
                place_obj = {
                    "place_no": place_no,
                    "category": category,
                    "pet_type": pet_type
                }
                place_info_list.append(place_obj)
                print(place_no, place_name, category, pet_type)
                with open(categories_and_pet_types_ndjson, "a", encoding="utf-8") as f:
                    f.write(json.dumps(place_obj, ensure_ascii=False) + "\n")
    except Exception as e:
        print(e)
    finally:
        print(f"{len(place_info_list)}개의 장소 분류 완료")

RDS_HOST = os.getenv("RDS_HOST")
RDS_PORT = os.getenv("RDS_PORT")
RDS_USER = os.getenv("RDS_USER")
RDS_PASSWORD = os.getenv("RDS_PASSWORD")
RDS_DATABASE = os.getenv("RDS_DATABASE")
# DIRECTORY = os.path.dirname(os.path.abspath(__file__))

def save_category_and_pet_type_in_rds(tmp_path : str) -> None:
    categories_and_pet_types_ndjson = os.path.join(tmp_path,"categories-and-pet-types.ndjson")    
    conn = pymysql.connect(host=RDS_HOST, port=int(RDS_PORT), user=RDS_USER, password=RDS_PASSWORD, database=RDS_DATABASE)
    cursor = conn.cursor()
    
    with open(categories_and_pet_types_ndjson, "r", encoding="utf-8") as f:
        for line in f:
            record = json.loads(line.strip())
            place_no = record["place_no"]
            category = record["category"]
            pet_type = record["pet_type"]
            print(place_no, category, pet_type)
            cursor.execute("UPDATE place SET category = %s, pet_type = %s WHERE place_no = %s", (category, pet_type, place_no))
    conn.commit()
    cursor.close()
    conn.close()
    return



def test():
    # text = text = "안녕강아지는 경기 하남시에 위치한 반려견 호텔링 및 데이케어 서비스 제공 장소입니다. 반려견과 함께하는 이용객들을 위해 다양한 서비스와 편의를 제공하고 있습니다."
    # pet_type_result = clf(text, candidate_labels=["강아지", "고양이"])
    # print(pet_type_result)
    # category_result = clf(text, candidate_labels=["카페", "음식점", "숙박", "놀이터", "미용", "동물병원"])#["반려견 호텔", "반려동물 숙박", "반려동물 미용", "동물병원", "약국", "반려동물동반 카페", "반려동물동반 식당", "애견놀이터", "애견유치원",  "기타"])
    # print(category_result)
    classify_category_and_pet_type("../tmp")

test()