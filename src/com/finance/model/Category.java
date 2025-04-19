package com.finance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Category {
    private String name;

    // 使用 @JsonCreator 和 @JsonProperty 注解
    @JsonCreator
    public Category(@JsonProperty("name") String name) {
        this.name = name;
    }

    // Getter 和 Setter 方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}