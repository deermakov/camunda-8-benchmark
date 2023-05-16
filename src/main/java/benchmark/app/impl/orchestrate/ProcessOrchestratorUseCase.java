package benchmark.app.impl.orchestrate;

import benchmark.app.api.BpmnEngine;
import benchmark.app.impl.TaskList;
import benchmark.domain.bpmn.BpmnUserTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Оркестратор процесса
 * Имитирует действия внешних участников - напр. пользователей или интегрируемых систем
 */
@Component
@RequiredArgsConstructor
public class ProcessOrchestratorUseCase {
    private final BpmnEngine bpmnEngine;
    private final TaskList taskList;

    public void execute() {
        long processInstanceKey = bpmnEngine.startProcess(null);

        // todo пауза по появления id таски из elastic'а (должна быть > 1 сек)
        Optional<BpmnUserTask> userTask = taskList.getActiveUserTasks(processInstanceKey, "user").stream().findFirst();
        long taskKey = userTask.map(BpmnUserTask::getKey).orElse(null);
        bpmnEngine.performUserTask(taskKey, null);
    }
}
