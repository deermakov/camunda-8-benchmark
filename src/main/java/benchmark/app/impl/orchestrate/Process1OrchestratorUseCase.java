package benchmark.app.impl.orchestrate;

import benchmark.app.api.BpmnEngine;
import benchmark.app.impl.TaskList;
import benchmark.domain.bpmn.BpmnUserTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Оркестратор процесса process-1.bpmn
 * Имитирует действия внешних участников - напр. пользователей или интегрируемых систем
 */
@Component
@RequiredArgsConstructor
public class Process1OrchestratorUseCase {
    private final String PROCESS_DEFINITION_ID = "process-1";

    private final BpmnEngine bpmnEngine;
    private final TaskList taskList;

    public void execute() {
        Map<String, Object> variables = new HashMap<>();
        long processInstanceKey = bpmnEngine.startProcess(PROCESS_DEFINITION_ID,variables);

        // todo пауза по появления id таски из elastic'а (должна быть > 1 сек)
        Optional<BpmnUserTask> userTask = taskList.getActiveUserTasks(processInstanceKey, "user").stream().findFirst();
        long taskKey = userTask.map(BpmnUserTask::getKey).orElse(null);
        bpmnEngine.performUserTask(taskKey, null);
    }
}
