package uk.gov.hmcts.opal.logging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoIdentifierRepository;

@ExtendWith(MockitoExtension.class)
class PdpoIdentifierServiceImplTest {

    @Mock
    private PdpoIdentifierRepository identifierRepository;

    @InjectMocks
    private PdpoIdentifierServiceImpl service;

    @Test
    void findOrCreate_returnsExistingEntityWhenPresent() {
        // Arrange
        PdpoIdentifierEntity existing = PdpoIdentifierEntity.builder()
            .id(12L)
            .businessIdentifier("ACME")
            .build();
        when(identifierRepository.findByBusinessIdentifier("ACME")).thenReturn(Optional.of(existing));

        // Act
        PdpoIdentifierEntity result = service.findOrCreate("ACME");

        // Assert
        assertThat(result).isEqualTo(existing);
        verify(identifierRepository).findByBusinessIdentifier("ACME");
        verifyNoMoreInteractions(identifierRepository);
    }

    @Test
    void findOrCreate_createsAndSavesEntityWhenMissing() {
        // Arrange
        when(identifierRepository.findByBusinessIdentifier("NEW-CO")).thenReturn(Optional.empty());
        PdpoIdentifierEntity saved = PdpoIdentifierEntity.builder()
            .id(99L)
            .businessIdentifier("NEW-CO")
            .build();
        when(identifierRepository.saveAndFlush(any(PdpoIdentifierEntity.class)))
            .thenReturn(saved);

        // Act
        PdpoIdentifierEntity result = service.findOrCreate("NEW-CO");

        // Assert
        assertThat(result).isEqualTo(saved);
        verify(identifierRepository).findByBusinessIdentifier("NEW-CO");
        var captor = ArgumentCaptor.forClass(PdpoIdentifierEntity.class);
        verify(identifierRepository).saveAndFlush(captor.capture());
        PdpoIdentifierEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();
        assertThat(captured.getBusinessIdentifier()).isEqualTo("NEW-CO");
    }

    @Test
    void findByBusinessIdentifier_delegatesToRepository() {
        // Arrange
        PdpoIdentifierEntity entity = PdpoIdentifierEntity.builder()
            .id(7L)
            .businessIdentifier("ACME")
            .build();
        when(identifierRepository.findByBusinessIdentifier("ACME")).thenReturn(Optional.of(entity));

        // Act
        Optional<PdpoIdentifierEntity> result = service.findByBusinessIdentifier("ACME");

        // Assert
        assertThat(result).contains(entity);
        verify(identifierRepository).findByBusinessIdentifier("ACME");
    }
}
