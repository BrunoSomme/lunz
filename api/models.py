import string
import random
from sqlalchemy import Column, Integer, String, Float, ForeignKey
from sqlalchemy.orm import relationship, declarative_base
import time

Base = declarative_base()

class User(Base):
    __tablename__ = "users"

    id = Column(String(12), primary_key=True)  # Auto-generiert, muss nicht übergeben werden
    name = Column(String, nullable=False, unique=True)
    password = Column(String, nullable=False)

    images = relationship("Image", back_populates="user")

class Image(Base):
    __tablename__ = "images"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(String(12), ForeignKey("users.id"))
    timestamp = Column(Float, default=lambda: time.time())
    category = Column(String, nullable=True)
    result_path = Column(String, nullable=False)

    user = relationship("User", back_populates="images")
