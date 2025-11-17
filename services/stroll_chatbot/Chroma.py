import pymysql
from dotenv import load_dotenv
import os
from langchain_chroma import Chroma
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_core.documents import Document
import json
from langchain_text_splitters import RecursiveCharacterTextSplitter


chroma = None;
load_dotenv()

def init_chroma():
    global chroma
    conn = pymysql.connect(
        host=os.getenv("RDS_HOST"),
        user=os.getenv("RDS_USER"),
        password=os.getenv("RDS_PASSWORD"),
        database=os.getenv("RDS_DATABASE"),
        port=int(os.getenv("RDS_PORT")),
    )

    cursor = conn.cursor()
    cursor.execute("SELECT * FROM place")
    places = cursor.fetchall()
    
    # 중복 제거를 위해 place_no를 키로 사용
    unique_places = {}
    for place in places:
        place_no = place[0]
        if place_no not in unique_places:
            unique_places[place_no] = place
    
    place_obj_list = []
    for place in unique_places.values():
        # 스키마 매핑:
        # place_no=0, title=1, content=2, category=3, written_date=4
        # gu_address=5, after_gu_address=6, detail_address=7
        # x=8, y=9, user_id=10, pet_type=11
        
        # 구 주소 정규화 (메타데이터 필터링을 위해 일관성 확보)
        gu_raw = place[5]  # gu_address
        # "서울 성동구" -> "성동구", "서울특별시 성동구" -> "성동구"
        gu_normalized = gu_raw.replace("서울 ", "").replace("서울특별시 ", "")
        
        # 주소 생성
        address_parts = [gu_raw]
        if place[6]:  # after_gu_address
            address_parts.append(place[6])
        if place[7]:  # detail_address
            address_parts.append(place[7])
        full_address = " ".join(address_parts)
        
        place_obj_list.append({
            "data": {
                "장소(시설) 이름": place[1],  # title
                "장소(시설) 설명": place[11],  # content
                "펫 종류": place[12],
                "장소(시설) 주소": full_address,
                "카테고리": place[3],
                # "경도": place[8],  # x
                # "위도": place[9],  # y
            },
            "metadata": {
                "category": place[3],  # category
                "gu_address": gu_normalized,
                "pet_type": place[11] if place[11] else None,  # pet_type (nullable)
            }
        })
    
    # data_str = "\n".join(list(map(json.dumps, place_obj_list)))
    # text_splitter = RecursiveCharacterTextSplitter(separators=["\n"], chunk_size=1000, chunk_overlap=200)
    # texts = text_splitter.split_text(data_str)
    doc_page_contents = [json.dumps(place_obj["data"]) for place_obj in place_obj_list]
    doc_metadata = [place_obj["metadata"] for place_obj in place_obj_list]
    documents = [Document(page_content=doc_page_contents[i], metadata=doc_metadata[i]) for i in range(len(doc_page_contents))]

    
    # documents = [Document(json.dumps(place_obj["data"]), metadata=place_obj["metadata"]) for place_obj in place_obj_list]
    # print(documents[0].page_content)
    chroma = Chroma.from_documents(documents=documents, embedding=HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2", cache_folder="embedding_cache", model_kwargs={'trust_remote_code': True}), persist_directory="chroma_db")
    # chroma.persist()
def load_chroma():
    global chroma
    if(chroma is None):
        chroma = Chroma(persist_directory="chroma_db", embedding_function=HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2", cache_folder="embedding_cache", model_kwargs={'trust_remote_code': True}))
    return chroma

# init_chroma()
load_chroma()


    