from fastapi import FastAPI, UploadFile, File, Form, HTTPException, Depends, Query, Request
from fastapi.staticfiles import StaticFiles
from sqlalchemy.orm import Session
import os
import uuid
import shutil
from typing import List, Optional

# Импортируем модели, схемы и настройки БД
import models
import schemas
import crud
from database import SessionLocal, engine

# Создаем таблицы в базе данных
models.Base.metadata.create_all(bind=engine)

app = FastAPI()

# --- Middleware для логирования ---
@app.middleware("http")
async def log_requests(request: Request, call_next):
    print(f"DEBUG: Incoming request {request.method} to {request.url}")
    response = await call_next(request)
    return response

# Папка для загрузок
UPLOAD_DIR = "uploads"
if not os.path.exists(UPLOAD_DIR):
    os.makedirs(UPLOAD_DIR)

# Примонтируем папку со статикой
app.mount("/static", StaticFiles(directory="."), name="static")


# --- Зависимость для получения сессии БД ---
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# --- ЭНДПОИНТЫ ДЛЯ АУТЕНТИФИКАЦИИ ---

@app.post("/register", response_model=schemas.UserResponse)
async def register(user: schemas.UserCreate, db: Session = Depends(get_db)):
    print(f"DEBUG: Registering user with data: {user.dict()}")
    db_user = crud.get_user_by_username(db, username=user.username)
    if db_user:
        raise HTTPException(status_code=400, detail="Username already registered")
    return crud.create_user(db=db, username=user.username, password=user.password, school=user.school)

@app.post("/login", response_model=schemas.UserResponse)
async def login(
    username: str = Form(...), 
    password: str = Form(...), 
    device_id: str = Form(...),
    db: Session = Depends(get_db)
):
    user = crud.authenticate_user(db, username=username, password=password, device_id=device_id)
    if not user:
        raise HTTPException(
            status_code=401,
            detail="Incorrect username or password",
        )
    return user

@app.get("/auth/me", response_model=schemas.UserResponse)
async def read_users_me(device_id: str = Query(...), db: Session = Depends(get_db)):
    users = db.query(models.User).all()
    print(f"DEBUG: Current device IDs in DB: {[u.last_device_id for u in users]}")

    user = crud.get_user_by_device_id(db, device_id=device_id)
    if not user:
        raise HTTPException(status_code=401, detail="Device not registered")
    
    print(f"Auth success for device: {device_id}, User: {user.username}")
    return user

@app.post("/logout")
async def logout(user_id: int = Form(...), db: Session = Depends(get_db)):
    if crud.logout_user(db, user_id=user_id):
        return {"status": "success", "message": "User logged out"}
    raise HTTPException(status_code=404, detail="User not found")


# --- ЭНДПОИНТЫ ДЛЯ КОНСПЕКТОВ ---

@app.post("/notes/", response_model=schemas.NoteResponse)
async def create_note(
    subject: str = Form(...),
    topic: str = Form(...),
    user_id: int = Form(...),
    grade: int = Form(...),
    images: List[UploadFile] = File(...),
    db: Session = Depends(get_db)
):
    image_urls = []
    for image in images:
        file_ext = image.filename.split('.')[-1]
        filename = f"{uuid.uuid4()}.{file_ext}"
        file_path = os.path.join(UPLOAD_DIR, filename)

        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(image.file, buffer)
        
        image_urls.append(f"static/uploads/{filename}")

    note_data = schemas.NoteCreate(grade=grade, subject=subject, topic=topic)
    db_note = crud.create_note_with_images(db=db, note=note_data, user_id=user_id, image_urls=image_urls)
    
    # Формируем правильный ответ
    return schemas.NoteResponse(
        id=db_note.id,
        grade=db_note.grade,
        subject=db_note.subject,
        topic=db_note.topic,
        author=db_note.author.username if db_note.author else "Unknown",
        date=db_note.created_at.strftime("%Y-%m-%d %H:%M"),
        images=[image.url for image in db_note.images],
        upvotes_count=0, # Новый конспект, лайков 0
        is_upvoted=False # Создатель не может лайкнуть свой конспект при создании
    )

@app.get("/notes/", response_model=List[schemas.NoteResponse])
def get_all_notes(
    device_id: Optional[str] = None,
    sort: Optional[str] = "newest", 
    subject: Optional[str] = None,
    topic: Optional[str] = None,
    school: Optional[str] = None,
    skip: int = 0, 
    limit: int = 100, 
    db: Session = Depends(get_db)
):
    user = None
    if device_id:
        user = crud.get_user_by_device_id(db, device_id=device_id)

    notes_from_db = crud.get_notes(
        db, user_id=user.id if user else None, skip=skip, limit=limit, sort=sort, subject=subject, topic=topic, school=school
    )
    
    response_notes = []
    for note in notes_from_db:
        is_upvoted = False
        if user:
            is_upvoted = any(upvoter.id == user.id for upvoter in note.upvoted_by_users)

        response_notes.append(
            schemas.NoteResponse(
                id=note.id,
                grade=note.grade,
                subject=note.subject,
                topic=note.topic,
                author=note.author.username if note.author else "Unknown",
                date=note.created_at.strftime("%Y-%m-%d %H:%M"),
                images=[image.url for image in note.images],
                upvotes_count=len(note.upvoted_by_users),
                is_upvoted=is_upvoted
            )
        )
    return response_notes

@app.get("/notes/{note_id}", response_model=schemas.NoteResponse)
def get_note_details(note_id: int, device_id: Optional[str] = None, db: Session = Depends(get_db)):
    note = crud.get_note(db, note_id=note_id)
    if not note:
        raise HTTPException(status_code=404, detail="Note not found")

    user = None
    if device_id:
        user = crud.get_user_by_device_id(db, device_id=device_id)

    is_upvoted = False
    if user:
        is_upvoted = any(upvoter.id == user.id for upvoter in note.upvoted_by_users)

    return schemas.NoteResponse(
        id=note.id,
        grade=note.grade,
        subject=note.subject,
        topic=note.topic,
        author=note.author.username if note.author else "Unknown",
        date=note.created_at.strftime("%Y-%m-%d %H:%M"),
        images=[image.url for image in note.images],
        upvotes_count=len(note.upvoted_by_users),
        is_upvoted=is_upvoted
    )

@app.post("/notes/{note_id}/upvote", response_model=schemas.UpvoteResponse)
async def upvote_note(note_id: int, device_id: str = Query(...), db: Session = Depends(get_db)):
    user = crud.get_user_by_device_id(db, device_id=device_id)
    if not user:
        raise HTTPException(status_code=401, detail="User not recognized for this device")

    response = crud.toggle_note_upvote(db=db, note_id=note_id, user_id=user.id)
    if response is None:
        raise HTTPException(status_code=404, detail="Note not found")
    return response
