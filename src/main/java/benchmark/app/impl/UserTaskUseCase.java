package benchmark.app.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import benchmark.app.api.BpmnEngine;
import benchmark.app.api.UserTaskInbound;

@Component
@RequiredArgsConstructor
public class UserTaskUseCase implements UserTaskInbound {

    private final BpmnEngine bpmnEngine;

    @Override
    public void execute(long taskKey, String inputData) {
        bpmnEngine.performUserTask(taskKey, inputData);
    }
}
