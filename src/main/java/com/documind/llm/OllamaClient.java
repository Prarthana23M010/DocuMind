package com.documind.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.io.IOException;


public class OllamaClient implements LlmClient {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper json = new ObjectMapper();
    private final String baseUrl;
    private final String model;

    public OllamaClient(String baseUrl, String model) {
        this.baseUrl = baseUrl;
        this.model = model;
    }


   public String chat(List<Message> messages) throws Exception {
        HttpRequest request = buildRequest(messages, false);
        HttpResponse<String> response = sendWithRetry(request);
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP" + response.statusCode() + ":" + response.body());

        }

        return json.readTree(response.body()).path("message").path("content").asText();
    }

  public  void chatStream(List<Message> messages, Consumer<String> onToken) throws Exception {
        HttpRequest request = buildRequest(messages, true);
        HttpResponse<java.io.InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP" + response.statusCode());
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(response.body()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                JsonNode node = json.readTree(line);
                onToken.accept(node.path("message").path("content").asText());
                if (node.path("done").asBoolean()) break;

            }
        }
    }

    private HttpRequest buildRequest(List<Message> messages, boolean stream) throws Exception {
        String body = json.writeValueAsString(Map.of("model", model,
                "messages", messages,
                "stream", stream));
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/chat"))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }
    private HttpResponse<String> sendWithRetry(HttpRequest request) throws Exception{
        long delay=1000;
        int maxAttempts=4;
        for(int attempt=1;attempt<=maxAttempts;attempt++){
            try{
                HttpResponse<String> response=http.send(request,HttpResponse.BodyHandlers.ofString());
                if(response.statusCode()<500 || attempt==maxAttempts){
                    return response;
                }
                System.err.println("Attempt"+" "+ attempt+" "+"got HTTP"+" "+
                        response.statusCode()+",retrying in"+" "+delay+"ms");

            }
            catch (IOException e){
                if (attempt==maxAttempts){
                    throw e;
                }
                System.err.println("Attempts"+attempt+"failed,retrying in"+delay+"ms");
            }
            Thread.sleep(delay);
            delay*=2;
        }
        throw new IllegalStateException("Unreachable");
    }



}
