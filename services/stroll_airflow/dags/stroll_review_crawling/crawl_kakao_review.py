from selenium.webdriver.remote.webelement import WebElement
from selenium import webdriver
from .review import review
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.by import By

def get_review_text(review_li : WebElement, driver : webdriver.Chrome) -> str | None:
    try:
        review_p = review_li.find_element(By.CSS_SELECTOR, ":scope > div > div.area_review > div > div.review_detail > div.wrap_review > a > p")
    except Exception as e:
        # 빈 리뷰 처리
        return None
    driver.execute_script("arguments[0].click();", review_p)
    review_text = review_p.text
    return review_text

def crawl_kakao_review(driver : webdriver.Chrome, place_no : str, place_name : str, address : str) -> list[review]:
    place_review_list = []
    try:
        driver.implicitly_wait(5) 
        driver.get(f"https://map.kakao.com/")
        input_element = driver.find_element(By.CSS_SELECTOR, "#search\\.keyword\\.query")
        input_element.send_keys(f"{address} {place_name}") 
        input_element.send_keys(Keys.ENTER)
        first_searched_place_link = driver.find_element(By.CSS_SELECTOR, "#info\\.search\\.place\\.list > li:nth-child(1) > div.info_item > div.contact.clickArea > a.moreview") #에러 발생
        href = first_searched_place_link.get_attribute("href")
        driver.get(href + "#review")
        review_ul = []
        try:
            review_ul = driver.find_element(By.CSS_SELECTOR, "#mainContent ul.list_review")
        except Exception as e:
            print(f"{place_no} {place_name}은(는) 카카오 리뷰가 없습니다.")
            return place_review_list
        review_li_list = review_ul.find_elements(By.CSS_SELECTOR, ":scope > li")
        for review_li in review_li_list:
            review_text = get_review_text(review_li, driver)
            if(review_text is None):
                continue
            print(review_text)
            place_review_list.append(review(place_no, place_name, address, review_text))
        print(f"{place_no} {place_name} 리뷰 크롤링 성공")
    except Exception as e:
        print(f"{place_name} 리뷰 크롤링 실패: {e}")
    finally:
        driver.switch_to.default_content()
        return place_review_list
