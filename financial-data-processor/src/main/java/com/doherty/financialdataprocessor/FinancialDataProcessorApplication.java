package com.doherty.financialdataprocessor;

import com.doherty.LongTermStockData;
import com.doherty.QuarterlyEbit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class FinancialDataProcessorApplication {

	public static void main(String[] args) throws IOException, InterruptedException {

		SpringApplication.run(FinancialDataProcessorApplication.class, args);

		KafkaConsumer<String, String> consumer = getKafkaConsumer();
		consumer.subscribe(Arrays.asList("tradable_symbols"));

		Producer<String, LongTermStockData> financialDataProducer = getFinancialDataProducer();
		Producer<String, String> symbolProducer = getSymbolProducer();

		long start = System.currentTimeMillis();
		long elapsedTimeInMillis;
		int apiCallCount = 0;
		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
			for (ConsumerRecord<String, String> record : records) {
				if (apiCallCount >= 746) {
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
				System.out.println("===================");
				System.out.printf("key = %s, value = %s%n", record.key(), record.value());
				LongTermStockData longTermStockData = new LongTermStockData();
				longTermStockData.setSymbol(record.key());
				longTermStockData.setName(record.value());
				try {
					updateLongTermDataWithIncome(longTermStockData, record.key());
					System.out.println("Updated income");
					apiCallCount++;
					updateLongTermDataWithBalanceSheet(longTermStockData, record.key());
					System.out.println("Updated balance sheet");
					apiCallCount++;
				} catch (BadDataError e) {
					System.out.println("BAD DATA");
					continue;
				}
				try {
					updateLongTermDataWithInstitutionalOwnership(longTermStockData, record.key());
					System.out.println("Updated institutional");
					apiCallCount++;
				} catch (Exception e) {
					System.out.println("No institutional");
				}

				financialDataProducer.send(new ProducerRecord<>("long_term_financial_data", record.key(), longTermStockData));
				symbolProducer.send(new ProducerRecord<>("filtered_symbols", record.key(), record.value()));

			}
		}

	}

	private static KafkaConsumer<String, String> getKafkaConsumer() throws IOException {
		final Properties consumerProps = loadConfig("src/main/resources/client.properties");
		consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "java-lt-clients");
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		return new KafkaConsumer<>(consumerProps);
	}

	private static KafkaProducer<String, LongTermStockData> getFinancialDataProducer() throws IOException {
		final Properties producerProps = loadConfig("src/main/resources/client.properties");
		producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producerProps.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
		return new KafkaProducer<>(producerProps);
	}

	private static KafkaProducer<String, String> getSymbolProducer() throws IOException {
		final Properties producerProps = loadConfig("src/main/resources/client.properties");
		producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		return new KafkaProducer<>(producerProps);
	}

	private static void updateLongTermDataWithIncome(LongTermStockData longTermStockData, String symbol) {
		String incomeUrl = "https://financialmodelingprep.com/api/v3/income-statement/" + symbol +
							"?period=quarter&limit=4&apikey=";
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<IncomeStatement[]> incomeResponse = restTemplate.getForEntity(incomeUrl, IncomeStatement[].class);
		IncomeStatement[] quarterlyIncomeStatements = incomeResponse.getBody();
		System.out.println("QUERTERLY INCOME:");
		System.out.println(quarterlyIncomeStatements.toString());
		int annualEbit = 0;
		longTermStockData.setEbitQuarterly(new ArrayList<>());
		for (IncomeStatement incomeStatement : quarterlyIncomeStatements) {
			QuarterlyEbit quarterlyEbit = new QuarterlyEbit();
			quarterlyEbit.setDate(incomeStatement.getDate());
			quarterlyEbit.setEbit(incomeStatement.getEbitda());
			longTermStockData.getEbitQuarterly().add(quarterlyEbit);
			annualEbit += quarterlyEbit.getEbit();
		}
		longTermStockData.setEbitAnnual(annualEbit);
	}

	private static void updateLongTermDataWithBalanceSheet(LongTermStockData longTermStockData, String symbol) throws BadDataError {
		String balanceSheetUrl = "https://financialmodelingprep.com/api/v3/balance-sheet-statement/" + symbol +
									"?period=quarter&limit=1&apikey=";
		RestTemplate restTemplate = new RestTemplate();
		BalanceSheet[] balanceSheetResponse = restTemplate.getForEntity(balanceSheetUrl, BalanceSheet[].class).getBody();
		if (balanceSheetResponse.length > 0) {
			BalanceSheet balanceSheet = Arrays.stream(balanceSheetResponse).findFirst().get();
			System.out.println("BALANCE SHEET");
			System.out.println(balanceSheet.toString());
			longTermStockData.setAssets(balanceSheet.getTotalAssets());
			longTermStockData.setCash(balanceSheet.getCashAndCashEquivalents());
			longTermStockData.setDebt(balanceSheet.getTotalDebt());
			longTermStockData.setEquity(balanceSheet.getTotalEquity());
		} else {
			throw new BadDataError();
		}
	}

	private static void updateLongTermDataWithInstitutionalOwnership(LongTermStockData longTermStockData, String symbol) {
		String institutionalOwnershipUrl = "https://financialmodelingprep.com/api/v4/institutional-ownership/symbol-ownership?symbol="
				+ symbol+
				"&includeCurrentQuarter=true&apikey=";
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<InstitutionalOwnership[]> institutionalOwnershipResponse = restTemplate.getForEntity(institutionalOwnershipUrl, InstitutionalOwnership[].class);
		InstitutionalOwnership institutionalOwnership = Arrays.stream(institutionalOwnershipResponse.getBody()).findFirst().get();
		longTermStockData.setInstitutionalInvestment(institutionalOwnership.getOwnershipPercent());
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
