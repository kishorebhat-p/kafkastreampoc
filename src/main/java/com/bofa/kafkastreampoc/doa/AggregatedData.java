package com.bofa.kafkastreampoc.doa;

import java.util.ArrayList;
import java.util.List;

public class AggregatedData {

	public long getFirstEvent() {
		return firstEvent;
	}

	public void setFirstEvent(long firstEvent) {
		this.firstEvent = firstEvent;
	}

	public long getLastEvent() {
		return lastEvent;
	}

	public void setLastEvent(long lastEvent) {
		this.lastEvent = lastEvent;
	}

	public List<String> getListOfEvents() {
		return listOfEvents;
	}

	public void setListOfEvents(List<String> listOfEvents) {
		this.listOfEvents = listOfEvents;
	}

	private long firstEvent = 0;

	private long lastEvent = 0;

	private List<String> listOfEvents = new ArrayList<String>();
	
	private String payment_id = "";

	public String getPayment_id() {
		return payment_id;
	}

	public void setPayment_id(String payment_id) {
		this.payment_id = payment_id;
	}
}
