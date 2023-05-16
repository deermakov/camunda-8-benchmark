package benchmark.app.api;

public interface BpmnEngine {
    void startProcess(String startParam, String processExternalId);

    void performUserTask(long taskKey, String inputData);
}
