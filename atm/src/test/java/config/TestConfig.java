package config;

import io.swagger.client.api.BankCoreDemoApiApi;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan
@EnableWebMvc
public class TestConfig implements WebMvcConfigurer {

    @Bean
    public BankCoreDemoApiApi bankClient(){
        return Mockito.mock(BankCoreDemoApiApi.class);
    }

}
