package com.excuses.model;

public record ExcuseDto(
        String text,
        String categoryId,
        String categoryName,
        String categoryIcon,
        String lang,
        int totalAvailable
) {
}
