package io.proj3ct.springdemobot.service;

import com.vdurmont.emoji.EmojiParser;
import io.proj3ct.springdemobot.config.BotConfig;
import io.proj3ct.springdemobot.model.User;
import io.proj3ct.springdemobot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    final BotConfig config;

    private final String[] jokes = {
            "Why did the scarecrow win an award? Because he was outstanding in his field!",
            "Why don't skeletons fight each other? They don't have the guts.",
            "What do you call fake spaghetti? An impasta!",
            "Why did the tomato turn red? Because it saw the salad dressing!",
            "How do you organize a space party? You planet."
    };

    static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can use the following commands:\n\n" +
            "/start - See a welcome message\n" +
            "/mydata - View the data stored about yourself\n" +
            "/deletedata - Delete your stored data\n" +
            "/help - View this help message\n" +
            "/settings - Adjust your preferences (This feature is not available yet)";

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case "weather":
                case "register":
                    sendMessage(chatId, "This feature is not available yet.");
                    break;
                case "get random joke":
                    sendRandomJoke(chatId);
                    break;
                case "/mydata":
                    sendUserData(chatId);
                    break;
                case "/deletedata":
                    deleteUserData(chatId);
                    break;
                case "check my data":
                    sendUserData(chatId);
                    break;

                case "delete my data":
                    deleteUserData(chatId);
                    break;


                default:
                    sendMessage(chatId, "Sorry, command was not recognized.");
            }
        }
    }

    private void deleteUserData(long chatId) {
        if (userRepository.existsById(chatId)) {
            userRepository.deleteById(chatId);
            sendMessage(chatId, "Your data has been deleted successfully.");
        } else {
            sendMessage(chatId, "Sorry, we don't have any data stored about you.");
        }
    }


    private void sendUserData(long chatId) {
        User user = userRepository.findById(chatId).orElse(null);
        if (user != null) {
            String userData = String.format("First Name: %s\nLast Name: %s\nUsername: %s\nRegistered At: %s",
                    user.getFirstName(), user.getLastName(), user.getUserName(), user.getRegisteredAt());
            sendMessage(chatId, userData);
        } else {
            sendMessage(chatId, "Sorry, we don't have any data stored about you.");
        }
    }


    private void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();
            User user = new User();
            user.setChatID(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + ", nice to meet you!" + " :blush:");
        sendMessage(chatId, answer);
    }

    private void sendRandomJoke(long chatId) {
        Random rand = new Random();
        String joke = jokes[rand.nextInt(jokes.length)];
        sendMessage(chatId, joke);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
