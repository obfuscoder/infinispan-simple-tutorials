package org.infinispan.tutorial.simple.spring.embedded;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.infinispan.commons.api.BasicCache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.spring.provider.SpringCache;
import org.infinispan.spring.provider.SpringEmbeddedCacheManager;
import org.infinispan.spring.session.configuration.EnableInfinispanEmbeddedHttpSession;
import org.infinispan.spring.starter.embedded.InfinispanCacheConfigurer;
import org.infinispan.spring.starter.embedded.InfinispanGlobalConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableInfinispanEmbeddedHttpSession
public class SpringApp {
    private static final String CACHE_NAME = "sessions";

    @Bean
    public InfinispanCacheConfigurer cacheConfigurer() {
        return manager -> {
            Configuration ispnConfig = new ConfigurationBuilder()
                    .clustering().cacheMode(CacheMode.REPL_SYNC)
                    .build();
            manager.defineConfiguration(CACHE_NAME, ispnConfig);
        };
    }

    @Bean
    public InfinispanGlobalConfigurer globalConfiguration() {
        return () -> GlobalConfigurationBuilder
                .defaultClusteredBuilder()
                .transport().defaultTransport()
                .addProperty("configurationFile", "jgroups.xml")
                .build();
    }

    @RestController
    static class SessionController {

        @Autowired
        SpringEmbeddedCacheManager cacheManager;

        @RequestMapping("/session")
        public Map<String, String> session(HttpServletRequest request) {
            new Random();
            Map<String, String> result = new HashMap<>();
            String sessionId = request.getSession(true).getId();
            result.put("created:", sessionId);
            result.put("caches:" , cacheManager.getCacheNames().stream().collect(Collectors.joining(",")));
            SpringCache cache = cacheManager.getCache(CACHE_NAME);
            byte[] data = new byte[2^16];
            random.nextBytes(data)
            cache.put(UUID.randomUUID(), data);
            BasicCache<?, ?> nativeCache = cache.getNativeCache();
            result.put("active:", String.valueOf(nativeCache.size()));
            return result;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class, args);
    }
}
