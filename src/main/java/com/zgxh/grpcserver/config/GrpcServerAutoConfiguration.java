package com.zgxh.grpcserver.config;

import com.zgxh.grpcserver.postprocessor.GrpcServiceAnnotationPostProcessor;
import com.zgxh.grpcserver.server.GrpcServer;
import com.zgxh.grpcserver.utils.EnvironmentPropertyResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Set;

import static com.zgxh.grpcserver.common.Constants.GRPC_BASE_PACKAGES_BEAN_NAME;


/**
 * @author Yu Yang
 */
@Configuration
public class GrpcServerAutoConfiguration {

    @Bean(GRPC_BASE_PACKAGES_BEAN_NAME)
    public Set<String> grpcServiceBasePackages(Environment environment) {
        EnvironmentPropertyResolver environmentPropertyResolver = new EnvironmentPropertyResolver(environment);
        return environmentPropertyResolver.grpcServiceBasePackages();
    }

    @Bean
    public GrpcServiceAnnotationPostProcessor grpcServiceAnnotationPostProcessor(
            @Qualifier(GRPC_BASE_PACKAGES_BEAN_NAME) Set<String> basePackages) {
        return new GrpcServiceAnnotationPostProcessor(basePackages);
    }

    @Bean
    public GrpcServer grpcServer() {
        return new GrpcServer();
    }
}
