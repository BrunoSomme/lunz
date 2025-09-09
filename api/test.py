# client_test.py
import requests
import json
from datetime import datetime

# Server-IP deines Raspberry Pi einsetzen:
BASE_URL = "http://192.168.178.49:8000"
USER_ID = ''

# 1. Signup:
def signup():
    global USER_ID 
    r = requests.post(f"{BASE_URL}/signup", data={"name":"pascal","password":"pw"})
    if r.status_code==200:
        resp = r.json() 
        user_id = resp["user_id"]
        USER_ID = user_id
        print("User created. User ID:", user_id)
    else:
        print(r.status_code)

#2 Signin
def signin():
    global USER_ID 
    r = requests.post(f"{BASE_URL}/signin", data={"name":"pascal","password":"pw"})
    if r.status_code==200:
        resp = r.json() 
        user_id = resp["user_id"]
        USER_ID = user_id
        print("Signed in. User ID:", user_id)
    else:
        print(r.status_code)
    

# 3. Bild hochladen
def upload():
    global USER_ID 
    with open("test.jpg", "rb") as f:
        files = {"file": ("test.jpg", f, "image/jpeg")}
        data = {"user_id": USER_ID}
        r = requests.post(f"{BASE_URL}/upload", files=files, data=data)

    if r.status_code==200:
        print("Upload erfolgreich.") 
        resp = r.json() 
        print(f"Im Bild mit der ID {resp['id']} "
        f"wurde(n) das/die Objekt(e) {', '.join(json.loads(resp['category']))} gefunden "
        f"(aufgenommen am {datetime.fromtimestamp(float(resp['timestamp'])).strftime('%d.%m-%Y um %H:%M:%S')}).")
    else:
        print(r.status_code)

#4 Gallerie aufrufen
def gallery():
    global USER_ID 
    r = requests.get(f"{BASE_URL}/gallery/{USER_ID}")
    if r.status_code == 200:
        resp = r.json() 
        print(resp)

        for img in resp:
            print(f"ID: {img['id']}  Kategorie: {img['category']}  URL: {BASE_URL}/{img['result_url']}")
    else:
        print(r.status_code)

# 5. Bild löschen


def main():
    signup()
    signin()
    upload()
    #gallery()

if __name__ == "__main__":
    main()