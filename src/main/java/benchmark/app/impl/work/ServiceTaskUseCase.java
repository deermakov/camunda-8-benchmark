package benchmark.app.impl.work;

import benchmark.app.api.ServiceTaskInbound;
import benchmark.app.impl.StatisticsCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ServiceTaskUseCase implements ServiceTaskInbound {

    private final StatisticsCollector statisticsCollector;
    @Override
    public void execute(Map<String, Object> variables) {
        variables.put("key1", "value1");
        statisticsCollector.incCompletedJobs();
    }
}
