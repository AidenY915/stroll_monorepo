from selenium import webdriver
from selenium.webdriver.common.by import By
import time
from .crawl_naver_review import crawl_naver_review
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from dotenv import load_dotenv
import os
from .crawl_kakao_review import crawl_kakao_review
import json

def load_chromedriver():
    load_dotenv()
    options = Options()
    options.add_argument("--headless=new")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--remote-debugging-port=9222")
    options.add_argument("--user-data-dir=/tmp/chrome-user-data")
    options.add_argument("--data-path=/tmp/chrome-data")
    options.add_argument("--disk-cache-dir=/tmp/chrome-cache")
    options.add_argument("--disable-gpu")          # GPU 없는 환경에서 안전장치
    options.add_argument("--disable-software-rasterizer")
    options.add_argument("--no-first-run")
    options.add_argument("--no-default-browser-check")
    options.binary_location = os.getenv("CHROME_PATH")
    # 드라이버 경로 설정 (chromedriver.exe 위치에 맞게 수정) 
    service = Service(executable_path = os.getenv("CHROMEDRIVER_PATH"))
    driver = webdriver.Chrome(options = options, service=service)
    driver.implicitly_wait(5)
    return driver

def crawl_review(tmp_path : str):
    driver = load_chromedriver()
    
    crawled_reviews_dir = os.path.join(tmp_path, "crawled-reviews")
    if(os.path.exists(crawled_reviews_dir)):
        os.remove(crawled_reviews_dir)
    os.makedirs(crawled_reviews_dir, exist_ok=True)
    
    with open(os.path.join(tmp_path, "empty_places.ndjson"), "r", encoding="utf-8") as f:
        place_obj_list = []
        for line in f:
            record = json.loads(line)
            place_no = record["place_no"]
            place_name = record["title"]
            address = record["address"]
            naver_reviews = crawl_naver_review(driver, place_no, place_name, address)
            kakao_reviews = crawl_kakao_review(driver, place_no, place_name, address)
            review_list = naver_reviews + kakao_reviews
            place_obj = {
                "place_no": place_no,
                "reviews": [review.__dict__ for review in review_list]
            }
            place_obj_list.append(place_obj)
            with open(os.path.join(crawled_reviews_dir, f"{place_no}.json"), "w", encoding="utf-8") as f:
                f.write(json.dumps(place_obj, ensure_ascii=False) + "\n")
    print(f"총 {len(place_obj_list)}개의 장소를 크롤링하였습니다.")   

def __main__():
    crawl_review()

# __main__()

