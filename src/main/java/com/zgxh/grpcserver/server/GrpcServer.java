package com.zgxh.grpcserver.server;

import com.zgxh.grpcserver.annotation.GrpcService;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Grpc 服务器
 *
 * @author Yu Yang
 */
@Slf4j
@Getter
@Setter
public class GrpcServer implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Server server;

    @Value("${grpc.server.port}")
    private String port;

    /**
     * 启动 grpc 服务
     *
     * @throws IOException
     */
    public void startServer() throws IOException {
        Set<BindableService> grpcServices = getGrpcBeansFromIocContainer();
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(Integer.parseInt(port))
                .addService(ProtoReflectionService.newInstance());
        grpcServices.forEach(serverBuilder::addService);
        server = serverBuilder.build().start();
        log.info("Grpc server starts listening on port: " + port + " (grpc).");

        // 在spring停止运行的时候关闭 grpc 服务
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("try to shutdown the grpc server....");
            try {
                this.stopServer();
            } catch (Exception e) {
                log.error("failed to stop the grpc server!");
                e.printStackTrace();
            }
            log.info("The grpc server has been stopped!");
        }));
    }

    /**
     * 停止 grpc 服务
     *
     * @throws InterruptedException
     */
    private void stopServer() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * 当IoC容器刷新结束时，启动Grpc Server
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            startServer();
        } catch (IOException e) {
            log.error("GrpcServer failed to start! error: {}", e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取IoC容器中的所有被{@link GrpcService}标注的Bean
     *
     * @return
     */
    private Set<BindableService> getGrpcBeansFromIocContainer() {
        Map<String, Object> annotatedBeans = applicationContext.getBeansWithAnnotation(GrpcService.class);
        return annotatedBeans.values().stream().filter(item -> item instanceof BindableService)
                .map(item -> (BindableService) item).collect(Collectors.toSet());
    }
}