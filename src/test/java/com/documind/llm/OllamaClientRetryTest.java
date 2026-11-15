package com.documind.llm;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OllamaClientRetryTest {
    private static final String OK_BODY="{\"message\":{\"role\":\"assistant\",\"content\":\"hello\"},\"done\":true}";
    private HttpServer startfakeServer(AtomicInteger calls, int failures,int failStatus) throws  Exception{
        HttpServer server =HttpServer.create(new InetSocketAddress(0),0);
        server.createContext("/api/chat",exchange -> {
            int n=calls.incrementAndGet();
            boolean fail = n<= failures;
            int status=fail?failStatus:200;
            byte[] body=(fail?"error":OK_BODY).getBytes();
            exchange.sendResponseHeaders(status,body.length);
            try(OutputStream os = exchange.getResponseBody()){
                os.write(body);
            }

        });
        server.start();
        return server;
    }
    private LlmClient clientFor(HttpServer server)
    {
        String baseUrl="http://localhost:"+server.getAddress().getPort();
        return new OllamaClient(baseUrl,"test-model");
    }
    @Test
    void retryTest() throws Exception{
        AtomicInteger calls = new AtomicInteger();
        HttpServer server = startfakeServer(calls,2,503);
        try{
            String reply = clientFor(server).chat(List.of(new Message("user","hi")));

            assertEquals("hello",reply);
            assertEquals(3,calls.get());

        }
        finally{
            server.stop(0);
        }
    }
    @Test
    void doesNotRetryTest() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        HttpServer server = startfakeServer(calls, 99, 400);
        try {
            LlmClient client = clientFor(server);
            assertThrows(RuntimeException.class, () -> client.chat(List.of(new Message("user", "hi"))));
            assertEquals(1, calls.get());

        } finally {
            server.stop(0);
        }
    }
    }

