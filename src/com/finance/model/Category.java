package com.finance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Category {
    private String type; // Classification type, such as "Income" or "Expense"
    private String name; // Classification name

    // Use @JsonCreator and @JsonProperty annotations
    @JsonCreator
    public Category(@JsonProperty("type") String type, @JsonProperty("name") String name) {
        this.type = type;
        this.name = name;
    }

    // Getter and Setter methods
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}