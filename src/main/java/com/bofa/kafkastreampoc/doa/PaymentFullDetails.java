package com.bofa.kafkastreampoc.doa;

public class PaymentFullDetails {

	private String paymentID;

	private String cardID;

	private long paymenttimeinMS;

	private String fromID;

	private String toID;

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

	public long getPaymenttimeinMS() {
		return paymenttimeinMS;
	}

	public void setPaymenttimeinMS(long paymenttimeinMS) {
		this.paymenttimeinMS = paymenttimeinMS;
	}

	public String getFromID() {
		return fromID;
	}

	public void setFromID(String fromID) {
		this.fromID = fromID;
	}

	public String getToID() {
		return toID;
	}

	public void setToID(String toID) {
		this.toID = toID;
	}
	
	
}
