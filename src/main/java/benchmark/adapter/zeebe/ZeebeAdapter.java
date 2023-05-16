package benchmark.adapter.zeebe;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.lifecycle.ZeebeClientLifecycle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import benchmark.app.api.BpmnEngine;
import benchmark.app.api.ServiceTaskInbound;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ZeebeAdapter implements BpmnEngine {

    private final ZeebeClientLifecycle client;

    private final ServiceTaskInbound serviceTaskInbound;

    private final String PROCESS_DEFINITION_ID = "process-1";

    @Override
    public void startProcess(String startParam, String processExternalId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("startParam", startParam);
        variables.put("processExternalId", processExternalId);

        final ProcessInstanceEvent event =
            client
                .newCreateInstanceCommand()
                .bpmnProcessId(PROCESS_DEFINITION_ID)
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        log.info("startProcess(): process started for processExternalId = {}, processDefinitionKey={}, bpmnProcessId={}, version={}, processInstanceKey={}",
            processExternalId, event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());
    }

    @Override
    public void performUserTask(long taskKey, String inputData) {

        log.info("performUserTask(): taskKey = {}, inputData = {}", taskKey, inputData);

        //long taskKey = userTaskInfoHolder.getUserTaskKey(processExternalId, "input-data");

        Map<String, Object> variables = new HashMap<>();
        variables.put("inputData", inputData);

        client
            .newCompleteCommand(taskKey)
            .variables(variables)
            .send()
            .join();
    }

    /*
        Пример worker'а для BpmnUserTask'ов

        // autoComplete = false чтобы user task'а не завершалась и висела в task list'е
        // до выполнения inputData() через rest
        @JobWorker(type = "io.camunda.zeebe:userTask", autoComplete = false, fetchVariables = "processExternalId")
        public void handleUserTask(final ActivatedJob job) {

            String processExternalId = (String) job.getVariablesAsMap().get("processExternalId");
            String elementId = job.getElementId();
            long key = job.getKey();

            log.info("handleUserTask(): processExternalId = {}, elementId = {}, key = {}", processExternalId, elementId, key);

            userTaskInfoHolder.registerUserTask(processExternalId, elementId, key);
        }
    */
    // autoComplete = false чтобы можно было обновить переменные в процессе, см. в коде
    @JobWorker(type = "service-task-1", autoComplete = false)
    public void performServiceTask(final ActivatedJob job) {

        String startParam = (String) job.getVariablesAsMap().get("startParam");
        String inputData = (String) job.getVariablesAsMap().get("inputData");

        String processedData = serviceTaskInbound.execute(startParam, inputData);
        log.info("performServiceTask: {}", processedData);

        Map<String, Object> variables = new HashMap<>();
        variables.put("processedData", processedData);

        client
            .newCompleteCommand(job.getKey())
            .variables(variables)
            .send()
            .join();
    }
}
