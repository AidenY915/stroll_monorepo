import time
from selenium import webdriver

def scroll_down(driver : webdriver.Chrome):
    last_height = driver.execute_script("return document.body.scrollHeight")
    while True:
        driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
        time.sleep(0.5)
        new_height = driver.execute_script("return document.body.scrollHeight")
        if new_height == last_height:   # 더 이상 늘어나지 않으면 종료
            break
        last_height = new_height