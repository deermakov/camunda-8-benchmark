package benchmark.fw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
        @Bean(name = "processStartExecutor")
        public Executor processStartExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            int clusterSize = 3;
            int threads = clusterSize * 8;
            executor.setCorePoolSize(threads);
            executor.setMaxPoolSize(threads);
            return executor;
        }

}
