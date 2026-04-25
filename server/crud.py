from sqlalchemy.orm import Session, joinedload
from sqlalchemy import desc, asc, func, delete
from sqlalchemy.exc import IntegrityError
import bcrypt
import models
import schemas
from typing import List, Optional

# --- Функции для работы с Пользователями (User) ---

def get_user(db: Session, user_id: int):
    return db.query(models.User).filter(models.User.id == user_id).first()

def get_user_by_username(db: Session, username: str):
    return db.query(models.User).filter(models.User.username == username).first()

def get_user_by_device_id(db: Session, device_id: str):
    return db.query(models.User).filter(models.User.last_device_id == device_id).first()

def get_users(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.User).offset(skip).limit(limit).all()

def create_user(db: Session, user: schemas.UserCreate):
    password_bytes = user.password.encode('utf-8')
    salt = bcrypt.gensalt()
    hashed_password = bcrypt.hashpw(password_bytes, salt).decode('utf-8')
    
    school = user.school.strip() if user.school else None

    db_user = models.User(
        username=user.username, 
        hashed_password=hashed_password, 
        school=school,
        grade=user.grade
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

def authenticate_user(db: Session, username: str, password: str, device_id: Optional[str] = None):
    user = get_user_by_username(db, username)
    if not user:
        return None
    
    password_bytes = password.encode('utf-8')
    hashed_bytes = user.hashed_password.encode('utf-8')
    if not bcrypt.checkpw(password_bytes, hashed_bytes):
        return None

    # Если аутентификация прошла успешно, обновляем device_id
    if device_id:
        existing_user = get_user_by_device_id(db, device_id)
        if existing_user and existing_user.id != user.id:
            existing_user.last_device_id = None
            db.add(existing_user)

        user.last_device_id = device_id
        db.add(user)
        db.commit()
        db.refresh(user)
        
    return user

def logout_user(db: Session, user_id: int):
    user = get_user(db, user_id)
    if user:
        user.last_device_id = None
        db.add(user)
        db.commit()
        return True
    return False

def update_user(db: Session, user_id: int, user_update: schemas.UserUpdate):
    db_user = get_user(db, user_id)
    if not db_user:
        return None

    update_data = user_update.dict(exclude_unset=True)

    if "password" in update_data and update_data["password"]:
        password = update_data["password"]
        password_bytes = password.encode('utf-8')
        salt = bcrypt.gensalt()
        hashed_password = bcrypt.hashpw(password_bytes, salt).decode('utf-8')
        db_user.hashed_password = hashed_password
    
    if "username" in update_data and update_data["username"]:
        db_user.username = update_data["username"]
        
    if "school" in update_data:
        school = update_data["school"]
        db_user.school = school.strip() if school else None

    if "grade" in update_data:
        db_user.grade = update_data["grade"]

    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

# --- Функции для работы с Конспектами (Note) ---

def get_note(db: Session, note_id: int):
    return db.query(models.Note).options(
        joinedload(models.Note.author), 
        joinedload(models.Note.upvoted_by_users)
    ).filter(models.Note.id == note_id).first()

def get_notes(
    db: Session, 
    user_id: Optional[int] = None,
    skip: int = 0, 
    limit: int = 100, 
    sort: str = "newest",
    subject: Optional[str] = None,
    topic: Optional[str] = None,
    school: Optional[str] = None
):
    query = db.query(models.Note).options(joinedload(models.Note.author), joinedload(models.Note.upvoted_by_users))

    # 1. Фильтрация
    if subject:
        query = query.filter(models.Note.subject == subject)
    if topic:
        query = query.filter(models.Note.topic.contains(topic))
    if school:
        query = query.join(models.Note.author).filter(models.User.school == school)

    # 2. Сортировка
    if sort == "popular":
        query = query.outerjoin(models.upvotes).group_by(models.Note.id).order_by(func.count(models.upvotes.c.user_id).desc())
    elif sort == "unpopular":
        query = query.outerjoin(models.upvotes).group_by(models.Note.id).order_by(func.count(models.upvotes.c.user_id).asc())
    elif sort == "oldest":
        query = query.order_by(asc(models.Note.created_at))
    else:  # "newest" or default
        query = query.order_by(desc(models.Note.created_at))

    # 3. Пагинация и выполнение
    return query.offset(skip).limit(limit).all()

def create_note_with_images(db: Session, note: schemas.NoteCreate, user_id: int, image_urls: List[str]):
    db_note = models.Note(**note.dict(), author_id=user_id)
    db.add(db_note)
    db.commit()
    db.refresh(db_note)

    for url in image_urls:
        db_image = models.NoteImage(url=url, note_id=db_note.id)
        db.add(db_image)

    db.commit()
    db.refresh(db_note)
    return db_note

def toggle_note_upvote(db: Session, note_id: int, user_id: int):
    print(f"DEBUG: Upvote request for note_id: {note_id} from user_id: {user_id}")
    
    try:
        # Ищем существующий лайк
        existing_upvote_query = db.query(models.upvotes).filter(
            models.upvotes.c.user_id == user_id,
            models.upvotes.c.note_id == note_id
        )
        existing_upvote = db.execute(existing_upvote_query).first()

        if existing_upvote:
            print(f"DEBUG: User {user_id} already upvoted note {note_id}. Removing upvote.")
            delete_stmt = delete(models.upvotes).where(
                models.upvotes.c.user_id == user_id,
                models.upvotes.c.note_id == note_id
            )
            db.execute(delete_stmt)
            is_upvoted = False
        else:
            print(f"DEBUG: User {user_id} has not upvoted note {note_id}. Adding upvote.")
            insert_stmt = models.upvotes.insert().values(user_id=user_id, note_id=note_id)
            db.execute(insert_stmt)
            is_upvoted = True
        
        db.commit()
        print("DEBUG: Commit successful.")

        # Считаем актуальное количество
        count = db.query(models.upvotes).filter(models.upvotes.c.note_id == note_id).count()
        
        print(f"DEBUG: Returning upvote response. Count: {count}, is_upvoted: {is_upvoted}")
        return schemas.UpvoteResponse(upvotes_count=count, is_upvoted=is_upvoted)

    except Exception as e:
        print(f"ERROR: Exception during upvote toggle: {e}")
        db.rollback()
        raise e
