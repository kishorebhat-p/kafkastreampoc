package com.bofa.kafkastreampoc;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import com.bofa.kafkastreampoc.doa.PaymentDetails;
import com.bofa.kafkastreampoc.doa.PaymentFullDetails;
import com.bofa.kafkastreampoc.doa.PaymentTransaaction;
import com.bofa.kafkastreampoc.serde.CustomSerdes;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KTableJoinExample {

	static final String PARENT_TOPIC = "TestParentTopic";
	static final String CHILD_TOPIC = "TestChildTopic";
	static final String DEFAULT_HOST = "vmpaykafkaub01";

	static final String TO_TOPIC = "TestAggregatedTopic_2";

	static final String ORPHAN_TOPIC = "TestOrphanTopic";

	static final String NULLIPARA_TOPIC = "TestNulliparaTopic";

	static final String AGGREGATED_STORE = "aggreatedStore_2";

	static final int DEFAULT_PORT = 9092;

	public static void main(final String[] args) throws Exception {

		final String bootstrapServers = DEFAULT_HOST + ":" + DEFAULT_PORT;

		ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(2);

		final Properties streamsConfiguration = new Properties();
		// Give the Streams application a unique name. The name must be unique in the
		// Kafka cluster
		// against which the application is run.
		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "TestParentChildTopic_2");
		streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "TestParentChildTopic-client_2");
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
		final File example = Files.createTempDirectory("TestParentChildTopic_2").toFile();
		streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, example.getPath());

		KafkaStreams streams = createStreams(streamsConfiguration, threadPool);
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

	static KafkaStreams createStreams(final Properties streamsConfiguration, ScheduledThreadPoolExecutor threadPool) {
		final Serde<String> stringSerde = Serdes.String();
		final StreamsBuilder builder = new StreamsBuilder();
		final long timeoutInternval = 120000;

		final KTable<String, PaymentTransaaction> transactions = builder.table(PARENT_TOPIC,
				Consumed.with(stringSerde, CustomSerdes.TransactionSerde()));
		final KTable<String, PaymentDetails> paymentDetails = builder.table(CHILD_TOPIC,
				Consumed.with(stringSerde, CustomSerdes.DetailsSerde()));
		final PaymentDetailsJoiner trackJoiner = new PaymentDetailsJoiner();

		
		Materialized.as("MaterializedDataForStream").withRetention(Duration.ofMinutes(3));
		Materialized<String, PaymentFullDetails, KeyValueStore<Bytes, byte[]>> with = Materialized.with(stringSerde,
				CustomSerdes.FullPaymentSerde());
		final KTable<String, PaymentFullDetails> fullPaymentDetails = transactions.outerJoin(paymentDetails,
				trackJoiner,with);

		final KStream<String, PaymentFullDetails> fullPaymentStream = fullPaymentDetails.toStream();

		// Write to final Topic if Parent and child is present
		fullPaymentStream.filter((k, v) -> (v.isChildPresent() && v.isParentPresent())).to(TO_TOPIC,
				Produced.with(stringSerde, CustomSerdes.FullPaymentSerde()));

		// Write to Orphan Topic if Parent is not present
		fullPaymentStream.filter(
				(k, v) -> (v.isChildPresent() && !v.isParentPresent() && v.hasMessageTimeElapsed(timeoutInternval)))
				.to(ORPHAN_TOPIC, Produced.with(stringSerde, CustomSerdes.FullPaymentSerde()));

		// Write to Parent without child
		fullPaymentStream.filter(
				(k, v) -> (!v.isChildPresent() && v.isParentPresent() && v.hasMessageTimeElapsed(timeoutInternval)))
				.to(NULLIPARA_TOPIC, Produced.with(stringSerde, CustomSerdes.FullPaymentSerde()));

		return new KafkaStreams(builder.build(), streamsConfiguration);

	}

}
