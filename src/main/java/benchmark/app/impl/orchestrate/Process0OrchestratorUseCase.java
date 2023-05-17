package benchmark.app.impl.orchestrate;

import benchmark.app.api.BpmnEngine;
import benchmark.app.impl.TaskList;
import benchmark.domain.bpmn.BpmnUserTask;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Оркестратор процесса process-0.bpmn
 * Имитирует действия внешних участников - напр. пользователей или интегрируемых систем
 */
@Component
@RequiredArgsConstructor
public class Process0OrchestratorUseCase {
    private final String PROCESS_DEFINITION_ID = "process-0";
    private final BpmnEngine bpmnEngine;

    @EventListener(ApplicationReadyEvent.class)
    public void execute() {
        Map<String, Object> variables = new HashMap<>();
        bpmnEngine.startProcess(PROCESS_DEFINITION_ID,variables);
    }
}
