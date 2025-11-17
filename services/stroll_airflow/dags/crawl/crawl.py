import re
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.common.exceptions import NoSuchElementException
import time
import json
from urllib.parse import quote
import requests
import hashlib

from dotenv import load_dotenv
import os


CHROMEDRIVER_PATH = None
CHROME_PATH = None
driver = None
DIRECTORY_PATH = __file__[0:__file__.rfind(os.sep)]
TMP_PATH = None

def init():
    """Chrome 드라이버를 초기화하고 필요한 설정을 로드합니다."""
    global driver
    global DIRECTORY_PATH
    global CHROMEDRIVER_PATH
    global CHROME_PATH
    global TMP_PATH

    # 이미지 저장을 위한 디렉토리 생성
    if not os.path.exists('./images'):
        os.makedirs('./images')
    
    # tmp/images 디렉토리 생성
    tmp_images_path = f'{TMP_PATH}/images'
    if not os.path.exists(tmp_images_path):
        os.makedirs(tmp_images_path)
        print(f"📁 이미지 저장 디렉토리 생성: {tmp_images_path}")

    # Airflow 환경에서는 .env 파일이 /usr/local/airflow/.env에 마운트됨
    load_dotenv(DIRECTORY_PATH + os.sep + '.env', override=True)
    

    if(os.name=='posix'): # 리눅스 환경
        CHROME_PATH = os.getenv("CHROME_PATH")
        CHROMEDRIVER_PATH = os.getenv("CHROMEDRIVER_PATH")
        TMP_PATH=os.getenv("TMP_PATH")
    else:
        CHROME_PATH = DIRECTORY_PATH + os.sep + os.getenv("CHROME_PATH")
        CHROMEDRIVER_PATH = DIRECTORY_PATH + os.sep + os.getenv("CHROMEDRIVER_PATH")

    print(DIRECTORY_PATH)
    print(CHROME_PATH)
    print(CHROMEDRIVER_PATH)
    
    os.environ["XDG_RUNTIME_DIR"] = "/tmp"
    # 옵션 설정
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
    options.binary_location = CHROME_PATH
    # 드라이버 경로 설정 (chromedriver.exe 위치에 맞게 수정) 
    service = Service(executable_path=CHROMEDRIVER_PATH)
    driver = webdriver.Chrome(options = options, service=service)

def load_url_config():
    """naver_map_url.json 파일을 읽어서 URL 설정을 로드합니다."""
    config_path = DIRECTORY_PATH + os.sep + 'naver_map_url.json'
    with open(config_path, 'r', encoding='utf-8') as f:
        return json.load(f)

def generate_urls(config):
    """JSON 설정에서 모든 URL 조합을 생성합니다."""
    urls = []
    url_template = config['url_template']
    locations = config['locations']
    queries = config['queries']
    
    for location in locations:
        for category, query_list in queries.items():
            for keyword in query_list:
                # URL 생성: https://map.naver.com/p/search/강남구 강아지
                url = url_template.format(
                    location=location['name'],
                    keyword=keyword
                )
                urls.append({
                    'url': url,
                    'location': location['name'],
                    'category': category,
                    'query': f"{location['name']} {keyword}"
                })
    return urls

def download_image(image_url, place_name):
    """이미지를 다운로드하고 tmp/images에 저장합니다."""
    try:
        # URL에서 이미지 다운로드
        response = requests.get(image_url, timeout=10)
        if response.status_code == 200:
            # 파일명 생성 (URL 해시 + 장소명)
            url_hash = hashlib.md5(image_url.encode()).hexdigest()[:8]
            safe_place_name = "".join(c for c in place_name if c.isalnum() or c in (' ', '-', '_')).strip()[:30]
            filename = f"{safe_place_name}_{url_hash}.jpg"
            
            # 이미지 저장
            image_path = f'{TMP_PATH}/images/{filename}'
            with open(image_path, 'wb') as f:
                f.write(response.content)
            
            return filename
        else:
            print(f"  ⚠️ 이미지 다운로드 실패 (status: {response.status_code})")
            return None
    except Exception as e:
        print(f"  ⚠️ 이미지 다운로드 에러: {e}")
        return None

def export_ndjson(place_obj_list):
    """크롤링한 장소 데이터를 NDJSON 파일로 저장합니다."""
    with open(f'{TMP_PATH}/place_obj_list.ndjson', 'w', encoding='utf-8') as f:
        for place_obj in place_obj_list:
            f.write(json.dumps(place_obj, ensure_ascii=False) + '\n')


def crawl_single_page(url_info):
    """단일 URL에 대해 크롤링을 수행합니다."""
    url = url_info['url']
    location = url_info['location']
    category = url_info['category']
    query = url_info['query']
    
    print(f"\n{'='*80}")
    print(f"🌐 크롤링 시작")
    print(f"   위치: {location} | 카테고리: {category} | 검색어: {query}")
    print(f"   URL: {url}")
    print(f"{'='*80}")
    
    driver.switch_to.default_content()
    driver.get(url)
    time.sleep(3)
    
    place_list = []
    success_count = 0
    fail_count = 0
    
    #iframe 변경
    driver.switch_to.default_content()
    driver.switch_to.frame("searchIframe")
    while True:
        driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
        time.sleep(2)

        li_elements = driver.find_elements(By.CSS_SELECTOR, '#_pcmap_list_scroll_container > ul > li')
        print(f"📋 찾은 장소 리스트 개수: {len(li_elements)}개")
        li_len = len(li_elements)
        print(str(li_len) + "개의 li 찾음")
        for i in range(li_len):
            try:
                #iframe 변경
                driver.switch_to.default_content()
                time.sleep(0.1)
                driver.switch_to.frame("searchIframe")
                time.sleep(0.1)
                li_elements = driver.find_elements(By.CSS_SELECTOR, '#_pcmap_list_scroll_container > ul > li')
                print(str(i)+"번째 li 실행")
                li = li_elements[i]
                # 장소 클릭 시도
                try:
                    click_element = li.find_element(By.CSS_SELECTOR, 'div.qbGlu > div.ouxiq > div.ApCpt > a')
                    click_element.click()
                except NoSuchElementException:
                    try:
                        click_element = li.find_element(By.CSS_SELECTOR, 'div.zzp3_ > div.TbelT > a')
                        click_element.click()
                    except NoSuchElementException:
                        print(f"  ⚠️ 클릭할 요소를 찾을 수 없습니다.")
                        continue
                
                # iframe 전환 대기
                time.sleep(3)
                
                try:
                    # iframe 변경
                    driver.switch_to.default_content()
                    time.sleep(0.1)
                    driver.switch_to.frame("entryIframe")
                    time.sleep(2)
                    
                    # 제목과 카테고리 추출
                    title_element = driver.find_element(By.CSS_SELECTOR, "#_title > div > span.GHAhO")
                    category_element = driver.find_element(By.CSS_SELECTOR, "#_title > div > span.lnJFt")
                    title = title_element.text
                    if("입양" in title or "분양" in title or "보호" in title or "유기" in title or "냄새제거" in title or "가전" in title or "청소" in title):
                        continue
                    category = category_element.text
                    print(f"  📍 {title} - {category}")
                    
                    # 주소 토글 버튼 클릭
                    toggle_button = driver.find_element(By.CSS_SELECTOR, "div.O8qbU.tQY7D > div > a")
                    toggle_button.click()
                    time.sleep(2)
                    
                    # 주소 추출
                    try:
                        address_div = driver.find_element(By.CSS_SELECTOR, "div.O8qbU.tQY7D > div > div.Y31Sf > div:nth-child(1)")
                        address = address_div.text.replace("도로명", "").strip()
                    except NoSuchElementException:
                        address = "주소 정보 없음"
                        
                except Exception as e:
                    print(f"  ⚠️ 상세 정보 추출 실패: {e}")
                    # searchIframe으로 돌아가기
                    driver.switch_to.default_content()
                    driver.switch_to.frame("searchIframe")
                    continue
                
                # 이미지 찾기 및 다운로드
                image_filenames = []
                try:
                    # 이미지 요소 찾기 (여러 선택자 시도)
                    img_elements = driver.find_elements(By.CSS_SELECTOR, 'div.uDR4i img')
                    for img in img_elements:
                        image_url = img.get_attribute('src')
                        if image_url:
                            print(f"이미지 발견: {image_url[:80]}...")
                            image_filename = download_image(image_url, title)
                            if image_filename:
                                image_filenames.append(image_filename)
                                print(f"이미지 저장: {image_filename}")
                                
                except Exception as img_error:
                    print(f"※이미지 처리 중 오류: {img_error}")
                print(f"찾은 이미지 개수: {len(image_filenames)}개")                            
                # searchIframe으로 돌아가기
                driver.switch_to.default_content()
                time.sleep(0.1)
                driver.switch_to.frame("searchIframe")
                time.sleep(0.1)
                # 데이터 저장
                place_obj = {
                    "placeName": title,
                    "category": category,
                    "address": address,
                    "location": location,
                    "searchCategory": category,
                    "searchQuery": query,
                    "imageFile": image_filenames
                }
                place_list.append(place_obj)
                success_count += 1
                print(f"✓ [{success_count}] {title}")
                
            except Exception as e:
                fail_count += 1
                print(f"✗ 크롤링 실패 (장소 #{success_count + fail_count}): {e}")
                # searchIframe으로 돌아가기
                try:
                    driver.switch_to.default_content()
                    driver.switch_to.frame("searchIframe")
                except:
                    pass
                continue
        #iframe 변경
        driver.switch_to.default_content()
        time.sleep(0.1)
        driver.switch_to.frame("searchIframe")
        time.sleep(0.1)
        next_page_a = driver.find_element(By.CSS_SELECTOR, "#app-root > div > div.XUrfU > div.zRM9F > a:last-of-type")
        if next_page_a.get_attribute("aria-disabled") == "false":
            next_page_a.click()
            time.sleep(1)
        else:
            break
    print(f"✅ 완료 - 성공: {success_count}개, 실패: {fail_count}개")
    return place_list

def crawl(ti=None, **context):
    """네이버 지도에서 장소 데이터를 크롤링합니다."""
    init()
    
    # URL 설정 로드
    print("📖 URL 설정 파일 로드 중...")
    config = load_url_config()
    url_list = generate_urls(config)
    print(f"✅ 총 {len(url_list)}개의 URL 생성 완료")
    print(f"   - 위치: {len(config['locations'])}개")
    print(f"   - 검색어: {sum(len(queries) for queries in config['queries'].values())}개")
    
    # 모든 URL에 대해 크롤링 수행
    all_place_obj_list = []
    total_success = 0
    total_fail = 0
    
    for idx, url_info in enumerate(url_list, 1):
        print(f"\n\n진행률: [{idx}/{len(url_list)}]")
        try:
            place_list = crawl_single_page(url_info)
            all_place_obj_list.extend(place_list)
            total_success += len(place_list)
        except Exception as e:
            print(f"❌ URL 크롤링 실패: {e}")
            total_fail += 1
            continue
        export_ndjson(all_place_obj_list)
        print(f"중간 저장 완료")
    
    # 디버깅용 스크린샷 저장 (마지막 페이지)
    screenshot_path = f'{TMP_PATH}/debug_screenshot.png'
    driver.save_screenshot(screenshot_path)
    print(f"\n📸 디버그 스크린샷 저장: {screenshot_path}")
    
    # 디버깅용 HTML 저장 (마지막 페이지)
    html_path = f'{TMP_PATH}/debug_page.html'
    with open(html_path, 'w', encoding='utf-8') as f:
        f.write(driver.page_source)
    print(f"📄 디버그 HTML 저장: {html_path}")
    
    # 백업용으로 NDJSON 저장
    export_ndjson(all_place_obj_list)
    
    print(f"\n{'='*80}")
    print(f"=== 전체 크롤링 완료 ===")
    print(f"크롤링한 URL: {len(url_list)}개")
    print(f"수집된 장소: {len(all_place_obj_list)}개")
    print(f"성공: {total_success}개")
    print(f"실패: {total_fail}개")
    print(f"{'='*80}")
    
    # 종료
    driver.quit()
    
    # 장소 리스트를 XCom으로 반환 (Dynamic Task Mapping에서 사용)
    return all_place_obj_list


