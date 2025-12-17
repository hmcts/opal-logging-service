package uk.gov.hmcts.opal.logging.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.logging.generated.http.api.LogApi;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogResponse;
import uk.gov.hmcts.opal.logging.mapper.PersonalDataProcessingLogMapper;
import uk.gov.hmcts.opal.logging.service.PersonalDataProcessingLogService;

/**
 * Implements the POST /log/pdpo endpoint defined in the OpenAPI contract.
 * The controller is only enabled when test-support endpoints are turned on.
 */
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "opal.logging.test-support.enabled", havingValue = "true")
public class PersonalDataProcessingLogController implements LogApi {

    private final PersonalDataProcessingLogService logService;
    private final PersonalDataProcessingLogMapper mapper;

    @Override
    public ResponseEntity<AddPdpoLogResponse> logPdpoPost(
        AddPdpoLogRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapper.toDto(logService.recordLog(request)));
    }
}
