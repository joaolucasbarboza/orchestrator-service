package com.joaobarboza.orchestratorservice.core.service;

import com.joaobarboza.orchestratorservice.core.dto.Event;
import com.joaobarboza.orchestratorservice.core.dto.History;
import com.joaobarboza.orchestratorservice.core.enums.EEventSource;
import com.joaobarboza.orchestratorservice.core.enums.ESagaStatus;
import com.joaobarboza.orchestratorservice.core.enums.ETopics;
import com.joaobarboza.orchestratorservice.core.producer.SagaOrchestratorProducer;
import com.joaobarboza.orchestratorservice.core.saga.SagaExecutionController;
import com.joaobarboza.orchestratorservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.joaobarboza.orchestratorservice.core.enums.ETopics.NOTIFY_ENDING;

@Slf4j
@Service
@AllArgsConstructor
public class OrchestratorService {

    private final JsonUtil jsonUtil;
    private final SagaOrchestratorProducer producer;
    private final SagaExecutionController sagaExecutionController;

    public void startSaga(Event event) {
        event.setSource(EEventSource.ORCHESTRATOR);
        event.setStatus(ESagaStatus.SUCCESS);
        ETopics topic = getTopic(event);

        log.info("SAGA STARTED");
        addHistory(event, "Saga started");
        sendToProducerWithTopic(event, topic);
    }

    public void finishSagaSuccess(Event event) {
        event.setSource(EEventSource.ORCHESTRATOR);
        event.setStatus(ESagaStatus.SUCCESS);

        log.info("SAGA FINISHED SUCCESSFULLY FOR EVENT: {}", event.getId());
        addHistory(event, "Saga finished successfully");
        notifyFinishedSaga(event);
    }

    public void finishSagaFail(Event event) {
        event.setSource(EEventSource.ORCHESTRATOR);
        event.setStatus(ESagaStatus.FAIL);

        log.info("SAGA FINISHED WITH ERRORS FOR EVENT: {}", event.getId());
        addHistory(event, "Saga finished with errors");
        notifyFinishedSaga(event);
    }

    public void continueSaga(Event event) {
        ETopics topic = getTopic(event);
        log.info("CONTINUE SAGA FOR EVENT: {}, NEXT TOPIC: {}", event.getId(), topic.getTopic());
        sendToProducerWithTopic(event, topic);
    }

    private ETopics getTopic(Event event) {
        return sagaExecutionController.getNextTopics(event);
    }

    private void addHistory(Event event, String message) {
        History history = History.builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }

    private void notifyFinishedSaga(Event event) {
        producer.sendEvent(jsonUtil.toJson(event), NOTIFY_ENDING.getTopic());
    }

    private void sendToProducerWithTopic(Event event, ETopics topic) {
        producer.sendEvent(jsonUtil.toJson(event), topic.getTopic());
    }
}
