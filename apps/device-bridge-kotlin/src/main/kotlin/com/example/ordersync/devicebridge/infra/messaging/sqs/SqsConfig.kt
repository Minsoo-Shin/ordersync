package com.example.ordersync.devicebridge.infra.messaging.sqs

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI

@Configuration
class SqsConfig(
	@Value("\${spring.cloud.aws.region.static:ap-northeast-2}")
	private val region: String,
	
	@Value("\${spring.cloud.aws.credentials.access-key:}")
	private val accessKey: String,
	
	@Value("\${spring.cloud.aws.credentials.secret-key:}")
	private val secretKey: String,
	
	@Value("\${spring.cloud.aws.endpoint:}")
	private val endpoint: String?,
) {
	companion object {
		private val logger = LoggerFactory.getLogger(SqsConfig::class.java)
	}

	@Bean
	fun sqsClient(): SqsClient {
		val builder = SqsClient.builder()
			.region(Region.of(region))
			.credentialsProvider(
				StaticCredentialsProvider.create(
					AwsBasicCredentials.create(accessKey, secretKey)
				)
			)

		if (!endpoint.isNullOrBlank()) {
			builder.endpointOverride(URI.create(endpoint))
			logger.info("SQS client initialized with endpoint override: $endpoint (region: $region)")
		} else {
			logger.info("SQS client initialized for region: $region")
		}

		return builder.build()
	}
}

