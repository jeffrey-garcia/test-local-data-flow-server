//package com.example.jeffrey.localdataflowserver.LocalDataFlowServer.security;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//
//@Configuration
//@Order(99)
//public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests()
//                .antMatchers("/h2").permitAll()
//                .and()
//                .authorizeRequests().antMatchers("/h2/**").permitAll();
//
//
//        http.exceptionHandling().accessDeniedPage("/403");
//        http.csrf().disable();
//        http.headers().frameOptions().disable();
//    }
//
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication()
//                .withUser("guest").password("guest").roles("GUEST")
//                .and()
//                .withUser("admin").password("password").roles("ADMIN");
//    }
//}