import sqlite3

DB_PATH = "app.db"

def anzeigen():
     
    conn = sqlite3.connect(f"file:{DB_PATH}?mode=ro", uri=True)
    cur = conn.cursor()

    print("Tabellen in der DB:")
    cur.execute("SELECT name FROM sqlite_master WHERE type='table';")
    print(cur.fetchall())

    print("\nInhalt der Tabelle users:")
    for row in cur.execute("SELECT * FROM users;"):
        print(row)

    print("\nInhalt der Tabelle images:")
    for row in cur.execute("SELECT * FROM images;"):
        print(row)

    conn.close()


def deleteACHTUNG():
    from database import SessionLocal
    import models

    db = SessionLocal()

    db.query(models.User).delete()
    db.query(models.Image).delete()

    db.commit()
    db.close()



if __name__ == "__main__":
    anzeigen()
    deleteACHTUNG()