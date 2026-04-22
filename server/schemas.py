# schemas.py

from pydantic import BaseModel, validator
from datetime import datetime
from typing import List, Any, Optional

# --- Схемы для Изображений ---

class NoteImageBase(BaseModel):
    url: str

class NoteImageCreate(NoteImageBase):
    pass

class NoteImage(NoteImageBase):
    id: int
    note_id: int

    class Config:
        orm_mode = True

# --- Схемы для Конспектов (Note) ---

class NoteBase(BaseModel):
    """
    Базовая схема для конспекта.
    """
    grade: int
    subject: str
    topic: str

class NoteCreate(NoteBase):
    """
    Схема для создания нового конспекта.
    """
    pass

class Note(NoteBase):
    """
    Схема для чтения данных о конспекте из БД.
    """
    id: int
    created_at: datetime
    author: Any  # Принимаем объект User
    images: List[NoteImage] = []

    class Config:
        orm_mode = True

# --- Новая схема ответа для Android ---

class NoteResponse(BaseModel):
    """
    Схема для ответа клиенту, полностью соответствующая ожиданиям Android.
    """
    id: int
    grade: int
    subject: str
    topic: str
    author: str
    date: str
    images: List[str]
    upvotes_count: int
    is_upvoted: bool

    class Config:
        orm_mode = True

# --- Схемы для Апвоутов ---

class UpvoteResponse(BaseModel):
    upvotes_count: int
    is_upvoted: bool

# --- Схемы для Пользователей (User) ---

class UserBase(BaseModel):
    """
    Базовая схема для пользователя.
    """
    username: str

class UserCreate(UserBase):
    """
    Схема для создания нового пользователя.
    """
    password: str
    school: Optional[str] = None

class User(UserBase):
    """
    Схема для чтения данных о пользователе (полная).
    """
    id: int
    school: Optional[str]
    rating: float
    created_at: datetime
    notes: list[Note] = []

    class Config:
        orm_mode = True

class UserResponse(BaseModel):
    """
    Схема для ответа при аутентификации.
    Содержит только необходимые для клиента поля.
    """
    id: int
    username: str
    school: Optional[str]

    class Config:
        orm_mode = True
