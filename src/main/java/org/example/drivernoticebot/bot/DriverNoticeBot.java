package org.example.drivernoticebot.bot;


import lombok.RequiredArgsConstructor;
import org.example.drivernoticebot.information.DriverNotice;
import org.example.drivernoticebot.repository.DriverRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;


@Component
@RequiredArgsConstructor
public class DriverNoticeBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final Map<Long, Integer> userStates = new HashMap<>();
    private final DriverRepository repository;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                Long chatId = update.getMessage().getChatId();


                int currentState = userStates.getOrDefault(chatId, 0);
                try {
                    switch (currentState) {
                        case 0:
                            if (update.getMessage().getText().equalsIgnoreCase("/start") ||
                                    update.getMessage().getText().equalsIgnoreCase("start")) {
                                execute(new SendMessage(chatId.toString(), "ENTER NAME"));
                                userStates.put(chatId, 1);
                            }
                            break;
                        case 1:
                            if (!repository.existsByChatId(chatId)) {
                                DriverNotice driver = new DriverNotice();
                                driver.setChatId(chatId);
                                driver.setDriverName(update.getMessage().getText());
                                this.repository.save(driver);
                            }
                            driverTypeButton(chatId);
                            userStates.put(chatId, 3);
                            break;
                        case 2:
                            execute(new SendMessage(chatId.toString(), "ENTER LAST WORKING DATE (DD/MM/YYYY)"));
                            userStates.put(chatId, 3);
                            break;
                        case 3:
                            if (isValidDate(update.getMessage().getText())) {
                                if (repository.existsByChatId(chatId)) {
                                    Optional<DriverNotice> notice = repository.findByChatId(chatId);
                                    DriverNotice driver = notice.get();
                                    driver.setLastWorkingDate(update.getMessage().getText());
                                    this.repository.save(driver);
                                }
                                execute(new SendMessage(chatId.toString(), "ENTER REASON FOR LEAVING"));
                                userStates.put(chatId, 4);
                            } else {
                                execute(new SendMessage(chatId.toString(), "Invalid date format. Please enter the date in DD/MM/YYYY format"));
                            }
                            break;
                        case 4:
                            if (repository.existsByChatId(chatId)) {
                                Optional<DriverNotice> notice = repository.findByChatId(chatId);
                                DriverNotice driver = notice.get();
                                driver.setReasonForLeaving(update.getMessage().getText());
                                this.repository.save(driver);

                            }
                            execute(new SendMessage(chatId.toString(), "ENTER RETURNING DATE IF AVAILABLE (DD/MM/YYYY)"));
                            userStates.put(chatId, 5);
                            break;
                        case 5:
                            if (isValidDate(update.getMessage().getText())) {
                                if (repository.existsByChatId(chatId)) {
                                    Optional<DriverNotice> notice = repository.findByChatId(chatId);
                                    DriverNotice driver = notice.get();
                                    driver.setReturningDateIfAvailable(update.getMessage().getText());
                                    this.repository.save(driver);

                                }
                                execute(new SendMessage(chatId.toString(), "ENTER UNIT NUMBER"));
                                userStates.put(chatId, 6);
                            } else {
                                execute(new SendMessage(chatId.toString(), "Invalid date format. Please enter the date in DD/MM/YYYY format"));
                            }
                            break;
                        case 6:
                            if (repository.existsByChatId(chatId)) {
                                Optional<DriverNotice> notice = repository.findByChatId(chatId);
                                if (notice.isPresent()) {
                                    DriverNotice driver = notice.get();
                                    driver.setTruckUnitNumber(update.getMessage().getText());
                                    repository.save(driver);

                                    submitButton(chatId);
                                }
                            }
                            userStates.put(chatId, 6);
                            break;
                        default:
                            break;
                    }
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } else if (update.hasCallbackQuery() && update.getCallbackQuery().getData().equalsIgnoreCase("submit")) {
            long callbackChatId = update.getCallbackQuery().getMessage().getChatId();
            if (repository.existsByChatId(callbackChatId)) {
                Optional<DriverNotice> notice = repository.findByChatId(callbackChatId);
                if (notice.isPresent()) {
                    DriverNotice driver = notice.get();
                    sendMessageToChannel(driver.toString());

                    SendMessage confirmMessage = new SendMessage();
                    confirmMessage.setChatId(String.valueOf(callbackChatId));
                    confirmMessage.setText("REGISTER SUCCESSFULLY \uD83D\uDC4F");
                    try {
                        execute(confirmMessage);
                        AnswerCallbackQuery answer = new AnswerCallbackQuery();
                        answer.setCallbackQueryId(update.getCallbackQuery().getId());
                        answer.setText("Submission confirmed.");
                        execute(answer);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            Long callbackChatId = update.getCallbackQuery().getMessage().getChatId();
            if (repository.existsByChatId(callbackChatId)) {
                Optional<DriverNotice> notice = repository.findByChatId(callbackChatId);
                if (notice.isPresent()) {
                    DriverNotice driver = notice.get();
                    SendMessage confirmMessage = new SendMessage();
                    if (update.getCallbackQuery().getData().equals("COMPANY DRIVER")) {
                        driver.setDriverType(update.getCallbackQuery().getData());
                        confirmMessage.setChatId(String.valueOf(callbackChatId));
                        confirmMessage.setText("ENTER LAST WORKING DATE (DD/MM/YYYY)");
                    } else if (update.getCallbackQuery().getData().equals("LEASE/RENT DRIVER")) {
                        driver.setDriverType(update.getCallbackQuery().getData());
                        confirmMessage.setChatId(String.valueOf(callbackChatId));
                        confirmMessage.setText("ENTER LAST WORKING DATE (DD/MM/YYYY)");
                    }
                    this.repository.save(driver);
                    try {
                        execute(confirmMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void submitButton(Long chatId) {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(" \uD83D\uDFE2 SUBMIT");
        button.setCallbackData("submit");
        rowInline.add(button);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please confirm your registration:");
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    private void driverTypeButton(Long chatId) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();

        button.setText(" \uD83D\uDFE2 COMPANY DRIVER");
        button.setCallbackData("COMPANY DRIVER");

        button2.setText(" \uD83D\uDFE2 LEASE/RENT DRIVER");
        button2.setCallbackData("LEASE/RENT DRIVER");

        rowInline.add(button);
        rowInline.add(button2);

        rowsInline.add(rowInline);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        SendMessage message = new SendMessage();

        message.setChatId(chatId.toString());
        message.setText("SELECT YOUR DRIVER TYPE");
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToChannel(String text) {
        SendMessage message = new SendMessage();
        long channelId = -1001954003495L;
        message.setChatId(Long.toString(channelId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, dateFormatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public void location(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please share your location:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton locationButton = new KeyboardButton("Share Location");
        locationButton.setRequestLocation(true);
        row.add(locationButton);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sharePhone(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please share your phone number:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton phoneButton = new KeyboardButton("Share Phone Number");
        phoneButton.setRequestContact(true);
        row.add(phoneButton);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }
}
