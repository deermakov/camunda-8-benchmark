package benchmark.app.api;

import java.util.Map;

public interface ServiceTaskInbound {
    void execute(Map<String, Object> variables);
}
