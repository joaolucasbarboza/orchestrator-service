package com.joaobarboza.orchestratorservice.core.dto;


import com.joaobarboza.orchestratorservice.core.enums.EEventSource;
import com.joaobarboza.orchestratorservice.core.enums.ESagaStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class History {

    private EEventSource source;
    private ESagaStatus status;
    private String message;
    private LocalDateTime createdAt;
}
