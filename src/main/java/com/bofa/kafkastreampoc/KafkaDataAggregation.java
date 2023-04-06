package com.bofa.kafkastreampoc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueStore;

import com.bofa.kafkastreampoc.doa.AggregatedData;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giladam.kafka.jacksonserde.Jackson2Deserializer;
import com.giladam.kafka.jacksonserde.Jackson2Serde;

public class KafkaDataAggregation {

	static final String FROM_TOPIC = "TestForAggreationTopic";
	static final String DEFAULT_HOST = "vmpaykafkaub01";

	static final String TO_TOPIC = "TestToAggregationTopic";

	static final String AGGREGATED_STORE = "aggreatedStore";

	static final int DEFAULT_PORT = 9092;

	public static void main(final String[] args) throws Exception {

		final String bootstrapServers = DEFAULT_HOST + ":" + DEFAULT_PORT;

		final Properties streamsConfiguration = new Properties();
		// Give the Streams application a unique name. The name must be unique in the
		// Kafka cluster
		// against which the application is run.
		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "TestForAggreationTopic");
		streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "TestForAggreationTopic-client");
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
		final File example = Files.createTempDirectory("examplestate").toFile();
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

		final ObjectMapper objectMapper = new ObjectMapper();

		final KStream<String, String> textLines = builder.stream(FROM_TOPIC,
				Consumed.with(Serdes.String(), Serdes.String()));

		KStream<String, String> transformed = textLines.map((key, value) -> {

			JsonNode jsonNode;
			String keyValue = "";
			try {
				jsonNode = objectMapper.readTree(value);
				System.out.println("Read messaging ..1");
				keyValue = jsonNode.get("PAYMENT_ID").asText();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/**
			 * Logic
			 */
			KeyValue<String, String> kv = new KeyValue<>(keyValue, value);
			return kv;

		});

		// KGroupedStream<String, String> groupedByKey =
		transformed.groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
				.windowedBy(TimeWindows.of(Duration.ofMinutes(5)).grace(Duration.ofMinutes(2))).aggregate(
						// Initializer
						() -> intiateAggregator(),
						// aggregator
						(k, v, aggV) -> aggregator(k, v, aggV),
						Materialized.<String, AggregatedData, KeyValueStore<Bytes, byte[]>>as(AGGREGATED_STORE)
								.withKeySerde(Serdes.String()).withValueSerde(new Jackson2Serde(AggregatedData.class)))
				.toStream().filter((k, v) -> {
					String updatedJsonString = "";
					boolean isPresent = true;
					try {
						System.out.println("Read messaging ..3");
						byte[] serializedBytes = objectMapper.writeValueAsBytes(v);
						AggregatedData data = objectMapper.readValue(serializedBytes, AggregatedData.class);
						if (data.getListOfEvents().size() == 1) {
							isPresent = false;
						}
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					return isPresent;
				}).to(TO_TOPIC);
		;

		return new KafkaStreams(builder.build(), streamsConfiguration);
	}

	private static AggregatedData aggregator(String keyValue, String value, AggregatedData collectedData) {
		final ObjectMapper objectMapper = new ObjectMapper();
		AggregatedData returnData = collectedData;
		JsonNode jsonNode;
		try {
			jsonNode = objectMapper.readTree(value);
			long eventTime = jsonNode.get("UPDATE_TIME").asLong();
			if (collectedData.getFirstEvent() == 0 || collectedData.getFirstEvent() > eventTime) {
				collectedData.setFirstEvent(eventTime);
			}
			if (collectedData.getFirstEvent() == 0 || collectedData.getLastEvent() < eventTime) {
				collectedData.setLastEvent(eventTime);
			}

			String status_code = jsonNode.get("STATUS").asText();
			String reason_code = jsonNode.get("REASON_CODE").asText();
			String eventDetails = "{ \"STATUS\":\"" + status_code + "\"," + " \"REASON_CODE\":\"" + reason_code + "\"}";
			System.out.println("Read messaging ..2");
			returnData.getListOfEvents().add(eventDetails);
			returnData.setPayment_id(keyValue);

		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return returnData;
	}

	private static AggregatedData intiateAggregator() {
		return new AggregatedData();
	}

}
