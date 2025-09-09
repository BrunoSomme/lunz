# objekterkennung.py
import shutil
import os
from ultralytics import YOLO

model = YOLO("yolov8n.pt") 

def objekterkennung(temp_path, result_path):

    results = model(temp_path)

    #SPÄTER WIEDER REIN MACHEN
    #boxes = results[0].boxes
    #results[0].boxes = boxes[boxes.conf > 0.4]
    results[0].save(filename=result_path)

    os.remove(temp_path)

    return [model.names[int(cls)] for cls in results[0].boxes.cls]