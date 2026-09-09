package com.nnp.keycloak.config;

/**
 * @author AC
 *
 */
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
/**
 * @author AC
 *
 */
@Configuration
@Slf4j
public class ModelMapperConfig {
	@Bean(name = "modelMapper")
	public ModelMapper getModelMapper() {
		log.info("ModelMapperConfig getModelMapper() - Configure ModelMapper");
		return new ModelMapper();
	}

}
