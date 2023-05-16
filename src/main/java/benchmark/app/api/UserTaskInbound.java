package benchmark.app.api;

public interface UserTaskInbound {
    void execute(long taskKey, String inputData);
}
