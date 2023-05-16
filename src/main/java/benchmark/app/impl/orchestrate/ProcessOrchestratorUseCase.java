package benchmark.app.impl.orchestrate;

import benchmark.app.api.BpmnEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Оркестратор процесса
 * Имитирует действия внешних участников - напр. пользователей или интегрируемых систем
 */
@Component
@RequiredArgsConstructor
public class ProcessOrchestratorUseCase {
    private final BpmnEngine bpmnEngine;

    public void execute() {
        bpmnEngine.startProcess(null);

        long taskKey = -1;
        bpmnEngine.performUserTask(taskKey, null);
    }
}
