package uk.gov.hmcts.opal.logging.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.generated.model.AddPDPLRequestPersonalDataProcessingLogging;
import uk.gov.hmcts.opal.generated.model.AddPDPLRequestPersonalDataProcessingLogging.CategoryEnum;
import uk.gov.hmcts.opal.generated.model.PDPLIdentifierPersonalDataProcessingLogging;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoIdentifierRepository;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoLogRepository;
import uk.gov.hmcts.opal.logging.testsupport.AbstractIntegrationTest;

class PersonalDataProcessingLogServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PersonalDataProcessingLogService service;
    @Autowired
    private PdpoIdentifierRepository identifierRepository;
    @Autowired
    private PdpoLogRepository logRepository;

    @BeforeEach
    void clean() {
        logRepository.deleteAll();
        identifierRepository.deleteAll();
    }

    @Test
    void reusesExistingBusinessIdentifierAcrossLogs() {
        AddPDPLRequestPersonalDataProcessingLogging request = baseRequest()
            .businessIdentifier("SharingCo");

        service.recordLog(request);
        service.recordLog(request);

        assertThat(identifierRepository.findAll()).hasSize(1);
        assertThat(logRepository.count()).isEqualTo(2);
    }

    @Test
    @Transactional
    void persistsDisclosureRecipientAndIndividuals() {
        PDPLIdentifierPersonalDataProcessingLogging recipient = participant("recipient-42", "EXTERNAL_SERVICE");

        AddPDPLRequestPersonalDataProcessingLogging request = baseRequest()
            .category(CategoryEnum.DISCLOSURE)
            .recipient(recipient)
            .individuals(List.of(
                participant("person-1", "DEFENDANT"),
                participant("person-2", "MINOR_CREDITOR")
            ));

        PdpoLogEntity persisted = service.recordLog(request);
        PdpoLogEntity fromDb = logRepository.findById(persisted.getId()).orElseThrow();

        assertThat(fromDb.getRecipientIdentifier()).isEqualTo("recipient-42");
        assertThat(fromDb.getIndividuals()).hasSize(2);
    }

    private AddPDPLRequestPersonalDataProcessingLogging baseRequest() {
        return new AddPDPLRequestPersonalDataProcessingLogging()
            .createdBy(participant("requestor-1", "OPAL_USER_ID"))
            .businessIdentifier("SharingCo")
            .createdAt(OffsetDateTime.parse("2025-11-15T12:45:00Z"))
            .ipAddress("192.168.1.10")
            .category(CategoryEnum.COLLECTION)
            .individuals(new ArrayList<>(List.of(participant("person-1", "DEFENDANT"))));
    }

    private PDPLIdentifierPersonalDataProcessingLogging participant(String identifier, String type) {
        return new PDPLIdentifierPersonalDataProcessingLogging()
            .id(identifier)
            .type(type);
    }
}
