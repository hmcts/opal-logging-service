package uk.gov.hmcts.opal.logging.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest.CategoryEnum;
import uk.gov.hmcts.opal.logging.generated.dto.ParticipantIdentifier;

class PdpoQueueMessageMapperTest {

    private final ObjectMapper jsonMapper = JsonMapper.builder()
        .findAndAddModules()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build();

    private final PdpoQueueMessageMapper queueMessageMapper = Mappers.getMapper(PdpoQueueMessageMapper.class);
    private static final int NON_STRING_JSON_VALUE = 123;

    @Test
    void toAddPdpoLogRequestMapsAllFieldsFromLegacyIndividuals() {
        JsonNode individuals = jsonMapper.valueToTree(List.of(
            Map.of("id", "ind-1", "type", "DEFENDANT"),
            Map.of("id", "ind-2", "type", "MINOR_CREDITOR")));

        AddPdpoLogRequest request = queueMessageMapper.toAddPdpoLogRequest(queueDetails(individuals));

        assertThat(request.getCreatedBy()).isEqualTo(new ParticipantIdentifier().id("user-1").type("OPAL_USER_ID"));
        assertThat(request.getCreatedAt()).isEqualTo(OffsetDateTime.parse("2025-11-09T10:15:30Z"));
        assertThat(request.getBusinessIdentifier()).isEqualTo("ACME");
        assertThat(request.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(request.getCategory()).isEqualTo(CategoryEnum.COLLECTION);
        assertThat(request.getRecipient())
            .isEqualTo(new ParticipantIdentifier().id("service-1").type("EXTERNAL_SERVICE"));
        assertThat(request.getIndividuals()).containsExactly(
            new ParticipantIdentifier().id("ind-1").type("DEFENDANT"),
            new ParticipantIdentifier().id("ind-2").type("MINOR_CREDITOR")
        );
    }

    @Test
    void toAddPdpoLogRequestMapsCompactIndividuals() {
        Map<String, Object> compactIndividuals = new LinkedHashMap<>();
        compactIndividuals.put("DEFENDANT", List.of("ind-1", "ind-2"));
        compactIndividuals.put("MINOR_CREDITOR", List.of("ind-3"));

        AddPdpoLogRequest request = queueMessageMapper.toAddPdpoLogRequest(
            queueDetails(jsonMapper.valueToTree(compactIndividuals)));

        assertThat(request.getIndividuals()).containsExactly(
            new ParticipantIdentifier().id("ind-1").type("DEFENDANT"),
            new ParticipantIdentifier().id("ind-2").type("DEFENDANT"),
            new ParticipantIdentifier().id("ind-3").type("MINOR_CREDITOR"));
    }

    @Test
    void toAddPdpoLogRequestReturnsNullIndividualsWhenMissing() {
        AddPdpoLogRequest request = queueMessageMapper.toAddPdpoLogRequest(queueDetails(null));

        assertThat(request.getIndividuals()).isNull();
    }

    @Test
    void toAddPdpoLogRequestIgnoresNullCompactEntries() {
        Map<String, Object> compactIndividuals = new LinkedHashMap<>();
        compactIndividuals.put("DEFENDANT", null);
        compactIndividuals.put("MINOR_CREDITOR", List.of("ind-3"));

        AddPdpoLogRequest request = queueMessageMapper.toAddPdpoLogRequest(
            queueDetails(jsonMapper.valueToTree(compactIndividuals)));

        assertThat(request.getIndividuals()).containsExactly(
            new ParticipantIdentifier().id("ind-3").type("MINOR_CREDITOR"));
    }

    @Test
    void toAddPdpoLogRequestRejectsScalarIndividuals() {
        JsonNode individuals = jsonMapper.valueToTree("ind-1");

        assertThatThrownBy(() -> queueMessageMapper.toAddPdpoLogRequest(queueDetails(individuals)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("individuals must be an array or object");
    }

    @Test
    void toAddPdpoLogRequestRejectsCompactEntryThatIsNotArray() {
        JsonNode individuals = jsonMapper.valueToTree(Map.of("DEFENDANT", "ind-1"));

        assertThatThrownBy(() -> queueMessageMapper.toAddPdpoLogRequest(queueDetails(individuals)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("individuals compact entries must be arrays");
    }

    @Test
    void toAddPdpoLogRequestRejectsCompactIdentifierThatIsNotString() {
        JsonNode individuals = jsonMapper.valueToTree(Map.of("DEFENDANT", List.of(NON_STRING_JSON_VALUE)));

        assertThatThrownBy(() -> queueMessageMapper.toAddPdpoLogRequest(queueDetails(individuals)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("individual identifiers must be strings");
    }

    @Test
    void toAddPdpoLogRequestRejectsLegacyIdThatIsNotString() {
        JsonNode individuals = jsonMapper.valueToTree(List.of(Map.of("id", NON_STRING_JSON_VALUE, "type",
            "DEFENDANT")));

        assertThatThrownBy(() -> queueMessageMapper.toAddPdpoLogRequest(queueDetails(individuals)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("id must be a string");
    }

    @Test
    void toAddPdpoLogRequestRejectsLegacyTypeThatIsNotString() {
        JsonNode individuals = jsonMapper.valueToTree(List.of(Map.of("id", "ind-1", "type", NON_STRING_JSON_VALUE)));

        assertThatThrownBy(() -> queueMessageMapper.toAddPdpoLogRequest(queueDetails(individuals)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("type must be a string");
    }

    private PdpoQueueDetails queueDetails(JsonNode individuals) {
        PdpoQueueDetails queueDetails = new PdpoQueueDetails();
        queueDetails.setCreatedBy(new ParticipantIdentifier().id("user-1").type("OPAL_USER_ID"));
        queueDetails.setCreatedAt(OffsetDateTime.parse("2025-11-09T10:15:30Z"));
        queueDetails.setBusinessIdentifier("ACME");
        queueDetails.setIpAddress("10.0.0.1");
        queueDetails.setCategory(CategoryEnum.COLLECTION);
        queueDetails.setRecipient(new ParticipantIdentifier().id("service-1").type("EXTERNAL_SERVICE"));
        queueDetails.setIndividuals(individuals);
        return queueDetails;
    }
}
