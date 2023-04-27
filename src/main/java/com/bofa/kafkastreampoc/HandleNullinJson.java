package com.bofa.kafkastreampoc;

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HandleNullinJson {

	public static void main(final String[] args) throws Exception {

		String jsonString = "{\"id\":1 , \"data\":null, \"data2\":\"\"}";

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		

		//final JsonNode jsonNode = objectMapper.readTree(jsonString);
		JsonNode jsonNode = objectMapper.readValue(jsonString, JsonNode.class);
		//JsonNode jsonNode = objectMapper.readValue(jsonString, GenericJsonNode.class);
		
		System.out.println(jsonNode.hasNonNull(0));
		System.out.println(jsonNode.isEmpty());
		
		String outputStr = objectMapper.writeValueAsString(jsonNode);

		System.out.println(outputStr);
		
		//new Gson();
	}

}

//https://mkyong.com/java/jackson-how-to-ignore-null-fields/
//https://howtodoinjava.com/gson/gson-exclude-or-ignore-fields/
//https://self-learning-java-tutorial.blogspot.com/2015/10/gson-ignore-null-and-empty-fields.html
//https://dzone.com/articles/custom-json-deserialization-with-jackson
//https://www.baeldung.com/jackson-ignore-null-fields
