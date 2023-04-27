package com.bofa.kafkastreampoc.serde;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import com.bofa.kafkastreampoc.doa.PaymentDetails;
import com.bofa.kafkastreampoc.doa.PaymentFullDetails;
import com.bofa.kafkastreampoc.doa.PaymentTransaaction;

public class CustomSerdes {

	public static Serde<PaymentTransaaction> TransactionSerde() {
		JsonSerializer<PaymentTransaaction> serializer = new JsonSerializer<>();
		JsonDeserializer<PaymentTransaaction> deserializer = new JsonDeserializer<>(PaymentTransaaction.class);
		return Serdes.serdeFrom(serializer, deserializer);
	}

	public static Serde<PaymentDetails> DetailsSerde() {
		JsonSerializer<PaymentDetails> serializer = new JsonSerializer<>();
		JsonDeserializer<PaymentDetails> deserializer = new JsonDeserializer<>(PaymentDetails.class);
		return Serdes.serdeFrom(serializer, deserializer);
	}
	
	public static Serde<PaymentFullDetails> FullPaymentSerde() {
		JsonSerializer<PaymentFullDetails> serializer = new JsonSerializer<>();
		JsonDeserializer<PaymentFullDetails> deserializer = new JsonDeserializer<>(PaymentFullDetails.class);
		return Serdes.serdeFrom(serializer, deserializer);
	}
}
