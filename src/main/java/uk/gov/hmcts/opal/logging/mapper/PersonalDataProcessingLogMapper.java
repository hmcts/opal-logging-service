package uk.gov.hmcts.opal.logging.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.generated.model.PDPLIdentifierPersonalDataProcessingLogging;
import uk.gov.hmcts.opal.generated.model.PersonalDataProcessingLogPersonalDataProcessingLogging;
import uk.gov.hmcts.opal.logging.domain.PdpoCategory;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogIndividualEntity;

/**
 * MapStruct mapper that converts persisted {@link PdpoLogEntity} objects into
 * the generated {@link PersonalDataProcessingLogPersonalDataProcessingLogging} DTO returned by the API.
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
    PersonalDataProcessingLogPersonalDataProcessingLogging toDto(PdpoLogEntity entity);

    @Named("createdBy")
    default PDPLIdentifierPersonalDataProcessingLogging mapCreatedBy(PdpoLogEntity entity) {
        return new PDPLIdentifierPersonalDataProcessingLogging()
            .id(entity.getCreatedByIdentifier())
            .type(entity.getCreatedByIdentifierType());
    }

    @Named("recipient")
    default PDPLIdentifierPersonalDataProcessingLogging mapRecipient(PdpoLogEntity entity) {
        if (entity.getRecipientIdentifier() == null) {
            return null;
        }
        return new PDPLIdentifierPersonalDataProcessingLogging()
            .id(entity.getRecipientIdentifier())
            .type(entity.getRecipientIdentifierType());
    }

    default PersonalDataProcessingLogPersonalDataProcessingLogging.CategoryEnum mapCategory(PdpoCategory category) {
        if (category == null) {
            return null;
        }
        return switch (category) {
            case COLLECTION ->
                PersonalDataProcessingLogPersonalDataProcessingLogging.CategoryEnum.COLLECTION;
            case ALTERATION ->
                PersonalDataProcessingLogPersonalDataProcessingLogging.CategoryEnum.ALTERATION;
            case CONSULTATION ->
                PersonalDataProcessingLogPersonalDataProcessingLogging.CategoryEnum.CONSULTATION;
            case DISCLOSURE ->
                PersonalDataProcessingLogPersonalDataProcessingLogging.CategoryEnum.DISCLOSURE;
            case COMBINATION ->
                PersonalDataProcessingLogPersonalDataProcessingLogging.CategoryEnum.COMBINATION;
            case ERASURE ->
                PersonalDataProcessingLogPersonalDataProcessingLogging.CategoryEnum.ERASURE;
        };
    }

    default List<PDPLIdentifierPersonalDataProcessingLogging> mapIndividuals(
        List<PdpoLogIndividualEntity> individuals
    ) {
        return individuals == null ? List.of() : individuals.stream()
            .map(individual -> new PDPLIdentifierPersonalDataProcessingLogging()
                .id(individual.getIndividualIdentifier())
                .type(individual.getIndividualType()))
            .toList();
    }
}
