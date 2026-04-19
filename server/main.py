# main.py

import os
import time  # Добавляем импорт времени
from fastapi import FastAPI, Depends, HTTPException, UploadFile, File, Form, Request
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
from fastapi.exceptions import RequestValidationError

import crud, models, schemas
from database import SessionLocal, engine
# from moderation import is_content_safe # Временно отключаем модерацию

# Создаем все таблицы в базе данных
models.Base.metadata.create_all(bind=engine)

app = FastAPI()

# --- Зависимости и обработчики ---

def get_db():
    """Зависимость для получения сессии базы данных."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """Обработчик ошибок валидации для отладки."""
    print(f"ОШИБКА ВАЛИДАЦИИ: {exc.errors()}")
    return JSONResponse(
        status_code=422,
        content={"detail": exc.errors()},
    )

# --- Эндпоинты ---

@app.post("/register/", response_model=schemas.User)
def register_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    """Регистрация нового пользователя."""
    db_user = crud.get_user_by_username(db, username=user.username)
    if db_user:
        raise HTTPException(status_code=400, detail="Имя пользователя уже занято")
    return crud.create_user(db=db, user=user)

@app.post("/upload_note/")
async def upload_note(
    # Параметры оставляем, чтобы не ломать клиент, но пока не используем
    db: Session = Depends(get_db),
    grade: int = Form(...),
    subject: str = Form(...),
    topic: str = Form(...),
    author_id: int = Form(...),
    image: UploadFile = File(...)
):
    """
    (Временная версия для отладки)
    Загружает файл с уникальным именем и немедленно возвращает успех.
    """
    # 1. Фиксированный путь и уникальное имя файла
    upload_dir = "L:/Samsung/DropMeNote/server/uploads/"
    filename = f"note_{int(time.time())}.jpg"
    file_path = os.path.join(upload_dir, filename)
    
    os.makedirs(upload_dir, exist_ok=True)

    # 2. Блок сохранения файла
    try:
        content = await image.read()
        with open(file_path, "wb") as f:
            f.write(content)
        print(f"ЛОГ: Файл сохранен по адресу: {file_path}")
        print(f"ЛОГ: Размер файла: {len(content)} байт")
    except Exception as e:
        print(f"ОШИБКА СЕРВЕРА: Не удалось сохранить файл. Ошибка: {e}")
        raise HTTPException(status_code=500, detail="Не удалось сохранить файл на сервере.")

    # 3. Временно убираем всю остальную логику, чтобы избежать 404
    
    # # Проверка существования автора
    # if not crud.get_user(db, user_id=author_id):
    #     os.remove(file_path)
    #     raise HTTPException(status_code=404, detail=f"Автор с ID {author_id} не найден.")

    # # Модерация контента (EasyOCR)
    # if not is_content_safe(file_path):
    #     os.remove(file_path)
    #     return JSONResponse(status_code=400, content={"message": "Модерация не пройдена."})
    
    # # Сохранение в базу данных
    # try:
    #     note_schema = schemas.NoteCreate(...)
    #     crud.create_note(db=db, note=note_schema, author_id=author_id)
    # except Exception as e:
    #     os.remove(file_path)
    #     raise HTTPException(status_code=500, detail="Ошибка сервера при сохранении данных.")

    # 4. Немедленный успешный ответ
    return {"status": "success", "file": filename}


@app.get("/notes/", response_model=list[schemas.Note])
def read_notes(grade: int = None, subject: str = None, skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    """Получение списка конспектов с фильтрами."""
    notes = crud.get_notes(db, skip=skip, limit=limit, grade=grade, subject=subject)
    return notes
