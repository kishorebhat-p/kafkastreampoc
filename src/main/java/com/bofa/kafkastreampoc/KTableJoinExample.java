package com.bofa.kafkastreampoc;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import com.bofa.kafkastreampoc.doa.PaymentDetails;
import com.bofa.kafkastreampoc.doa.PaymentFullDetails;
import com.bofa.kafkastreampoc.doa.PaymentTransaaction;
import com.bofa.kafkastreampoc.serde.CustomSerdes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.state.KeyValueStore;

public class KTableJoinExample {

	static final String PARENT_TOPIC = "TestParentTopic";
	static final String CHILD_TOPIC = "TestChildTopic";
	static final String DEFAULT_HOST = "localhost";

	static final String TO_TOPIC = "TestAggregatedTopic";

	static final String AGGREGATED_STORE = "aggreatedStore";

	static final int DEFAULT_PORT = 9092;
	private static Object KeyValueStore;   // TODO might remove
	private static Object Materialized;		// TODO might remove

	public static void main(final String[] args) throws Exception {

		final String bootstrapServers = DEFAULT_HOST + ":" + DEFAULT_PORT;

		final Properties streamsConfiguration = new Properties();
		// Give the Streams application a unique name. The name must be unique in the
		// Kafka cluster
		// against which the application is run.
		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "TestParentChildTopic");
		streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "TestParentChildTopic-client");
		// Where to find Kafka broker(s).
		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		// Set the default key serde
		streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		// Set the default value serde
		streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		// Provide the details of our embedded http service that we'll use to connect to
		// this streams
		// instance and discover locations of stores.
		streamsConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG, DEFAULT_HOST + ":" + DEFAULT_PORT);
		final File example = Files.createTempDirectory("TestParentChildTopic").toFile();
		streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, example.getPath());

		KafkaStreams streams = createStreams(streamsConfiguration);
		streams.cleanUp();
		streams.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				streams.close();
			} catch (final Exception e) {
				// ignored
			}
		}));
	}

	static KafkaStreams createStreams(final Properties streamsConfiguration) {
		final Serde<String> stringSerde = Serdes.String();
		final StreamsBuilder builder = new StreamsBuilder();

		final KTable<String, PaymentTransaaction> transactions = builder.table(PARENT_TOPIC,
				Consumed.with(stringSerde, CustomSerdes.TransactionSerde()));
		final KTable<String, PaymentDetails> paymentDetails = builder.table(CHILD_TOPIC,
				Consumed.with(stringSerde, CustomSerdes.DetailsSerde()));
		final PaymentDetailsJoiner trackJoiner = new PaymentDetailsJoiner();


		// TODO: just crudely converting to KStream for now. If this approach works just replace above Tables with Streams.
		final KStream<String, PaymentTransaaction> transaactionStream = transactions.toStream();
		final KStream<String, PaymentDetails> paymentDetailsStream = paymentDetails.toStream();

//		final KTable<String, PaymentFullDetails> fullPaymentDetails = transactions.join(
//				paymentDetails,
//				trackJoiner
//		);

//		final KStream<String, PaymentFullDetails> fullPaymentDetails = transaactionStream.join(
//				paymentDetailsStream,
//				trackJoiner,
////				JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(5))
//				JoinWindows.of(Duration.ofMinutes(5)),
//				Joined.with(stringSerde, CustomSerdes.TransactionSerde(), CustomSerdes.DetailsSerde())
////				Joined.with(stringSerde, CustomSerdes.TransactionSerde(), CustomSerdes.DetailsSerde())
////						.withName("transaction-paymentDetails-join")
////						.withWindowSizeMs(TimeUnit.MINUTES.toMillis(5))
//		);

		// TODO this is Kishore's outerJoin
		final KStream<String, PaymentFullDetails> fullPaymentDetails = transaactionStream.outerJoin(
				paymentDetailsStream,
				trackJoiner,
				JoinWindows.ofTimeDifferenceAndGrace(Duration.ofMinutes(2), Duration.ofMinutes(1)),
				StreamJoined.with(Serdes.String(), /* key */
						CustomSerdes.TransactionSerde(), /* left value */
						CustomSerdes.DetailsSerde()) /* right value */
		);

		fullPaymentDetails.filter((k, v) -> v != null).to(TO_TOPIC,
				Produced.with(stringSerde, CustomSerdes.FullPaymentSerde()));

		return new KafkaStreams(builder.build(), streamsConfiguration);

	}

}
