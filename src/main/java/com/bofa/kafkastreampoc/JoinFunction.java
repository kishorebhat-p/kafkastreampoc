package com.bofa.kafkastreampoc;

import java.util.function.Function;

import com.bofa.kafkastreampoc.doa.PaymentTransaaction;

public class JoinFunction  implements Function<PaymentTransaaction,String>{

	@Override
	public String apply(PaymentTransaaction t) {
		// TODO Auto-generated method stub
		return null;
	}

}
