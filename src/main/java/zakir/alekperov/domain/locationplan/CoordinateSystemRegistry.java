package zakir.alekperov.domain.locationplan;

import java.util.HashMap;
import java.util.Map;

/**
 * Справочник местных систем координат (МСК) для регионов России.
 * 
 * Каждый субъект РФ имеет свою местную систему координат,
 * которая используется для геодезических работ и кадастра.
 */
public class CoordinateSystemRegistry {
    
    private static final Map<String, CoordinateSystem> SYSTEMS = new HashMap<>();
    
    static {
        // Центральный федеральный округ
        SYSTEMS.put("г. Москва", new CoordinateSystem("МСК-77", "Местная система координат г. Москвы"));
        SYSTEMS.put("Московская область", new CoordinateSystem("МСК-50", "Местная система координат Московской области"));
        SYSTEMS.put("Белгородская область", new CoordinateSystem("МСК-31", "Местная система координат Белгородской области"));
        SYSTEMS.put("Брянская область", new CoordinateSystem("МСК-32", "Местная система координат Брянской области"));
        SYSTEMS.put("Владимирская область", new CoordinateSystem("МСК-33", "Местная система координат Владимирской области"));
        SYSTEMS.put("Воронежская область", new CoordinateSystem("МСК-36", "Местная система координат Воронежской области"));
        SYSTEMS.put("Ивановская область", new CoordinateSystem("МСК-37", "Местная система координат Ивановской области"));
        SYSTEMS.put("Калужская область", new CoordinateSystem("МСК-40", "Местная система координат Калужской области"));
        SYSTEMS.put("Костромская область", new CoordinateSystem("МСК-44", "Местная система координат Костромской области"));
        SYSTEMS.put("Курская область", new CoordinateSystem("МСК-46", "Местная система координат Курской области"));
        SYSTEMS.put("Липецкая область", new CoordinateSystem("МСК-48", "Местная система координат Липецкой области"));
        SYSTEMS.put("Орловская область", new CoordinateSystem("МСК-57", "Местная система координат Орловской области"));
        SYSTEMS.put("Рязанская область", new CoordinateSystem("МСК-62", "Местная система координат Рязанской области"));
        SYSTEMS.put("Смоленская область", new CoordinateSystem("МСК-67", "Местная система координат Смоленской области"));
        SYSTEMS.put("Тамбовская область", new CoordinateSystem("МСК-68", "Местная система координат Тамбовской области"));
        SYSTEMS.put("Тверская область", new CoordinateSystem("МСК-69", "Местная система координат Тверской области"));
        SYSTEMS.put("Тульская область", new CoordinateSystem("МСК-71", "Местная система координат Тульской области"));
        SYSTEMS.put("Ярославская область", new CoordinateSystem("МСК-76", "Местная система координат Ярославской области"));
        
        // Северо-Западный федеральный округ
        SYSTEMS.put("г. Санкт-Петербург", new CoordinateSystem("МСК-78", "Местная система координат г. Санкт-Петербурга"));
        SYSTEMS.put("Ленинградская область", new CoordinateSystem("МСК-47", "Местная система координат Ленинградской области"));
        SYSTEMS.put("Новгородская область", new CoordinateSystem("МСК-53", "Местная система координат Новгородской области"));
        SYSTEMS.put("Псковская область", new CoordinateSystem("МСК-60", "Местная система координат Псковской области"));
        SYSTEMS.put("Калининградская область", new CoordinateSystem("МСК-39", "Местная система координат Калининградской области"));
        
        // Южный федеральный округ
        SYSTEMS.put("Республика Адыгея", new CoordinateSystem("МСК-01", "Местная система координат Республики Адыгея"));
        SYSTEMS.put("Республика Калмыкия", new CoordinateSystem("МСК-08", "Местная система координат Республики Калмыкия"));
        SYSTEMS.put("Республика Крым", new CoordinateSystem("МСК-91", "Местная система координат Республики Крым"));
        SYSTEMS.put("Краснодарский край", new CoordinateSystem("МСК-23", "Местная система координат Краснодарского края"));
        SYSTEMS.put("Астраханская область", new CoordinateSystem("МСК-30", "Местная система координат Астраханской области"));
        SYSTEMS.put("Волгоградская область", new CoordinateSystem("МСК-34", "Местная система координат Волгоградской области"));
        SYSTEMS.put("Ростовская область", new CoordinateSystem("МСК-61", "Местная система координат Ростовской области"));
        
        // Приволжский федеральный округ
        SYSTEMS.put("Республика Башкортостан", new CoordinateSystem("МСК-02", "Местная система координат Республики Башкортостан"));
        SYSTEMS.put("Республика Марий Эл", new CoordinateSystem("МСК-12", "Местная система координат Республики Марий Эл"));
        SYSTEMS.put("Республика Мордовия", new CoordinateSystem("МСК-13", "Местная система координат Республики Мордовия"));
        SYSTEMS.put("Республика Татарстан", new CoordinateSystem("МСК-16", "Местная система координат Республики Татарстан"));
        SYSTEMS.put("Удмуртская Республика", new CoordinateSystem("МСК-18", "Местная система координат Удмуртской Республики"));
        SYSTEMS.put("Чувашская Республика", new CoordinateSystem("МСК-21", "Местная система координат Чувашской Республики"));
        SYSTEMS.put("Пермский край", new CoordinateSystem("МСК-59", "Местная система координат Пермского края"));
        SYSTEMS.put("Кировская область", new CoordinateSystem("МСК-43", "Местная система координат Кировской области"));
        SYSTEMS.put("Нижегородская область", new CoordinateSystem("МСК-52", "Местная система координат Нижегородской области"));
        SYSTEMS.put("Оренбургская область", new CoordinateSystem("МСК-56", "Местная система координат Оренбургской области"));
        SYSTEMS.put("Пензенская область", new CoordinateSystem("МСК-58", "Местная система координат Пензенской области"));
        SYSTEMS.put("Самарская область", new CoordinateSystem("МСК-63", "Местная система координат Самарской области"));
        SYSTEMS.put("Саратовская область", new CoordinateSystem("МСК-64", "Местная система координат Саратовской области"));
        SYSTEMS.put("Ульяновская область", new CoordinateSystem("МСК-73", "Местная система координат Ульяновской области"));
        
        // Уральский федеральный округ
        SYSTEMS.put("Курганская область", new CoordinateSystem("МСК-45", "Местная система координат Курганской области"));
        SYSTEMS.put("Свердловская область", new CoordinateSystem("МСК-66", "Местная система координат Свердловской области"));
        SYSTEMS.put("Тюменская область", new CoordinateSystem("МСК-72", "Местная система координат Тюменской области"));
        SYSTEMS.put("Челябинская область", new CoordinateSystem("МСК-74", "Местная система координат Челябинской области"));
    }
    
    /**
     * Получить систему координат для указанного региона.
     * 
     * @param regionName название региона (субъекта РФ)
     * @return система координат или null, если регион не найден
     */
    public static CoordinateSystem getSystemForRegion(String regionName) {
        if (regionName == null || regionName.isBlank()) {
            return null;
        }
        
        // Прямое совпадение
        CoordinateSystem system = SYSTEMS.get(regionName.trim());
        if (system != null) {
            return system;
        }
        
        // Нечеткое совпадение (без учета регистра и лишних пробелов)
        String normalized = regionName.trim().toLowerCase();
        for (Map.Entry<String, CoordinateSystem> entry : SYSTEMS.entrySet()) {
            if (entry.getKey().toLowerCase().contains(normalized) || 
                normalized.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * Проверить, есть ли система координат для региона.
     */
    public static boolean hasSystemForRegion(String regionName) {
        return getSystemForRegion(regionName) != null;
    }
    
    /**
     * Класс, представляющий систему координат.
     */
    public static class CoordinateSystem {
        private final String code;          // Код системы (например, "МСК-67")
        private final String description;   // Полное название
        
        public CoordinateSystem(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getShortName() {
            return code;
        }
        
        @Override
        public String toString() {
            return code + " - " + description;
        }
    }
}
