from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.staticfiles import StaticFiles
import sqlite3
import os
import uuid
from datetime import datetime
from typing import List
import bcrypt
import shutil

app = FastAPI()

# Папка для загрузок
UPLOAD_DIR = "uploads"
if not os.path.exists(UPLOAD_DIR):
    os.makedirs(UPLOAD_DIR)

app.mount("/static/uploads", StaticFiles(directory=UPLOAD_DIR), name="static_uploads")

# --- Настройка и инициализация БД ---

def get_db():
    conn = sqlite3.connect('notes_v2.db', check_same_thread=False)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    conn = sqlite3.connect('notes_v2.db')
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE,
            password TEXT
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS notes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            subject TEXT,
            topic TEXT,
            user_id INTEGER,
            grade TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users (id)
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS note_images (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            note_id INTEGER,
            image_url TEXT,
            FOREIGN KEY (note_id) REFERENCES notes (id)
        )
    ''')
    conn.commit()
    conn.close()

init_db()

# --- ЭНДПОИНТЫ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ (без изменений) ---

@app.post("/register")
async def register(username: str = Form(...), password: str = Form(...)):
    salt = bcrypt.gensalt()
    password_bytes = password.encode('utf-8')
    hash_pw = bcrypt.hashpw(password_bytes, salt).decode('utf-8')

    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("INSERT INTO users (username, password) VALUES (?, ?)", (username, hash_pw))
        conn.commit()
        return {"status": "success", "message": "User registered"}
    except sqlite3.IntegrityError:
        raise HTTPException(status_code=400, detail="Username already exists")
    finally:
        conn.close()

@app.post("/login")
async def login(username: str = Form(...), password: str = Form(...)):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM users WHERE username = ?", (username,))
    user = cursor.fetchone()
    conn.close()

    if user:
        password_bytes = password.encode('utf-8')
        hashed_bytes = user['password'].encode('utf-8')
        if bcrypt.checkpw(password_bytes, hashed_bytes):
            return {"id": user['id'], "username": user['username']}
    raise HTTPException(status_code=401, detail="Invalid credentials")

# --- ЭНДПОИНТЫ ДЛЯ КОНСПЕКТОВ ---

@app.post("/notes/")
async def create_note(
        subject: str = Form(...),
        topic: str = Form(...),
        user_id: int = Form(...),
        grade: str = Form(...),
        images: List[UploadFile] = File(...)
):
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO notes (subject, topic, user_id, grade) VALUES (?, ?, ?, ?)",
            (subject, topic, user_id, grade)
        )
        note_id = cursor.lastrowid
        
        image_urls = []
        for img in images:
            # Создаем уникальное имя файла, чтобы избежать конфликтов
            file_ext = img.filename.split('.')[-1]
            filename = f"{note_id}_{uuid.uuid4()}.{file_ext}"
            file_path = os.path.join(UPLOAD_DIR, filename)
            
            with open(file_path, "wb") as buffer:
                shutil.copyfileobj(img.file, buffer)
            
            cursor.execute(
                "INSERT INTO note_images (note_id, image_url) VALUES (?, ?)",
                (note_id, filename) # Сохраняем только имя файла
            )
            image_urls.append(f"/{UPLOAD_DIR}/{filename}")

        conn.commit()
        return {"status": "success", "note_id": note_id, "images": image_urls}
    finally:
        conn.close()

@app.get("/notes/")
def get_all_notes():
    """
    Возвращает список всех конспектов, объединяя данные о конспекте
    со списком его изображений.
    """
    conn = get_db()
    cursor = conn.cursor()

    # Используем LEFT JOIN, чтобы получить все конспекты, даже те, у которых нет картинок
    query = """
        SELECT
            n.id,
            n.subject,
            n.topic,
            n.grade,
            n.created_at,
            u.username as author,
            ni.image_url
        FROM
            notes n
        LEFT JOIN
            note_images ni ON n.id = ni.note_id
        LEFT JOIN
            users u ON n.user_id = u.id
        ORDER BY
            n.created_at DESC;
    """
    cursor.execute(query)
    rows = cursor.fetchall()
    conn.close()

    # Группируем результаты по ID конспекта
    notes_dict = {}
    base_url = "static/uploads" # Используем путь, который ожидает клиент

    for row in rows:
        note_id = row['id']
        if note_id not in notes_dict:
            notes_dict[note_id] = {
                "id": note_id,
                "subject": row['subject'],
                "topic": row['topic'],
                "grade": row['grade'],
                "author": row['author'],
                "created_at": row['created_at'],
                "images": [] # Создаем пустой список для изображений
            }
        
        # Добавляем URL изображения, если оно есть
        if row['image_url']:
            notes_dict[note_id]['images'].append(f"{base_url}/{row['image_url']}")

    # Преобразуем словарь обратно в список
    return list(notes_dict.values())
