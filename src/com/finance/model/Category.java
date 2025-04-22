package com.finance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Category {
    private String type; // 分类类型，例如“收入”或“支出”
    private String name; // 分类名称

    // 使用 @JsonCreator 和 @JsonProperty 注解
    @JsonCreator
    public Category(@JsonProperty("type") String type, @JsonProperty("name") String name) {
        this.type = type;
        this.name = name;
    }

    // Getter 和 Setter 方法
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