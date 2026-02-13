# Инструкция по запуску и тестированию

## Шаг 1: Получить код

```bash
# Перейдите в папку проекта
cd /path/to/TechPasport

# Получите последние изменения
git pull origin main

# Проверьте, что все файлы на месте
git log --oneline -5
```

Вы должны увидеть последние 12 коммитов:
- `docs: add testing guide...`
- `docs: add architecture documentation...`
- `feat(infrastructure): add SQL migration...`
- `feat(bootstrap): add UploadPlanImageUseCase...`
- `feat(application): add UploadPlanImageService...`
- `feat(application): add upload plan image command...`
- `feat(domain): add LocationPlan aggregate root...`
- и т.д.

## Шаг 2: Собрать проект

```bash
# Если у вас Maven
mvn clean compile

# Если у вас Gradle
./gradlew clean build
```

## Шаг 3: Проверка новых файлов

Проверьте наличие ключевых файлов:

```bash
# Domain слой
ls -la src/main/java/zakir/alekperov/domain/locationplan/
```

Должны быть:
- `PlanMode.java` ✅
- `PlanImage.java` ✅
- `PlanScale.java` ✅
- `CoordinatePoint.java` ✅
- `BuildingLitera.java` ✅
- `BuildingCoordinates.java` ✅
- `LocationPlan.java` ✅
- `LocationPlanRepository.java` (уже был)

```bash
# Application слой
ls -la src/main/java/zakir/alekperov/application/locationplan/
```

Должны быть:
- `commands/UploadPlanImageCommand.java` ✅
- `usecases/UploadPlanImageUseCase.java` ✅
- `services/UploadPlanImageService.java` ✅

```bash
# SQL миграция
ls -la src/main/resources/db/migration/
```

Должен быть:
- `V2__add_plan_mode_and_image.sql` ✅

## Шаг 4: Запустить приложение

### Через IDE (IntelliJ IDEA / Eclipse)

1. Откройте `ApplicationLauncher.java`
2. Нажмите **Run** (зеленая стрелка)

### Через командную строку

```bash
# Maven
mvn javafx:run

# Gradle
./gradlew run
```

## Шаг 5: Тестирование нового функционала

### Тест 1: Проверка инициализации

При запуске в консоли должны быть сообщения:

```
=== Инициализация TechPasport ===
→ Загрузка конфигурации...
→ Инициализация инфраструктуры...
→ Создание репозиториев...
→ Создание use cases...
=== Контейнер зависимостей готов ===
```

✅ **Ожидаемый результат**: Приложение запустилось без ошибок

### Тест 2: Проверка Domain моделей (через консоль)

Создайте тестовый класс или запустите в JShell:

```java
// Тест 1: Создание PlanScale
import zakir.alekperov.domain.locationplan.*;
import zakir.alekperov.domain.shared.*;

PlanScale scale = new PlanScale(500);
System.out.println("✅ PlanScale: " + scale.toDisplayString());

// Тест 2: Создание PlanImage
byte[] testImage = new byte[1024]; // 1 KB
PlanImage image = new PlanImage(testImage, "test.png");
System.out.println("✅ PlanImage: " + image.getFileName() + " (" + image.getFormat() + ")");

// Тест 3: Создание LocationPlan с изображением
PassportId passportId = new PassportId("TEST-001");
LocationPlan plan = LocationPlan.createWithUploadedImage(
    passportId,
    image,
    java.time.LocalDate.now(),
    "Тестовые примечания"
);
System.out.println("✅ LocationPlan: " + plan);
System.out.println("✅ Режим: " + (plan.isUploadedImage() ? "UPLOADED_IMAGE" : "MANUAL_DRAWING"));
```

✅ **Ожидаемый результат**: Все объекты созданы без ошибок

### Тест 3: Валидация PlanImage

```java
// Тест невалидного формата
try {
    byte[] testImage = new byte[1024];
    PlanImage image = new PlanImage(testImage, "test.txt");
    System.out.println("❌ Ошибка: валидация не сработала!");
} catch (ValidationException e) {
    System.out.println("✅ Валидация формата: " + e.getMessage());
}

// Тест большого размера
try {
    byte[] largeImage = new byte[11 * 1024 * 1024]; // 11 MB
    PlanImage image = new PlanImage(largeImage, "large.png");
    System.out.println("❌ Ошибка: валидация размера не сработала!");
} catch (ValidationException e) {
    System.out.println("✅ Валидация размера: " + e.getMessage());
}
```

✅ **Ожидаемый результат**: Валидация работает

### Тест 4: Инварианты LocationPlan

```java
// Тест: нельзя добавить здания в режиме UPLOADED_IMAGE
try {
    PassportId passportId = new PassportId("TEST-002");
    byte[] testImage = new byte[1024];
    PlanImage image = new PlanImage(testImage, "plan.png");
    
    LocationPlan plan = LocationPlan.createWithUploadedImage(
        passportId, image, java.time.LocalDate.now(), ""
    );
    
    // Попытка добавить здание
    BuildingLitera litera = new BuildingLitera("А");
    List<CoordinatePoint> points = List.of(
        new CoordinatePoint(0, 0),
        new CoordinatePoint(10, 0),
        new CoordinatePoint(10, 10)
    );
    BuildingCoordinates building = new BuildingCoordinates(litera, "Тест", points);
    plan.addBuilding(building);
    
    System.out.println("❌ Ошибка: инвариант не сработал!");
} catch (ValidationException e) {
    System.out.println("✅ Инвариант работает: " + e.getMessage());
}
```

✅ **Ожидаемый результат**: Инварианты защищают от некорректных состояний

### Тест 5: Проверка SQL миграции

```bash
# Проверьте структуру БД (если у вас установлен sqlite3)
sqlite3 data/techpassport.db ".schema location_plan"
```

Должны быть поля:
```sql
passport_id TEXT PRIMARY KEY,
plan_mode TEXT NOT NULL,  -- НОВОЕ!
scale_denominator INTEGER,
executor_name TEXT,
plan_date TEXT NOT NULL,
notes TEXT,
uploaded_image_data BLOB,  -- НОВОЕ!
uploaded_image_filename TEXT,  -- НОВОЕ!
uploaded_image_format TEXT,  -- НОВОЕ!
created_at TEXT NOT NULL,
updated_at TEXT NOT NULL
```

✅ **Ожидаемый результат**: Схема обновлена с новыми полями

## Шаг 6: Интеграция с UI (в разработке)

Для полной работы нужно добавить в `LocationPlanTabController`:

1. **RadioButton для выбора режима**:
   - Ручное рисование
   - Загрузка изображения

2. **Button для загрузки изображения**

3. **ImageView для отображения загруженного плана**

## Что проверять

### ✅ Checklist

- [ ] Приложение запускается без ошибок
- [ ] В консоли есть сообщение "UploadPlanImageUseCase" при инициализации
- [ ] Domain модели создаются без ошибок
- [ ] Валидация PlanImage работает (формат, размер)
- [ ] Инварианты LocationPlan защищают от некорректных состояний
- [ ] SQL миграция применилась (новые поля в БД)

## Возможные проблемы

### Проблема 1: Компиляция не проходит

**Решение**:
```bash
# Проверьте Java версию
java -version  # Должна быть 17+

# Очистите кэш
mvn clean
# или
./gradlew clean

# Пересоберите
mvn compile
```

### Проблема 2: ClassNotFoundException при запуске

**Решение**:
```bash
# Проверьте, что файлы скомпилировались
find target/classes -name "*.class" | grep -i planmode
find target/classes -name "*.class" | grep -i planimage
```

### Проблема 3: SQL миграция не применилась

**Решение**:
```bash
# Удалите старую БД (если это тестовые данные)
rm data/techpassport.db

# Запустите приложение заново
# БД будет создана с новой схемой
```

## Следующие шаги

1. ✅ Domain слой готов
2. ✅ Application слой готов
3. ✅ Infrastructure слой готов (миграция SQL)
4. ✅ DependencyContainer обновлен
5. ⚠️ UI слой - нужно добавить элементы управления

**Следующая задача**: Добавить UI элементы для загрузки изображений.

## Полезные ссылки

- Архитектурная документация: `docs/LOCATION_PLAN_ARCHITECTURE.md`
- SQL миграция: `src/main/resources/db/migration/V2__add_plan_mode_and_image.sql`
- Domain модели: `src/main/java/zakir/alekperov/domain/locationplan/`
