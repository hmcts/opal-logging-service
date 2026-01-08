package uk.gov.hmcts.opal.logging.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogResponse;
import uk.gov.hmcts.opal.logging.generated.dto.SearchPdpoLogRequest;
import uk.gov.hmcts.opal.logging.generated.http.api.TestSupportApi;
import uk.gov.hmcts.opal.logging.mapper.PersonalDataProcessingLogMapper;
import uk.gov.hmcts.opal.logging.service.PersonalDataProcessingLogService;

/**
 * Implements the /test-support/search endpoint defined in the OpenAPI contract.
 */
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "opal.logging.test-support.enabled", havingValue = "true")
public class TestSupportPersonalDataProcessingLogController implements TestSupportApi {

    private final PersonalDataProcessingLogService logService;
    private final PersonalDataProcessingLogMapper mapper;

    @Override
    public ResponseEntity<List<AddPdpoLogResponse>> testSupportSearchPost(SearchPdpoLogRequest request) {
        return ResponseEntity.ok(
            logService.searchLogs(request).stream()
                .map(mapper::toDto)
                .toList()
        );
    }
}
