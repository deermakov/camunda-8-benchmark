package benchmark.adapter.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import benchmark.app.api.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class FrontController {

    private final StartProcessInbound startProcessInbound;
    private final UserTaskInbound inputDataInbound;

    @PostMapping("/start")
    public StartProcessResponseDto startProcess(@RequestBody StartProcessRequestDto request) {
        log.info("startProcess(): {}", request);
        String processId = startProcessInbound.execute(request.getStartParam());
        return new StartProcessResponseDto(processId);
    }

    @PostMapping("/user-task")
    public void userTask(@RequestBody UserTaskDto request) {
        log.info("userTask(): {}", request);
        inputDataInbound.execute(request.getTaskKey(), request.getInputData());
    }

}
