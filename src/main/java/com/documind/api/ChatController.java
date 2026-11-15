package com.documind.api;

import com.documind.llm.LlmClient;
import com.documind.llm.Message;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.security.sasl.SaslServer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@RestController
public class ChatController {
    private  final LlmClient llm;
    public ChatController(LlmClient llm) {
        this.llm = llm;
    }
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request)  throws Exception {
        String reply =llm.chat(List.of(
                new Message("system","You are a concise assistant."),
                new Message("user", request.message())
        ));
        return new ChatResponse(reply);
    }
    @GetMapping(value="/chat/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public  SseEmitter stream(@RequestParam String message)
    {
        SseEmitter emitter = new SseEmitter(300_000L);
        Thread.startVirtualThread(()  -> {
            try{
                llm.chatStream(List.of(
                        new Message("system","You are a concise assistant."),
                        new Message("user",message)),
                        token ->{
                    try{
                        emitter.send(token);

                    }
                    catch (IOException e)
                    {
                        throw new UncheckedIOException(e);
                    }
                        }
                );
                emitter.complete();
            }
            catch (Exception e)
            {
                emitter.complete();
            }
        });
        return emitter;
    }
}
