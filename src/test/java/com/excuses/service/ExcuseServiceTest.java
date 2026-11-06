package com.excuses.service;

import com.excuses.model.CategoryDto;
import com.excuses.model.ExcuseDto;
import com.excuses.model.Language;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExcuseServiceTest {

    private ExcuseService excuseService;

    @BeforeEach
    void setUp() {
        excuseService = new ExcuseService(new ObjectMapper());
        excuseService.init();
    }

    @Test
    @DisplayName("Should support all 5 defined languages including Arabic RTL")
    void testSupportedLanguages() {
        List<Language> languages = excuseService.getSupportedLanguages();
        assertNotNull(languages);
        assertEquals(5, languages.size());

        List<String> codes = languages.stream().map(Language::code).toList();
        assertTrue(codes.containsAll(List.of("pl", "en", "es", "fr", "ar")));

        Language arabic = languages.stream().filter(l -> "ar".equals(l.code())).findFirst().orElseThrow();
        assertEquals("rtl", arabic.dir());
    }

    @Test
    @DisplayName("Should return localized categories including 'all'")
    void testGetCategories() {
        List<CategoryDto> categoriesPl = excuseService.getCategories("pl");
        assertNotNull(categoriesPl);
        // "all" + 4 categories = 5
        assertEquals(5, categoriesPl.size());
        assertEquals("all", categoriesPl.get(0).id());
        assertEquals("Wszystkie kategorie", categoriesPl.get(0).name());

        List<CategoryDto> categoriesEn = excuseService.getCategories("en");
        assertEquals("All Categories", categoriesEn.get(0).name());
    }

    @Test
    @DisplayName("Should generate random excuse for specific category")
    void testGetRandomExcuseSpecificCategory() {
        ExcuseDto excuse = excuseService.getRandomExcuse("work", "pl");
        assertNotNull(excuse);
        assertEquals("work", excuse.categoryId());
        assertEquals("pl", excuse.lang());
        assertNotNull(excuse.text());
        assertFalse(excuse.text().isBlank());
        assertEquals(20, excuse.totalAvailable());
    }

    @Test
    @DisplayName("Should generate random excuse across all categories")
    void testGetRandomExcuseAll() {
        ExcuseDto excuse = excuseService.getRandomExcuse("all", "en");
        assertNotNull(excuse);
        assertNotNull(excuse.categoryId());
        assertEquals("en", excuse.lang());
        assertNotNull(excuse.text());
        assertFalse(excuse.text().isBlank());
        assertEquals(80, excuse.totalAvailable());
    }

    @Test
    @DisplayName("Should provide UI translations dictionary for all 5 languages")
    void testUiTranslations() {
        Map<String, Map<String, String>> translations = excuseService.getUiTranslations();
        assertNotNull(translations);
        assertTrue(translations.containsKey("generateBtn"));
        Map<String, String> btnTranslations = translations.get("generateBtn");
        assertEquals("Wygeneruj wymówkę", btnTranslations.get("pl"));
        assertEquals("Generate Excuse", btnTranslations.get("en"));
        assertEquals("توليد عذر", btnTranslations.get("ar"));
        assertEquals("Generar excusa", btnTranslations.get("es"));
        assertEquals("Générer une excuse", btnTranslations.get("fr"));
    }
}
