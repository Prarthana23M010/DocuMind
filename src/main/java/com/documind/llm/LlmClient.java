package com.documind.llm;
import java.util.List;
import java.util.function.Consumer;


public interface LlmClient {
    String chat(List<Message> messages) throws Exception;
    void chatStream(List<Message> messages,Consumer<String> onToken) throws Exception;

}
