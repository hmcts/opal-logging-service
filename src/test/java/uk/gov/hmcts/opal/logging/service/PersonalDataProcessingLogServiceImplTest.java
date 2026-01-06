package uk.gov.hmcts.opal.logging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest.CategoryEnum;
import uk.gov.hmcts.opal.logging.generated.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.generated.dto.SearchPdpoLogRequest;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoIdentifierRepository;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoLogRepository;
import uk.gov.hmcts.opal.logging.persistence.specification.PdpoLogSpecifications;

@ExtendWith(MockitoExtension.class)
class PersonalDataProcessingLogServiceImplTest {

    @Mock
    private PdpoIdentifierRepository identifierRepository;
    @Mock
    private PdpoLogRepository logRepository;
    @Captor
    private ArgumentCaptor<PdpoLogEntity> logCaptor;
    private PdpoLogSpecifications logSpecifications;

    private PersonalDataProcessingLogServiceImpl service;

    @BeforeEach
    void setUp() {
        logSpecifications = new PdpoLogSpecifications();
        service = new PersonalDataProcessingLogServiceImpl(identifierRepository, logRepository, logSpecifications);
        lenient().when(logRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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

        ParticipantIdentifier first = identifier("ind-1", "DEFENDANT");
        ParticipantIdentifier second = identifier("ind-2", "MINOR_CREDITOR");

        service.recordLog(minimalDetails()
            .businessIdentifier("ACME")
            .individuals(List.of(first, second)));

        verify(logRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getIndividuals()).hasSize(2);
    }

    @Test
    void record_throwsWhenCategoryMissing() {
        AddPdpoLogRequest request = minimalDetails()
            .category(null);

        assertThatThrownBy(() -> service.recordLog(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("category must be provided");
        verifyNoInteractions(logRepository);
    }

    @Test
    void searchLogs_requiresAtLeastOneFilter() {
        assertThatThrownBy(() -> service.searchLogs(new SearchPdpoLogRequest()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("At least one search parameter");
        verifyNoInteractions(logRepository);
    }

    @Test
    void searchLogs_requiresCreatedByIdentifierAndTypeTogether() {
        SearchPdpoLogRequest request = new SearchPdpoLogRequest()
            .createdBy(new ParticipantIdentifier().id("user-1"));

        assertThatThrownBy(() -> service.searchLogs(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("created_by.id and created_by.type");
        verifyNoInteractions(logRepository);
    }

    @Test
    void searchLogs_delegatesToRepository() {
        PdpoLogEntity entity = PdpoLogEntity.builder()
            .createdByIdentifier("user-1")
            .createdByIdentifierType("OPAL_USER_ID")
            .build();
        when(logRepository.findAll(
            org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<PdpoLogEntity>>any(),
            any(Sort.class)
        )).thenReturn(List.of(entity));

        List<PdpoLogEntity> results = service.searchLogs(
            new SearchPdpoLogRequest()
                .createdBy(new ParticipantIdentifier().id("user-1").type("OPAL_USER_ID"))
                .businessIdentifier("ACME")
        );

        assertThat(results).containsExactly(entity);
        verify(logRepository).findAll(
            org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<PdpoLogEntity>>any(),
            any(Sort.class)
        );
    }

    private AddPdpoLogRequest minimalDetails() {
        return new AddPdpoLogRequest()
            .createdBy(identifier("user-1", "OPAL_USER_ID"))
            .businessIdentifier("ACME")
            .createdAt(OffsetDateTime.parse("2025-11-09T10:15:30Z"))
            .ipAddress("10.0.0.1")
            .category(CategoryEnum.COLLECTION)
            .individuals(new ArrayList<>());
    }

    private ParticipantIdentifier identifier(String id, String type) {
        return new ParticipantIdentifier()
            .id(id)
            .type(type);
    }
}
