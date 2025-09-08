# main.py
from fastapi import FastAPI, UploadFile, Form, Depends, HTTPException, status
from fastapi.responses import FileResponse, JSONResponse
from fastapi.staticfiles import StaticFiles
from sqlalchemy.orm import Session
import shutil
from database import get_db
import models
from objekterkennung import objekterkennung
import time
import string
import random

#uvicorn main:app --reload --host 0.0.0.0 --port 8000

app = FastAPI(debug=True)
app.mount("/data", StaticFiles(directory="data"), name="data")

#zusätzliche variable: speichern oder nicht!!!!!
@app.post("/upload")
async def upload_image(file: UploadFile = None, db: Session = Depends(get_db)):

    print("errrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr", file)
    user_id = "Nhg3fmoPAO0O"
    ts = time.time()
    temp_path = f"data/temp/{user_id}_{ts}.jpg"
    result_path = f"data/results/{user_id}_{ts}_rslt.jpg" 

    #temp Datei speichern
    with open(temp_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)


    # KI zeug
    category = objekterkennung(temp_path, result_path)

    # In db speichern
    image_entry = models.Image(
        user_id=user_id,
        timestamp=ts,
        category=category,
        result_path=result_path,
    )
    db.add(image_entry)
    db.commit()
    db.refresh(image_entry)

    return {
    #    "id": image_entry.id,
    #    "category": category,
    #    "timestamp": str(image_entry.timestamp),
        "result_url": result_path
    }

@app.get("/gallery/{user_id}")
def get_gallery(user_id: str, db: Session = Depends(get_db)):
    images = (
        db.query(models.Image)
        .filter(models.Image.user_id == user_id)
        .order_by(models.Image.timestamp.desc())
        .limit(10)
        .all()
    )

    return [
        {
            "id": img.id,
            "category": img.category,
            "timestamp": str(img.timestamp),
            "result_url": f"{img.result_path}"
        }
        for img in images
    ]


'''
@app.get("/image/{image_id}")
def get_image(image_id: int, db: Session = Depends(get_db)):

    image = db.query(models.Image).filter(models.Image.id == image_id).first()
    if not image:
        raise HTTPException(status_code=404, detail="Image not found")

    return FileResponse(image.result_path, media_type="image/jpeg")
'''




@app.post("/signup")
def signup(name: str, password: str, db: Session = Depends(get_db)):
    # prüfen ob existierend
    existing = db.query(models.User).filter(models.User.name == name).first()
    if existing:
        raise HTTPException(status_code=400, detail="Username already exists.")

    # User anlegen
    chars = string.ascii_letters + string.digits
    while True:
        user_id = ''.join(random.choices(chars, k=12))
        exists = db.query(models.User).filter(models.User.id == user_id).first()
        if not exists:
            break

    user = models.User(id=user_id, name=name, password=password)

    db.add(user)
    db.commit()
    db.refresh(user)
    return {"message": "User created.", "user_id": user.id}




@app.post("/signin")
def signin(name: str = Form(...), password: str = Form(...), db: Session = Depends(get_db)):

    # Passwort prüfen
    user = db.query(models.User).filter(models.User.name == name).first()
    if user and user.password == password:
        return {"user_id": user.id}
    else:
        raise HTTPException(status_code=400, detail="Falscher Username or Passwort.")

