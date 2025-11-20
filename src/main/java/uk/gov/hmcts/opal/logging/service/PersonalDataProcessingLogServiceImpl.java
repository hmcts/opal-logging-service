package uk.gov.hmcts.opal.logging.service;

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.logging.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.domain.PdpoCategory;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogIndividualEntity;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoIdentifierRepository;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoLogRepository;

@Service
@Transactional
public class PersonalDataProcessingLogServiceImpl implements PersonalDataProcessingLogService {

    private final PdpoIdentifierRepository identifierRepository;
    private final PdpoLogRepository logRepository;

    public PersonalDataProcessingLogServiceImpl(PdpoIdentifierRepository identifierRepository,
                                                PdpoLogRepository logRepository) {
        this.identifierRepository = identifierRepository;
        this.logRepository = logRepository;
    }

    @Override
    public PdpoLogEntity recordLog(PersonalDataProcessingLogDetails details) {
        String businessIdentifierValue = normalized(details.getBusinessIdentifier());
        PdpoIdentifierEntity identifier = identifierRepository.findByBusinessIdentifier(businessIdentifierValue)
            .orElseGet(() -> identifierRepository.save(
                PdpoIdentifierEntity.builder()
                    .businessIdentifier(businessIdentifierValue)
                    .build()
            ));

        PdpoCategory category = PdpoCategory.from(details.getCategory());

        PdpoLogEntity log = PdpoLogEntity.builder()
            .createdByIdentifier(normalized(details.getCreatedBy().getIdentifier()))
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

    private void applyRecipient(PersonalDataProcessingLogDetails details,
                                PdpoCategory category,
                                PdpoLogEntity log) {
        ParticipantIdentifier recipient = details.getRecipient();
        if (category.requiresRecipient() && recipient != null) {
            log.setRecipientIdentifier(normalized(recipient.getIdentifier()));
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
                    .individualIdentifier(normalized(participant.getIdentifier()))
                    .individualType(resolveType(participant))
                    .build();
                log.addIndividual(entity);
            });
    }

    private static String resolveType(ParticipantIdentifier identifier) {
        return normalized(identifier.getType().getType());
    }

    private static String normalized(String value) {
        return value == null ? null : value.trim();
    }
}
