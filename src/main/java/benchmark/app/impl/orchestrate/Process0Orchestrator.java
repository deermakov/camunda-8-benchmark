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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Оркестратор процесса process-0.bpmn
 * Имитирует действия внешних участников - напр. пользователей или интегрируемых систем
 * <p>
 * Реализует адаптивный алгоритм нагрузки:
 * при ошибке создания процесса скачком увеличивает задержку до следующей попытки,
 * при успешном создании - славно уменьшает задержку
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Process0Orchestrator {
    private final String PROCESS_DEFINITION_ID = "process-0";
    private final BpmnEngine bpmnEngine;
    private final StatisticsCollector statisticsCollector;

    private AtomicLong startDelay = new AtomicLong(10);// milliseconds before next process start
    private AtomicLong startedProcessInstances = new AtomicLong(0);// кол-во уже запущенных процессов
    private AtomicLong startingProcessInstances = new AtomicLong(0);// кол-во активных запросов на старт процесса
    private long lastLoggedProcessInstance = 0;

    @Autowired
    private final ApplicationContext applicationContext;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        final int PROCESSES_TO_START = 1000;

        Process0Orchestrator myself = applicationContext.getBean(Process0Orchestrator.class);
        while ((startedProcessInstances.get() < PROCESSES_TO_START) || (startingProcessInstances.get() > 0)) {
            try {
                Thread.sleep(startDelay.get());

                if (startedProcessInstances.get() < PROCESSES_TO_START) {// осталось что запускать ?
                    startedProcessInstances.incrementAndGet();// увеличиваем счетчик (авансом), чтобы "зарезервировать слот"
                    myself.startProcessInstance();
                    if (startedProcessInstances.get() - lastLoggedProcessInstance >= 100) {
                        log.info("Starting {} processes, delay = {}", startedProcessInstances.get(), startDelay.get());
                        lastLoggedProcessInstance += 100;
                    }
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
        log.info("All process '{}' instances are started", PROCESS_DEFINITION_ID);
    }

    @Async("processStartExecutor")
    public void startProcessInstance() {
        startingProcessInstances.incrementAndGet();

        Map<String, Object> variables = new HashMap<>();
        long processInstanceId = bpmnEngine.startProcess(PROCESS_DEFINITION_ID, variables);
        if (processInstanceId < 0) {// запуск процесса не удался
            startedProcessInstances.decrementAndGet(); // уменьшаем счетчик (= возвращаем этот слот в очередь на запуск)
            startDelay.updateAndGet(operand -> operand < 98 ? operand + 2 : 100); // увеличиваем задержку (резко)
            statisticsCollector.incStartedProcessInstancesException(""); // увеличиваем метрику ошибок запуска процесса
        } else { // запуск процесса удался
            startDelay.updateAndGet(operand -> operand > 1 ? --operand : 1); // уменьшаем задержку (плавно)
            statisticsCollector.incStartedProcessInstances(); // увеличиваем метрику успешных запусков процесса
        }

        startingProcessInstances.decrementAndGet();
    }
}
