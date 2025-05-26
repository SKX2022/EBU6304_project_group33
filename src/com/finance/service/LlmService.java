package com.finance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finance.model.DoubaoChatResponse; // 确保这个 DTO 路径正确

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LlmService {

    private String prompt;
    private String extractedAnswer;
    private String apiResponse; // Stores the raw API response

    // --- 火山引擎 Doubao API configuration ---
    // Please replace it with your actual oneAPI_KEY
    private static final String API_KEY = "d3d268fb-f434-43a1-ab47-e8865ab30da8"; // <-- Make sure that your API Key is correct
    private static final String HOST = "ark.cn-beijing.volces.com"; // API The host address may vary depending on the documentation
    private static final String PATH = "/api/v3/chat/completions"; // The path to Doubao 1.5 Lite
    private static final String MODEL_NAME = "doubao-1-5-lite-32k-250115"; // The name of the Doubao model used

    private static final long TIMEOUT_SECONDS = 60; // The request timeout period

    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper

   /**
     * Constructor, which is used to pass in prompts.
     *
     * @param prompt input text passed to the Doubao API.
     */
    public LlmService(String prompt) {
        this.prompt = prompt;
        this.extractedAnswer = null;
        this.apiResponse = null; // 初始化
        System.out.println("LlmService: The instance is created and the Prompt is set."); // 添加日志
    }

    /**
     * Execute a call to the Doubao API and store the result in an internal variable.
     * This method handles the construction, sending, and parsing of the request.
     *
     * @throws IOException if an IO error occurs.
     * @throws InterruptedException if the thread is interrupted while waiting for a response.
     * @throws LlmServiceException if the API returns an error status code or the parsing response fails.
     */
    public void callLlmApi() throws IOException, InterruptedException, LlmServiceException {
        System.out.println("LlmService: Start calling callLlmApi()..."); // Add logs
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();

        // 1. Build the request body (according to the Doubao API documentation)
        ObjectNode requestBodyJson = objectMapper.createObjectNode();
        requestBodyJson.put("model", MODEL_NAME);

        ArrayNode messages = requestBodyJson.putArray("messages");
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", this.prompt);

        String requestBody = objectMapper.writeValueAsString(requestBodyJson);
        System.out.println("LlmService:Request body (JSON) sent to API:\n" + requestBody); // **Print Request Body**

        // 2. Build an HTTP request (using API Key authentication)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://" + HOST + PATH))
                .header("Content-Type", "application/json")
                // Use your API_KEY in the Authorization header
                .header("Authorization", "Bearer " + API_KEY) // <--- Here are the key modifications
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        System.out.println("LlmService:Prepare to send a request to a URI:" + request.uri()); // **Print Request URI**
        System.out.println("LlmService: Request header Authorization: Bearer " + API_KEY.substring(0, Math.min(API_KEY.length(), 8)) + "..."); // **Print part of the API key and confirm the header information**

        // 3. Send a request and get a response
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.err.println("LlmService Error: HTTP request failed to be sent (IOException): " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw the exception
        } catch (InterruptedException e) {
            System.err.println("LlmService Error: HTTP request failed to be sent (InterruptedException): " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw the exception
        }


        int statusCode = response.statusCode();
        System.out.println("LlmService: Received API response with status code: " + statusCode); // **Print Response Status Code**

        if (statusCode >= 200 && statusCode < 300) {
            this.apiResponse = response.body();
            System.out.println("LlmService: Original API response body:\n" + this.apiResponse); // **Print original API response body**
            parseLlmApiResponse(); // Parse API responses and extract answers
        } else {
            System.err.println("LlmService Error: The Doubao API call failed, status code: " + statusCode + ", Response body: " + response.body()); // Add an error log
            throw new LlmServiceException("Doubao API The call failed, and the status code is displayed: " + statusCode + ", Response body: " + response.body());
        }
        System.out.println("LlmService: callLlmApi() Execution is complete"); // Add logs
    }

    /**
     * Parse the answer from the original JSON response of the Doubao API.
     * Now parses directly to DoubaoChatResponse, as its structure has been matched to the API return.
     *
     * @throws LlmServiceException if the API response cannot be resolved or the answer field cannot be found.
     */
    private void parseLlmApiResponse() throws LlmServiceException {
        System.out.println("LlmService: Start parsing API responses..."); //Add logs
        if (this.apiResponse == null || this.apiResponse.isEmpty()) {
            System.err.println("LlmService Error: The API response is empty and cannot be resolved."); // Add an error log
            throw new LlmServiceException("Doubao API The response is empty and cannot be resolved.");
        }

        try {

            // Deserialize the JSON string of the API response directly into a DoubaoChatResponse object
            DoubaoChatResponse doubaoResponse = objectMapper.readValue(this.apiResponse, DoubaoChatResponse.class);
            System.out.println("LlmService: Successfully deserialize the API response as a DoubaoChatResponse object。"); // 添加日志

            // Get answers based on the new DTO structure
            // Check that the choices list exists and is not empty
            if (doubaoResponse.getChoices() != null &&
                    !doubaoResponse.getChoices().isEmpty() &&
                    doubaoResponse.getChoices().get(0).getMessage() != null) { // Make sure that the message for the first choice is not empty

                this.extractedAnswer = doubaoResponse.getChoices().get(0).getMessage().getContent();
                System.out.println("LlmService: The answer was successfully extracted from the response."); // Add logs

                if (this.extractedAnswer == null || this.extractedAnswer.isEmpty()) {
                    System.err.println("LlmService Warning: The extracted answer is empty or does not exist."); // Add a warning log
                    throw new LlmServiceException("The answer extracted from the Doubao API response is empty or does not exist.");
                }
            } else {
                System.err.println("LlmService Error: The Doubao API response structure is not as expected and an answer could not be found."); // Add an error log
                System.err.println("LlmService Response body: " + this.apiResponse); // Print the response body for debugging
                throw new LlmServiceException("Doubao API The response structure was not as expected and no answer could be found. Response body: " + this.apiResponse);
            }

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("LlmService Error: JSON parsing failed: " + e.getMessage()); // Add an error log
            e.printStackTrace();
            throw new LlmServiceException("Failed to parse the Doubao API response: " + e.getMessage(), e);
        } catch (Exception e) { // Catch all other unknown anomalies
            System.err.println("LlmService Error: An unknown error occurred while parsing the API response: " + e.getMessage()); // Add an error log
            e.printStackTrace();
            throw new LlmServiceException("An unknown error occurred while parsing the Doubao API response: " + e.getMessage(), e);
        }
        System.out.println("LlmService: API The response is parsed."); // Add logs
    }

    /**
     * Get the answer returned by the Doubao API. You can't get it until callLlmApi() is called.
     *
     * @return answer to the Doubao API, if the API has not been called or the parsing fails, null will be returned.
     */
    public String getAnswer() {
        return extractedAnswer;
    }

  /**
     * Custom exception class to indicate Doubao API call or parsing failure.
     */
    public static class LlmServiceException extends Exception {
        public LlmServiceException(String message) {
            super(message);
        }

        public LlmServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}