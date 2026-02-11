package zakir.alekperov.bootstrap;

import zakir.alekperov.domain.shared.PassportId;
import zakir.alekperov.domain.locationplan.*;
import zakir.alekperov.infrastructure.database.*;
import zakir.alekperov.infrastructure.persistence.locationplan.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Утилита для создания тестовых данных.
 */
public class TestDataCreator {
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║  Создание тестовых данных             ║");
        System.out.println("╚═══════════════════════════════════════╝");
        
        // Инициализация инфраструктуры
        DatabaseConnection dbConnection = new DatabaseConnection(null);
        TransactionTemplate transactionTemplate = new TransactionTemplate(dbConnection);
        
        // Инициализация БД
        try {
            DatabaseMigration migration = new DatabaseMigration(dbConnection);
            migration.migrate();
            System.out.println("✓ База данных инициализирована");
        } catch (Exception e) {
            System.err.println("✗ Ошибка миграции: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Создание репозитория
        LocationPlanRepository repository = new LocationPlanRepositoryImpl(transactionTemplate);
        
        // Создание тестового паспорта ID
        PassportId passportId = PassportId.fromString("test-passport-001");
        
        System.out.println("\n→ Создание ситуационного плана...");
        
        // Создание ситуационного плана
        LocationPlan plan = LocationPlan.create(
            passportId,
            PlanScale.fromDenominator(500),
            "Иванов И.И."
        );
        
        plan.updatePlanDate(LocalDate.of(2026, 2, 11));
        plan.updateNotes("Тестовый ситуационный план для демонстрации системы.\nСоздан автоматически.");
        
        System.out.println("  Паспорт ID: " + passportId.getValue());
        System.out.println("  Масштаб: " + plan.getScale().format());
        System.out.println("  Исполнитель: " + plan.getExecutorName());
        
        // Добавление координат здания A
        System.out.println("\n→ Добавление координат здания A...");
        List<CoordinatePoint> pointsA = List.of(
            CoordinatePoint.of(BigDecimal.valueOf(10.50), BigDecimal.valueOf(20.30)),
            CoordinatePoint.of(BigDecimal.valueOf(30.50), BigDecimal.valueOf(20.30)),
            CoordinatePoint.of(BigDecimal.valueOf(30.50), BigDecimal.valueOf(40.70)),
            CoordinatePoint.of(BigDecimal.valueOf(10.50), BigDecimal.valueOf(40.70))
        );
        
        BuildingCoordinates buildingA = BuildingCoordinates.create(
            "A",
            "Жилой дом (основной корпус)",
            pointsA
        );
        
        plan.addBuildingCoordinates(buildingA);
        System.out.println("  Литера: A");
        System.out.println("  Точек: " + pointsA.size());
        
        // Добавление координат здания Б
        System.out.println("\n→ Добавление координат здания Б...");
        List<CoordinatePoint> pointsB = List.of(
            CoordinatePoint.of(BigDecimal.valueOf(50.00), BigDecimal.valueOf(15.00)),
            CoordinatePoint.of(BigDecimal.valueOf(65.00), BigDecimal.valueOf(15.00)),
            CoordinatePoint.of(BigDecimal.valueOf(65.00), BigDecimal.valueOf(35.00)),
            CoordinatePoint.of(BigDecimal.valueOf(50.00), BigDecimal.valueOf(35.00))
        );
        
        BuildingCoordinates buildingB = BuildingCoordinates.create(
            "Б",
            "Хозяйственная постройка",
            pointsB
        );
        
        plan.addBuildingCoordinates(buildingB);
        System.out.println("  Литера: Б");
        System.out.println("  Точек: " + pointsB.size());
        
        // Добавление координат здания В
        System.out.println("\n→ Добавление координат здания В...");
        List<CoordinatePoint> pointsV = List.of(
            CoordinatePoint.of(BigDecimal.valueOf(70.00), BigDecimal.valueOf(10.00)),
            CoordinatePoint.of(BigDecimal.valueOf(85.00), BigDecimal.valueOf(10.00)),
            CoordinatePoint.of(BigDecimal.valueOf(85.00), BigDecimal.valueOf(25.00)),
            CoordinatePoint.of(BigDecimal.valueOf(70.00), BigDecimal.valueOf(25.00))
        );
        
        BuildingCoordinates buildingV = BuildingCoordinates.create(
            "В",
            "Гараж",
            pointsV
        );
        
        plan.addBuildingCoordinates(buildingV);
        System.out.println("  Литера: В");
        System.out.println("  Точек: " + pointsV.size());
        
        // Сохранение
        System.out.println("\n→ Сохранение в базу данных...");
        try {
            repository.save(plan);
            System.out.println("✓ Ситуационный план сохранен успешно");
        } catch (Exception e) {
            System.err.println("✗ Ошибка сохранения: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Проверка загрузки
        System.out.println("\n→ Проверка загрузки из БД...");
        try {
            var loadedPlan = repository.findByPassportId(passportId);
            if (loadedPlan.isPresent()) {
                LocationPlan loaded = loadedPlan.get();
                System.out.println("✓ План загружен успешно");
                System.out.println("  ID: " + loaded.getPassportId());
                System.out.println("  Масштаб: " + loaded.getScale().format());
                System.out.println("  Зданий: " + loaded.getBuildingsCount());
                System.out.println("  Исполнитель: " + loaded.getExecutorName());
                System.out.println("  Дата: " + loaded.getPlanDate());
                
                System.out.println("\n  Здания:");
                for (BuildingCoordinates building : loaded.getBuildingsCoordinates()) {
                    System.out.println("    - Литера " + building.getLitera() + 
                        ": " + building.getDescription() + 
                        " (" + building.getPointsCount() + " точек)");
                }
            } else {
                System.err.println("✗ План не найден после сохранения!");
            }
        } catch (Exception e) {
            System.err.println("✗ Ошибка загрузки: " + e.getMessage());
            e.printStackTrace();
        } finally {
            dbConnection.close();
        }
        
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║  Тестовые данные созданы успешно!     ║");
        System.out.println("║  ID паспорта: test-passport-001       ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.println("\nТеперь можете запустить приложение:");
        System.out.println("  mvn javafx:run");
    }
}
