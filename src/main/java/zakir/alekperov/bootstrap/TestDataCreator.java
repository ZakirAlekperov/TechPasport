package zakir.alekperov.bootstrap;

import zakir.alekperov.domain.locationplan.*;
import zakir.alekperov.domain.shared.PassportId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class TestDataCreator {
    private final LocationPlanRepository locationPlanRepository;
    
    public TestDataCreator(LocationPlanRepository locationPlanRepository) {
        this.locationPlanRepository = locationPlanRepository;
    }
    
    public void createTestLocationPlan() {
        System.out.println("\n=== Создание тестового ситуационного плана ===");
        
        PassportId testPassportId = PassportId.fromString("TEST-PASSPORT-001");
        
        Optional<LocationPlan> existingPlan = locationPlanRepository.findByPassportId(testPassportId);
        if (existingPlan.isPresent()) {
            System.out.println("⚠️  Тестовый план уже существует, пропускаем создание");
            return;
        }
        
        try {
            PlanScale scale = new PlanScale(500);
            LocationPlan plan = LocationPlan.createManualDrawing(
                testPassportId,
                scale,
                "Иванов И.И.",
                LocalDate.now(),
                "Тестовый ситуационный план для демонстрации"
            );
            
            System.out.println("✅ Создан план: " + testPassportId.getValue());
            System.out.println("   Масштаб: " + scale.toDisplayString());
            
            addTestBuilding_A(plan);
            addTestBuilding_B(plan);
            addTestBuilding_C(plan);
            
            locationPlanRepository.save(plan);
            
            System.out.println("✅ Тестовые данные сохранены!");
            
            Optional<LocationPlan> loaded = locationPlanRepository.findByPassportId(testPassportId);
            if (loaded.isPresent()) {
                System.out.println("\n✅ Проверка: план успешно загружен из БД");
                System.out.println("   Масштаб: " + loaded.get().getScale().map(PlanScale::toDisplayString).orElse("N/A"));
                System.out.println("   Зданий: " + loaded.get().getBuildings().size());
            }
            
            System.out.println("\n👉 Детали зданий:");
            for (BuildingCoordinates building : loaded.get().getBuildings()) {
                System.out.println("   • Литера " + building.getLitera().getValue() + 
                    ": " + building.getDescription() + 
                    " (" + building.getPoints().size() + " точек)");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка создания тестовых данных: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void addTestBuilding_A(LocationPlan plan) {
        List<CoordinatePoint> points = List.of(
            new CoordinatePoint(10.0, 10.0),
            new CoordinatePoint(30.0, 10.0),
            new CoordinatePoint(30.0, 20.0),
            new CoordinatePoint(10.0, 20.0)
        );
        
        BuildingLitera litera = new BuildingLitera("А");
        BuildingCoordinates building = new BuildingCoordinates(
            litera,
            "Административное здание",
            points
        );
        
        plan.addBuilding(building);
        System.out.println("   + Добавлено здание литера А (" + points.size() + " точек)");
    }
    
    private void addTestBuilding_B(LocationPlan plan) {
        List<CoordinatePoint> points = List.of(
            new CoordinatePoint(40.0, 10.0),
            new CoordinatePoint(60.0, 10.0),
            new CoordinatePoint(60.0, 25.0),
            new CoordinatePoint(40.0, 25.0)
        );
        
        BuildingLitera litera = new BuildingLitera("Б");
        BuildingCoordinates building = new BuildingCoordinates(
            litera,
            "Производственный корпус",
            points
        );
        
        plan.addBuilding(building);
        System.out.println("   + Добавлено здание литера Б (" + points.size() + " точек)");
    }
    
    private void addTestBuilding_C(LocationPlan plan) {
        List<CoordinatePoint> points = List.of(
            new CoordinatePoint(10.0, 30.0),
            new CoordinatePoint(25.0, 30.0),
            new CoordinatePoint(25.0, 40.0),
            new CoordinatePoint(10.0, 40.0)
        );
        
        BuildingLitera litera = new BuildingLitera("В");
        BuildingCoordinates building = new BuildingCoordinates(
            litera,
            "Складское помещение",
            points
        );
        
        plan.addBuilding(building);
        System.out.println("   + Добавлено здание литера В (" + points.size() + " точек)");
    }
}
