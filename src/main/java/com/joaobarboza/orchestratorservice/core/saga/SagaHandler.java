package com.joaobarboza.orchestratorservice.core.saga;

import com.joaobarboza.orchestratorservice.core.enums.EEventSource;
import com.joaobarboza.orchestratorservice.core.enums.ESagaStatus;

import static com.joaobarboza.orchestratorservice.core.enums.ETopics.*;

public final class SagaHandler {

    private SagaHandler() {}

    public static final Object[][] SAGA_HANDLER = {
            { EEventSource.ORCHESTRATOR, ESagaStatus.SUCCESS, PRODUCT_VALIDATION_SUCCESS },
            { EEventSource.ORCHESTRATOR, ESagaStatus.FAIL, FINISH_FAIL},

            { EEventSource.PRODUCT_VALIDATION_SERVICE, ESagaStatus.ROLLBACK_PENDING, PRODUCT_VALIDATION_FAIL },
            { EEventSource.PRODUCT_VALIDATION_SERVICE, ESagaStatus.FAIL, FINISH_FAIL },
            { EEventSource.PRODUCT_VALIDATION_SERVICE, ESagaStatus.SUCCESS, PAYMENT_SUCCESS },

            { EEventSource.PAYMENT_SERVICE, ESagaStatus.ROLLBACK_PENDING, PAYMENT_FAIL },
            { EEventSource.PAYMENT_SERVICE, ESagaStatus.FAIL, PRODUCT_VALIDATION_FAIL },
            { EEventSource.PAYMENT_SERVICE, ESagaStatus.SUCCESS, INVENTORY_SUCCESS },


            { EEventSource.INVENTORY_SERVICE, ESagaStatus.ROLLBACK_PENDING, INVENTORY_FAIL },
            { EEventSource.INVENTORY_SERVICE, ESagaStatus.FAIL, PAYMENT_FAIL },
            { EEventSource.INVENTORY_SERVICE, ESagaStatus.SUCCESS, FINISH_SUCCESS },
    };

    public static final int EVENT_SOURCE_INDEX = 0;
    public static final int SAGA_STATUS_INDEX = 1;
    public static final int NEXT_TOPIC_INDEX = 2;
}
