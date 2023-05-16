package benchmark.app.impl.work;

import org.springframework.stereotype.Component;
import benchmark.app.api.ServiceTaskInbound;

import java.text.MessageFormat;
import java.util.Map;

@Component
public class ServiceTaskUseCase implements ServiceTaskInbound {
    @Override
    public void execute(Map<String, Object> variables) {
        variables.put("key1", "value1");
    }
}
