# crud.py

from sqlalchemy.orm import Session
import models, schemas
from passlib.context import CryptContext

# Контекст для хеширования паролей
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def get_password_hash(password):
    """Хеширует пароль."""
    return pwd_context.hash(password)

# --- CRUD для Пользователей ---

def get_user(db: Session, user_id: int):
    """Получает пользователя по ID."""
    return db.query(models.User).filter(models.User.id == user_id).first()

def get_user_by_username(db: Session, username: str):
    """Получает пользователя по имени."""
    return db.query(models.User).filter(models.User.username == username).first()

def create_user(db: Session, user: schemas.UserCreate):
    """Создает нового пользователя."""
    hashed_password = get_password_hash(user.password)
    db_user = models.User(username=user.username, hashed_password=hashed_password)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

# --- CRUD для Конспектов ---

def get_notes(db: Session, skip: int = 0, limit: int = 100, grade: int = None, subject: str = None):
    """
    Получает список конспектов с возможностью фильтрации.
    """
    query = db.query(models.Note)
    if grade is not None:
        query = query.filter(models.Note.grade == grade)
    if subject is not None:
        query = query.filter(models.Note.subject == subject)
    return query.offset(skip).limit(limit).all()

def create_note(db: Session, note: schemas.NoteCreate, author_id: int):
    """
    Создает новый конспект.
    """
    db_note = models.Note(**note.dict(), author_id=author_id)
    db.add(db_note)
    db.commit()
    db.refresh(db_note)
    return db_note
