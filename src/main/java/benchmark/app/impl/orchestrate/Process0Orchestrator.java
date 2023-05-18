package benchmark.app.impl.orchestrate;

import benchmark.app.api.BpmnEngine;
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
        for (int i = 1; i <= 1000; i++) {
            myself.startProcessInstance();
        }
        log.info("All process '{}' instances are started", PROCESS_DEFINITION_ID);
    }

    @Async
    public void startProcessInstance() {
        Map<String, Object> variables = new HashMap<>();
        long processInstanceId = -1;
        while (processInstanceId < 0){
            processInstanceId = bpmnEngine.startProcess(PROCESS_DEFINITION_ID, variables);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e){
                log.error("", e);
            }
        }
    }

}
