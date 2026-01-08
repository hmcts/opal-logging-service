package uk.gov.hmcts.opal.logging.persistence.specification;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;

/**
 * Minimal subset of shared helpers for composing {@link Specification} instances.
 */
public abstract class EntitySpecs<E> {

    @SafeVarargs
    protected final List<Specification<E>> specificationList(Optional<Specification<E>>... optionalSpecs) {
        return Arrays.stream(optionalSpecs)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

    protected Optional<String> notBlank(String candidate) {
        if (candidate == null) {
            return Optional.empty();
        }
        String value = candidate.trim();
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

    protected <T> Optional<T> notNull(T value) {
        return Optional.ofNullable(value);
    }
}
