package benchmark.domain.bpmn;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BpmnUserTask {
    private long key;
    private String elementId;
    private String assignee;
    private BpmnProcess process;
}
