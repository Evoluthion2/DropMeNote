# models.py

from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey, Table
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from database import Base

# Вспомогательная таблица для связи "многие-ко-многим" между пользователями и лайкнутыми конспектами
upvotes = Table('upvotes', Base.metadata,
    Column('user_id', Integer, ForeignKey('users.id'), primary_key=True),
    Column('note_id', Integer, ForeignKey('notes.id'), primary_key=True)
)

class User(Base):
    """
    Модель пользователя.
    """
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    school = Column(String, nullable=True)
    rating = Column(Float, default=0.0)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    last_device_id = Column(String, nullable=True, index=True)

    notes = relationship("Note", back_populates="author")
    upvoted_notes = relationship("Note", secondary=upvotes, back_populates="upvoted_by_users")

class Note(Base):
    """
    Модель конспекта.
    """
    __tablename__ = "notes"

    id = Column(Integer, primary_key=True, index=True)
    grade = Column(Integer, nullable=False)
    subject = Column(String, nullable=False)
    topic = Column(String, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    author_id = Column(Integer, ForeignKey("users.id"))

    author = relationship("User", back_populates="notes")
    upvoted_by_users = relationship("User", secondary=upvotes, back_populates="upvoted_notes")
    images = relationship("NoteImage", back_populates="note", cascade="all, delete-orphan")

class NoteImage(Base):
    """
    Модель для хранения изображений конспекта.
    """
    __tablename__ = "note_images"

    id = Column(Integer, primary_key=True, index=True)
    url = Column(String, nullable=False)
    note_id = Column(Integer, ForeignKey("notes.id"))

    note = relationship("Note", back_populates="images")
