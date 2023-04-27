package com.bofa.kafkastreampoc.serde;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.util.Map;

public class JsonDeserializer<T> implements Deserializer<T> {
	private Class<T> destinationClass;

	final ObjectMapper objectMapper = new ObjectMapper();

	public JsonDeserializer(Class<T> destinationClass) {
		this.destinationClass = destinationClass;
	}

	

	@Override
	public void configure(Map<String, ?> props, boolean isKey) {
		// nothing to do
	}

	@Override
	public T deserialize(String topic, byte[] bytes) {
		if (bytes == null)
			return null;

		try {
			// Type type = destinationClass != null ? destinationClass :
			// reflectionTypeToken;
			return objectMapper.readValue(bytes, destinationClass);
		} catch (Exception e) {
			throw new SerializationException("Error deserializing message", e);
		}
	}

	@Override
	public void close() {
		// nothing to do
	}
}
