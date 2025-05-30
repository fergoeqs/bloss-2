package org.fergoeqs.blps1.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.fergoeqs.blps1.dto.ApplicationRequest;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

@Component
public class JsonMessageConverter implements MessageConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        try {
            String json = objectMapper.writeValueAsString(object);
            return session.createTextMessage(json);
        } catch (Exception e) {
            throw new MessageConversionException("Failed to convert object to JSON", e);
        }
    }

    @Override
    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        if (message instanceof TextMessage textMessage) {
            try {
                return objectMapper.readValue(textMessage.getText(), ApplicationRequest.class);
            } catch (Exception e) {
                throw new MessageConversionException("Failed to convert JSON to ApplicationRequest", e);
            }
        }
        throw new MessageConversionException("Unsupported message type: " + message.getClass().getName());
    }
}