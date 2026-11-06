package com.excuses.model;

public record CategoryDto(
        String id,
        String name,
        String icon,
        int count
) {
}
