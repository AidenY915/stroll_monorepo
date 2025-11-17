from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.messages import HumanMessage, SystemMessage
from dotenv import load_dotenv
from langchain_core.output_parsers import StrOutputParser
import os
from Chroma import load_chroma
import json
import re
load_dotenv()
llm = ChatOpenAI(model="gpt-4o-mini", api_key=os.getenv("OPENAI_API_KEY"))
strOutputParser = StrOutputParser()
chroma = load_chroma()

# 서울의 구 목록 (추가 식별용)
GU_LIST = ["종로구", "중구", "용산구", "성동구", "광진구", "동대문구", "중랑구", "성북구", "강북구", "도봉구", 
           "노원구", "은평구", "서대문구", "마포구", "양천구", "강서구", "구로구", "금천구", "영등포구", "동작구", 
           "관악구", "서초구", "강남구", "송파구", "강동구"]


prompt_template = ChatPromptTemplate.from_messages([
    ("system", """this is the summary of conversation: {summary}\n
        You are a helpful assistant that you give plan and root for the day.\n
        You speak in Korean.\n
        answer the question with the following context: \n\n{context}\n\n
        the context is JSON objects about the place, so the answer should be about the place.
        Each JSON object contains:
        - "장소(시설) 이름": the name of the place
        - "장소(시설) 설명": the description of the place
        - "장소(시설) 주소": the address of the place
        - "카테고리": the category of the place
        - "펫 종류": the target type of pet
        
        CRITICAL RULES:
        1. ONLY use information that is EXACTLY stated in the context. DO NOT make up or invent any information.
        2. DO NOT create fake place names, addresses, or descriptions.
        3. If you cannot find the information in the context, explicitly say "정보를 찾을 수 없습니다" or "해당 장소의 상세 정보가 제공되지 않았습니다".
        4. If the user asks about a specific district (구), ONLY use places from that district.
        5. Ignore all places from other districts even if they seem relevant.
        6. If the context does not contain enough information to answer, acknowledge what information is missing.
        
        IMPORTANT: Never hallucinate or create information that is not in the context!"""),
    ("human", "{question}"),
])
chain = prompt_template | llm | strOutputParser

#요약과 히스토리 관리 필요.


def extract_gu_name(question):
    """
    질문에서 구 이름을 추출하는 함수
    """
    # 정확한 구 이름 매칭
    for gu in GU_LIST:
        if gu in question:
            return gu
    
    # 별칭 매핑 (필요시 추가)
    alias_map = {
        "홍대": "마포구",
        "강남": "강남구",
        "이태원": "용산구",
        "명동": "중구",
        "동대문": "동대문구",
    }
    for alias, gu_name in alias_map.items():
        if alias in question:
            return gu_name
    
    return None


def extract_pet_type(question):
    """
    질문에서 반려동물 타입을 추출하는 함수
    """
    pet_keywords = {
        "강아지": "강아지",
        "개": "강아지",
        "멍멍이": "강아지",
        "고양이": "고양이",
        "냥이": "고양이",
        "야옹이": "고양이",
        "반려견": "강아지",
        "반려묘": "고양이",
    }
    
    for keyword, pet_type in pet_keywords.items():
        if keyword in question:
            return pet_type
    
    return None


def request_llm(message):
    summary = ""
    question = message
    
    # 메시지에서 구 이름 추출
    gu_name = extract_gu_name(question)
    pet_type = extract_pet_type(question)
    
    # 검색 쿼리 개선: 지역 정보가 있으면 쿼리에 추가
    search_query = question
    if gu_name:
        # 지역 정보를 강조하기 위해 쿼리에 추가
        search_query = f"{gu_name} {question}"
        print(f"[DEBUG] 구 이름 추출: {gu_name}")
    if pet_type:
        print(f"[DEBUG] 반려동물 타입 추출: {pet_type}")
    
    # Chroma는 다중 필터를 지원하지 않으므로, 검색 후 수동 필터링
    # 먼저 지역 필터로 검색하거나, 전체 검색 후 필터링
    if gu_name and pet_type:
        # 지역과 반려동물 타입 둘 다 있을 때: 더 중요한 필터를 우선 적용
        # 지역이 더 명확하므로 지역 필터로 먼저 검색
        context_docs = chroma.similarity_search(search_query, k=20, filter={"gu_address": gu_name})
        # 반려동물 타입으로 추가 필터링
        context_docs = [doc for doc in context_docs if doc.metadata.get("pet_type") == pet_type or doc.metadata.get("pet_type") is None]
        context_docs = context_docs[:10]  # 상위 10개만
    elif gu_name:
        context_docs = chroma.similarity_search(search_query, k=10, filter={"gu_address": gu_name})
    elif pet_type:
        context_docs = chroma.similarity_search(search_query, k=10, filter={"pet_type": pet_type})
    else:
        context_docs = chroma.similarity_search(search_query, k=10)
    
    # 디버깅: 검색 결과 확인
    if context_docs:
        print(f"\n[DEBUG] 검색 결과 상위 3개:")
        for i, doc in enumerate(context_docs[:3]):
            content = json.loads(doc.page_content)
            print(f"  {i+1}. {content.get('장소(시설) 이름', 'N/A')} (메타데이터: {doc.metadata})")
    else:
        print("검색 결과가 없습니다.")
        # 검색 결과가 없으면 즉시 "모르겠다"고 응답
        return "죄송합니다. 요청하신 지역에 대한 정보를 찾을 수 없습니다."
    
    context = "\n\n".join([doc.page_content for doc in context_docs])
    
    # LLM 응답 생성
    response = chain.invoke({"summary": summary, "question": question, "context": context})
    return response


def main():
    # 테스트 케이스들
    test_queries = [
        "성동구 강아지랑 외출하고 싶어. 코스 추천해줘",
        "광진구에서 강아지랑 음식점 갔다가 카페 갈거야. 추천해줘.",
        """
        {
            "카테고리": "카페"
            "펫 종류": "강아지"
            "장소(시설) 주소": "서울 광진구"
        }
        """
        # "마포구 카페 추천"
    ]
    
    for query in test_queries:
        print(f"\n{'='*60}")
        print(f"질문: {query}")
        print('='*60)
        response = request_llm(query)
        print(f"\n응답: {response}\n")

if __name__ == "__main__":
    main()