class review: 
    def __init__(self, place_no, place_name, address, review_text):
        self.place_no = place_no
        self.place_name = place_name
        self.address = address
        self.review_text = review_text
        

    def __str__(self):
        return f"place_name: {self.place_name}, address: {self.address}, review_text: {self.review_text}, place_no: {self.place_no}"
    def __repr__(self):
        return self.__str__()
    