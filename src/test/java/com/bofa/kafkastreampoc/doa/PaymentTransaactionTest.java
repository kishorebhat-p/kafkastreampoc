package com.bofa.kafkastreampoc.doa;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PaymentTransaactionTest {

	@Test
	public void test() throws JsonProcessingException {
		PaymentTransaaction details = new PaymentTransaaction();
		details.setFromID("A");
		details.setPaymentID("12345");
		details.setPaymenttimeinMS(12322222);
		details.setToID("B");
		
		final ObjectMapper objectMapper = new ObjectMapper();
		
		String jsonInString = objectMapper.writeValueAsString(details);
		
		System.out.println(jsonInString);
	}

}
