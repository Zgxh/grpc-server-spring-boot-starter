package com.zgxh.grpcserver.postprocessor;

import com.google.api.client.util.Sets;
import com.google.common.collect.Lists;
import com.zgxh.grpcserver.annotation.GrpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * 注入GrpcService的Bean
 *
 * @author Yu Yang
 */
@Slf4j
public class GrpcServiceAnnotationPostProcessor implements BeanDefinitionRegistryPostProcessor,
        EnvironmentAware, ResourceLoaderAware {

    private final Set<String> packagesToScan;
    private Environment environment;
    private ResourceLoader resourceLoader;
    private Set<BeanDefinition> scannedGrpcServiceBeanDefinitions = Sets.newHashSet();

    // 需要管理的Bean类型
    private static final List<Class<? extends Annotation>> grpcServiceAnnotations = Lists.newArrayList(GrpcService.class);

    public GrpcServiceAnnotationPostProcessor(Set<String> basePackages) {
        this.packagesToScan = basePackages;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        scanGrpcServiceBeans(registry);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private void scanGrpcServiceBeans(BeanDefinitionRegistry registry) {
        ClassPathBeanDefinitionScanner scanner =
                new ClassPathBeanDefinitionScanner(registry, false, environment, resourceLoader);
        for (Class<? extends Annotation> grpcAnnotation : grpcServiceAnnotations) {
            scanner.addIncludeFilter(new AnnotationTypeFilter(grpcAnnotation));
        }

        for (String packageToScan : packagesToScan) {
            log.info("GrpcServiceAnnotationPostProcessor >> scanGrpcServiceBeans at package: {}", packageToScan);
            scanner.scan(packageToScan);
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(packageToScan);
            scannedGrpcServiceBeanDefinitions.addAll(candidateComponents);
        }

        DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        for (BeanDefinition scannedBeanDefinition : scannedGrpcServiceBeanDefinitions) {
            String generatedBeanName = beanNameGenerator.generateBeanName(scannedBeanDefinition, registry);
            registry.registerBeanDefinition(generatedBeanName, scannedBeanDefinition);
        }
    }
}
