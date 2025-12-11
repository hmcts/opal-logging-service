package uk.gov.hmcts.opal.logging.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.AddPDPLRequestPersonalDataProcessingLogging;
import uk.gov.hmcts.opal.generated.model.PDPLIdentifierPersonalDataProcessingLogging;
import uk.gov.hmcts.opal.generated.model.PersonalDataProcessingLogPersonalDataProcessingLogging;
import uk.gov.hmcts.opal.logging.domain.PdpoCategory;
import uk.gov.hmcts.opal.logging.mapper.PersonalDataProcessingLogMapper;
import uk.gov.hmcts.opal.logging.mapper.PersonalDataProcessingLogMapperImpl;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogIndividualEntity;
import uk.gov.hmcts.opal.logging.service.PersonalDataProcessingLogService;

@ExtendWith(MockitoExtension.class)
class PersonalDataProcessingLogControllerTest {

    @Mock
    private PersonalDataProcessingLogService service;

    private PersonalDataProcessingLogController controller;

    @BeforeEach
    void setUp() {
        PersonalDataProcessingLogMapper mapper = new PersonalDataProcessingLogMapperImpl();
        controller = new PersonalDataProcessingLogController(service, mapper);
    }

    @Test
    void logPdpoPostReturnsMappedPersonalDataProcessingLog() {
        AddPDPLRequestPersonalDataProcessingLogging request = baseRequest();
        PdpoLogEntity persisted = persistedLog();

        when(service.recordLog(request)).thenReturn(persisted);

        ResponseEntity<PersonalDataProcessingLogPersonalDataProcessingLogging> response = controller.logPdpoPost(
            request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        PersonalDataProcessingLogPersonalDataProcessingLogging body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getPdpoLogId()).isEqualTo(123L);
        assertThat(body.getBusinessIdentifierId()).isEqualTo(99L);
        assertThat(body.getBusinessIdentifier()).isEqualTo("SharingCo");
        assertThat(body.getIpAddress()).isEqualTo("192.168.1.10");
        assertThat(body.getCategory()).isEqualTo(
            PersonalDataProcessingLogPersonalDataProcessingLogging.CategoryEnum.DISCLOSURE
        );
        assertThat(body.getCreatedBy().getId()).isEqualTo("requestor-1");
        assertThat(body.getCreatedBy().getType()).isEqualTo("OPAL_USER_ID");
        assertThat(body.getRecipient()).isNotNull();
        assertThat(body.getRecipient().getId()).isEqualTo("recipient-42");
        assertThat(body.getIndividuals()).hasSize(1);
        assertThat(body.getIndividuals().get(0).getId()).isEqualTo("person-1");
    }

    private AddPDPLRequestPersonalDataProcessingLogging baseRequest() {
        return new AddPDPLRequestPersonalDataProcessingLogging()
            .createdBy(new PDPLIdentifierPersonalDataProcessingLogging().id("requestor-1").type("OPAL_USER_ID"))
            .businessIdentifier("SharingCo")
            .createdAt(OffsetDateTime.parse("2025-11-15T12:45:00Z"))
            .ipAddress("192.168.1.10")
            .category(AddPDPLRequestPersonalDataProcessingLogging.CategoryEnum.DISCLOSURE)
            .recipient(new PDPLIdentifierPersonalDataProcessingLogging().id("recipient-42").type("EXTERNAL_SERVICE"))
            .addIndividualsItem(new PDPLIdentifierPersonalDataProcessingLogging().id("person-1").type("DEFENDANT"));
    }

    private PdpoLogEntity persistedLog() {
        PdpoIdentifierEntity identifier = PdpoIdentifierEntity.builder()
            .id(99L)
            .businessIdentifier("SharingCo")
            .build();

        PdpoLogEntity log = PdpoLogEntity.builder()
            .id(123L)
            .createdByIdentifier("requestor-1")
            .createdByIdentifierType("OPAL_USER_ID")
            .createdAt(OffsetDateTime.parse("2025-11-15T12:45:00Z"))
            .ipAddress("192.168.1.10")
            .category(PdpoCategory.DISCLOSURE)
            .recipientIdentifier("recipient-42")
            .recipientIdentifierType("EXTERNAL_SERVICE")
            .businessIdentifier(identifier)
            .build();

        log.addIndividual(PdpoLogIndividualEntity.builder()
                              .individualIdentifier("person-1")
                              .individualType("DEFENDANT")
                              .build());

        return log;
    }
}
