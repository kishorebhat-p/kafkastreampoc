package com.bofa.kafkastreampoc.doa;

public class PaymentDetails {
	
	private String paymentID;
	
	public String getPaymentID() {
		return paymentID;
	}

	public void setPaymentID(String paymentID) {
		this.paymentID = paymentID;
	}

	public String getCardID() {
		return cardID;
	}

	public void setCardID(String cardID) {
		this.cardID = cardID;
	}

	private String cardID;

}
