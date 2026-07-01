package uk.gov.hmcts.opal.logging.messaging;

import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.generated.dto.ParticipantIdentifier;

@Component
public class PdpoQueueMessageMapper {

    public AddPdpoLogRequest toAddPdpoLogRequest(PdpoQueueDetails queueDetails) {
        return new AddPdpoLogRequest()
            .createdBy(queueDetails.getCreatedBy())
            .businessIdentifier(queueDetails.getBusinessIdentifier())
            .createdAt(queueDetails.getCreatedAt())
            .ipAddress(queueDetails.getIpAddress())
            .category(queueDetails.getCategory())
            .recipient(queueDetails.getRecipient())
            .individuals(parseIndividuals(queueDetails.getIndividuals()));
    }

    public List<ParticipantIdentifier> parseIndividuals(JsonNode individualsNode) {
        if (individualsNode == null || individualsNode.isNull()) {
            return null;
        }
        if (individualsNode.isArray()) {
            return parseLegacyIndividuals(individualsNode);
        }
        if (individualsNode.isObject()) {
            return parseCompactIndividuals(individualsNode);
        }
        throw new IllegalArgumentException("individuals must be an array or object");
    }

    private List<ParticipantIdentifier> parseLegacyIndividuals(JsonNode individualsNode) {
        return individualsNode.valueStream()
            .map(participantNode -> new ParticipantIdentifier()
                .id(textValue(participantNode, "id"))
                .type(textValue(participantNode, "type")))
            .toList();
    }

    private List<ParticipantIdentifier> parseCompactIndividuals(JsonNode individualsNode) {
        return individualsNode.propertyStream()
            .filter(property -> property.getValue() != null && !property.getValue().isNull())
            .flatMap(property -> expandCompactIndividuals(property.getKey(), property.getValue()).stream())
            .toList();
    }

    private List<ParticipantIdentifier> expandCompactIndividuals(String type, JsonNode identifiersNode) {
        if (!identifiersNode.isArray()) {
            throw new IllegalArgumentException("individuals compact entries must be arrays");
        }

        return identifiersNode.valueStream()
            .map(identifierNode -> {
                if (!identifierNode.isString()) {
                    throw new IllegalArgumentException("individual identifiers must be strings");
                }
                return new ParticipantIdentifier().id(identifierNode.stringValue()).type(type);
            })
            .toList();
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode valueNode = node.get(fieldName);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        if (!valueNode.isString()) {
            throw new IllegalArgumentException(fieldName + " must be a string");
        }
        return valueNode.stringValue();
    }
}
