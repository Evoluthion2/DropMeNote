# main.py

import os
import time
import sqlite3
from fastapi import FastAPI, HTTPException, UploadFile, File, Form, Request
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
from fastapi.exceptions import RequestValidationError

# --- Инициализация FastAPI ---

app = FastAPI()


# Убедись, что путь к папке верный
upload_path = r"L:\Samsung\DropMeNote\server\uploads"

if not os.path.exists(upload_path):
    os.makedirs(upload_path)

app.mount("/uploads", StaticFiles(directory=upload_path), name="uploads")

# --- Настройка Базы Данных (sqlite3) ---

DB_NAME = "notes.db"

def setup_database():
    """Инициализирует базу данных и создает таблицу, если ее нет."""
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()
    # Создаем таблицу notes, если она не существует
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS notes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            subject TEXT NOT NULL,
            topic TEXT NOT NULL,
            grade INTEGER NOT NULL,
            image_url TEXT NOT NULL
        )
    """)
    conn.commit()
    conn.close()
    print("ЛОГ: База данных 'notes.db' успешно настроена.")

# --- Обработчики ---

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """Обработчик ошибок валидации для отладки."""
    print(f"ОШИБКА ВАЛИДАЦИИ: {exc.errors()}")
    return JSONResponse(
        status_code=422,
        content={"detail": exc.errors()},
    )

# --- Эндпоинты ---

@app.post("/upload_note/")
async def upload_note(
    grade: int = Form(...),
    subject: str = Form(...),
    topic: str = Form(...),
    image: UploadFile = File(...)
):
    """
    Загружает конспект и сохраняет его в базу данных.
    Модерация временно отключена для простоты.
    """
    # 1. Сохранение файла с уникальным именем
    upload_dir = "uploads"
    # Уникальное имя, чтобы файлы не перезаписывались
    filename = f"note_{int(time.time())}_{image.filename.replace(' ', '_')}"
    file_path = os.path.join(upload_dir, filename)
    
    os.makedirs(upload_dir, exist_ok=True)

    try:
        content = await image.read()
        with open(file_path, "wb") as f:
            f.write(content)
        print(f"ЛОГ: Файл сохранен по адресу: {file_path}")
    except Exception as e:
        print(f"ОШИБКА СЕРВЕРА: Не удалось сохранить файл. Ошибка: {e}")
        raise HTTPException(status_code=500, detail="Не удалось сохранить файл на сервере.")
    
    # 2. Сохранение в базу данных (sqlite3)
    try:
        conn = sqlite3.connect(DB_NAME)
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO notes (subject, topic, grade, image_url) VALUES (?, ?, ?, ?)",
            (subject, topic, grade, filename) # Сохраняем только имя файла
        )
        conn.commit()
        conn.close()
        print(f"ЛОГ: Запись для файла '{filename}' успешно создана в БД.")
    except Exception as e:
        print(f"ОШИБКА БД: Не удалось создать запись. Ошибка: {e}")
        # Если запись в БД не удалась, удаляем уже сохраненный файл
        os.remove(file_path)
        raise HTTPException(status_code=500, detail="Ошибка сервера при сохранении данных.")

    return {"status": "success", "filename": filename}


@app.get("/notes/")
def get_notes():
    """Возвращает список всех конспектов из базы данных."""
    try:
        conn = sqlite3.connect(DB_NAME)
        conn.row_factory = sqlite3.Row  # Позволяет обращаться к колонкам по имени
        cursor = conn.cursor()
        cursor.execute("SELECT id, subject, topic, grade, image_url FROM notes")
        notes = [dict(row) for row in cursor.fetchall()]
        conn.close()
        
        # Добавляем полный URL к картинке
        base_url = "http://192.168.0.104:8000" # Замените на ваш IP, если нужно
        for note in notes:
            note['image_url'] = f"{base_url}/uploads/{note['image_url']}"
            
        return notes
    except Exception as e:
        print(f"ОШИБКА БД: Не удалось получить список конспектов. Ошибка: {e}")
        raise HTTPException(status_code=500, detail="Ошибка сервера при чтении данных.")

# --- Статические файлы ---

# Этот вызов монтирует папку 'uploads' для доступа по URL /uploads
# Он должен быть в конце файла
app.mount("/uploads", StaticFiles(directory="uploads"), name="uploads")

# --- Запуск ---

# Вызываем настройку БД один раз при старте приложения
setup_database()
