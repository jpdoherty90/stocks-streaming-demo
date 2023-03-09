package com.doherty.tradinghoursprocessor;

import com.doherty.StockQuote;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

@SpringBootApplication
public class TradingHoursProcessorApplication {

	public static void main(String[] args) throws IOException, InterruptedException {

		SpringApplication.run(TradingHoursProcessorApplication.class, args);

		KafkaConsumer<String, String> consumer = getKafkaConsumer();
		consumer.subscribe(Arrays.asList("filtered_symbols"));

		Producer<String, StockQuote> quoteProducer = getStockQuoteProducer();
		Producer<String, String> symbolProducer = getSymbolProducer();

		long start = System.currentTimeMillis();
		long elapsedTimeInMillis;
		int apiCallCount = 0;

		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
			System.out.println("===================");
			if (records.count() > 0) {

				String symbolsString = "";
				for (ConsumerRecord<String, String> record : records) {
					symbolsString += record.key() + ",";
				}
				symbolsString = symbolsString.substring(0, symbolsString.length() - 1);
				String quoteUrl = "https://financialmodelingprep.com/api/v3/quote/" + symbolsString +
									"?apikey=";
				RestTemplate restTemplate = new RestTemplate();
				if (apiCallCount >= 749) {
					System.out.println("Close to API limit, will sleep if necessary");
					elapsedTimeInMillis = (System.currentTimeMillis() - start);
					if (elapsedTimeInMillis < 61000) {
						System.out.println("Sleeping until minute is up");
						Thread.sleep(61000 - elapsedTimeInMillis);
					} else {
						System.out.println("No need to sleep");
					}
					apiCallCount = 0;
					start = System.currentTimeMillis();
				}
				StockQuoteDTO[] quoteDTOs = restTemplate.getForEntity(quoteUrl, StockQuoteDTO[].class).getBody();
				apiCallCount++;
				System.out.println("Quotes:");
				for (StockQuoteDTO quoteDTO : quoteDTOs) {
					if (isValid(quoteDTO)) {
						// Produce quote to stock_quotes topic
						StockQuote quote = getQuoteFromDTO(quoteDTO);
						quoteProducer.send(new ProducerRecord<>("stock_quotes", quote.getSymbol(), quote));
						System.out.println(quote.toString());
						// Produce symbol back onto filtered symbols topic
						symbolProducer.send(new ProducerRecord<>("filtered_symbols", quote.getSymbol(), quote.getName()));
					}
				}
			}

		}

	}

	private static StockQuote getQuoteFromDTO(StockQuoteDTO quoteDTO) {
		return StockQuote.newBuilder()
				.setSymbol(quoteDTO.getSymbol())
				.setName(quoteDTO.getName())
				.setPrice(quoteDTO.getPrice())
				.setChangePercentage(quoteDTO.changesPercentage)
				.setMarketCap(quoteDTO.getMarketCap())
				.setVolume(quoteDTO.getVolume())
				.setAverageVolume(quoteDTO.avgVolume)
				.setPe(quoteDTO.getPe())
				.build();
	}

	private static Producer<String, String> getSymbolProducer() throws IOException {
		final Properties producerProps = loadConfig("src/main/resources/client.properties");
		producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		return new KafkaProducer<>(producerProps);
	}

	private static Producer<String, StockQuote> getStockQuoteProducer() throws IOException {
		final Properties producerProps = loadConfig("src/main/resources/client.properties");
		producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producerProps.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
		return new KafkaProducer<>(producerProps);
	}

	private static boolean isValid(StockQuoteDTO quote) {
		return quote.getMarketCap() > 0
				&& quote.getAvgVolume() > 0
				&& quote.getPe() > 0.0;
	}

	private static KafkaConsumer<String, String> getKafkaConsumer() throws IOException {
		final Properties consumerProps = loadConfig("src/main/resources/client.properties");
		consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "trading-hours-processors");
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "20");
		consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		return new KafkaConsumer<>(consumerProps);
	}

	public static Properties loadConfig(final String configFile) throws IOException {
		if (!Files.exists(Paths.get(configFile))) {
			throw new IOException(configFile + " not found.");
		}
		final Properties cfg = new Properties();
		try (InputStream inputStream = new FileInputStream(configFile)) {
			cfg.load(inputStream);
		}
		return cfg;
	}

}
