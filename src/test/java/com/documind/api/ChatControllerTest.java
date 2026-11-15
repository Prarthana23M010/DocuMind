package com.documind.api;

import com.documind.llm.LlmClient;
import com.documind.llm.Message;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatControllerTest {

    static class FakeLlmClient implements LlmClient {
        final List<List<Message>> received =new ArrayList<>();

        @Override
        public String chat(List<Message> messages){
            received.add(messages);
            return "fake reply";
        }
        @Override
        public void chatStream(List<Message> messages ,Consumer<String> onToken)
        {
           onToken.accept("fake");
           onToken.accept("stream");

        }
    }

    @Test
    void chatReturnsTheReplyFromTheLlmClient() throws Exception{
        FakeLlmClient fake=new FakeLlmClient();
        ChatController controller=new ChatController(fake);
        ChatResponse   response= controller.chat(new ChatRequest("hello"));
        assertEquals("fake reply",response.reply());
    }

    @Test
    void chatSendSystemAndUserMessagesToTheLlmClient() throws Exception{
        FakeLlmClient fake=new FakeLlmClient();
        ChatController controller=new ChatController(fake);

        controller.chat(new ChatRequest("hello"));

        assertEquals(1,fake.received.size());
        List<Message> sent = fake.received.get(0);
        assertEquals(2,sent.size());
        assertEquals("system",sent.get(0).role());
        assertEquals("user",sent.get(1).role());
        assertEquals("hello",sent.get(1).content());
    }
}
