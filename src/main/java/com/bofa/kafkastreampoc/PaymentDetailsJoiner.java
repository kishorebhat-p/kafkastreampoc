package com.bofa.kafkastreampoc;

import org.apache.kafka.streams.kstream.ValueJoiner;

import com.bofa.kafkastreampoc.doa.PaymentDetails;
import com.bofa.kafkastreampoc.doa.PaymentFullDetails;
import com.bofa.kafkastreampoc.doa.PaymentTransaaction;

public class PaymentDetailsJoiner implements ValueJoiner<PaymentTransaaction, PaymentDetails, PaymentFullDetails> {

	@Override
	public PaymentFullDetails apply(PaymentTransaaction parent, PaymentDetails child) {
		PaymentFullDetails fullDetails = new PaymentFullDetails();
		fullDetails.setCreationTime(System.currentTimeMillis());
		if (parent == null && child != null) {
			// Only Child is present
			System.out.println("In parent Null  " + child.toString());
			fullDetails.setParentPresent(false);
			fullDetails.setChildPresent(true);
			fullDetails.setToID(child.getPaymentID());
			fullDetails.setPaymentID(child.getPaymentID());
			fullDetails.setCardID(child.getCardID());
		} else if (child == null && parent != null) {
			// Only Parent is present
			System.out.println("In child Null  " + parent.toString());
			fullDetails.setParentPresent(true);
			fullDetails.setPaymentID(parent.getPaymentID());
			fullDetails.setPaymenttimeinMS(parent.getPaymenttimeinMS());
			fullDetails.setFromID(parent.getFromID());
			fullDetails.setToID(parent.getToID());
			fullDetails.setChildPresent(false);
		} else if (parent != null && child != null) {
			System.out.println("In FULL  " + parent.toString() + "...." + child.toString());
			// Both Parent & Child is present
			fullDetails.setParentPresent(true);
			fullDetails.setChildPresent(true);
			fullDetails.setPaymentID(parent.getPaymentID());
			fullDetails.setPaymenttimeinMS(parent.getPaymenttimeinMS());
			fullDetails.setFromID(parent.getFromID());
			fullDetails.setToID(parent.getToID());
			fullDetails.setCardID(child.getCardID());
		}
		return fullDetails;

	}

}
