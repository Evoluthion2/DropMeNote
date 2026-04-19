# moderation.py

import easyocr
import os

# Список запрещенных слов для модерации
BAD_WORDS = ["мат1", "мат2", "плохоеслово"]

# Инициализация ридера EasyOCR. 
# Указываем языки, которые нужно распознавать.
# gpu=False указывает, что нужно использовать CPU. Если у вас настроен CUDA, можно установить True.
reader = easyocr.Reader(['ru', 'en'], gpu=False)

def is_content_safe(image_path: str) -> bool:
    """
    Проверяет изображение на наличие запрещенных слов.

    Args:
        image_path: Путь к файлу изображения.

    Returns:
        True, если запрещенных слов не найдено, иначе False.
    """
    print(f"Начинаю проверку файла: {image_path}")
    
    # Проверяем, существует ли файл
    if not os.path.exists(image_path):
        print(f"Ошибка: Файл не найден по пути {image_path}")
        return False

    try:
        # Распознаем текст на изображении
        result = reader.readtext(image_path, detail=0, paragraph=True)
        
        if not result:
            print("Текст на изображении не найден.")
            return True # Считаем безопасным, если текста нет

        # Объединяем распознанный текст в одну строку в нижнем регистре
        full_text = " ".join(result).lower()
        print(f"Распознанный текст: {full_text}")

        # Проверяем наличие запрещенных слов
        for word in BAD_WORDS:
            if word.lower() in full_text:
                print(f"Найдено запрещенное слово: {word}")
                return False
        
        print("Запрещенных слов не найдено.")
        return True

    except Exception as e:
        print(f"Произошла ошибка во время обработки изображения: {e}")
        # В случае ошибки обработки считаем контент небезопасным
        return False
