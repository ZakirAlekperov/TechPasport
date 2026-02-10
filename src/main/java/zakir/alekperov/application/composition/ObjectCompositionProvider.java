package zakir.alekperov.application.composition;

import zakir.alekperov.domain.building.BuildingComponent;

import java.math.BigDecimal;
import java.util.List;

/**
 * Временный источник данных для состава объекта.
 * Будет заменён на парсер Word / БД.
 */
public class ObjectCompositionProvider {

    public List<BuildingComponent> loadComponents() {
        return List.of(
                new BuildingComponent(
                        1,
                        "Жилой дом",
                        "Жилое",
                        new BigDecimal("1250.50"),
                        5
                ),
                new BuildingComponent(
                        2,
                        "Подвал",
                        "Техническое",
                        new BigDecimal("320.00"),
                        1
                ),
                new BuildingComponent(
                        3,
                        "Пристройка",
                        "Нежилое",
                        new BigDecimal("180.75"),
                        2
                )
        );
    }
}
