# schemas.py

from pydantic import BaseModel
from datetime import datetime

# --- Схемы для Конспектов (Note) ---

class NoteBase(BaseModel):
    """
    Базовая схема для конспекта.
    Содержит общие поля, которые есть и при создании, и при чтении.
    """
    grade: int
    subject: str
    topic: str
    photo_url: str

class NoteCreate(NoteBase):
    """
    Схема для создания нового конспекта.
    Наследуется от NoteBase и не добавляет новых полей,
    но используется для валидации входных данных при создании.
    """
    pass

class Note(NoteBase):
    """
    Схема для чтения данных о конспекте.
    Наследуется от NoteBase и добавляет поля, которые генерируются сервером.
    """
    id: int
    author_id: int
    rating: float
    created_at: datetime

    class Config:
        """
        Конфигурация для Pydantic модели.
        orm_mode = True позволяет Pydantic модели работать с объектами SQLAlchemy,
        автоматически преобразуя их в JSON-совместимый формат.
        """
        orm_mode = True

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

class User(UserBase):
    """
    Схема для чтения данных о пользователе.
    """
    id: int
    rating: float
    created_at: datetime
    notes: list[Note] = []

    class Config:
        orm_mode = True
