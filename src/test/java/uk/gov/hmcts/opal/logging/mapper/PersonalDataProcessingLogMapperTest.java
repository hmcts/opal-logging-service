package uk.gov.hmcts.opal.logging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogResponse;
import uk.gov.hmcts.opal.logging.domain.PdpoCategory;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogIndividualEntity;

class PersonalDataProcessingLogMapperTest {

    private final PersonalDataProcessingLogMapper mapper = Mappers.getMapper(PersonalDataProcessingLogMapper.class);

    @Test
    void toDtoMapsAllFields() {
        PdpoIdentifierEntity identifier = PdpoIdentifierEntity.builder()
            .id(98L)
            .businessIdentifier("SharingCo")
            .build();

        PdpoLogEntity entity = PdpoLogEntity.builder()
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
        entity.addIndividual(PdpoLogIndividualEntity.builder()
                                 .individualIdentifier("person-1")
                                 .individualType("DEFENDANT")
                                 .build());

        AddPdpoLogResponse dto = mapper.toDto(entity);

        assertThat(dto.getPdpoLogId()).isEqualTo(123L);
        assertThat(dto.getBusinessIdentifierId()).isEqualTo(98L);
        assertThat(dto.getBusinessIdentifier()).isEqualTo("SharingCo");
        assertThat(dto.getCreatedBy().getId()).isEqualTo("requestor-1");
        assertThat(dto.getCreatedBy().getType()).isEqualTo("OPAL_USER_ID");
        assertThat(dto.getRecipient().getId()).isEqualTo("recipient-42");
        assertThat(dto.getRecipient().getType()).isEqualTo("EXTERNAL_SERVICE");
        assertThat(dto.getCategory()).isEqualTo(AddPdpoLogResponse.CategoryEnum.DISCLOSURE);
        assertThat(dto.getIndividuals()).hasSize(1);
        assertThat(dto.getIndividuals().get(0).getId()).isEqualTo("person-1");
    }

    @Test
    void toDtoOmitsRecipientWhenNotProvided() {
        PdpoLogEntity entity = PdpoLogEntity.builder()
            .createdByIdentifier("requestor-1")
            .createdByIdentifierType("OPAL_USER_ID")
            .createdAt(OffsetDateTime.parse("2025-11-15T12:45:00Z"))
            .ipAddress("192.168.1.10")
            .category(PdpoCategory.COLLECTION)
            .businessIdentifier(PdpoIdentifierEntity.builder()
                                    .id(55L)
                                    .businessIdentifier("ACME")
                                    .build())
            .build();

        AddPdpoLogResponse dto = mapper.toDto(entity);

        assertThat(dto.getRecipient()).isNull();
    }

    @Test
    void mapCategoryReturnsNullWhenCategoryNull() {
        assertThat(mapper.mapCategory(null)).isNull();
    }

    @Test
    void mapIndividualsReturnsEmptyListWhenNull() {
        assertThat(mapper.mapIndividuals(null)).isEqualTo(List.of());
    }

    @ParameterizedTest
    @EnumSource(PdpoCategory.class)
    void toDtoMapsEveryCategory(PdpoCategory category) {
        PdpoLogEntity entity = PdpoLogEntity.builder()
            .id(10L)
            .createdByIdentifier("requestor-1")
            .createdByIdentifierType("OPAL_USER_ID")
            .createdAt(OffsetDateTime.parse("2025-11-15T12:45:00Z"))
            .ipAddress("192.168.1.10")
            .category(category)
            .businessIdentifier(PdpoIdentifierEntity.builder()
                                    .id(20L)
                                    .businessIdentifier("ACME")
                                    .build())
            .build();

        AddPdpoLogResponse dto = mapper.toDto(entity);

        assertThat(dto.getCategory()).isEqualTo(AddPdpoLogResponse.CategoryEnum.valueOf(category.name()));
    }
}
