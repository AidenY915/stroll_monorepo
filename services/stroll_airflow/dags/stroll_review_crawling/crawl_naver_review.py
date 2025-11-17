from selenium import webdriver
from selenium.webdriver.common.by import By
import time
from .util import scroll_down
from .review import review

def get_review_text(review_li, driver):
    review_a = review_li.find_element(By.CSS_SELECTOR, "div.pui__vn15t2 > a:nth-child(1)")
    driver.execute_script("arguments[0].click();", review_a)
    # time.sleep(1)
    # review_a.click() #Selenium으로 직접 클릭하려면 viewport로 보여야 함. JS로 처리하는 것이 훨씬 에러 발생률 적음
    review_text = review_a.text
    return review_text

def crawl_naver_review(driver, place_no, place_name, address):
    place_review_list = []
    try:
        driver.implicitly_wait(5) 
        driver.get(f"https://map.naver.com/p/search/{address} {place_name}")
        driver.switch_to.frame("entryIframe")
        a_list = driver.find_elements(By.CSS_SELECTOR, "#app-root > div > div > div.place_fixed_maintab > div > div > div > div > a")
        for a in a_list:
            if '리뷰' in a.text:
                a.click()
                break
        scroll_down(driver)
        review_ul = driver.find_element(By.CSS_SELECTOR, "#_review_list")
        time.sleep(1)
        scroll_down(driver)
        review_li_list = review_ul.find_elements(By.CSS_SELECTOR, "li")
        for review_li in review_li_list:
            review_text = get_review_text(review_li, driver)
            print(review_text)
            place_review_list.append(review(place_no, place_name, address, review_text))
        print(f"{place_no} {place_name} 리뷰 크롤링 성공")
    except Exception as e:
        print(f"{place_name} 리뷰 크롤링 실패: {e}")
    finally:
        driver.switch_to.default_content()
        return place_review_list

