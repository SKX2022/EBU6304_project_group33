package com.finance.model;// 请根据你的实际包结构调整

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DoubaoChatResponse {
    // Remove the code and msg fields, as they are not present in the actual response body

    private java.util.List<Choice> choices; // Resolve choices directly from the outermost layer

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

    // --- Inner classes remain unchanged ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("completion_tokens")
        private int completionTokens;
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("total_tokens")
        private int totalTokens;

        // If you want, you can add more fields from usage
        // For example:
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
        private int index; //Add an index field
        private Object logprobs; // logprobs can be null, or an object, using Object or JsonNode

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