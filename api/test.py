# client_test.py
import requests

# Server-IP deines Raspberry Pi einsetzen:
BASE_URL = "http://127.0.0.1:8000"
USER_ID = ''

def pretty_print_POST(req):
    print('{}\n{}\r\n{}\r\n\r\n{}'.format(
        '-----------START-----------',
        req.method + ' ' + req.url,
        '\r\n'.join('{}: {}'.format(k, v) for k, v in req.headers.items()),
        req.body,
    ))

# 1. Signup:
def signup():
    global USER_ID 
    #r = requests.post(f"{BASE_URL}/signup", data={"name":"Bruno","password":"Sommer"})
    req = requests.Request("POST", f"{BASE_URL}/signup", data={"name":"Bruno","password":"Sommer"})
    prepared = req.prepare()
    pretty_print_POST(prepared)
    #if r.status_code==200:
    #    resp = r.json() 
    #    user_id = resp["user_id"]
    #    USER_ID = user_id
    #    print("User ID:", user_id)
    #else:
    #    print(f"Error Code: {r.status_code}. User already exist8s",)

#2 Signin
def signin():
    global USER_ID 
    r = requests.post(f"{BASE_URL}/signin", data={"name":"pascal","password":"pw"})
    if r.status_code==200:
        resp = r.json() 
        user_id = resp["user_id"]
        print("User ID:", user_id)
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
        print(r.json()) 
    else:
        print(f"Error Code: {r.status_code}. No Upload available",)

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
    #signin()
    #upload()
    #gallery()

if __name__ == "__main__":
    main()
