package com.stroll.www.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "aws")
public class AwsProps {
	private String s3AccessKeyId;
	private String s3SecretAccessKey;
	private String s3Bucket;
	private String s3Region;
}
