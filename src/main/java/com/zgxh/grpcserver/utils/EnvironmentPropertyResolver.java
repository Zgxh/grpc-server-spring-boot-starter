package com.zgxh.grpcserver.utils;

import com.google.common.collect.Sets;
import org.springframework.core.env.Environment;

import java.util.Set;

import static com.zgxh.grpcserver.common.Constants.GRPC_SERVICE_SCAN_BASE_PACKAGES;


/**
 * @author Yu Yang
 */
public class EnvironmentPropertyResolver {

    private final Environment environment;

    public EnvironmentPropertyResolver(Environment environment) {
        this.environment = environment;
    }

    @SuppressWarnings("unchecked")
    public Set<String> grpcServiceBasePackages() {
        return environment.getProperty(GRPC_SERVICE_SCAN_BASE_PACKAGES, Set.class, Sets.newHashSet());
    }
}
