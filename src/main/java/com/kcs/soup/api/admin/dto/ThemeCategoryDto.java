package com.kcs.soup.api.admin.dto;

import com.kcs.soup.entity.mysql.Theme;
import com.kcs.soup.entity.mysql.ThemeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ThemeCategoryDto {
    private String mainCategory;
    private String subCategory;

    public ThemeCategory toThemeCategoryEntity(Theme theme) {
        return ThemeCategory.builder()
                .mainCategory(mainCategory)
                .subCategory(subCategory)
                .theme(theme)
                .build();
    }
}
