package uk.gov.hmcts.opal.logging.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogResponse;
import uk.gov.hmcts.opal.logging.generated.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.domain.PdpoCategory;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogIndividualEntity;

/**
 * MapStruct mapper that converts persisted {@link PdpoLogEntity} objects into
 * the generated {@link AddPdpoLogResponse} DTO returned by the API.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PersonalDataProcessingLogMapper {

    @Mapping(target = "createdBy", source = "entity", qualifiedByName = "createdBy")
    @Mapping(target = "recipient", source = "entity", qualifiedByName = "recipient")
    @Mapping(target = "businessIdentifierId", source = "entity.businessIdentifier.id")
    @Mapping(target = "businessIdentifier", source = "entity.businessIdentifier.businessIdentifier")
    @Mapping(target = "pdpoLogId", source = "entity.id")
    @Mapping(target = "category", source = "entity.category")
    @Mapping(target = "individuals", source = "entity.individuals")
    AddPdpoLogResponse toDto(PdpoLogEntity entity);

    @Named("createdBy")
    default ParticipantIdentifier mapCreatedBy(PdpoLogEntity entity) {
        return new ParticipantIdentifier()
            .id(entity.getCreatedByIdentifier())
            .type(entity.getCreatedByIdentifierType());
    }

    @Named("recipient")
    default ParticipantIdentifier mapRecipient(PdpoLogEntity entity) {
        if (entity.getRecipientIdentifier() == null) {
            return null;
        }
        return new ParticipantIdentifier()
            .id(entity.getRecipientIdentifier())
            .type(entity.getRecipientIdentifierType());
    }

    default AddPdpoLogResponse.CategoryEnum mapCategory(PdpoCategory category) {
        if (category == null) {
            return null;
        }
        return switch (category) {
            case COLLECTION ->
                AddPdpoLogResponse.CategoryEnum.COLLECTION;
            case ALTERATION ->
                AddPdpoLogResponse.CategoryEnum.ALTERATION;
            case CONSULTATION ->
                AddPdpoLogResponse.CategoryEnum.CONSULTATION;
            case DISCLOSURE ->
                AddPdpoLogResponse.CategoryEnum.DISCLOSURE;
            case COMBINATION ->
                AddPdpoLogResponse.CategoryEnum.COMBINATION;
            case ERASURE ->
                AddPdpoLogResponse.CategoryEnum.ERASURE;
        };
    }

    default List<ParticipantIdentifier> mapIndividuals(
        List<PdpoLogIndividualEntity> individuals
    ) {
        return individuals == null ? List.of() : individuals.stream()
            .map(individual -> new ParticipantIdentifier()
                .id(individual.getIndividualIdentifier())
                .type(individual.getIndividualType()))
            .toList();
    }
}
