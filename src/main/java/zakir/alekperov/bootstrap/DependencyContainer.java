package zakir.alekperov.bootstrap;

import zakir.alekperov.infrastructure.database.*;
import zakir.alekperov.infrastructure.persistence.locationplan.*;
import zakir.alekperov.application.locationplan.*;
import zakir.alekperov.domain.locationplan.LocationPlanRepository;
import zakir.alekperov.ui.tabs.commoninfo.CommonInfoTabController;
import zakir.alekperov.ui.tabs.locationplan.LocationPlanTabController;

/**
 * Контейнер зависимостей.
 * Создает и связывает все компоненты приложения вручную.
 * Явная альтернатива Spring DI или другим фреймворкам.
 */
public final class DependencyContainer {
    
    // Configuration
    private ApplicationConfiguration configuration;
    
    // Infrastructure Layer
    private DatabaseConnection databaseConnection;
    private TransactionTemplate transactionTemplate;
    private DatabaseInitializer databaseInitializer;
    
    // Repositories
    private LocationPlanRepository locationPlanRepository;
    
    // Application Layer - Use Cases
    private SaveLocationPlanUseCase saveLocationPlanUseCase;
    private LoadLocationPlanUseCase loadLocationPlanUseCase;
    private AddBuildingCoordinatesUseCase addBuildingCoordinatesUseCase;
    private DeleteBuildingUseCase deleteBuildingUseCase;
    
    // UI Layer - Controllers (создаются через FXML)
    private CommonInfoTabController commonInfoTabController;
    private LocationPlanTabController locationPlanTabController;
    
    /**
     * Инициализировать все зависимости.
     * Вызывается один раз при запуске приложения.
     */
    public void initialize() {
        System.out.println("=== Инициализация контейнера зависимостей ===");
        
        initializeConfiguration();
        initializeInfrastructure();
        initializeRepositories();
        initializeUseCases();
        // Контроллеры создаются через FXML и ControllerFactory
        
        System.out.println("=== Контейнер зависимостей готов ===");
    }
    
    private void initializeConfiguration() {
        System.out.println("→ Загрузка конфигурации...");
        configuration = new ApplicationConfiguration();
    }
    
    private void initializeInfrastructure() {
        System.out.println("→ Инициализация инфраструктуры...");
        
        String dbDirectory = configuration.getDatabaseDirectory();
        databaseConnection = new DatabaseConnection(dbDirectory);
        transactionTemplate = new TransactionTemplate(databaseConnection);
        databaseInitializer = new DatabaseInitializer(databaseConnection);
    }
    
    private void initializeRepositories() {
        System.out.println("→ Создание репозиториев...");
        
        locationPlanRepository = new LocationPlanRepositoryImpl(transactionTemplate);
    }
    
    private void initializeUseCases() {
        System.out.println("→ Создание use cases...");
        
        saveLocationPlanUseCase = new SaveLocationPlanService(locationPlanRepository);
        loadLocationPlanUseCase = new LoadLocationPlanService(locationPlanRepository);
        addBuildingCoordinatesUseCase = new AddBuildingCoordinatesService(locationPlanRepository);
        deleteBuildingUseCase = new DeleteBuildingService(locationPlanRepository);
    }
    
    /**
     * Зарегистрировать контроллер CommonInfo, созданный через FXML.
     */
    public void registerCommonInfoTabController(CommonInfoTabController controller) {
        this.commonInfoTabController = controller;
        System.out.println("✓ CommonInfoTabController зарегистрирован в DI");
    }
    
    /**
     * Зарегистрировать контроллер LocationPlan, созданный через FXML.
     */
    public void registerLocationPlanTabController(LocationPlanTabController controller) {
        this.locationPlanTabController = controller;
        System.out.println("✓ LocationPlanTabController зарегистрирован в DI");
    }
    
    /**
     * Освободить ресурсы при завершении приложения.
     */
    public void shutdown() {
        System.out.println("Завершение работы приложения...");
        
        if (databaseConnection != null) {
            databaseConnection.close();
        }
        
        System.out.println("Приложение завершено");
    }
    
    /**
     * Алиас для shutdown() для совместимости с try-with-resources.
     */
    public void close() {
        shutdown();
    }
    
    // Геттеры для получения зависимостей
    
    public ApplicationConfiguration getConfiguration() {
        return configuration;
    }
    
    public DatabaseInitializer getDatabaseInitializer() {
        return databaseInitializer;
    }
    
    public CommonInfoTabController getCommonInfoTabController() {
        return commonInfoTabController;
    }
    
    public LocationPlanTabController getLocationPlanTabController() {
        return locationPlanTabController;
    }
    
    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }
    
    // Геттеры для use cases
    
    public SaveLocationPlanUseCase getSaveLocationPlanUseCase() {
        return saveLocationPlanUseCase;
    }
    
    public LoadLocationPlanUseCase getLoadLocationPlanUseCase() {
        return loadLocationPlanUseCase;
    }
    
    public AddBuildingCoordinatesUseCase getAddBuildingCoordinatesUseCase() {
        return addBuildingCoordinatesUseCase;
    }
    
    public DeleteBuildingUseCase getDeleteBuildingUseCase() {
        return deleteBuildingUseCase;
    }
}
