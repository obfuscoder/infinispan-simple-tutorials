package org.infinispan.tutorial.simple.spring.embedded;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.infinispan.commons.api.BasicCache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.provider.SpringCache;
import org.infinispan.spring.provider.SpringEmbeddedCacheManager;
import org.infinispan.spring.provider.SpringEmbeddedCacheManagerFactoryBean;
import org.infinispan.spring.session.configuration.EnableInfinispanEmbeddedHttpSession;
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
    public SpringEmbeddedCacheManagerFactoryBean springCache() {
        return new SpringEmbeddedCacheManagerFactoryBean();
    }

    @Bean
    EmbeddedCacheManager embeddedCacheManager() {
        GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder().clusteredDefault().build();
        Configuration configuration = new ConfigurationBuilder().clustering().cacheMode(CacheMode.REPL_SYNC).build();
        return new DefaultCacheManager(globalConfiguration, configuration);
    }

    @RestController
    static class SessionController {

        @Autowired
        SpringEmbeddedCacheManager cacheManager;

        @RequestMapping("/session")
        public Map<String, String> session(HttpServletRequest request) {
            Map<String, String> result = new HashMap<>();
            String sessionId = request.getSession(true).getId();
            result.put("created:", sessionId);
            result.put("caches:" , cacheManager.getCacheNames().stream().collect(Collectors.joining(",")));
            SpringCache cache = cacheManager.getCache(CACHE_NAME);
            BasicCache<?, ?> nativeCache = cache.getNativeCache();
            result.put("active:", String.valueOf(nativeCache.size()));
            return result;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class, args);
    }
}
