package zakir.alekperov.service.address;

/**
 * Модель подсказки адреса от DaData
 */
public class AddressSuggestion {
    private final String value;           // Короткое значение
    private final String unrestricted;    // Полное значение
    private final String postalCode;      // Почтовый индекс
    private final String region;          // Регион
    private final String city;            // Город
    private final String street;          // Улица
    private final String house;           // Дом
    private final String kladrId;         // ID в КЛАДР
    private final String fiasId;          // ID в ФИАС
    
    public AddressSuggestion(String value, String unrestricted, String postalCode,
                           String region, String city, String street, String house,
                           String kladrId, String fiasId) {
        this.value = value;
        this.unrestricted = unrestricted;
        this.postalCode = postalCode;
        this.region = region;
        this.city = city;
        this.street = street;
        this.house = house;
        this.kladrId = kladrId;
        this.fiasId = fiasId;
    }
    
    // Getters
    public String getValue() { return value; }
    public String getUnrestricted() { return unrestricted; }
    public String getPostalCode() { return postalCode; }
    public String getRegion() { return region; }
    public String getCity() { return city; }
    public String getStreet() { return street; }
    public String getHouse() { return house; }
    public String getKladrId() { return kladrId; }
    public String getFiasId() { return fiasId; }
    
    @Override
    public String toString() {
        return value;
    }

    /**
 * Получить только название без типа (без "г", "ул", "д" и т.д.)
 */
public String getCleanValue() {
    // Убираем префиксы типа "г", "обл", "ул" и т.д.
    String clean = value;
    
    // Список префиксов для удаления
    String[] prefixes = {"г ", "обл ", "край ", "респ ", "ул ", "пер ", "д ", "корп ", "стр "};
    
    for (String prefix : prefixes) {
        if (clean.startsWith(prefix)) {
            clean = clean.substring(prefix.length());
        }
    }
    
    return clean.trim();
}

}
