package com.bofa.kafkastreampoc.doa;

public class PaymentFullDetails {

	private String paymentID;

	private String cardID;

	private long paymenttimeinMS;

	private String fromID;

	private String toID;

	private long creationTime;

	private boolean isParentPresent = false;

	private boolean isChildPresent = false;

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

	public boolean hasMessageTimeElapsed(long timeoutInternval) {

		long timeforFailure = System.currentTimeMillis() - timeoutInternval;
		long lowerrange = System.currentTimeMillis() - (2 * timeoutInternval);
		System.out.println("In hasMessageTimeElapsed for ID    " + paymentID + "..." + creationTime + "...."
				+ timeforFailure + "...." + lowerrange);
		if (creationTime < timeforFailure && creationTime > lowerrange) {
			return true;
		} else {
			return false;
		}

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

	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public boolean isParentPresent() {
		return isParentPresent;
	}

	public void setParentPresent(boolean isParentPresent) {
		this.isParentPresent = isParentPresent;
	}

	public boolean isChildPresent() {
		return isChildPresent;
	}

	public void setChildPresent(boolean isChildPresent) {
		this.isChildPresent = isChildPresent;
	}

	@Override
	public String toString() {
		return "PaymentFullDetails [paymentID=" + paymentID + ", cardID=" + cardID + ", paymenttimeinMS="
				+ paymenttimeinMS + ", fromID=" + fromID + ", toID=" + toID + ", creationTime=" + creationTime
				+ ", isParentPresent=" + isParentPresent + ", isChildPresent=" + isChildPresent + "]";
	}

}
