package controller;


import org.springframework.ai.client.AiClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/springAi")
public class TestController {

    private final AiClient aiClient;

    public TestController(AiClient aiClient) {
        this.aiClient = aiClient;
    }


    @GetMapping("/ai/generate")
    public String generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return aiClient.generate(message);
    }

}