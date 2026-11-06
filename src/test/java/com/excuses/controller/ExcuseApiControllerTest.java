package com.excuses.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExcuseApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/languages returns 5 languages")
    void testGetLanguages() throws Exception {
        mockMvc.perform(get("/api/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].code", hasItems("pl", "en", "ar", "es", "fr")));
    }

    @Test
    @DisplayName("GET /api/categories?lang=pl returns categories in Polish")
    void testGetCategories() throws Exception {
        mockMvc.perform(get("/api/categories").param("lang", "pl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].id", is("all")))
                .andExpect(jsonPath("$[0].name", is("Wszystkie kategorie")))
                .andExpect(jsonPath("$[0].count", is(80)));
    }

    @Test
    @DisplayName("GET /api/excuse/random returns valid excuse")
    void testGetRandomExcuse() throws Exception {
        mockMvc.perform(get("/api/excuse/random")
                        .param("category", "fitness")
                        .param("lang", "es"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId", is("fitness")))
                .andExpect(jsonPath("$.lang", is("es")))
                .andExpect(jsonPath("$.text", not(emptyOrNullString())));
    }

    @Test
    @DisplayName("GET /api/ui-translations returns translations")
    void testGetUiTranslations() throws Exception {
        mockMvc.perform(get("/api/ui-translations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generateBtn.pl", is("Wygeneruj wymówkę")))
                .andExpect(jsonPath("$.generateBtn.en", is("Generate Excuse")));
    }
}
