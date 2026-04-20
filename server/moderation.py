# moderation.py

import easyocr
import os

# Список запрещенных слов для модерации
BAD_WORDS = ["мат1", "мат2", "плохоеслово"]

# Инициализация ридера EasyOCR. 
# Эта строка выполняется один раз при импорте модуля (при старте сервера).
reader = easyocr.Reader(['ru', 'en'], gpu=False)

def check_text_safety(image_path: str) -> bool:
    """
    Проверяет изображение на наличие запрещенных слов.

    Args:
        image_path: Путь к файлу изображения.

    Returns:
        True, если запрещенных слов не найдено, иначе False.
    """
    print(f"ЛОГ: Начинаю AI-модерацию файла: {image_path}")
    
    if not os.path.exists(image_path):
        print(f"ОШИБКА МОДЕРАЦИИ: Файл не найден по пути {image_path}")
        return False

    try:
        # Распознаем текст на изображении
        result = reader.readtext(image_path, detail=0, paragraph=True)
        
        if not result:
            print("ЛОГ: Текст на изображении не найден, модерация пройдена.")
            return True # Считаем безопасным, если текста нет

        full_text = " ".join(result).lower()
        print(f"ЛОГ: Распознанный текст: {full_text[:100]}...") # Выводим только часть текста

        # Проверяем наличие запрещенных слов
        for word in BAD_WORDS:
            if word.lower() in full_text:
                print(f"ЛОГ: Модерация не пройдена. Найдено запрещенное слово: '{word}'")
                return False
        
        print("ЛОГ: Модерация пройдена, запрещенных слов не найдено.")
        return True

    except Exception as e:
        print(f"ОШИБКА МОДЕРАЦИИ: Произошла ошибка во время обработки изображения: {e}")
        # В случае ошибки обработки считаем контент небезопасным, чтобы избежать пропуска
        return False
