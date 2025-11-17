from transformers import pipeline
import pymysql
import re

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

def extract_gu_name(text : str) -> str:
    pattern = r"\s([가-힣]+)구" 
    words = []
    match = re.search(pattern, text)
    if match:
        groups = match.groups()
        for group in groups:
            words.append(group)
        return words
    return [text]

def extract_pet_type(text : str) -> str:
    patterns = [r"\s([가-힣]+)와", r"\s([가-힣]+)과", r"\s([가-힣]+)이", r"\s([가-힣]+)랑", r"\s([가-힣]+)를", r"\s([가-힣]+)을"]
    words = []
    for pattern in patterns:
        match = re.search(pattern, text)
        if(match is None):
            continue
        groups = match.groups()
        for group in groups:
            words.append(group)
    return words

def extract_category(text : str) -> str:
    patterns = [r"\s([가-힣]+)로", r"\s([가-힣]+)에서", r"\s([가-힣]+)을", r"\s([가-힣]+)를"]
    words = []
    for pattern in patterns:
        match = re.search(pattern, text)
        if(match is None):
            continue
        groups = match.groups()
        for group in groups:
            words.append(group)
    return words

def filter_gu_name(gu_name_list : list[str]) -> list[str]:
    clf = pipeline("zero-shot-classification",
        model = "MoritzLaurer/mDeBERTa-v3-base-xnli-multilingual-nli-2mil7",
        device=-1, # -1 : CPU만 사용
        truncation=True, # 문장 길이 초과 시 자르기
        max_length=512, # 문장 길이 최대 512
    )
    filtered_gu_name_list = []
    for gu_name in gu_name_list:
        rslt = clf(gu_name, candidate_labels=["종로구", "중구", "용산구", "성동구", "광진구", "동대문구", "중랑구", "성북구", "강북구", "도봉구"])
        if rslt["scores"][0] > 0.9:
            filtered_gu_name_list.append(rslt["labels"][0])
    filtered_gu_name_list = list(set(filtered_gu_name_list))
    return filtered_gu_name_list

def preprocess_prompt(text : str) -> str:
    gu_name_list = extract_gu_name(text)
    gu_name_list = filter_gu_name(gu_name_list)
    pet_type_list = extract_pet_type(text)
    pet_type_candidate = ", ".join(pet_type_list) if len(pet_type_list) > 0 else text
    pet_type = classify_pet_type(pet_type_candidate)
    category_list = extract_category(text)  
    category_candidate = ", ".join(category_list) if len(category_list) > 0 else text
    category = classify_category(category_candidate)
    return {
        "gu_name": gu_name_list,
        "pet_type": pet_type,
        "category": category
    }


print(preprocess_prompt("종로구에 강아지와 놀러가고 싶어요. 카페 추천해줘."))

