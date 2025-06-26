/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.autoconfigure.dashscope;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants.DEFAULT_BASE_URL;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

@ConfigurationProperties(DashScopeChatProperties.CONFIG_PREFIX)
public class DashScopeChatProperties extends DashScopeParentProperties {

	/**
	 * Spring AI Alibaba configuration prefix.
	 */
	public static final String CONFIG_PREFIX = "spring.ai.dashscope.chat";

	/**
	 * Default DashScope Chat model.
	 */
	public static final String DEFAULT_DEPLOYMENT_NAME = "qwen-plus";

	/**
	 * Default temperature speed.
	 */
	private static final Double DEFAULT_TEMPERATURE = 0.8d;

	/**
	 * Enable Dashscope ai chat client.
	 */
	private boolean enabled = true;

//	@NestedConfigurationProperty用于标识一个 @ConfigurationProperties 类中的字段，该字段本身又是一个配置属性类。
//		这种设计允许在配置属性类中嵌套其他配置属性类，使得配置结构更复杂、更有层次性。
//@NestedConfigurationProperty 的作用
//	支持复杂的配置结构：当一个配置属性类需要包含其他配置属性类时，@NestedConfigurationProperty 可以帮助自动识别和绑定这种嵌套关系。
//	增强配置的可读性和组织性：通过嵌套配置，可以更直观地表示配置之间的层次关系和组织结构，使得配置文件更易于理解和维护。
//	自动属性绑定：Spring Boot 会自动将外部配置中的嵌套属性绑定到对应的嵌套配置类上，简化了配置管理过程。
//	spring.ai.dashscope.chat.options.model=qwen-plus  可注入到DashScopeChatOptions内的model
	@NestedConfigurationProperty
	private DashScopeChatOptions options = DashScopeChatOptions.builder()
		.withModel(DEFAULT_DEPLOYMENT_NAME)
		.withTemperature(DEFAULT_TEMPERATURE)
		.build();

	public DashScopeChatProperties() {
		super.setBaseUrl(DEFAULT_BASE_URL);
	}

	public DashScopeChatOptions getOptions() {

		return this.options;
	}

	public void setOptions(DashScopeChatOptions options) {

		this.options = options;
	}

	public boolean isEnabled() {

		return this.enabled;
	}

	public void setEnabled(boolean enabled) {

		this.enabled = enabled;
	}

}
