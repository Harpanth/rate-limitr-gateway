// package com.example.rate_limiter_gateway.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.boot.web.servlet.FilterRegistrationBean;

// import com.example.rate_limiter_gateway.filter.ApiKeyFilter;

// @Configuration
// public class FilterConfig {

// @Bean
// public FilterRegistrationBean<ApiKeyFilter>
// apiKeyFilterRegistration(ApiKeyFilter apiKeyFilter) {

// FilterRegistrationBean<ApiKeyFilter> registrationBean = new
// FilterRegistrationBean<>();

// registrationBean.setFilter(apiKeyFilter);
// registrationBean.addUrlPatterns("/*"); // apply to ALL routes
// registrationBean.setOrder(1); // priority

// return registrationBean;
// }
// }
