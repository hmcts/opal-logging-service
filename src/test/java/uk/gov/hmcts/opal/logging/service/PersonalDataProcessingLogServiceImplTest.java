package uk.gov.hmcts.opal.logging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.logging.dto.IdentifierType;
import uk.gov.hmcts.opal.logging.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoIdentifierRepository;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoLogRepository;

@ExtendWith(MockitoExtension.class)
class PersonalDataProcessingLogServiceImplTest {

    @Mock
    private PdpoIdentifierRepository identifierRepository;
    @Mock
    private PdpoLogRepository logRepository;
    @Captor
    private ArgumentCaptor<PdpoLogEntity> logCaptor;

    private PersonalDataProcessingLogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PersonalDataProcessingLogServiceImpl(identifierRepository, logRepository);
        when(logRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void record_reusesExistingBusinessIdentifier() {
        PdpoIdentifierEntity existing = PdpoIdentifierEntity.builder()
            .id(42L)
            .businessIdentifier("ACME")
            .build();
        when(identifierRepository.findByBusinessIdentifier("ACME")).thenReturn(Optional.of(existing));

        service.recordLog(minimalDetails().businessIdentifier("ACME"));

        verify(identifierRepository, never()).save(any());
        verify(logRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getBusinessIdentifier()).isEqualTo(existing);
    }

    @Test
    void record_createsNewBusinessIdentifierWhenMissing() {
        when(identifierRepository.findByBusinessIdentifier("NEW-CO")).thenReturn(Optional.empty());
        when(identifierRepository.save(any())).thenAnswer(invocation -> {
            PdpoIdentifierEntity entity = invocation.getArgument(0);
            entity.setId(7L);
            return entity;
        });

        service.recordLog(minimalDetails().businessIdentifier("NEW-CO"));

        verify(identifierRepository).save(any());
        verify(logRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getBusinessIdentifier().getBusinessIdentifier()).isEqualTo("NEW-CO");
    }

    @Test
    void record_persistsAllIndividuals() {
        when(identifierRepository.findByBusinessIdentifier("ACME")).thenReturn(Optional.of(
            PdpoIdentifierEntity.builder().id(42L).businessIdentifier("ACME").build()
        ));

        ParticipantIdentifier first = new ParticipantIdentifier()
            .identifier("ind-1")
            .type(new IdentifierType().type("DEFENDANT"));
        ParticipantIdentifier second = new ParticipantIdentifier()
            .identifier("ind-2")
            .type(new IdentifierType().type("MINOR_CREDITOR"));

        service.recordLog(minimalDetails()
            .businessIdentifier("ACME")
            .individuals(List.of(first, second)));

        verify(logRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getIndividuals()).hasSize(2);
    }

    private PersonalDataProcessingLogDetails minimalDetails() {
        return new PersonalDataProcessingLogDetails()
            .createdBy(new ParticipantIdentifier()
                .identifier("user-1")
                .type(new IdentifierType().type("OPAL_USER_ID")))
            .businessIdentifier("ACME")
            .createdAt(OffsetDateTime.parse("2025-11-09T10:15:30Z"))
            .ipAddress("10.0.0.1")
            .category(PersonalDataProcessingCategory.COLLECTION)
            .individuals(new ArrayList<>());
    }
}
