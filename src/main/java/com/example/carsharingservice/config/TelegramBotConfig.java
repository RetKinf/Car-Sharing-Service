package com.example.carsharingservice.config;

import com.example.carsharingservice.api.telegram.TelegramNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
public class TelegramBotConfig {

    @Bean
    public TelegramClient telegramClient(@Value("${telegram.bot.token}") String botToken) {
        return new OkHttpTelegramClient(botToken);
    }

    @Bean
    public TelegramNotificationService telegramNotificationService(TelegramClient telegramClient) {
        return new TelegramNotificationService(telegramClient);
    }
}
