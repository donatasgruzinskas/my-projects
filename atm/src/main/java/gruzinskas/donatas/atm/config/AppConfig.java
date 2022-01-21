package gruzinskas.donatas.atm.config;

import io.swagger.client.ApiClient;
import io.swagger.client.api.BankCoreDemoApiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Value("${bank.api.url}")
    private String apiUrl;

    @Bean
    public BankCoreDemoApiApi bankClient(){
        ApiClient client = new ApiClient(new RestTemplate());
        client.setBasePath(apiUrl);
        return new BankCoreDemoApiApi(client);
    }
}
