package benchmark.adapter.zeebe;

import benchmark.app.impl.TaskList;
import benchmark.domain.bpmn.BpmnProcess;
import benchmark.domain.bpmn.BpmnUserTask;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.protocol.jackson.ZeebeProtocolModule;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.value.JobRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceCreationRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static io.camunda.zeebe.protocol.record.ValueType.*;
import static io.camunda.zeebe.protocol.record.intent.JobIntent.*;
import static io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent.ELEMENT_COMPLETED;
import static io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent.ELEMENT_TERMINATED;
import static io.camunda.zeebe.protocol.record.value.BpmnElementType.PROCESS;

/**
 * todo Document type ElasticSearchPoller
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticSearchPoller {

    private static final String VAR_PROCESS_EXTERNAL_ID = "processExternalId";
    private static final String HEADER_ASSIGNEE = "io.camunda.zeebe:assignee";

    // mapper для десериализации объектов протокола Zeebe (в т.ч. объектов RecordValue)
    final ObjectMapper zeebeMapper = new ObjectMapper().registerModule(new ZeebeProtocolModule());

    final ObjectMapper rawMapper = new ObjectMapper();

    private final ElasticsearchOperations elasticsearchOperations;

    private final TaskList taskList;
    private final String queryString = """
        {
            "bool": {
                "must": [
                    {
                        "range": {
                            "position": {
                                "gt": "%s"
                            }
                        }
                    },
                    {
                        "range": {
                            "timestamp": {
                                "lt": "%s"
                            }
                        }
                    }
                ]
            }
        }
        """;
    private long lastPosition = 0;

    //@Scheduled(fixedRate = 2000)
    public <T extends RecordValue> void poll() {
        log.info("poll(): lastPosition = {}", lastPosition);

        // не вычитываем самые свежие записи, которые укладываются в последний refresh interval
        // индекса в Elastic (1 сек), т.к. они могут не попасть в выборку (из-за того, что индекс
        // не полностью обновлен) и потом уже никогда не вычитаются (т.к. lastPosition уже уедет
        // за них)
        long timestamp = new Date().getTime() - 1000;// default index refresh interval
        String queryStringParameterized = String.format(queryString, lastPosition, timestamp);
        Pageable pageable = PageRequest.of(0, 1000);
        Sort sort = Sort.by(Sort.Direction.ASC, "position");
        Query query = new StringQuery(queryStringParameterized, pageable, sort);

        SearchHits<?> searchHits = elasticsearchOperations.search(query, Object.class, IndexCoordinates.of("zeebe-record-*"));
        List<Record<T>> newRecordsObj = searchHits.getSearchHits().stream().map(
            searchHit -> {
                //log.info("jsonNodeSearchHit = {}", searchHit);
                Object obj = searchHit.getContent();
                Record<T> record;
                try {
                    String json = rawMapper.writeValueAsString(obj);
                    log.info("poll(): {}", json);
                    record = zeebeMapper.readValue(json, new TypeReference<Record<T>>() {
                    });
                    //log.info("class = {}", record.getValue().getClass());
                    return record;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        ).toList();

        newRecordsObj.forEach(
            record -> {
                log.info("poll(): record position = {}, valueType = {}, intent = {}", record.getPosition(), record.getValueType(), record.getIntent());
                lastPosition = record.getPosition();
                mapRecord(record);
            }
        );
    }

    private void mapRecord(Record record) {
        if (record.getValue() instanceof ProcessInstanceCreationRecordValue value) {
            if (record.getValueType() == PROCESS_INSTANCE_CREATION) {
                long processInstanceKey = value.getProcessInstanceKey();
                String processExternalId = (String) value.getVariables().get(VAR_PROCESS_EXTERNAL_ID);

                BpmnProcess process = new BpmnProcess(processInstanceKey, processExternalId);
                taskList.registerProcessStart(process);
            }
        } else if (record.getValue() instanceof ProcessInstanceRecordValue value) {
            if (record.getValueType() == PROCESS_INSTANCE &&
                value.getBpmnElementType() == PROCESS &&
                (record.getIntent() == ELEMENT_COMPLETED || record.getIntent() == ELEMENT_TERMINATED)) {
                long processInstanceKey = value.getProcessInstanceKey();
                taskList.registerProcessEnd(processInstanceKey);
            }
        } else if (record.getValue() instanceof JobRecordValue value) {
            long processInstanceKey = value.getProcessInstanceKey();
            long key = record.getKey();

            if (record.getValueType() == JOB &&
                record.getIntent() == CREATED) {

                BpmnProcess process = taskList.getActiveProcesses().get(processInstanceKey);

                BpmnUserTask userTask = new BpmnUserTask(key,
                    value.getElementId(),
                    value.getCustomHeaders().get(HEADER_ASSIGNEE),
                    process);

                taskList.registerUserTaskStart(processInstanceKey, userTask);
            } else if (record.getValueType() == JOB &&
                (record.getIntent() == COMPLETED || record.getIntent() == CANCELED || record.getIntent() == TIMED_OUT)) {
                taskList.registerUserTaskEnd(processInstanceKey, key);
            }
        }
    }
}
