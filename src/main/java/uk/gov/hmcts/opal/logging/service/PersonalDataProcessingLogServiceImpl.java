package uk.gov.hmcts.opal.logging.service;

import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.logging.domain.PdpoCategory;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.generated.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.generated.dto.SearchPdpoLogRequest;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogIndividualEntity;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoIdentifierRepository;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoLogRepository;
import uk.gov.hmcts.opal.logging.persistence.specification.PdpoLogSearchCriteria;
import uk.gov.hmcts.opal.logging.persistence.specification.PdpoLogSpecifications;

@Service
@Transactional
public class PersonalDataProcessingLogServiceImpl implements PersonalDataProcessingLogService {

    private final PdpoIdentifierRepository identifierRepository;
    private final PdpoLogRepository logRepository;
    private final PdpoLogSpecifications logSpecifications;

    public PersonalDataProcessingLogServiceImpl(PdpoIdentifierRepository identifierRepository,
                                                PdpoLogRepository logRepository,
                                                PdpoLogSpecifications logSpecifications) {
        this.identifierRepository = identifierRepository;
        this.logRepository = logRepository;
        this.logSpecifications = logSpecifications;
    }

    @Override
    public PdpoLogEntity recordLog(AddPdpoLogRequest details) {
        String businessIdentifierValue = normalized(details.getBusinessIdentifier());
        PdpoIdentifierEntity identifier = identifierRepository.findByBusinessIdentifier(businessIdentifierValue)
            .orElseGet(() -> identifierRepository.save(
                PdpoIdentifierEntity.builder()
                    .businessIdentifier(businessIdentifierValue)
                    .build()
            ));

        PdpoCategory category = resolveCategory(details);

        PdpoLogEntity log = PdpoLogEntity.builder()
            .createdByIdentifier(normalized(details.getCreatedBy().getId()))
            .createdByIdentifierType(resolveType(details.getCreatedBy()))
            .createdAt(details.getCreatedAt())
            .ipAddress(normalized(details.getIpAddress()))
            .category(category)
            .businessIdentifier(identifier)
            .build();

        applyRecipient(details, category, log);
        attachIndividuals(details.getIndividuals(), log);

        return logRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PdpoLogEntity> searchLogs(SearchPdpoLogRequest criteria) {
        PdpoLogSearchCriteria parameters = validateAndNormalise(criteria);
        Specification<PdpoLogEntity> spec = logSpecifications.findBySearchCriteria(parameters);

        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        return logRepository.findAll(spec, sort);
    }

    private void applyRecipient(AddPdpoLogRequest details,
                                PdpoCategory category,
                                PdpoLogEntity log) {
        ParticipantIdentifier recipient = details.getRecipient();
        if (category.requiresRecipient() && recipient != null) {
            log.setRecipientIdentifier(normalized(recipient.getId()));
            log.setRecipientIdentifierType(resolveType(recipient));
        } else {
            log.setRecipientIdentifier(null);
            log.setRecipientIdentifierType(null);
        }
    }

    private void attachIndividuals(List<ParticipantIdentifier> participants, PdpoLogEntity log) {
        if (participants == null) {
            return;
        }
        participants.stream()
            .filter(Objects::nonNull)
            .forEach(participant -> {
                PdpoLogIndividualEntity entity = PdpoLogIndividualEntity.builder()
                    .individualIdentifier(normalized(participant.getId()))
                    .individualType(resolveType(participant))
                    .build();
                log.addIndividual(entity);
            });
    }

    private static String resolveType(ParticipantIdentifier identifier) {
        return normalized(identifier.getType());
    }

    private static String normalized(String value) {
        return value == null ? null : value.trim();
    }

    private static String normalizedOrNull(String value) {
        String trimmed = normalized(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }

    private PdpoCategory resolveCategory(AddPdpoLogRequest details) {
        if (details.getCategory() == null) {
            throw badRequest("category must be provided");
        }
        return PdpoCategory.fromRequestCategory(details.getCategory());
    }

    private PdpoLogSearchCriteria validateAndNormalise(SearchPdpoLogRequest request) {
        if (request == null) {
            throw badRequest("Request payload is required");
        }

        ParticipantIdentifier createdBy = request.getCreatedBy();
        String createdByIdentifier = null;
        String createdByType = null;
        if (createdBy != null) {
            createdByIdentifier = normalizedOrNull(createdBy.getId());
            createdByType = normalizedOrNull(createdBy.getType());
            if (createdByIdentifier == null || createdByType == null) {
                throw badRequest("created_by.id and created_by.type must both be supplied");
            }
        }

        String businessIdentifier = normalizedOrNull(request.getBusinessIdentifier());
        if (createdByIdentifier == null && businessIdentifier == null) {
            throw badRequest("At least one search parameter must be provided");
        }

        PdpoCategory category = null;
        if (request.getCategory() != null) {
            category = PdpoCategory.valueOf(request.getCategory().name());
        }

        return new PdpoLogSearchCriteria(createdByIdentifier, createdByType, businessIdentifier, category);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

}
