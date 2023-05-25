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
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Process0Orchestrator {
    private final String PROCESS_DEFINITION_ID = "process-0";
    private final BpmnEngine bpmnEngine;
    private final StatisticsCollector statisticsCollector;

    private AtomicLong lastStartProcessErrorTime = new AtomicLong();
    private AtomicLong startedProcessInstances = new AtomicLong(0);

    @Autowired
    private final ApplicationContext applicationContext;

    //private Process0Orchestrator myself;

    @PostConstruct
    protected void selfInject(){
        //myself = null;//applicationContext.getBean(Process0Orchestrator.class);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        Process0Orchestrator myself = applicationContext.getBean(Process0Orchestrator.class);
        while(startedProcessInstances.get() < 10000){
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                log.error("", e);
            }
            if (System.currentTimeMillis() - lastStartProcessErrorTime.get() > 25) {
                myself.startProcessInstance();
            }
        }
        log.info("All process '{}' instances are started", PROCESS_DEFINITION_ID);
    }

    @Async
    public void startProcessInstance() {
        Map<String, Object> variables = new HashMap<>();
        long processInstanceId = bpmnEngine.startProcess(PROCESS_DEFINITION_ID, variables);
        if (processInstanceId < 0) {
            lastStartProcessErrorTime.set(System.currentTimeMillis());
        } else {
            startedProcessInstances.incrementAndGet();
            statisticsCollector.incStartedProcessInstances();
        }
    }

}
