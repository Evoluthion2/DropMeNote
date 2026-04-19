# database.py

from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# URL для подключения к базе данных SQLite
# База данных будет храниться в файле 'notes.db' в корневой директории проекта
SQLALCHEMY_DATABASE_URL = "sqlite:///./notes.db"

# Создаем движок SQLAlchemy
# connect_args={"check_same_thread": False} необходим только для SQLite,
# чтобы разрешить использование сессии в разных потоках (например, в запросах FastAPI)
engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)

# Создаем фабрику сессий, которая будет создавать новые сессии для каждого запроса
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Создаем базовый класс для декларативных моделей SQLAlchemy
Base = declarative_base()
