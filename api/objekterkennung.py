# objekterkennung.py
import shutil

def objekterkennung(temp_path, result_path):

    file = open(temp_path, "rb")










    with open(result_path, "wb") as buffer:
        shutil.copyfileobj(file, buffer)
    
    return "test"