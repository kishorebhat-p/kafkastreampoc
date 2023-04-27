package com.bofa.kafkastreampoc.doa;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PaymentDetailsTest {

	@Test
	public void test() throws JsonProcessingException {
		PaymentDetails details = new PaymentDetails();
		details.setCardID("1234");
		details.setPaymentID("12345");
		
		final ObjectMapper objectMapper = new ObjectMapper();
		
		String jsonInString = objectMapper.writeValueAsString(details);
		
		System.out.println(jsonInString);

	}

}
