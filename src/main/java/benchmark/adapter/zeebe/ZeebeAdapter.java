package benchmark.adapter.zeebe;

import benchmark.app.api.BpmnEngine;
import benchmark.app.api.ServiceTaskInbound;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.lifecycle.ZeebeClientLifecycle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ZeebeAdapter implements BpmnEngine {

    private final ZeebeClientLifecycle client;

    private final ServiceTaskInbound serviceTaskInbound;

    @Override
    public long startProcess(String processDefinitionId, Map<String, Object> variables) {

        try {
            final ProcessInstanceEvent event =
                client
                    .newCreateInstanceCommand()
                    .bpmnProcessId(processDefinitionId)
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            log.debug("startProcess(): process started with processDefinitionKey={}, bpmnProcessId={}, version={}, processInstanceKey={}",
                event.getProcessDefinitionKey(),
                event.getBpmnProcessId(),
                event.getVersion(),
                event.getProcessInstanceKey());

            return event.getProcessInstanceKey();
        } catch (ClientStatusException e) {
            log.debug(e.getMessage());
            return -1;
        }
    }

    @Override
    public void performUserTask(long taskKey, Map<String, Object> variables) {

        log.info("performUserTask(): taskKey = {}, variables = {}", taskKey, variables);

        //long taskKey = userTaskInfoHolder.getUserTaskKey(processExternalId, "input-data");

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
        Map<String, Object> variables = job.getVariablesAsMap();
        log.debug("performServiceTask() before: {}", variables);
        serviceTaskInbound.execute(variables);
        log.debug("performServiceTask() after: {}", variables);

        client
            .newCompleteCommand(job.getKey())
            .variables(variables)
            .send()
            .join();
    }
}
