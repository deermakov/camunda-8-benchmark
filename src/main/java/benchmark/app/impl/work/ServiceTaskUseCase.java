package benchmark.app.impl.work;

import benchmark.app.api.ServiceTaskInbound;
import benchmark.app.impl.StatisticsCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceTaskUseCase implements ServiceTaskInbound {

    private final StatisticsCollector statisticsCollector;

    @Override
    public void execute(Map<String, Object> variables) {
        variables.put("key1", "value1");
        statisticsCollector.incCompletedJobs();

        long cnt = statisticsCollector.getCompletedJobsMeter().getCount();
        if (cnt % 100 == 0) {
            log.info("Completed {} jobs", cnt);
        }
    }
}
