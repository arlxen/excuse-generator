package com.excuses.service;

import com.excuses.model.CategoryDto;
import com.excuses.model.ExcuseDatabase;
import com.excuses.model.ExcuseDto;
import com.excuses.model.Language;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ExcuseService {

    private static final Logger log = LoggerFactory.getLogger(ExcuseService.class);

    private final ObjectMapper objectMapper;

    @Value("${excuses.file.path:excuses.json}")
    private String excusesFilePath = "excuses.json";

    private ExcuseDatabase database = new ExcuseDatabase();

    private final List<Language> languages = List.of(
            new Language("pl", "Polish", "Polski", "🇵🇱", "ltr"),
            new Language("en", "English", "English", "🇬🇧", "ltr"),
            new Language("es", "Spanish", "Español", "🇪🇸", "ltr"),
            new Language("fr", "French", "Français", "🇫🇷", "ltr"),
            new Language("ar", "Arabic", "العربية", "🇸🇦", "rtl")
    );

    private final Map<String, String> categoryIcons = Map.of(
            "all", "✨",
            "social", "🥂",
            "work", "💼",
            "school", "🎓",
            "fitness", "🏋️"
    );

    private final Map<String, String> allCategoryNames = Map.of(
            "pl", "Wszystkie kategorie",
            "en", "All Categories",
            "ar", "جميع الفئات",
            "es", "Todas las categorías",
            "fr", "Toutes les catégories"
    );

    public ExcuseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        loadData();
    }

    public void setExcusesFilePath(String excusesFilePath) {
        this.excusesFilePath = excusesFilePath;
    }

    public synchronized void loadData() {
        try {
            if (excusesFilePath != null && !excusesFilePath.isBlank()) {
                File externalFile = new File(excusesFilePath);
                if (externalFile.exists() && externalFile.isFile()) {
                    log.info("Loading excuses from external file: {}", externalFile.getAbsolutePath());
                    this.database = objectMapper.readValue(externalFile, ExcuseDatabase.class);
                    return;
                }
            }

            File currentDirFile = new File("excuses.json");
            if (currentDirFile.exists() && currentDirFile.isFile()) {
                log.info("Loading excuses from current directory: {}", currentDirFile.getAbsolutePath());
                this.database = objectMapper.readValue(currentDirFile, ExcuseDatabase.class);
                return;
            }

            ClassPathResource resource = new ClassPathResource("excuses.json");
            if (resource.exists()) {
                log.info("Loading excuses from classpath resource: excuses.json");
                try (InputStream is = resource.getInputStream()) {
                    this.database = objectMapper.readValue(is, ExcuseDatabase.class);
                    return;
                }
            }

            log.warn("Could not find excuses.json file in filesystem or classpath!");
        } catch (Exception e) {
            log.error("Failed to parse excuses.json: {}", e.getMessage(), e);
        }
    }

    public List<Language> getSupportedLanguages() {
        return languages;
    }

    public List<CategoryDto> getCategories(String lang) {
        String effectiveLang = normalizeLang(lang);
        List<CategoryDto> list = new ArrayList<>();

        int totalCount = database.getCategories().values().stream()
                .mapToInt(cat -> cat.getExcuses() != null ? cat.getExcuses().size() : 0)
                .sum();

        String allName = allCategoryNames.getOrDefault(effectiveLang, allCategoryNames.get("en"));
        list.add(new CategoryDto("all", allName, categoryIcons.getOrDefault("all", "✨"), totalCount));

        for (Map.Entry<String, ExcuseDatabase.CategoryDefinition> entry : database.getCategories().entrySet()) {
            String catId = entry.getKey();
            ExcuseDatabase.CategoryDefinition catDef = entry.getValue();

            String localizedName = null;
            if (catDef.getName() != null) {
                localizedName = catDef.getName().get(effectiveLang);
                if (localizedName == null) {
                    localizedName = catDef.getName().get("en");
                }
            }
            if (localizedName == null) {
                localizedName = catId;
            }

            int count = catDef.getExcuses() != null ? catDef.getExcuses().size() : 0;
            String icon = categoryIcons.getOrDefault(catId, "📌");
            list.add(new CategoryDto(catId, localizedName, icon, count));
        }

        return list;
    }

    public ExcuseDto getRandomExcuse(String categoryId, String lang) {
        String effectiveLang = normalizeLang(lang);
        record ExcuseItem(String text, String categoryId, String categoryName) {}
        List<ExcuseItem> candidates = new ArrayList<>();

        Map<String, ExcuseDatabase.CategoryDefinition> categories = database.getCategories();
        if (categories == null || categories.isEmpty()) {
            return new ExcuseDto("No excuses loaded", "unknown", "Unknown", "❓", effectiveLang, 0);
        }

        if (categoryId != null && !categoryId.isBlank() && !"all".equalsIgnoreCase(categoryId)) {
            ExcuseDatabase.CategoryDefinition catDef = categories.get(categoryId.toLowerCase());
            if (catDef != null && catDef.getExcuses() != null) {
                String catName = getCategoryLocalizedName(catDef, categoryId, effectiveLang);
                for (Map<String, String> excMap : catDef.getExcuses()) {
                    String text = getLocalizedText(excMap, effectiveLang);
                    if (text != null && !text.isBlank()) {
                        candidates.add(new ExcuseItem(text, categoryId.toLowerCase(), catName));
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            // Pick from all categories
            for (Map.Entry<String, ExcuseDatabase.CategoryDefinition> entry : categories.entrySet()) {
                String catId = entry.getKey();
                ExcuseDatabase.CategoryDefinition catDef = entry.getValue();
                String catName = getCategoryLocalizedName(catDef, catId, effectiveLang);
                if (catDef.getExcuses() != null) {
                    for (Map<String, String> excMap : catDef.getExcuses()) {
                        String text = getLocalizedText(excMap, effectiveLang);
                        if (text != null && !text.isBlank()) {
                            candidates.add(new ExcuseItem(text, catId, catName));
                        }
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            return new ExcuseDto("No excuses available for this selection.", "unknown", "Unknown", "❓", effectiveLang, 0);
        }

        int index = ThreadLocalRandom.current().nextInt(candidates.size());
        ExcuseItem chosen = candidates.get(index);
        String icon = categoryIcons.getOrDefault(chosen.categoryId(), "📌");

        return new ExcuseDto(
                chosen.text(),
                chosen.categoryId(),
                chosen.categoryName(),
                icon,
                effectiveLang,
                candidates.size()
        );
    }

    private String getCategoryLocalizedName(ExcuseDatabase.CategoryDefinition catDef, String catId, String lang) {
        if (catDef.getName() != null) {
            String name = catDef.getName().get(lang);
            if (name != null) return name;
            String en = catDef.getName().get("en");
            if (en != null) return en;
        }
        return catId;
    }

    private String getLocalizedText(Map<String, String> map, String lang) {
        if (map == null) return null;
        if (map.containsKey(lang)) return map.get(lang);
        if (map.containsKey("en")) return map.get("en");
        if (map.containsKey("pl")) return map.get("pl");
        return map.values().stream().findFirst().orElse(null);
    }

    private String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) {
            return "en";
        }
        String clean = lang.trim().toLowerCase();
        for (Language l : languages) {
            if (l.code().equalsIgnoreCase(clean)) {
                return l.code();
            }
        }
        return "en";
    }

    public Map<String, Map<String, String>> getUiTranslations() {
        return Map.of(
                "title", Map.of(
                        "pl", "Generator Wymówek",
                        "en", "Excuse Generator",
                        "ar", "مُوَلِّد الأعذار",
                        "es", "Generador de Excusas",
                        "fr", "Générateur d'Excuses"
                ),
                "subtitle", Map.of(
                        "pl", "Potrzebujesz bezbłędnej wymówki? Wybierz język oraz kategorię i kliknij przycisk poniżej!",
                        "en", "Need an airtight excuse? Select your language and category, then hit generate!",
                        "ar", "هل تحتاج إلى عذر لا تشوبه شائبة؟ اختر لغتك وفئتك واضغط على الزر أدناه!",
                        "es", "¿Necesitas una excusa perfecta? Selecciona tu idioma y categoría, ¡y pulsa el botón!",
                        "fr", "Besoin d'une excuse imparable ? Choisissez votre langue et catégorie, puis cliquez ci-dessous !"
                ),
                "langLabel", Map.of(
                        "pl", "Język:",
                        "en", "Language:",
                        "ar", "اللغة:",
                        "es", "Idioma:",
                        "fr", "Langue :"
                ),
                "catLabel", Map.of(
                        "pl", "Kategoria wymówki:",
                        "en", "Excuse Category:",
                        "ar", "فئة العذر:",
                        "es", "Categoría de excusa:",
                        "fr", "Catégorie d'excuse :"
                ),
                "generateBtn", Map.of(
                        "pl", "Wygeneruj wymówkę",
                        "en", "Generate Excuse",
                        "ar", "توليد عذر",
                        "es", "Generar excusa",
                        "fr", "Générer une excuse"
                ),
                "copyBtn", Map.of(
                        "pl", "Kopiuj",
                        "en", "Copy",
                        "ar", "نسخ",
                        "es", "Copiar",
                        "fr", "Copier"
                ),
                "copiedBtn", Map.of(
                        "pl", "Skopiowano!",
                        "en", "Copied!",
                        "ar", "تم النسخ!",
                        "es", "¡Copiado!",
                        "fr", "Copié !"
                ),
                "initialPrompt", Map.of(
                        "pl", "Kliknij przycisk powyżej, aby wylosować idealną wymówkę.",
                        "en", "Click the button above to roll a perfect excuse.",
                        "ar", "انقر فوق الزر أعلاه للحصول على عذر مثالي.",
                        "es", "Haz clic en el botón de arriba para obtener una excusa perfecta.",
                        "fr", "Cliquez sur le bouton ci-dessus pour obtenir une excuse parfaite."
                ),
                "totalCountLabel", Map.of(
                        "pl", "Dostępnych wymówek w bazie:",
                        "en", "Total excuses in database:",
                        "ar", "إجمالي الأعذار في القاعدة:",
                        "es", "Excusas totales disponibles:",
                        "fr", "Total des excuses disponibles :"
                ),
                "badgeHint", Map.of(
                        "pl", "Kategoria:",
                        "en", "Category:",
                        "ar", "الفئة:",
                        "es", "Categoría:",
                        "fr", "Catégorie :"
                )
        );
    }
}
