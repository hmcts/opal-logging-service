package uk.gov.hmcts.opal.logging.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoIdentifierRepository;

@Service
@RequiredArgsConstructor
public class PdpoIdentifierServiceImpl implements PdpoIdentifierService {

    private final PdpoIdentifierRepository identifierRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PdpoIdentifierEntity findOrCreate(String businessIdentifier) {
        return identifierRepository.findByBusinessIdentifier(businessIdentifier)
            .orElseGet(() -> identifierRepository.saveAndFlush(PdpoIdentifierEntity.builder()
                .businessIdentifier(businessIdentifier)
                .build()));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<PdpoIdentifierEntity> findByBusinessIdentifier(String businessIdentifier) {
        return identifierRepository.findByBusinessIdentifier(businessIdentifier);
    }
}
