-- Миграция V2: Добавление поддержки режимов работы и загрузки изображений
-- Дата: 2026-02-13
-- Описание: Добавляет возможность ручного рисования или загрузки готового изображения

-- Добавление поля plan_mode (режим работы)
ALTER TABLE location_plan 
ADD COLUMN plan_mode TEXT NOT NULL DEFAULT 'MANUAL_DRAWING'
CHECK (plan_mode IN ('MANUAL_DRAWING', 'UPLOADED_IMAGE'));

-- Добавление полей для загруженного изображения
ALTER TABLE location_plan 
ADD COLUMN uploaded_image_data BLOB;

ALTER TABLE location_plan 
ADD COLUMN uploaded_image_filename TEXT;

ALTER TABLE location_plan 
ADD COLUMN uploaded_image_format TEXT;

-- Делаем scale_denominator и executor_name необязательными (для режима UPLOADED_IMAGE)
-- SQLite не поддерживает ALTER COLUMN, нужно пересоздать таблицу

-- Создаем временную таблицу с новой структурой
CREATE TABLE location_plan_new (
    passport_id TEXT PRIMARY KEY,
    plan_mode TEXT NOT NULL DEFAULT 'MANUAL_DRAWING' CHECK (plan_mode IN ('MANUAL_DRAWING', 'UPLOADED_IMAGE')),
    scale_denominator INTEGER,
    executor_name TEXT,
    plan_date TEXT NOT NULL,
    notes TEXT,
    uploaded_image_data BLOB,
    uploaded_image_filename TEXT,
    uploaded_image_format TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Копируем данные из старой таблицы
INSERT INTO location_plan_new (
    passport_id, 
    plan_mode,
    scale_denominator, 
    executor_name, 
    plan_date, 
    notes,
    created_at,
    updated_at
)
SELECT 
    passport_id,
    'MANUAL_DRAWING',
    scale_denominator,
    executor_name,
    plan_date,
    notes,
    created_at,
    updated_at
FROM location_plan;

-- Удаляем старую таблицу
DROP TABLE location_plan;

-- Переименовываем новую таблицу
ALTER TABLE location_plan_new RENAME TO location_plan;

-- Создаем индекс для режима
CREATE INDEX idx_location_plan_mode ON location_plan(plan_mode);

-- Комментарии к полям (для документации)
-- plan_mode: Режим работы (ручное рисование или загруженное изображение)
-- scale_denominator: Знаменатель масштаба (обязательно для MANUAL_DRAWING)
-- executor_name: ФИО исполнителя (обязательно для MANUAL_DRAWING)
-- uploaded_image_data: Бинарные данные изображения (обязательно для UPLOADED_IMAGE)
-- uploaded_image_filename: Имя файла изображения
-- uploaded_image_format: Формат изображения (PNG, JPG, JPEG)
