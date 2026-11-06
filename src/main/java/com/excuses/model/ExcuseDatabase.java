package com.excuses.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExcuseDatabase {

    private Map<String, CategoryDefinition> categories = Collections.emptyMap();

    public Map<String, CategoryDefinition> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, CategoryDefinition> categories) {
        this.categories = categories;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryDefinition {
        private Map<String, String> name = Collections.emptyMap();
        private List<Map<String, String>> excuses = Collections.emptyList();

        public Map<String, String> getName() {
            return name;
        }

        public void setName(Map<String, String> name) {
            this.name = name;
        }

        public List<Map<String, String>> getExcuses() {
            return excuses;
        }

        public void setExcuses(List<Map<String, String>> excuses) {
            this.excuses = excuses;
        }
    }
}
