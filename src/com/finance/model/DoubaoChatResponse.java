package com.finance.model;// 请根据你的实际包结构调整

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DoubaoChatResponse {
    // 移除 code 和 msg 字段，因为实际响应体中没有它们

    private java.util.List<Choice> choices; // 直接从最外层开始解析 choices

    private long created;
    private String id;
    private String model;

    @JsonProperty("service_tier")
    private String serviceTier;

    private String object;
    private Usage usage;

    // Getters and Setters for top-level fields
    public java.util.List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(java.util.List<Choice> choices) {
        this.choices = choices;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getServiceTier() {
        return serviceTier;
    }

    public void setServiceTier(String serviceTier) {
        this.serviceTier = serviceTier;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    // --- 内部类保持不变 ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("completion_tokens")
        private int completionTokens;
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("total_tokens")
        private int totalTokens;

        // 如果需要，可以添加更多来自 usage 的字段
        // 例如：
        // @JsonProperty("prompt_tokens_details")
        // private PromptTokensDetails promptTokensDetails;
        // @JsonProperty("completion_tokens_details")
        // private CompletionTokensDetails completionTokensDetails;


        // Getters and Setters
        public int getCompletionTokens() {
            return completionTokens;
        }

        public void setCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
        }

        public int getPromptTokens() {
            return promptTokens;
        }

        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }

        public int getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }
    }

    // 如果你还需要解析 prompt_tokens_details 或 completion_tokens_details，可以添加这些内部类
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class PromptTokensDetails {
    //     @JsonProperty("cached_tokens")
    //     private int cachedTokens;
    //     public int getCachedTokens() { return cachedTokens; }
    //     public void setCachedTokens(int cachedTokens) { this.cachedTokens = cachedTokens; }
    // }
    //
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class CompletionTokensDetails {
    //     @JsonProperty("reasoning_tokens")
    //     private int reasoningTokens;
    //     public int getReasoningTokens() { return reasoningTokens; }
    //     public void setReasoningTokens(int reasoningTokens) { this.reasoningTokens = reasoningTokens; }
    // }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Message message;
        @JsonProperty("finish_reason")
        private String finishReason;
        private int index; // 添加 index 字段
        private Object logprobs; // logprobs 可以是null，或者一个对象，使用Object或JsonNode

        // Getters and Setters
        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Object getLogprobs() {
            return logprobs;
        }

        public void setLogprobs(Object logprobs) {
            this.logprobs = logprobs;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;

        // Getters and Setters
        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}