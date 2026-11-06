package com.excuses.controller;

import com.excuses.model.CategoryDto;
import com.excuses.model.ExcuseDto;
import com.excuses.model.Language;
import com.excuses.service.ExcuseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExcuseApiController {

    private final ExcuseService excuseService;

    public ExcuseApiController(ExcuseService excuseService) {
        this.excuseService = excuseService;
    }

    @GetMapping("/languages")
    public ResponseEntity<List<Language>> getLanguages() {
        return ResponseEntity.ok(excuseService.getSupportedLanguages());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getCategories(
            @RequestParam(name = "lang", defaultValue = "en") String lang
    ) {
        return ResponseEntity.ok(excuseService.getCategories(lang));
    }

    @GetMapping("/excuse/random")
    public ResponseEntity<ExcuseDto> getRandomExcuse(
            @RequestParam(name = "category", defaultValue = "all") String category,
            @RequestParam(name = "lang", defaultValue = "en") String lang
    ) {
        return ResponseEntity.ok(excuseService.getRandomExcuse(category, lang));
    }

    @GetMapping("/ui-translations")
    public ResponseEntity<Map<String, Map<String, String>>> getUiTranslations() {
        return ResponseEntity.ok(excuseService.getUiTranslations());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
