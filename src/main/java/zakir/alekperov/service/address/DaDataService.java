package zakir.alekperov.service.address;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для работы с API DaData - подсказки адресов ФИАС/КЛАДР
 */
public class DaDataService {
    
    private static final String API_URL = "https://suggestions.dadata.ru/suggestions/api/4_1/rs/suggest/address";
    private static final String API_KEY = "3f921259458d51b26e1aff0e74be9a6ac5c14c19"; // Получить на https://dadata.ru
    
    private final OkHttpClient client;
    private final Gson gson;
    
    public DaDataService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }
    
    /**
     * Поиск адресов по запросу
     * @param query текст для поиска
     * @param count количество результатов (максимум 20)
     * @return список адресов
     */
    public List<AddressSuggestion> getSuggestions(String query, int count) {
        List<AddressSuggestion> suggestions = new ArrayList<>();
        
        if (query == null || query.trim().isEmpty()) {
            return suggestions;
        }
        
        try {
            // Формируем JSON запрос
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("query", query);
            requestBody.addProperty("count", Math.min(count, 20));
            
            // Создаем HTTP запрос
            RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
            );
            
            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Token " + API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .post(body)
                    .build();
            
            // Выполняем запрос
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                    JsonArray jsonSuggestions = jsonObject.getAsJsonArray("suggestions");
                    
                    // Парсим результаты
                    for (int i = 0; i < jsonSuggestions.size(); i++) {
                        JsonObject suggestion = jsonSuggestions.get(i).getAsJsonObject();
                        suggestions.add(parseSuggestion(suggestion));
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Ошибка запроса к DaData API: " + e.getMessage());
        }
        
        return suggestions;
    }
    
    /**
     * Поиск регионов
     */
    public List<AddressSuggestion> getRegions(String query) {
        return getSuggestionsWithFilter(query, "region", null);
    }
    
    /**
     * Поиск городов в регионе
     */
    public List<AddressSuggestion> getCities(String query, String regionKladr) {
        return getSuggestionsWithFilter(query, "city", regionKladr);
    }
    
    /**
     * Поиск улиц в городе
     */
    public List<AddressSuggestion> getStreets(String query, String cityKladr) {
        return getSuggestionsWithFilter(query, "street", cityKladr);
    }
    
    /**
     * Поиск домов на улице
     */
    public List<AddressSuggestion> getHouses(String query, String streetKladr) {
        return getSuggestionsWithFilter(query, "house", streetKladr);
    }
    
    private AddressSuggestion parseSuggestion(JsonObject suggestion) {
        String value = suggestion.get("value").getAsString();
        String unrestricted = suggestion.get("unrestricted_value").getAsString();
        
        JsonObject data = suggestion.getAsJsonObject("data");
        
        String postalCode = getStringOrNull(data, "postal_code");
        String region = getStringOrNull(data, "region");
        String city = getStringOrNull(data, "city");
        String street = getStringOrNull(data, "street");
        String house = getStringOrNull(data, "house");
        String kladrId = getStringOrNull(data, "kladr_id");
        String fiasId = getStringOrNull(data, "fias_id");
        
        return new AddressSuggestion(
            value, unrestricted, postalCode,
            region, city, street, house,
            kladrId, fiasId
        );
    }
    
    private String getStringOrNull(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() 
            ? obj.get(key).getAsString() 
            : null;
    }

 
    public List<AddressSuggestion> getSuggestionsWithFilter(
        String query, String level, String parentKladr) {
    
    // Реализация уже есть в вашем коде, это публичная версия
    List<AddressSuggestion> suggestions = new ArrayList<>();
    
    try {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("query", query);
        requestBody.addProperty("count", 10);
        
        // Фильтр по уровню
        JsonObject fromBound = new JsonObject();
        fromBound.addProperty("value", level);
        requestBody.add("from_bound", fromBound);
        
        JsonObject toBound = new JsonObject();
        toBound.addProperty("value", level);
        requestBody.add("to_bound", toBound);
        
        // Фильтр по родительскому элементу
        if (parentKladr != null && !parentKladr.isEmpty()) {
            JsonArray locations = new JsonArray();
            JsonObject location = new JsonObject();
            location.addProperty("kladr_id", parentKladr);
            locations.add(location);
            requestBody.add("locations", locations);
        }
        
        RequestBody body = RequestBody.create(
            requestBody.toString(),
            MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Token " + API_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                JsonArray jsonSuggestions = jsonObject.getAsJsonArray("suggestions");
                
                for (int i = 0; i < jsonSuggestions.size(); i++) {
                    JsonObject suggestion = jsonSuggestions.get(i).getAsJsonObject();
                    suggestions.add(parseSuggestion(suggestion));
                }
            }
        }
        
    } catch (IOException e) {
        System.err.println("Ошибка запроса: " + e.getMessage());
    }
    
    return suggestions;
}

/**
 * Получить список элементов уровня без фильтрации по тексту
 * Используется для автоматического показа списка при выборе родителя
 */
public List<AddressSuggestion> getListByLevel(String level, String parentKladr) {
    List<AddressSuggestion> suggestions = new ArrayList<>();
    
    try {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("query", ""); // Пустой запрос для получения всех
        requestBody.addProperty("count", 20);
        
        // Фильтр по уровню
        JsonObject fromBound = new JsonObject();
        fromBound.addProperty("value", level);
        requestBody.add("from_bound", fromBound);
        
        JsonObject toBound = new JsonObject();
        toBound.addProperty("value", level);
        requestBody.add("to_bound", toBound);
        
        // Фильтр по родительскому элементу
        if (parentKladr != null && !parentKladr.isEmpty()) {
            JsonArray locations = new JsonArray();
            JsonObject location = new JsonObject();
            location.addProperty("kladr_id", parentKladr);
            locations.add(location);
            requestBody.add("locations", locations);
        }
        
        RequestBody body = RequestBody.create(
            requestBody.toString(),
            MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Token " + API_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                JsonArray jsonSuggestions = jsonObject.getAsJsonArray("suggestions");
                
                for (int i = 0; i < jsonSuggestions.size(); i++) {
                    JsonObject suggestion = jsonSuggestions.get(i).getAsJsonObject();
                    suggestions.add(parseSuggestion(suggestion));
                }
            }
        }
        
    } catch (IOException e) {
        System.err.println("Ошибка запроса списка: " + e.getMessage());
    }
    
    return suggestions;
}



}
