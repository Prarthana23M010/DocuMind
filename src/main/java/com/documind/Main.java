package com.documind;
import com.documind.llm.LlmClient;
import com.documind.llm.Message;
import com.documind.llm.OllamaClient;

import java.util.List;


public class Main{


    public static void main(String[] args) throws Exception{
        LlmClient client =new OllamaClient("http://localhost:11434","llama3.2");
        var message =List.of(
                new Message("system","You are a concise assistant."),
                new Message("user","Explain Java Virtual Threads in two sentences." )
        );
        System.out.println("---Blocking-----");
        System.out.println(client.chat(message));

        System.out.println("----Streaming-----");
        client.chatStream(message,token-> System.out.print(token));
        System.out.println();
    }
        }



//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
