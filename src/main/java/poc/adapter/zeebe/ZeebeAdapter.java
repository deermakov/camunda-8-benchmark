package poc.adapter.zeebe;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.lifecycle.ZeebeClientLifecycle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import poc.app.api.BpmnEngine;
import poc.app.api.ProcessDataInbound;

import java.util.Map;

/**
 * todo Document type ZeebeAdapter
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ZeebeAdapter implements BpmnEngine {
    private final ZeebeClientLifecycle client;

    private final ProcessDataInbound processDataInbound;

    private final String PROCESS_ID = "poc-process";

    @Override
    public void startProcess(){
        final ProcessInstanceEvent event =
            client
                .newCreateInstanceCommand()
                .bpmnProcessId(PROCESS_ID)
                .latestVersion()
                .variables(Map.of("message_content", "Hello from the Spring Boot get started"))
                .send()
                .join();

        log.info("Started instance for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
            event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());
    }

    @JobWorker(type = "process-data")
    public void processData(final ActivatedJob job) {

        final String message_content = (String) job.getVariablesAsMap().get("message_content");
        log.info("Sending email with message content: {}", message_content);

        processDataInbound.execute();
    }
}
