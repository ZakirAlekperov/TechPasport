# Архитектура Ситуационного Плана

## Обзор

Система работы с ситуационным планом поддерживает **два режима**:

1. **Ручное рисование** (`MANUAL_DRAWING`): пользователь указывает координаты зданий вручную
2. **Загруженное изображение** (`UPLOADED_IMAGE`): пользователь загружает готовый план

## Слоистая архитектура

### Domain Layer (Бизнес-логика)

```
zakir.alekperov.domain.locationplan/
├── PlanMode.java              # Enum режимов работы
├── PlanImage.java             # Value Object для изображения
├── PlanScale.java             # Value Object для масштаба
├── CoordinatePoint.java       # Value Object для координат
├── BuildingLitera.java        # Value Object для литеры
├── BuildingCoordinates.java   # Entity для координат здания
├── LocationPlan.java          # Aggregate Root
└── LocationPlanRepository.java # Интерфейс репозитория
```

#### Ключевые инварианты LocationPlan:

1. **Режим MANUAL_DRAWING**:
   - Обязателен `PlanScale`
   - Обязателен `executorName`
   - Может содержать `BuildingCoordinates`
   - **Не может** содержать `PlanImage`

2. **Режим UPLOADED_IMAGE**:
   - Обязателен `PlanImage`
   - **Не может** содержать `BuildingCoordinates`
   - `PlanScale` и `executorName` необязательны

3. **Общие инварианты**:
   - `PassportId` не может быть null
   - `planDate` не может быть в будущем
   - Нельзя одновременно иметь изображение и координаты

#### Валидация PlanImage:

- Размер ≤ 10 МБ
- Формат: PNG, JPG, JPEG
- Имя файла не пустое
- Defensive copy для байтового массива

### Application Layer (Use Cases)

```
zakir.alekperov.application.locationplan/
├── commands/
│   └── UploadPlanImageCommand.java
├── usecases/
│   └── UploadPlanImageUseCase.java
└── services/
    └── UploadPlanImageService.java
```

#### Ответственность Application слоя:

1. Преобразование DTO в domain модели
2. Оркестрация вызовов domain логики
3. Вызов репозиториев
4. Обработка исключений

**НЕ содержит**:
- Бизнес-логики (она в Domain)
- SQL запросов (они в Infrastructure)
- UI логики (она в UI слое)

### Infrastructure Layer (Технические детали)

```
zakir.alekperov.infrastructure.persistence.locationplan/
└── LocationPlanRepositoryImpl.java
```

#### Схема БД:

```sql
CREATE TABLE location_plan (
    passport_id TEXT PRIMARY KEY,
    plan_mode TEXT NOT NULL CHECK (plan_mode IN ('MANUAL_DRAWING', 'UPLOADED_IMAGE')),
    scale_denominator INTEGER,          -- необязателен
    executor_name TEXT,                 -- необязателен
    plan_date TEXT NOT NULL,
    notes TEXT,
    uploaded_image_data BLOB,           -- для UPLOADED_IMAGE
    uploaded_image_filename TEXT,
    uploaded_image_format TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE building_coordinates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    passport_id TEXT NOT NULL,
    litera TEXT NOT NULL,
    description TEXT NOT NULL,
    point_index INTEGER NOT NULL,
    x_coordinate REAL NOT NULL,
    y_coordinate REAL NOT NULL,
    FOREIGN KEY (passport_id) REFERENCES location_plan(passport_id)
);
```

### UI Layer

Контроллер получает use cases через DependencyContainer:

```java
private UploadPlanImageUseCase uploadPlanImageUseCase;

// При загрузке изображения
UploadPlanImageCommand command = new UploadPlanImageCommand(
    passportId,
    imageData,
    fileName,
    LocalDate.now(),
    notes
);

uploadPlanImageUseCase.execute(command);
```

## Архитектурные решения

### 1. Enum вместо Boolean

**Решение**: Использован `PlanMode` enum вместо `boolean isManualDrawing`

**Почему**:
- Явное перечисление режимов
- Легко добавить новые режимы
- Нет логической инверсии (`!isManualDrawing`)
- Type-safe в switch/case

### 2. Factory Methods вместо конструкторов

**Решение**: 
```java
LocationPlan.createManualDrawing(...);
LocationPlan.createWithUploadedImage(...);
```

**Почему**:
- Явное название намерения
- Нельзя перепутать параметры
- Конструктор только для восстановления из БД
- Валидация специфична для каждого режима

### 3. Optional для необязательных полей

**Решение**:
```java
public Optional<PlanScale> getScale() {
    return Optional.ofNullable(scale);
}
```

**Почему**:
- Явно указывает, что значение может отсутствовать
- Защита от NullPointerException
- Fluent API для обработки

### 4. Defensive Copy для массивов

**Решение**:
```java
public byte[] getImageData() {
    return Arrays.copyOf(imageData, imageData.length);
}
```

**Почему**:
- Защита от изменения внутреннего состояния
- Immutability Value Object
- Предсказуемое поведение

### 5. Разделение ответственности

- **Domain**: Валидация бизнес-правил
- **Application**: Оркестрация use cases
- **Infrastructure**: SQL, транзакции, маппинг
- **UI**: Отображение, пользовательский ввод

## Риски масштабирования

### 1. Размер изображений в БД

**Риск**: Хранение BLOB в SQLite может замедлить запросы

**Снижение**:
- Лимит 10 МБ на изображение
- Можно перейти на file storage + ссылки в БД
- Легко заменить реализацию репозитория

### 2. Добавление новых режимов

**Риск**: Может появиться третий режим

**Снижение**:
- Enum легко расширяется
- Factory methods изолируют создание
- Инварианты локализованы в конструкторе

### 3. Изменение форматов изображений

**Риск**: Могут потребоваться новые форматы

**Снижение**:
- Валидация в `PlanImage`
- Легко добавить в `ALLOWED_FORMATS`
- Не затрагивает другие слои

### 4. Переход на другую БД

**Риск**: SQLite может не подойти для многопользовательской работы

**Снижение**:
- Интерфейс `LocationPlanRepository` не зависит от БД
- Можно заменить реализацию на PostgreSQL/MySQL
- Domain модели не изменятся

### 5. Конкурентность

**Риск**: Одновременное редактирование

**Снижение**:
- Aggregate Root обеспечивает транзакционную границу
- `TransactionTemplate` для атомарности
- Можно добавить optimistic locking (версионирование)

## Тестирование

### Unit тесты

```java
// Domain модели - легко тестируются
@Test
void shouldRejectImageLargerThan10MB() {
    byte[] largeImage = new byte[11 * 1024 * 1024];
    
    assertThrows(ValidationException.class, () -> {
        new PlanImage(largeImage, "large.png");
    });
}

@Test
void shouldNotAllowBuildingsInUploadedImageMode() {
    LocationPlan plan = LocationPlan.createWithUploadedImage(...);
    
    assertThrows(ValidationException.class, () -> {
        plan.addBuilding(buildingCoordinates);
    });
}
```

### Integration тесты

- Тестирование репозитория с in-memory SQLite
- Тестирование use cases с mock репозиторием

## Заключение

Архитектура обеспечивает:

✅ **Явность**: нет скрытой магии, все зависимости четкие  
✅ **Предсказуемость**: инварианты защищают от некорректных состояний  
✅ **Масштабируемость**: легко добавлять новые режимы и функции  
✅ **Тестируемость**: domain модели без зависимостей  
✅ **Понятность**: код читается как документация
