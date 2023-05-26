package benchmark.app.impl;

import benchmark.domain.bpmn.BpmnProcess;
import benchmark.domain.bpmn.BpmnUserTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Getter
@Slf4j
public class TaskList {

    // ключ - ключ процесса (value.processInstanceKey)
    private final Map<Long, BpmnProcess> activeProcesses = new HashMap<>();

    synchronized public void registerProcessStart(BpmnProcess process) {
        long processInstanceKey = process.getProcessInstanceKey();
        log.info("registerProcessStart(): processInstanceKey = {}", processInstanceKey);
        activeProcesses.put(processInstanceKey, process);
    }

    synchronized public void registerProcessEnd(long processInstanceKey) {
        log.info("registerProcessEnd(): processInstanceKey = {}", processInstanceKey);
        activeProcesses.remove(processInstanceKey);
    }

    synchronized public void registerUserTaskStart(long processInstanceKey, BpmnUserTask userTask) {
        log.info("registerUserTaskStart(): processInstanceKey = {}, key = {}", processInstanceKey, userTask.getKey());
        BpmnProcess process = activeProcesses.get(processInstanceKey);
        process.getUserTasks().put(userTask.getKey(), userTask);
    }

    synchronized public void registerUserTaskEnd(long processInstanceKey, long key) {
        log.info("registerUserTaskEnd(): processInstanceKey = {}, key = {}", processInstanceKey, key);
        BpmnProcess process = activeProcesses.get(processInstanceKey);
        if (process != null) {
            process.getUserTasks().remove(key);
        } else {
            log.warn("Unknown process, ignoring: {}", processInstanceKey);
        }
    }

    public List<BpmnUserTask> getAllActiveUserTasks() {
        List<BpmnUserTask> result = new ArrayList<>();

        activeProcesses.values().forEach(
            process -> {
                log.info("getAllActiveUserTasks(): process = {}, tasks = {}", process.getProcessInstanceKey(), process.getUserTasks().size());
                result.addAll(process.getUserTasks().values());
            }
        );

        log.info("getAllActiveUserTasks(): result = {}", result.size());

        return result;
    }

    public List<BpmnUserTask> getActiveUserTasks(long processInstanceKey, String assignee) {
        List<BpmnUserTask> result = new ArrayList<>();

        activeProcesses.values().stream()
            .filter(bpmnProcess -> bpmnProcess.getProcessInstanceKey() == processInstanceKey)
            .forEach(
                process -> {
                    log.info("getActiveUserTasks(): process = {}, tasks = {}", process.getProcessInstanceKey(), process.getUserTasks().size());

                    process.getUserTasks().values()
                        .stream()
                        .filter(userTask -> assignee.equals(userTask.getAssignee()))
                        .forEach(result::add);
                }
            );

        log.info("getActiveUserTasks(): result = {}", result.size());

        return result;
    }
}
