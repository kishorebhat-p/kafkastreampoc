package com.bofa.kafkastreampoc;

import org.apache.kafka.streams.kstream.ValueJoiner;

import com.bofa.kafkastreampoc.doa.PaymentDetails;
import com.bofa.kafkastreampoc.doa.PaymentFullDetails;
import com.bofa.kafkastreampoc.doa.PaymentTransaaction;

public class PaymentDetailsJoiner implements ValueJoiner<PaymentTransaaction, PaymentDetails, PaymentFullDetails> {

	@Override
	public PaymentFullDetails apply(PaymentTransaaction value1, PaymentDetails value2) {
		// TODO Auto-generated method stub
		if (value2 != null && value2.getCardID() != null) {
			PaymentFullDetails fullDetails = new PaymentFullDetails();
			fullDetails.setPaymentID(value1.getPaymentID());
			fullDetails.setPaymenttimeinMS(value1.getPaymenttimeinMS());
			fullDetails.setFromID(value1.getFromID());
			fullDetails.setToID(value1.getToID());
			fullDetails.setCardID(value2.getCardID());
			return fullDetails;
		} else {
			return null;
		}

	}

}
