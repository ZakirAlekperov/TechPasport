# Состояние реализации архитектуры

Дата: 11.02.2026

## Структура проекта

Проект строится на **строгой слоистой архитектуре** (Clean Architecture / Hexagonal Architecture):

```
┌───────────────────────┐
│   UI Layer (JavaFX)    │
└────────┬─────────────┘
         │
┌────────┴────────────────┐
│  Application Layer      │
│  (Use Cases/Services)   │
└────────┬────────────────┘
         │
┌────────┴────────────────┐
│   Domain Layer          │
│   (Business Logic)      │
└────────┬────────────────┘
         │
┌────────┴────────────────┐
│  Infrastructure Layer   │
│  (DB, Files, Network)   │
└─────────────────────────┘
```

## Текущий статус

### ✅ ГОТОВО

**Bootstrap Layer:**
- ✅ `ApplicationLauncher` - точка входа
- ✅ `DependencyContainer` - ручной DI контейнер
- ✅ `ApplicationConfiguration` - конфигурация
- ✅ `DatabaseInitializer` - инициализация БД

**Infrastructure Layer:**
- ✅ `DatabaseConnection` - управление подключением к SQLite
- ✅ `TransactionTemplate` - управление транзакциями
- ✅ `DatabaseMigration` - миграции схемы БД

**Domain Layer (Shared):**
- ✅ `PassportId` - Value Object для ID
- ✅ `DomainException` - базовое исключение
- ✅ `ValidationException` - исключение валидации

**Domain Layer (LocationPlan):**
- ✅ `LocationPlanRepository` - интерфейс репозитория

### ⚠️ ТРЕБУЕТСЯ СОЗДАТЬ

Для успешной компиляции необходимо добавить:

#### Domain Layer - LocationPlan:
1. `PlanScale.java` - Value Object для масштаба плана
2. `CoordinatePoint.java` - Value Object для координат
3. `BuildingCoordinates.java` - Entity для координат здания
4. `LocationPlan.java` - главная Aggregate Root

#### Application Layer:
5. `SaveLocationPlanCommand.java` - команда для сохранения
6. `SaveLocationPlanUseCase.java` - интерфейс use case
7. `SaveLocationPlanService.java` - реализация use case
8. `LoadLocationPlanQuery.java` - запрос для загрузки
9. `LoadLocationPlanUseCase.java` - интерфейс
10. `LoadLocationPlanService.java` - реализация
11. `LocationPlanDTO.java` - DTO для передачи данных
12. `AddBuildingCoordinatesCommand.java`
13. `AddBuildingCoordinatesUseCase.java`
14. `AddBuildingCoordinatesService.java`

#### Infrastructure Layer:
15. `LocationPlanRepositoryImpl.java` - реализация репозитория

#### UI Layer:
16. `LocationPlanTabController.java` - контроллер вкладки

## Команды для тестирования

```bash
# Обновить зависимости
mvn clean install

# Компиляция
mvn compile

# Запуск
mvn javafx:run
```

## Следующие шаги

1. Создать все недостающие классы domain/locationplan
2. Создать application layer классы
3. Реализовать LocationPlanRepositoryImpl
4. Обновить LocationPlanTabController
5. Протестировать интеграцию

## Принципы разработки

1. **Строгое разделение слоев**: каждый слой знает только о слое, находящемся ниже
2. **Dependency Inversion**: зависимости направлены внутрь (к domain)
3. **Иммутабельность**: Value Objects неизменяемы
4. **Явные инварианты**: все бизнес-правила в domain слое
5. **Ручной DI**: нет зависимости от фреймворков
