package io.bosler.passport.security;

import io.bosler.passport.security.filter.CustomAuthenticationFilter;
import io.bosler.passport.security.filter.CustomAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration @EnableWebSecurity @RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    @Override
    //using password encoder
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean());

//         this code is for OAuth2
//        http.antMatcher("/**")
//                .cors().disable()
//                .csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/")
//                .permitAll()
//                .anyRequest()
//                .authenticated()
//                .and()
//                .oauth2Login();

        // this code is for username and password
        customAuthenticationFilter.setFilterProcessesUrl("/passport/login");
//        http.requiresChannel().anyRequest().requiresSecure();
        http.csrf().disable();
        http.cors();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests().antMatchers("/passport/login/**", "/passport/token/refresh").permitAll();
        http.authorizeRequests().antMatchers("/swagger-ui.html").permitAll();
        http.authorizeRequests().antMatchers("/swagger-ui/**").permitAll();

        // security temporarily disabled
//        http.authorizeRequests().antMatchers("/**").permitAll();
//        http.authorizeRequests().antMatchers("/fractal/**").permitAll();
//        http.authorizeRequests().antMatchers("/kitab/**").permitAll();
//        http.authorizeRequests().antMatchers("/zoro/**").permitAll();
        http.authorizeRequests().antMatchers("/v3/api-docs/**").permitAll();

        //below line of code gives away security
//         http.authorizeRequests().anyRequest().permitAll();
        //modifying it as,
        http.authorizeRequests().anyRequest().authenticated();
//
        http.addFilter(customAuthenticationFilter);
        http.addFilterBefore(new CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);


        //        http.oauth2Login().loginPage("/passport/oauth2");

    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
