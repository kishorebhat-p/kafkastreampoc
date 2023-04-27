package com.bofa.kafkastreampoc.serde;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

//https://medium.com/@agvillamizar/implementing-custom-serdes-for-java-objects-using-json-serializer-and-deserializer-in-kafka-streams-d794b66e7c03
public class JsonSerializer<T> implements Serializer<T> {

	final ObjectMapper objectMapper = new ObjectMapper();

	// default constructor needed by Kafka
	public JsonSerializer() {
	}

	@Override
	public void configure(Map<String, ?> props, boolean isKey) {
		// nothing to do
	}

	@Override
	public byte[] serialize(String topic, T data) {
		if (data == null)
			return null;

		try {
			return objectMapper.writeValueAsBytes(data);
		} catch (Exception e) {
			throw new SerializationException("Error serializing JSON message", e);
		}
	}

	@Override
	public void close() {
		// nothing to do
	}

}
