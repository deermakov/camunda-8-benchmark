package benchmark.app.api;

import java.util.Map;

public interface BpmnEngine {
    long startProcess(String processDefinitionId, Map<String, Object> variables);

    void performUserTask(long taskKey, Map<String, Object> variables);
}
