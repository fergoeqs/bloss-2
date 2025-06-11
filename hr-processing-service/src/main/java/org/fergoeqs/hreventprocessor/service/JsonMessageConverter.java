package org.fergoeqs.hreventprocessor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.fergoeqs.hreventprocessor.DTOs.ApplicationStatusEvent;
import org.fergoeqs.hreventprocessor.DTOs.CloseVacancyCommand;
import org.fergoeqs.hreventprocessor.DTOs.UpdateIssueKeyCommand;
import org.fergoeqs.hreventprocessor.DTOs.UpdateStatusCommand;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JsonMessageConverter implements MessageConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Class<?>> typeMap = new HashMap<>();

    public JsonMessageConverter() {
        typeMap.put("ApplicationStatusEvent", ApplicationStatusEvent.class);
        typeMap.put("UpdateIssueKeyCommand", UpdateIssueKeyCommand.class);
        typeMap.put("UpdateStatusCommand", UpdateStatusCommand.class);
        typeMap.put("CloseVacancyCommand", CloseVacancyCommand.class);
    }

    @Override
    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        try {
            String json = objectMapper.writeValueAsString(object);
            TextMessage message = session.createTextMessage(json);

            message.setStringProperty("ObjectType", object.getClass().getSimpleName());
            return message;
        } catch (Exception e) {
            throw new MessageConversionException("Failed to convert object to JSON", e);
        }
    }

    @Override
    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        if (message instanceof TextMessage textMessage) {
            try {
                String objectType = message.getStringProperty("ObjectType");
                if (objectType == null) {
                    throw new MessageConversionException("ObjectType property is missing");
                }

                Class<?> targetClass = typeMap.get(objectType);
                if (targetClass == null) {
                    throw new MessageConversionException("Unsupported ObjectType: " + objectType);
                }

                return objectMapper.readValue(textMessage.getText(), targetClass);
            } catch (Exception e) {
                throw new MessageConversionException("Failed to convert JSON to object", e);
            }
        }
        throw new MessageConversionException("Unsupported message type: " + message.getClass().getName());
    }
}