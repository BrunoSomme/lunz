from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, create_engine, Float
from sqlalchemy.orm import relationship, declarative_base, sessionmaker
import time
import os

Base = declarative_base()

# Tabellen
class User(Base):
    __tablename__ = "users"

    id = Column(String(12), primary_key=True)
    name = Column(String, nullable=False)
    password = Column(String, nullable=False)


class Image(Base):
    __tablename__ = "images"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    timestamp = Column(Float, default=time.time)
    category = Column(String, nullable=True)
    result_path = Column(String, nullable=False)

    user = relationship("User", back_populates="images")

DATABASE_URL = "sqlite:///./app.db"
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

if __name__ == "__main__":
    os.makedirs("data/temp", exist_ok=True) #Speicherort temporäre Bilder (zwischen empfangen und durch KI schicken)
    os.makedirs("data/results", exist_ok=True) #Speicherort "Galerie"
    print("paths created")
    Base.metadata.create_all(bind=engine)
    print("db created")
