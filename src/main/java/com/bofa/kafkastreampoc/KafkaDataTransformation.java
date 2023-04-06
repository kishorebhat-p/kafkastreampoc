package com.bofa.kafkastreampoc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

import com.bofa.kafkastreampoc.doa.ExtractedData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaDataTransformation {

	static final String SOURCE_TOPIC = "TestPaymentTopic";

	static final String DESTINATION_TOPIC = "TestCustomerDetailsTopic";

	static final String DEFAULT_HOST = "vmpaykafkaub01";

	static final int DEFAULT_PORT = 9092;

	public static void main(final String[] args) throws Exception {

		final String bootstrapServers = DEFAULT_HOST + ":" + DEFAULT_PORT;

		final Properties streamsConfiguration = new Properties();
		// Give the Streams application a unique name. The name must be unique in the
		// Kafka cluster
		// against which the application is run.
		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "Extract-Customer-Details");
		streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "Extract-Customer-Details-client");
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
		final File example = Files.createTempDirectory("example").toFile();
		streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, example.getPath());

		KafkaStreams streams = createStreams(streamsConfiguration);
		streams.cleanUp();
		streams.start();

		System.out.println("start messaging");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				streams.close();
			} catch (final Exception e) {
				// ignored
			}
		}));
	}

	static KafkaStreams createStreams(final Properties streamsConfiguration) {
		final StreamsBuilder builder = new StreamsBuilder();

		final ObjectMapper objectMapper = new ObjectMapper();

		final KStream<String, String> textLines = builder.stream(SOURCE_TOPIC,
				Consumed.with(Serdes.String(), Serdes.String()));

		textLines.mapValues(v -> {

			System.out.println("Read messaging");
			String updatedJsonString = "";
			try {
				final JsonNode jsonNode = objectMapper.readTree(v);

				String toLastName = jsonNode.get("TO_LAST_NM").asText();
				String toFirstName = jsonNode.get("TO_FRST_NM").asText();

				String fmFirstName = jsonNode.get("FR_FRST_NM").asText();
				String fmLastName = jsonNode.get("FR_LAST_NM").asText();

				String transID = jsonNode.get("PDS_TRANS_ID").asText();

				// FR_ACC_BAL
				int frAccBal = Integer.parseInt(jsonNode.get("FR_ACC_BAL").asText());
				// TO_CURR_BAL
				int toCurrBal = Integer.parseInt(jsonNode.get("TO_CURR_BAL").asText());

				ExtractedData data = new ExtractedData();

				data.setTO_LAST_NM(toLastName);
				data.setTO_FRST_NM(toFirstName);
				data.setFR_FRST_NM(fmFirstName);
				data.setFR_LAST_NM(fmLastName);
				data.setPDS_TRANS_ID(transID);
				data.setFR_ACC_BAL(frAccBal);
				data.setTO_CURR_BAL(toCurrBal);

				String outputStr = objectMapper.writeValueAsString(data);
				updatedJsonString = outputStr;

			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
			return updatedJsonString;
		}).to(DESTINATION_TOPIC);/*
									 * .filter((k, v) -> { String updatedJsonString = ""; boolean isPresent = true;
									 * try { final JsonNode jsonNode = objectMapper.readTree(v);
									 * 
									 * 
									 * } catch (final IOException e) { throw new RuntimeException(e); } return
									 * isPresent; })
									 */

		return new KafkaStreams(builder.build(), streamsConfiguration);
	}
}
