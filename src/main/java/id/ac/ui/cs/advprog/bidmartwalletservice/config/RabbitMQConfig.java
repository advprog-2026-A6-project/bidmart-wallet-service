package id.ac.ui.cs.advprog.bidmartwalletservice.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String WALLET_EXCHANGE = "bidmart.wallet.exchange";

    @Bean
    public TopicExchange walletExchange() {
        return new TopicExchange(WALLET_EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
