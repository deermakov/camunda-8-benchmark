package benchmark.app.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import benchmark.app.api.BpmnEngine;
import benchmark.app.api.StartProcessInbound;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StartProcessUseCase implements StartProcessInbound {
    private final BpmnEngine bpmnEngine;

    @Override
    public String execute(String inputData) {
        String processExternalId = UUID.randomUUID().toString();
        bpmnEngine.startProcess(inputData, processExternalId);
        return processExternalId;
    }
}
