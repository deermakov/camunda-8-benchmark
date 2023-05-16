package benchmark.app.api;

import java.util.Map;

public interface BpmnEngine {
    void startProcess(Map<String, Object> variables);

    void performUserTask(long taskKey, Map<String, Object> variables);
}
