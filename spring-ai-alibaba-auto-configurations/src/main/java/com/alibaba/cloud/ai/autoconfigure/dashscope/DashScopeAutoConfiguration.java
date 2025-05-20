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

import java.time.Duration;

import com.alibaba.cloud.ai.dashscope.api.DashScopeAgentApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAudioTranscriptionApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeImageApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeSpeechSynthesisApi;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioTranscriptionModel;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeSpeechSynthesisModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankModel;
import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import static com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeConnectionUtils.resolveConnectionProperties;

/**
 * @author nuocheng.lxm
 * @author yuluo
 * @since 2024/8/16 11:45
 */
// @formatter:off
// @ConditionalOnClass 注解用于根据特定类是否存在于类路径中来有条件地启用或禁用配置类、Bean 或方法。
@ConditionalOnClass(DashScopeApi.class)

//配合加载AutoConfiguration注解类的触发条件和时机：
//在 Spring Boot 中，自动配置类的加载和实例化是通过一系列机制实现的，其中一个关键机制涉及 META-INF/spring 文件夹中的 org.springframework.boot.autoconfigure.AutoConfiguration.imports 文件。这是 Spring Boot 用来配置自动配置类的一个重要组成部分。
//下面是自动扫描和实例化过程的详细说明：
//	自动扫描配置类的机制
//		META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports 文件：该文件列出了所有需要加载的自动配置类的完整类名。
//		Spring Boot 在启动时会扫描该文件以确定需要加载和应用的自动配置类。
//	SpringFactoriesLoader：
//		Spring 使用 SpringFactoriesLoader 类来读取 META-INF/spring.factories（以及 AutoConfiguration.imports）文件。
//		SpringFactoriesLoader 会扫描类路径中的这些文件，加载和解析其中定义的自动配置类。
//	自动配置类的加载：
//		Spring Boot 通过 @EnableAutoConfiguration 注解启用自动配置机制。
//		在应用启动时，EnableAutoConfigurationImportSelector 会处理该注解，并利用 SpringFactoriesLoader 来加载所有需要的自动配置类。
//		这些类会依次被应用，以根据应用的需求和环境来创建 Bean。
//实例化配置类的触发时机
//	应用启动时：
//		自动配置类的加载和实例化发生在 Spring Boot 应用启动过程中，是应用上下文初始化的一部分。
//	上下文刷新阶段：
//		在 Spring 应用上下文的刷新阶段，Spring Boot 会根据条件注解（如 @ConditionalOnClass、@ConditionalOnMissingBean 等）来评估哪些自动配置类应该被应用。
//		满足条件的自动配置类会被实例化，其定义的 Bean 会被创建并加入到应用上下文中。
//	条件匹配：
//		自动配置类会在评估条件注解之后实例化。所有条件满足后，Spring 负责调用配置类中的方法创建 Bean。

//@AutoConfiguration 注解用于标记自动配置类，这些类通常包含应用所需的 Bean 定义，并根据当前的类路径、环境变量、及其他条件来有条件地创建这些 Bean。
//before 属性
//	作用：before 属性用于指定当前自动配置类应在指定的其他自动配置类之前应用。
//	使用场景：如果当前自动配置类的设置需要在某些配置类之前完成，以便后者能够正确地利用前者的设置或结果，可以使用 before 属性。
//after 属性的作用
//	在自动配置过程中，有时需要控制配置类的加载顺序，after 属性就是用于指定当前自动配置类应该在哪些配置类之后加载。
//	控制加载顺序：after 属性可以指定一个或多个其他自动配置类，使当前配置类在这些类之后被加载。这对于确保某些配置在其他配置准备好之后才进行，是非常重要的。
@AutoConfiguration(after = {
		RestClientAutoConfiguration.class,
		SpringAiRetryAutoConfiguration.class,
        ToolCallingAutoConfiguration.class})

//@ImportAutoConfiguration用于显式地导入指定的自动配置类。这在测试环境或者应用程序需要特定的自动配置时非常有用，因为它允许开发者精确控制哪些自动配置类被应用，而不必依赖 Spring Boot 自动扫描和加载机制。
//@ImportAutoConfiguration 的作用
//	显式导入自动配置：通过明确指定要导入的自动配置类，开发者可以在需要时启用特定的自动配置，而不必启用所有可用的自动配置。
//	测试支持：在测试环境中，它允许开发者仅加载特定的自动配置类，从而加快测试启动速度，并避免不必要的配置。
//	控制配置环境：当应用程序需要在某些场景下启用或禁用特定的自动配置时，可以使用这个注解来实现更细粒度的控制。
//与 @AutoConfiguration 配合使用
//	默认自动配置机制：Spring Boot 通常通过 @EnableAutoConfiguration 自动加载所有在类路径上可用的自动配置类。这是通过扫描 META-INF/spring.factories 文件来实现的，文件中列出了所有可用的自动配置类。
//	选择性导入：通过使用 @ImportAutoConfiguration，开发者可以绕过自动扫描机制，而只导入他们明确指定的自动配置类。这可以在控制应用程序的启动行为时提供更大的灵活性。
@ImportAutoConfiguration(classes = {
		SpringAiRetryAutoConfiguration.class,
		RestClientAutoConfiguration.class,
		ToolCallingAutoConfiguration.class,
		WebClientAutoConfiguration.class
})


//@EnableConfigurationProperties，用于启用对 @ConfigurationProperties 注解类的支持。
// 它的主要作用是将某个或多个配置属性类（通常是 @ConfigurationProperties 标注的类）注入到 Spring 应用上下文中。这样，Spring Boot 就可以将外部配置（如 application.yml 或 application.properties 文件中的配置）绑定到这些类的属性中。
//@EnableConfigurationProperties 的作用
//	配置绑定：将外部配置文件中的属性绑定到 Java 类中，使得这些配置可以通过类型安全的方式在应用程序中使用。
//	简化配置管理：通过使用 @ConfigurationProperties 和 @EnableConfigurationProperties，开发人员可以将配置集中管理，并在代码中以更直观和类型安全的方式访问这些配置。
//	自动注入：标记为 @ConfigurationProperties 的类在启用后会自动作为 Bean 注入到 Spring 上下文中，无需显式声明 @Bean。
//使用时机
//	复杂配置映射：当你的应用程序需要从外部配置文件中读取和管理复杂的配置结构时，使用 @EnableConfigurationProperties 可以帮助将这些配置映射到 Java 类中。
//	类型安全的配置访问：在需要以类型安全的方式访问配置时，这种方法提供了编译时检查的能力，可以避免手动解析配置文件可能带来的错误。
//	模块化配置：当你有多个模块化的配置类，每个类处理不同的配置领域时，可以使用 @EnableConfigurationProperties 来管理这些类的实例化和属性绑定。

//如果一个类上标注了 @ConfigurationProperties 注解，但没有通过 @EnableConfigurationProperties 或其他方式使其生效，那么该类不会自动被 Spring 容器管理为一个 Bean。
//详细解释
//	@ConfigurationProperties 注解：此注解本身只是用于指示一个类用于绑定外部配置属性。它不具备自动将该类注册为 Spring Bean 的能力。其主要功能是将配置文件中的属性绑定到 Java 类的字段上。
//	@EnableConfigurationProperties 注解：这个注解是用于激活 @ConfigurationProperties 支持的关键。它告诉 Spring Boot 应用程序去管理一个或多个 @ConfigurationProperties 注解的类，使它们成为可用的 Spring Bean。
//其他激活方式：
//	在 @Configuration 类中显式声明 @Bean 方法。
//	在 Spring Boot 应用中使用 @SpringBootApplication 组合注解时包含了 @EnableConfigurationProperties，但需要通过类路径扫描或者显式声明来具体启用某些配置属性类。
@EnableConfigurationProperties({
		DashScopeConnectionProperties.class,
		DashScopeChatProperties.class,
		DashScopeImageProperties.class,
		DashScopeSpeechSynthesisProperties.class,
		DashScopeAudioTranscriptionProperties.class,
		DashScopeEmbeddingProperties.class,
		DashScopeRerankProperties.class
})
public class DashScopeAutoConfiguration {

	@Bean
	public RestClientCustomizer restClientCustomizer(DashScopeConnectionProperties commonProperties) {

		return restClientBuilder -> restClientBuilder
				.requestFactory(ClientHttpRequestFactories.get(ClientHttpRequestFactorySettings.DEFAULTS
						.withReadTimeout(Duration.ofSeconds(commonProperties.getReadTimeout()))));
	}

	/**
	 * Spring AI Alibaba DashScope Chat Configuration.
	 */
//@Configuration 和 @ConditionalOnProperty 注解通常用于条件性地配置 Bean 或组件。下面是对这两个注解的作用及其使用方式的详细说明：
//@Configuration
//	作用：@Configuration 注解用于标识一个类是 Spring 的配置类，这个类中的 @Bean 方法会被 Spring 容器管理，作为配置提供者。
//	使用场景：通常用于定义应用程序上下文的 Bean，尤其是在手动配置 Bean 或者需要一些复杂逻辑来创建 Bean 实例的时候。
//@ConditionalOnProperty
//	作用：@ConditionalOnProperty 是 Spring Boot 提供的一个条件注解，用于在指定的属性满足特定条件时才启用配置。
//	字段含义：
//		prefix：属性的前缀。它与 name 一起决定完整的属性名称。例如，如果 prefix 为 app 且 name 为 feature，则完整的属性名称为 app.feature。
//		name：属性的名称，结合 prefix 可以指定完整的属性路径。
//		havingValue：属性需要匹配的值。如果属性值与指定的 havingValue 相匹配，则条件成立。
//		matchIfMissing：一个布尔值，用于指示如果属性未定义，则条件是否应视为成立。默认为 false，即如果属性缺失则条件不成立。
//	使用场景：@ConditionalOnProperty 非常适合用来控制一些可选特性或功能的启用/禁用，比如启用/禁用某个模块、功能开关等。
	@Configuration
	@ConditionalOnProperty(
			prefix = DashScopeChatProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true)
	protected static class DashScopeChatConfiguration {

//@Bean 和 @ConditionalOnMissingBean 注解可以结合使用来控制 Bean 的创建逻辑，特别是在条件配置中。这种组合在以下情况下特别有用：当你希望提供一个默认的 Bean 实例，但允许用户通过自定义配置来覆盖这个默认实例。
//@Bean 注解
//	作用：@Bean 注解用于告诉 Spring 容器这个方法会返回一个对象，并且这个对象应该被注册为 Spring 应用上下文中的 Bean。
//	使用场景：通常用于在 Java 配置类中显式声明配置方法，以便 Spring 管理这些对象。
//@ConditionalOnMissingBean 注解
//	作用：@ConditionalOnMissingBean 是 Spring Boot 的一个条件注解。它指示 Spring 仅在应用上下文中没有指定类型的 Bean 时才创建被注解的方法返回的 Bean。
//	使用场景：用于在自动配置中提供一个默认实现，同时允许应用开发者通过显式定义来覆盖这个默认实现。
//组合使用的作用
//	当这两个注解一起使用时，意味着以下逻辑：
//		提供默认实现：方法被 @Bean 注解标记，这意味着它返回的对象将被注册为一个 Bean。
//		避免 Bean 重复创建：@ConditionalOnMissingBean 确保只有在上下文中不存在相同类型的 Bean 时，才会调用该方法并创建 Bean。这允许开发者在不修改自动配置代码的情况下，自定义或覆盖自动配置提供的 Bean。
		@Bean
		@ConditionalOnMissingBean
		public DashScopeChatModel dashscopeChatModel(
				DashScopeConnectionProperties commonProperties,
				DashScopeChatProperties chatProperties,
				RestClient.Builder restClientBuilder,
				WebClient.Builder webClientBuilder,
				ToolCallingManager toolCallingManager,
				RetryTemplate retryTemplate,
				ResponseErrorHandler responseErrorHandler,
				//在 Spring 框架中，当使用 ObjectProvider 注入 Bean 时，Spring 确保 ObjectProvider 本身绝不会为 null。这意味着即使 ObservationRegistry 类型的 Bean 不存在，ObjectProvider<ObservationRegistry> 入参也会被注入一个非空的 ObjectProvider 实例。此实例可以用于安全地尝试获取目标 Bean，或者检查目标 Bean 是否存在，并在没有可用 Bean 时执行特定的行为。
				//具体原因
				//	ObjectProvider 的非空特性：ObjectProvider 是一个特殊的依赖注入类型，它充当一个惰性的 Bean 提供者。无论目标 Bean 是否存在，ObjectProvider 本身总是可用的。这是因为 ObjectProvider 的设计意图就是提供一种安全的方式来处理可选性和延迟获取。
				//	Spring 注入机制：Spring 会确保所有声明的依赖注入类型都被正确设置。如果某个依赖项没有可用的 Bean，Spring 常规的做法是抛出异常，以指示配置错误。但是，通过使用 ObjectProvider，你可以避免这种情况，因为它提供了更灵活和安全的获取方式。
				//ObjectProvider 的作用
				//	延迟获取：ObjectProvider 可以在需要时才获取 Bean。这对防止在 Bean 初始化时出现循环依赖特别有帮助。
				//	灵活性：它提供了多种方法来获取 Bean，比如 getIfAvailable()、getIfUnique()，以及 stream() 方法来处理多个 Bean。
				//	处理选项 Bean：对于不是必须的或者可能不存在的 Bean，使用 ObjectProvider 可以更优雅地处理。
				//使用 ObjectProvider 的方法
				//	getIfAvailable()：尝试获取一个 Bean，如果不存在任何 Bean，则返回 null。
				//	getIfUnique()：尝试获取一个唯一的 Bean，如果存在多个或不存在任何 Bean，则返回 null。
				//	ifAvailable()：如果存在 Bean，则执行提供的 Consumer 操作。
				//	stream()：返回所有匹配类型的 Bean 的流，可以用于处理多个实例。
				ObjectProvider<ObservationRegistry> observationRegistry,
				ObjectProvider<ChatModelObservationConvention> observationConvention
		) {

			var dashscopeApi = dashscopeChatApi(
					commonProperties,
					chatProperties,
					restClientBuilder,
					webClientBuilder,
					responseErrorHandler
			);

			var dashscopeModel = new DashScopeChatModel(
					dashscopeApi,
					chatProperties.getOptions(),
					toolCallingManager,
					retryTemplate,
					// getIfUnique()：尝试获取唯一的 Bean。如果 Spring 上下文中只有一个该类型的 Bean，则返回该 Bean；否则返回 null。通过传递一个默认值的 Supplier，可以在没有唯一 Bean 时提供默认的替代。
					observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP)
			);
			// ifAvailable()：如果存在该类型的 Bean，则对 Bean 执行指定的消费者操作。即使 Bean 不存在，也不会抛出异常。
			observationConvention.ifAvailable(dashscopeModel::setObservationConvention);

			return dashscopeModel;
		}

		private DashScopeApi dashscopeChatApi(
				DashScopeConnectionProperties commonProperties,
				DashScopeChatProperties chatProperties,
				RestClient.Builder restClientBuilder,
				WebClient.Builder webClientBuilder,
				ResponseErrorHandler responseErrorHandler
		) {

			ResolvedConnectionProperties resolved = resolveConnectionProperties(
					commonProperties,
					chatProperties,
					"chat"
			);

			return new DashScopeApi(
					resolved.baseUrl(),
					resolved.apiKey(),
					resolved.workspaceId(),
					restClientBuilder,
					webClientBuilder,
					responseErrorHandler
			);
		}

		@Bean
		public DashScopeAgentApi dashscopeAgentApi(
				DashScopeConnectionProperties commonProperties,
				DashScopeChatProperties chatProperties,
				RestClient.Builder restClientBuilder,
				WebClient.Builder webClientBuilder,
				ResponseErrorHandler responseErrorHandler
		) {

			ResolvedConnectionProperties resolved = resolveConnectionProperties(commonProperties, chatProperties,
					"chat");

			return new DashScopeAgentApi(resolved.baseUrl(), resolved.apiKey(), resolved.workspaceId(),
					restClientBuilder, webClientBuilder, responseErrorHandler);
		}

	}

	/**
	 * Spring AI Alibaba DashScope Image Configuration.
	 */
	@Configuration
	@ConditionalOnProperty(
			prefix = DashScopeImageProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true)
	protected static class DashScopeImageConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public DashScopeImageModel dashScopeImageModel(
				DashScopeConnectionProperties commonProperties,
				DashScopeImageProperties imageProperties,
				RestClient.Builder restClientBuilder,
				WebClient.Builder webClientBuilder,
				RetryTemplate retryTemplate,
				ResponseErrorHandler responseErrorHandler
		) {

			ResolvedConnectionProperties resolved = resolveConnectionProperties(
					commonProperties,
					imageProperties,
					"image"
			);

			var dashScopeImageApi = new DashScopeImageApi(
					resolved.baseUrl(),
					resolved.apiKey(),
					resolved.workspaceId(),
					restClientBuilder,
					webClientBuilder,
					responseErrorHandler
			);

			DashScopeImageModel dashScopeImageModel = new DashScopeImageModel(
					dashScopeImageApi,
					imageProperties.getOptions(),
					retryTemplate
			);

			return dashScopeImageModel;
		}
	}

	/**
	 * Spring AI Alibaba DashScope Embedding Configuration.
	 */
	@Configuration
	@ConditionalOnProperty(
			prefix = DashScopeEmbeddingProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true
	)
	protected static class DashScopeEmbeddingConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@Primary
		public DashScopeEmbeddingModel dashscopeEmbeddingModel(
				DashScopeConnectionProperties commonProperties,
				DashScopeEmbeddingProperties embeddingProperties,
				RestClient.Builder restClientBuilder,
				WebClient.Builder webClientBuilder,
				RetryTemplate retryTemplate,
				ResponseErrorHandler responseErrorHandler,
				ObjectProvider<ObservationRegistry> observationRegistry,
				ObjectProvider<EmbeddingModelObservationConvention> observationConvention
		) {

			var dashScopeApi = dashscopeEmbeddingApi(
					commonProperties,
					embeddingProperties,
					restClientBuilder,
					webClientBuilder,
					responseErrorHandler
			);

			var embeddingModel = new DashScopeEmbeddingModel(
					dashScopeApi,
					embeddingProperties.getMetadataMode(),
					embeddingProperties.getOptions(),
					retryTemplate,
					observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP)
			);

			observationConvention.ifAvailable(embeddingModel::setObservationConvention);

			return embeddingModel;
		}

		private DashScopeApi dashscopeEmbeddingApi(
				DashScopeConnectionProperties commonProperties,
				DashScopeEmbeddingProperties embeddingProperties,
				RestClient.Builder restClientBuilder,
				WebClient.Builder webClientBuilder,
				ResponseErrorHandler responseErrorHandler
		) {
			ResolvedConnectionProperties resolved = resolveConnectionProperties(commonProperties, embeddingProperties,
					"embedding");

			return new DashScopeApi(resolved.baseUrl(), resolved.apiKey(), resolved.workspaceId(), restClientBuilder,
					webClientBuilder, responseErrorHandler);
		}

	}

	/**
	 * Spring AI Alibaba DashScope Speech Synthesis Configuration.
	 */
	@Configuration
	@ConditionalOnProperty(
			prefix = DashScopeSpeechSynthesisProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true)
	protected static class DashScopeSpeechSynthesisConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public DashScopeSpeechSynthesisModel dashScopeSpeechSynthesisModel(
				DashScopeConnectionProperties commonProperties,
				DashScopeSpeechSynthesisProperties speechSynthesisProperties,
				RetryTemplate retryTemplate
		) {

			var dashScopeSpeechSynthesisApi = dashScopeSpeechSynthesisApi(
					commonProperties,
					speechSynthesisProperties
			);

			return new DashScopeSpeechSynthesisModel(
					dashScopeSpeechSynthesisApi,
					speechSynthesisProperties.getOptions(),
					retryTemplate
			);
		}

		private DashScopeSpeechSynthesisApi dashScopeSpeechSynthesisApi(
				DashScopeConnectionProperties commonProperties,
				DashScopeSpeechSynthesisProperties speechSynthesisProperties
		) {

			ResolvedConnectionProperties resolved = resolveConnectionProperties(
					commonProperties,
					speechSynthesisProperties,
					"audio.synthesis"
			);

			return new DashScopeSpeechSynthesisApi(resolved.apiKey());
		}

	}

	/**
	 * Spring AI Alibaba DashScope Audio Transcription Configuration.
	 */
	@Configuration
	@ConditionalOnProperty(
			prefix = DashScopeAudioTranscriptionProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true)
	protected static class DashScopeAudioTranscriptionConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public DashScopeAudioTranscriptionModel dashScopeAudioTranscriptionModel(
				DashScopeConnectionProperties commonProperties,
				DashScopeAudioTranscriptionProperties audioTranscriptionProperties,
				RetryTemplate retryTemplate
		) {

			var dashScopeAudioTranscriptionApi = dashScopeAudioTranscriptionApi(
					commonProperties,
					audioTranscriptionProperties
			);

			return new DashScopeAudioTranscriptionModel(
					dashScopeAudioTranscriptionApi,
					audioTranscriptionProperties.getOptions(),
					retryTemplate
			);
		}

		private DashScopeAudioTranscriptionApi dashScopeAudioTranscriptionApi(
				DashScopeConnectionProperties commonProperties,
				DashScopeAudioTranscriptionProperties audioTranscriptionProperties
		) {

			ResolvedConnectionProperties resolved = resolveConnectionProperties(commonProperties,
					audioTranscriptionProperties, "audio.transcription");

			return new DashScopeAudioTranscriptionApi(resolved.apiKey());
		}

	}

	/**
	 * Spring AI Alibaba DashScope Rerank Configuration.
	 */
	@Configuration
	@ConditionalOnProperty(
			prefix = DashScopeRerankProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true
	)
	protected static class DashScopeRerankConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public DashScopeRerankModel dashscopeRerankModel(
				DashScopeConnectionProperties commonProperties,
				DashScopeRerankProperties rerankProperties,
				RestClient.Builder restClientBuilder,
				WebClient.Builder webClientBuilder,
				RetryTemplate retryTemplate,
				ResponseErrorHandler responseErrorHandler
		) {
			ResolvedConnectionProperties resolved = resolveConnectionProperties(
					commonProperties,
					rerankProperties,
					"rerank"
			);

			var dashscopeApi = new DashScopeApi(
					resolved.baseUrl(),
					resolved.apiKey(),
					resolved.workspaceId(),
					restClientBuilder,
					webClientBuilder,
					responseErrorHandler
			);

			return new DashScopeRerankModel(
					dashscopeApi,
					rerankProperties.getOptions(),
					retryTemplate
			);
		}

	}

}
// @formatter:on
