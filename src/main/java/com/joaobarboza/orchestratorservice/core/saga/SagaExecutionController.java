package com.joaobarboza.orchestratorservice.core.saga;

import com.joaobarboza.orchestratorservice.config.exception.ValidationException;
import com.joaobarboza.orchestratorservice.core.dto.Event;
import com.joaobarboza.orchestratorservice.core.enums.EEventSource;
import com.joaobarboza.orchestratorservice.core.enums.ESagaStatus;
import com.joaobarboza.orchestratorservice.core.enums.ETopics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;

import static com.joaobarboza.orchestratorservice.core.saga.SagaHandler.SAGA_HANDLER;

@Slf4j
@Component
@AllArgsConstructor
public class SagaExecutionController {

    private static final String SAGA_LOG_ID = "ORDER_ID: %S | TRANSACTION_ID: %S | EVENT_ID: %S";

    public ETopics getNextTopics(Event event) {
        if (ObjectUtils.isEmpty(event.getSource()) || ObjectUtils.isEmpty(event.getStatus())) {
            throw new ValidationException("Source and status must be informed");
        }

        ETopics topic = findTopicBySourceAndStatus(event);
        logCurrentSaga(event, topic);
        return topic;
    }

    private ETopics findTopicBySourceAndStatus(Event event) {
        return (ETopics) Arrays.stream(SagaHandler.SAGA_HANDLER)
                .filter(row -> isEventSourceAndStatusValid(event, row))
                .map(i -> i[SagaHandler.NEXT_TOPIC_INDEX])
                .findFirst()
                .orElseThrow(() -> new ValidationException("Topic not found"));
    }

    private boolean isEventSourceAndStatusValid(Event event, Object[] row) {
        var source = row[SagaHandler.EVENT_SOURCE_INDEX];
        var status = row[SagaHandler.SAGA_STATUS_INDEX];
        return event.getSource().equals(source) && event.getStatus().equals(status);
    }

    private void logCurrentSaga(Event event, ETopics topic) {
        String sagaWithInformationAtOrder = createLogSagaWithInformationAtOrder(event);
        EEventSource source = event.getSource();

        if (event.getStatus().equals(ESagaStatus.SUCCESS)) {
            log.info("### CURRENT SAGA: {} | SUCCESS | NEXT TOPIC {} | {}",
                    source, topic, sagaWithInformationAtOrder);
        }

        if (event.getStatus().equals(ESagaStatus.ROLLBACK_PENDING)) {
            log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK CURRENT SERVICE | NEXT TOPIC {} | {}",
                    source, topic, sagaWithInformationAtOrder);
        }

        if (event.getStatus().equals(ESagaStatus.FAIL)) {
            log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT TOPIC {} | {}",
                    source, topic, sagaWithInformationAtOrder);
        }
    }

    private String createLogSagaWithInformationAtOrder(Event event) {
        return String.format(SAGA_LOG_ID,
                event.getPayload().getId(), event.getTransactionId(), event.getId());
    }

}
