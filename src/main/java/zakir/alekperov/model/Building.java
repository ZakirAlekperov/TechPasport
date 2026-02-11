package zakir.alekperov.model;

import javafx.beans.property.*;

/**
 * Модель данных для строения на участке
 */
public class Building {
    
    private final StringProperty letter;           // Литера (А, Б, В...)
    private final StringProperty name;             // Наименование (Жилой дом, Баня...)
    private final IntegerProperty yearBuilt;       // Год ввода в эксплуатацию
    private final StringProperty wallMaterial;     // Материал стен
    private final DoubleProperty buildingArea;     // Площадь застройки, кв.м
    private final DoubleProperty height;           // Высота, м
    private final DoubleProperty volume;           // Объем, куб.м
    private final DoubleProperty inventoryValue;   // Инвентаризационная стоимость, руб.
    
    public Building() {
        this.letter = new SimpleStringProperty("");
        this.name = new SimpleStringProperty("");
        this.yearBuilt = new SimpleIntegerProperty(0);
        this.wallMaterial = new SimpleStringProperty("");
        this.buildingArea = new SimpleDoubleProperty(0.0);
        this.height = new SimpleDoubleProperty(0.0);
        this.volume = new SimpleDoubleProperty(0.0);
        this.inventoryValue = new SimpleDoubleProperty(0.0);
    }
    
    public Building(String letter, String name, int yearBuilt, String wallMaterial,
                   double buildingArea, double height, double volume, double inventoryValue) {
        this.letter = new SimpleStringProperty(letter);
        this.name = new SimpleStringProperty(name);
        this.yearBuilt = new SimpleIntegerProperty(yearBuilt);
        this.wallMaterial = new SimpleStringProperty(wallMaterial);
        this.buildingArea = new SimpleDoubleProperty(buildingArea);
        this.height = new SimpleDoubleProperty(height);
        this.volume = new SimpleDoubleProperty(volume);
        this.inventoryValue = new SimpleDoubleProperty(inventoryValue);
    }
    
    // Letter (Литера)
    public String getLetter() { return letter.get(); }
    public void setLetter(String letter) { this.letter.set(letter); }
    public StringProperty letterProperty() { return letter; }
    
    // Name (Наименование)
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }
    
    // Year Built (Год постройки)
    public int getYearBuilt() { return yearBuilt.get(); }
    public void setYearBuilt(int yearBuilt) { this.yearBuilt.set(yearBuilt); }
    public IntegerProperty yearBuiltProperty() { return yearBuilt; }
    
    // Wall Material (Материал стен)
    public String getWallMaterial() { return wallMaterial.get(); }
    public void setWallMaterial(String wallMaterial) { this.wallMaterial.set(wallMaterial); }
    public StringProperty wallMaterialProperty() { return wallMaterial; }
    
    // Building Area (Площадь застройки)
    public double getBuildingArea() { return buildingArea.get(); }
    public void setBuildingArea(double buildingArea) { this.buildingArea.set(buildingArea); }
    public DoubleProperty buildingAreaProperty() { return buildingArea; }
    
    // Height (Высота)
    public double getHeight() { return height.get(); }
    public void setHeight(double height) { this.height.set(height); }
    public DoubleProperty heightProperty() { return height; }
    
    // Volume (Объем)
    public double getVolume() { return volume.get(); }
    public void setVolume(double volume) { this.volume.set(volume); }
    public DoubleProperty volumeProperty() { return volume; }
    
    // Inventory Value (Инвентаризационная стоимость)
    public double getInventoryValue() { return inventoryValue.get(); }
    public void setInventoryValue(double inventoryValue) { this.inventoryValue.set(inventoryValue); }
    public DoubleProperty inventoryValueProperty() { return inventoryValue; }
    
    /**
     * Автоматический расчет объема (площадь × высота)
     */
    public void calculateVolume() {
        double calculatedVolume = buildingArea.get() * height.get();
        volume.set(Math.round(calculatedVolume * 10.0) / 10.0); // Округляем до 1 знака
    }
    
    @Override
    public String toString() {
        return String.format("Литера %s: %s (%d г., %s, %.1f кв.м, %.1f м, %.1f куб.м, %.2f руб.)",
            letter.get(), name.get(), yearBuilt.get(), wallMaterial.get(),
            buildingArea.get(), height.get(), volume.get(), inventoryValue.get());
    }
}
