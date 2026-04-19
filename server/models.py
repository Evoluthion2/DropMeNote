# models.py

from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from database import Base

class User(Base):
    """
    Модель пользователя.
    """
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    rating = Column(Float, default=0.0)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    notes = relationship("Note", back_populates="author")

class Note(Base):
    """
    Модель конспекта.
    """
    __tablename__ = "notes"

    id = Column(Integer, primary_key=True, index=True)
    grade = Column(Integer, nullable=False)
    subject = Column(String, nullable=False)
    topic = Column(String, nullable=False)
    rating = Column(Float, default=0.0)
    photo_url = Column(String, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    author_id = Column(Integer, ForeignKey("users.id"))

    author = relationship("User", back_populates="notes")
