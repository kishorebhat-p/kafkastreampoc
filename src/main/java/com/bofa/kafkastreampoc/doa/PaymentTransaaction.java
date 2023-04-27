package com.bofa.kafkastreampoc.doa;

public class PaymentTransaaction {

	private String paymentID;
	
	public String getPaymentID() {
		return paymentID;
	}

	public void setPaymentID(String paymentID) {
		this.paymentID = paymentID;
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

	private long paymenttimeinMS;
	
	private String fromID;
	
	private String toID;
}
