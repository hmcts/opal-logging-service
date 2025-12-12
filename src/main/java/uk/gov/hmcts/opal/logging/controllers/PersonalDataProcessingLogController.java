package uk.gov.hmcts.opal.logging.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.generated.http.api.PersonalDataProcessingLogApi;
import uk.gov.hmcts.opal.generated.model.AddPDPLRequestPersonalDataProcessingLogging;
import uk.gov.hmcts.opal.generated.model.PersonalDataProcessingLogPersonalDataProcessingLogging;
import uk.gov.hmcts.opal.logging.mapper.PersonalDataProcessingLogMapper;
import uk.gov.hmcts.opal.logging.service.PersonalDataProcessingLogService;

/**
 * Implements the POST /log/pdpo endpoint defined in the OpenAPI contract.
 * The controller is only enabled when test-support endpoints are turned on.
 */
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "opal.logging.test-support.enabled", havingValue = "true")
public class PersonalDataProcessingLogController implements PersonalDataProcessingLogApi {

    private final PersonalDataProcessingLogService logService;
    private final PersonalDataProcessingLogMapper mapper;

    @Override
    public ResponseEntity<PersonalDataProcessingLogPersonalDataProcessingLogging> logPdpoPost(
        AddPDPLRequestPersonalDataProcessingLogging request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapper.toDto(logService.recordLog(request)));
    }
}
