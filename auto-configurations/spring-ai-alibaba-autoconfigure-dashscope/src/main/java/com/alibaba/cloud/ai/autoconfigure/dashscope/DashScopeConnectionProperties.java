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

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants.DEFAULT_BASE_URL;
import static com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants.DEFAULT_READ_TIMEOUT;

/**
 * Spring AI Alibaba TongYi LLM connection properties.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

//@ConfigurationProperties 注解的类字段支持从外部配置文件（如 application.yml 或 application.properties）中进行属性绑定。对于字段名使用驼峰命名法的情况，Spring Boot 提供了自动的属性名称转换机制，使得 YAML 配置文件中的 kebab-case（短横线连接）形式能够映射到 Java 类中对应的驼峰命名字段。
//属性名称映射规则
//	驼峰命名法（Camel Case）：Java 类中的字段通常使用驼峰命名法，例如 readTimeout。
//	短横线分隔（Kebab Case）：在 application.yml 中，属性名称可以使用短横线分隔，例如 read-timeout。
//	映射机制：Spring Boot 会自动将 kebab-case 转换为驼峰命名法，这样 application.yml 中的 read-timeout 可以正确绑定到 Java 类中的 readTimeout 字段。
//实现与触发机制
//	Binder：Spring Boot 使用 Binder 类来处理这种属性绑定。具体来说，它会解析配置文件中的属性，并通过反射将这些属性注入到对应的 Java Bean 中。
//	Relaxed Binding：Spring Boot 的属性绑定是“宽松”的（Relaxed Binding），这意味着它可以自动处理不同格式的属性名称。这种机制允许多种形式的属性名，比如驼峰命名、短横线分隔、下划线分隔等。
//	触发时机：这种绑定在应用程序上下文初始化阶段发生，当 Spring 创建并初始化 @ConfigurationProperties 注解的 bean 时，Binder 负责将外部配置文件中的值映射到这些 bean 的属性上。
@ConfigurationProperties(DashScopeConnectionProperties.CONFIG_PREFIX)
public class DashScopeConnectionProperties extends DashScopeParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.dashscope";

	private Integer readTimeout;

	public DashScopeConnectionProperties() {
		super.setBaseUrl(DEFAULT_BASE_URL);
		readTimeout = DEFAULT_READ_TIMEOUT;
	}

	public Integer getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(Integer readTimeout) {
		this.readTimeout = readTimeout;
	}

}
