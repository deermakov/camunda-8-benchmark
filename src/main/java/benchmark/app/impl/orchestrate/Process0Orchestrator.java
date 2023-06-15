package benchmark.app.impl.orchestrate;

import benchmark.app.api.BpmnEngine;
import benchmark.app.impl.StatisticsCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Оркестратор процесса process-0.bpmn
 * Имитирует действия внешних участников - напр. пользователей или интегрируемых систем
 *
 * Реализует адаптивный алгоритм нагрузки:
 *  при ошибке создания процесса скачком увеличивает задержку до следующей попытки,
 *  при успешном создании - славно уменьшает задержку
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Process0Orchestrator {
    private final String PROCESS_DEFINITION_ID = "process-0";
    private final BpmnEngine bpmnEngine;
    private final StatisticsCollector statisticsCollector;

    private AtomicLong startDelay = new AtomicLong(10);// milliseconds before next process start
    private AtomicLong startedProcessInstances = new AtomicLong(0);
    private long lastLoggedProcessInstance = 0;

    @Autowired
    private final ApplicationContext applicationContext;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        Process0Orchestrator myself = applicationContext.getBean(Process0Orchestrator.class);
        while(startedProcessInstances.get() < 1000000){
            try {
                Thread.sleep(startDelay.get());
                if (startedProcessInstances.get() - lastLoggedProcessInstance >= 100){
                    log.info("Starting {} processes, delay = {}", startedProcessInstances.get(), startDelay.get());
                    lastLoggedProcessInstance += 100;
                }
                myself.startProcessInstance();
            } catch (Exception e) {
                log.error("", e);
            }
        }
        log.info("All process '{}' instances are started", PROCESS_DEFINITION_ID);
    }

    @Async("processStartExecutor")
    public void startProcessInstance() {
        Map<String, Object> variables = new HashMap<>();
        long processInstanceId = bpmnEngine.startProcess(PROCESS_DEFINITION_ID, variables);
        if (processInstanceId < 0) {// запуск процесса не удался
            startDelay.updateAndGet(operand -> operand < 98 ? operand+2 : 100); // увеличиваем задержку (резко)
            statisticsCollector.incStartedProcessInstancesException(""); // увеличиваем метрику ошибок запуска процесса
        } else { // запуск процесса удался
            startDelay.updateAndGet(operand -> operand > 1 ? --operand : 1); // уменьшаем задержку (плавно)
            startedProcessInstances.incrementAndGet(); // увеличиваем локальный счетчик запущенных процессов
            statisticsCollector.incStartedProcessInstances(); // увеличиваем метрику успешных запусков процесса
        }
    }

}
