package org.example.drivernoticebot.bot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.drivernoticebot.weather.WeatherService;
import org.example.drivernoticebot.information.Drivers;
import org.example.drivernoticebot.repository.DriversRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DriverNoticeBot2 extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final DriversRepository repository;
    private final WeatherService weatherService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<Long, Integer> userStates = new HashMap<>();
    private final Map<Long, Integer> updateStatus = new HashMap<>();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {

            Long chatId = update.getMessage().getChatId();
            if (update.getMessage().hasText() && (update.getMessage().getText().equalsIgnoreCase("start") ||
                    update.getMessage().getText().equalsIgnoreCase("/start"))) {
                replayButton(chatId);

            }
            if (update.getMessage().getText().equalsIgnoreCase("EXIT ✅")) {
                replayButton(chatId);
            }
            int currentState = userStates.getOrDefault(chatId, 0);
            try {
                extracted(update, currentState, chatId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            int updates = updateStatus.getOrDefault(chatId, 0);
            try {
                update(update, updates, chatId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            try {
                movie(update, chatId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            try {
                weather(update, chatId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            try {
                currency(update, chatId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            try {
                sports(update, chatId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            try {
                music(update, chatId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            try {
                cartoon(update, chatId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            try {
                news(update, chatId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            try {
                test(update, chatId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            try {
                gift(update, chatId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            try {
                game(update, chatId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else if (update.getMessage() != null) {
            if (update.getMessage().hasVoice()) {
                byte[] getVoiceData = new byte[0];
                Long chatId = update.getMessage().getChatId();
                sendVoice(chatId.toString(), new ByteArrayInputStream(getVoiceData), "voice");
            }

        } else if (update.hasCallbackQuery() && update.getCallbackQuery().getData().equalsIgnoreCase("submit")) {
            long callbackChatId = update.getCallbackQuery().getMessage().getChatId();
            if (repository.existsByChatId(callbackChatId)) {
                Optional<Drivers> notice = repository.findByChatId(callbackChatId);
                if (notice.isPresent()) {
                    Drivers driver = notice.get();
                    sendMessageToChannel(driver.toString());

                    SendMessage confirmMessage = new SendMessage();
                    confirmMessage.setChatId(String.valueOf(callbackChatId));
                    confirmMessage.setText("REGISTER SUCCESSFULLY \uD83D\uDC4F");
                    try {
                        execute(confirmMessage);
                        AnswerCallbackQuery answer = new AnswerCallbackQuery();
                        answer.setCallbackQueryId(update.getCallbackQuery().getId());
                        answer.setText("Submission confirmed.");
                        driver.setCreatedAt(LocalDateTime.now());
                        driver.setUpdatedAt(LocalDateTime.now());
                        this.repository.save(driver);
                        execute(answer);
                        replayButton(callbackChatId);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (update.hasCallbackQuery() && update.getCallbackQuery().getData().equalsIgnoreCase("update")) {
            long callbackChatId = update.getCallbackQuery().getMessage().getChatId();
            if (repository.existsByChatId(callbackChatId)) {
                Optional<Drivers> notice = repository.findByChatId(callbackChatId);
                if (notice.isPresent()) {
                    Drivers driver = notice.get();
                    sendMessageToChannel(driver.toString());

                    SendMessage confirmMessage = new SendMessage();
                    confirmMessage.setChatId(String.valueOf(callbackChatId));
                    confirmMessage.setText("UPDATE SUCCESSFULLY \uD83D\uDC4F");
                    try {
                        execute(confirmMessage);
                        AnswerCallbackQuery answer = new AnswerCallbackQuery();
                        answer.setCallbackQueryId(update.getCallbackQuery().getId());
                        answer.setText("UPDATE ");
                        driver.setUpdatedAt(LocalDateTime.now());
                        this.repository.save(driver);
                        execute(answer);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            Long callbackChatId = update.getCallbackQuery().getMessage().getChatId();
            if (repository.existsByChatId(callbackChatId)) {
                Optional<Drivers> notice = repository.findByChatId(callbackChatId);
                if (notice.isPresent()) {
                    Drivers driver = notice.get();
                    SendMessage confirmMessage = new SendMessage();
                    if (update.getCallbackQuery().getData().equals("HOME")) {
                        driver.setStatus(update.getCallbackQuery().getData());
                        confirmMessage.setChatId(String.valueOf(callbackChatId));
                        confirmMessage.setText("ENTER FROM (DD/MM/YYYY)");
                    } else if (update.getCallbackQuery().getData().equals("REST")) {
                        driver.setStatus(update.getCallbackQuery().getData());
                        confirmMessage.setChatId(String.valueOf(callbackChatId));
                        confirmMessage.setText("ENTER FROM (DD/MM/YYYY)");
                    } else if (update.getCallbackQuery().getData().equals("VOCATION")) {
                        driver.setStatus(update.getCallbackQuery().getData());
                        confirmMessage.setChatId(String.valueOf(callbackChatId));
                        confirmMessage.setText("ENTER FROM (DD/MM/YYYY)");
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

    private void extracted(Update update, int currentState, Long chatId) throws TelegramApiException {
        switch (currentState) {
            case 0:
                if (update.getMessage().getText().equalsIgnoreCase("REGISTER \uD83D\uDFE2")) {
                    if (this.repository.existsByChatId(chatId)) {
                        execute(new SendMessage(chatId.toString(), "YOU HAVE ALREADY REGISTERED"));
                    } else {
                        execute(new SendMessage(chatId.toString(), "ENTER NAME"));
                        userStates.put(chatId, 1);
                    }
                }
                break;
            case 1:
                if (!repository.existsByChatId(chatId)) {
                    Drivers driver = new Drivers();
                    driver.setChatId(chatId);
                    driver.setDriverName(update.getMessage().getText());
                    this.repository.save(driver);
                }
                execute(new SendMessage(chatId.toString(), "ENTER OFFICE"));
                userStates.put(chatId, 2);
                break;
            case 2:
                if (repository.existsByChatId(chatId)) {
                    Optional<Drivers> notice = repository.findByChatId(chatId);
                    Drivers driver = notice.get();
                    driver.setOffice(update.getMessage().getText());
                    this.repository.save(driver);
                }
                execute(new SendMessage(chatId.toString(), "ENTER DISPATCHER"));
                userStates.put(chatId, 3);
                break;
            case 3:
                if (repository.existsByChatId(chatId)) {
                    Optional<Drivers> notice = repository.findByChatId(chatId);
                    Drivers driver = notice.get();
                    driver.setDsp(update.getMessage().getText());
                    this.repository.save(driver);
                }
                statusButton(chatId);
                userStates.put(chatId, 4);
                break;
            case 4:
                if (isValidDate(update.getMessage().getText())) {
                    if (repository.existsByChatId(chatId)) {
                        Optional<Drivers> notice = repository.findByChatId(chatId);
                        Drivers driver = notice.get();
                        driver.setFromDate(update.getMessage().getText());
                        this.repository.save(driver);

                    }
                    execute(new SendMessage(chatId.toString(), "ENTER TO (DD/MM/YYYY)"));
                    userStates.put(chatId, 5);
                } else {
                    execute(new SendMessage(chatId.toString(), "Invalid date format. Please enter the date in DD/MM/YYYY format"));

                }
                break;
            case 5:
                if (isValidDate(update.getMessage().getText())) {
                    if (repository.existsByChatId(chatId)) {
                        Optional<Drivers> notice = repository.findByChatId(chatId);
                        Drivers driver = notice.get();
                        driver.setToDate(update.getMessage().getText());
                        this.repository.save(driver);

                    }
                    execute(new SendMessage(chatId.toString(), "ENTER ADDRESS"));
                    userStates.put(chatId, 6);
                } else {
                    execute(new SendMessage(chatId.toString(), "Invalid date format. Please enter the date in DD/MM/YYYY format"));

                }
                break;
            case 6:
                if (repository.existsByChatId(chatId)) {
                    Optional<Drivers> notice = repository.findByChatId(chatId);
                    if (notice.isPresent()) {
                        Drivers driver = notice.get();
                        driver.setAddress(update.getMessage().getText());
                        repository.save(driver);

                    }
                    execute(new SendMessage(chatId.toString(), "ENTER NOTE"));
                }
                userStates.put(chatId, 7);
                break;
            case 7:
                if (repository.existsByChatId(chatId)) {
                    Optional<Drivers> notice = repository.findByChatId(chatId);
                    if (notice.isPresent()) {
                        Drivers driver = notice.get();
                        driver.setNote(update.getMessage().getText());
                        repository.save(driver);

                        submitButton(chatId);

                    }
                }
                userStates.put(chatId, 0);
                break;
            default:
                break;
        }
    }

    private void movie(Update update, Long chatId) throws TelegramApiException {
        if (update.getMessage().getText().equalsIgnoreCase("MOVIE \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                movieButton(chatId);
            } else {
                execute(new SendMessage(chatId.toString(), "BEFORE SEE MOVIE , REGISTER PLEASE"));

            }
        }
    }

    private void sports(Update update, Long chatId) throws TelegramApiException {
        if (update.getMessage().getText().equalsIgnoreCase("SPORTS \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                execute(new SendMessage(chatId.toString(), "THERE IS NO INFORMATION "));
            } else {
                execute(new SendMessage(chatId.toString(), "BEFORE SEE SPORTS , REGISTER PLEASE"));

            }
        }
    }

    private void music(Update update, Long chatId) throws TelegramApiException {
        if (update.getMessage().getText().equalsIgnoreCase("MUSIC \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                execute(new SendMessage(chatId.toString(), "THERE IS NO INFORMATION "));
            } else {
                execute(new SendMessage(chatId.toString(), "BEFORE SEE MUSIC , REGISTER PLEASE"));

            }
        }
    }

    private void cartoon(Update update, Long chatId) throws TelegramApiException {
        if (update.getMessage().getText().equalsIgnoreCase("CARTOON \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                execute(new SendMessage(chatId.toString(), "THERE IS NO INFORMATION "));
            } else {
                execute(new SendMessage(chatId.toString(), "BEFORE SEE CARTOON , REGISTER PLEASE"));

            }
        }
    }

    private void test(Update update, Long chatId) throws TelegramApiException {
        if (update.getMessage().getText().equalsIgnoreCase("TEST \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                sendPrivateMessageWithButton("PLEASE SUBMIT", "1630048369");
            } else {
                execute(new SendMessage(chatId.toString(), "BEFORE TEST , REGISTER PLEASE"));
            }
        }
    }

    private void game(Update update, Long chatId) throws TelegramApiException {
        if (update.getMessage().getText().equalsIgnoreCase("GAME \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                gameButton(chatId);
            } else {
                execute(new SendMessage(chatId.toString(), "BEFORE TEST , REGISTER PLEASE"));
            }
        } else if (update.getMessage().getText().equalsIgnoreCase("FIND NUMBER \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                execute(new SendMessage(chatId.toString(), "ENTER THE NUMBER WHICH COMPUTER REMEMBERED (1,10)"));
            }
        }
    }

    private void gift(Update update, Long chatId) throws TelegramApiException {
        if (update.getMessage().getText().equalsIgnoreCase("GIFT \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                chooseGIFT(chatId);
            } else {
                execute(new SendMessage(chatId.toString(), "BEFORE SEE GIFT , REGISTER PLEASE"));
            }
        }
        switch (update.getMessage().getText()) {
            case "DANCING \uD83D\uDFE2": {
                SendAnimation sendAnimation = new SendAnimation();
                sendAnimation.setChatId(update.getMessage().getChatId().toString());
                sendAnimation.setAnimation(new InputFile("https://media.giphy.com/media/26ufdipQqU2lhNA4g/giphy.gif"));
                try {
                    execute(sendAnimation);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            default:
                break;
        }
    }

    private void news(Update update, Long chatId) throws TelegramApiException {
        if (update.getMessage().getText().equalsIgnoreCase("NEWS \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                newsButton(chatId);
            } else {
                execute(new SendMessage(chatId.toString(), "BEFORE SEE NEWS , REGISTER PLEASE"));

            }
        }
        if (update.getMessage().getText().equalsIgnoreCase("BIRTHDAY \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                birthdayButton(chatId);
            }
        }
        switch (update.getMessage().getText()) {
            case "1 \uD83D\uDFE2":
                execute(new SendMessage(chatId.toString(), "SIZ JUDA HAM AJOYIB INSONSIZ"));
                break;
            case "2 \uD83D\uDFE2":
                execute(new SendMessage(chatId.toString(), "SIZ JUDA HAM ZOR INSONSIZ"));
                break;
            case "3 \uD83D\uDFE2":
                execute(new SendMessage(chatId.toString(), "SIZ JUDA HAM ALO INSONSIZ"));
                break;
            case "4 \uD83D\uDFE2":
                execute(new SendMessage(chatId.toString(), "SIZ JUDA HAM AQILLI INSONSIZ"));
                break;
            case "5 \uD83D\uDFE2":
                execute(new SendMessage(chatId.toString(), "SIZ JUDA HAM GOOD INSONSIZ"));
                break;
            case "6 \uD83D\uDFE2":
                execute(new SendMessage(chatId.toString(), "SIZ JUDA HAM PERFECT INSONSIZ"));
                break;
            case "7 \uD83D\uDFE2":
                execute(new SendMessage(chatId.toString(), "SIZ JUDA HAM SECRET INSONSIZ"));
                break;
            case "8 \uD83D\uDFE2":
                execute(new SendMessage(chatId.toString(), "SIZ JUDA HAM NICE INSONSIZ"));
                break;
            default:
                break;
        }
    }

    private void currency(Update update, Long chatId) throws TelegramApiException {
        if (update.getMessage().getText().equalsIgnoreCase("CURRENCY RATE \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                currencyButton(chatId);
            } else {
                execute(new SendMessage(chatId.toString(), "BEFORE SEE CURRENCY RATE , REGISTER PLEASE"));

            }
        }
        if (update.getMessage().getText().equalsIgnoreCase("UZS \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                sendCurrencyRates(chatId.toString(), "UZS");
            }
        }
        if (update.getMessage().getText().equalsIgnoreCase("USD \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                sendCurrencyRates(chatId.toString(), "USD");
            }
        }
        if (update.getMessage().getText().equalsIgnoreCase("RUB \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                sendCurrencyRates(chatId.toString(), "RUB");
            }
        }
    }

    private void weather(Update update, Long chatId) throws TelegramApiException {
        if (update.getMessage().getText().equalsIgnoreCase("WEATHER \uD83D\uDFE2")) {
            if (this.repository.existsByChatId(chatId)) {
                weatherButton(chatId);
            } else {
                execute(new SendMessage(chatId.toString(), "BEFORE SEE WEATHER , REGISTER PLEASE"));

            }
        }

        if (update.getMessage().getText().equalsIgnoreCase("ANDIJON \uD83D\uDFE2")) {
            execute(new SendMessage(chatId.toString(), weatherService.getWeather("ANDIJON")));
        }
        if (update.getMessage().getText().equalsIgnoreCase("NAMANGAN \uD83D\uDFE2")) {
            execute(new SendMessage(chatId.toString(), weatherService.getWeather("NAMANGAN")));
        }
        if (update.getMessage().getText().equalsIgnoreCase("FERGANA \uD83D\uDFE2")) {
            execute(new SendMessage(chatId.toString(), weatherService.getWeather("FERGANA")));
        }
        if (update.getMessage().getText().equalsIgnoreCase("BUXORO \uD83D\uDFE2")) {
            execute(new SendMessage(chatId.toString(), weatherService.getWeather("BUXORO")));
        }
        if (update.getMessage().getText().equalsIgnoreCase("NAVOI \uD83D\uDFE2")) {
            execute(new SendMessage(chatId.toString(), weatherService.getWeather("NAVOI")));
        }
        if (update.getMessage().getText().equalsIgnoreCase("SAMARQAND \uD83D\uDFE2")) {
            execute(new SendMessage(chatId.toString(), weatherService.getWeather("SAMARQAND")));
        }
        if (update.getMessage().getText().equalsIgnoreCase("QASHQADARYO \uD83D\uDFE2")) {
            execute(new SendMessage(chatId.toString(), weatherService.getWeather("QASHQADARYO")));
        }
        if (update.getMessage().getText().equalsIgnoreCase("JIZZAX \uD83D\uDFE2")) {
            execute(new SendMessage(chatId.toString(), weatherService.getWeather("JIZZAX")));
        }
        if (update.getMessage().getText().equalsIgnoreCase("KHORAZM \uD83D\uDFE2")) {
            execute(new SendMessage(chatId.toString(), weatherService.getWeather("KHORAZM")));
        }
        if (update.getMessage().getText().equalsIgnoreCase("TOSHKENT \uD83D\uDFE2")) {
            execute(new SendMessage(chatId.toString(), weatherService.getWeather("TOSHKENT")));
        }
        if (update.getMessage().getText().equalsIgnoreCase("SIRDARYO \uD83D\uDFE2")) {
            execute(new SendMessage(chatId.toString(), weatherService.getWeather("SIRDARYO")));
        }
    }

    private void update(Update update, int state, Long chatId) throws TelegramApiException {
        switch (state) {
            case 0:
                if (update.getMessage().getText().equalsIgnoreCase("UPDATE \uD83D\uDFE2")) {
                    if (this.repository.existsByChatId(chatId)) {
                        execute(new SendMessage(chatId.toString(), "ENTER NAME"));
                        updateFields(chatId);
                        updateStatus.put(chatId, 1);
                    } else {
                        execute(new SendMessage(chatId.toString(), "BEFORE UPDATE , REGISTER PLEASE"));
                        updateStatus.put(chatId, 0);
                    }
                }
                break;
            case 1:
                if (update.getMessage().getText().equalsIgnoreCase("EXIT ✅")) {
                    updateStatus.put(chatId, 0);
                } else if (repository.existsByChatId(chatId)) {
                    Drivers driver = new Drivers();
                    driver.setDriverName(update.getMessage().getText());
                    this.repository.save(driver);
                    execute(new SendMessage(chatId.toString(), "ENTER OFFICE"));
                    updateStatus.put(chatId, 2);
                }
                break;
            case 2:
                if (update.getMessage().getText().equalsIgnoreCase("EXIT ✅")) {
                    updateStatus.put(chatId, 0);
                } else if (repository.existsByChatId(chatId)) {
                    Optional<Drivers> notice = repository.findByChatId(chatId);
                    Drivers driver = notice.get();
                    driver.setOffice(update.getMessage().getText());
                    this.repository.save(driver);
                    execute(new SendMessage(chatId.toString(), "ENTER DISPATCHER"));
                    updateStatus.put(chatId, 3);
                }
                break;
            case 3:
                if (update.getMessage().getText().equalsIgnoreCase("EXIT ✅")) {
                    updateStatus.put(chatId, 0);
                } else if (repository.existsByChatId(chatId)) {
                    Optional<Drivers> notice = repository.findByChatId(chatId);
                    Drivers driver = notice.get();
                    driver.setDsp(update.getMessage().getText());
                    this.repository.save(driver);
                    statusButton(chatId);
                    updateStatus.put(chatId, 4);
                }
                break;
            case 4:
                if (update.getMessage().getText().equalsIgnoreCase("EXIT ✅")) {
                    updateStatus.put(chatId, 0);
                } else if (isValidDate(update.getMessage().getText())) {
                    if (repository.existsByChatId(chatId)) {
                        Optional<Drivers> notice = repository.findByChatId(chatId);
                        Drivers driver = notice.get();
                        driver.setFromDate(update.getMessage().getText());
                        this.repository.save(driver);

                        execute(new SendMessage(chatId.toString(), "ENTER TO (DD/MM/YYYY)"));
                        updateStatus.put(chatId, 5);
                    }
                } else {
                    execute(new SendMessage(chatId.toString(), "Invalid date format. Please enter the date in DD/MM/YYYY format"));

                }
                break;
            case 5:
                if (update.getMessage().getText().equalsIgnoreCase("EXIT ✅")) {
                    updateStatus.put(chatId, 0);
                } else if (isValidDate(update.getMessage().getText())) {
                    if (repository.existsByChatId(chatId)) {
                        Optional<Drivers> notice = repository.findByChatId(chatId);
                        Drivers driver = notice.get();
                        driver.setToDate(update.getMessage().getText());
                        this.repository.save(driver);

                        execute(new SendMessage(chatId.toString(), "ENTER ADDRESS"));
                        updateStatus.put(chatId, 6);
                    }
                } else {
                    execute(new SendMessage(chatId.toString(), "Invalid date format. Please enter the date in DD/MM/YYYY format"));

                }
                break;
            case 6:
                if (update.getMessage().getText().equalsIgnoreCase("EXIT ✅")) {
                    updateStatus.put(chatId, 0);
                } else if (repository.existsByChatId(chatId)) {
                    Optional<Drivers> notice = repository.findByChatId(chatId);
                    if (notice.isPresent()) {
                        Drivers driver = notice.get();
                        driver.setAddress(update.getMessage().getText());
                        repository.save(driver);

                        execute(new SendMessage(chatId.toString(), "ENTER NOTE"));
                        updateStatus.put(chatId, 7);
                    }
                }
                break;
            case 7:
                if (update.getMessage().getText().equalsIgnoreCase("EXIT ✅")) {
                    updateStatus.put(chatId, 0);
                } else if (repository.existsByChatId(chatId)) {
                    Optional<Drivers> notice = repository.findByChatId(chatId);
                    if (notice.isPresent()) {
                        Drivers driver = notice.get();
                        driver.setNote(update.getMessage().getText());
                        repository.save(driver);

                        updateButton(chatId);

                        updateStatus.put(chatId, 0);
                    }
                }
                break;
            default:
                break;
        }
    }

    public void submitButton(Long chatId) {

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

    public void statusButton(Long chatId) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();

        button.setText(" \uD83D\uDFE2 HOME");
        button.setCallbackData("HOME");

        button2.setText(" \uD83D\uDFE2 REST");
        button2.setCallbackData("REST");

        button3.setText(" \uD83D\uDFE2 VOCATION");
        button3.setCallbackData("VOCATION");

        rowInline.add(button);
        rowInline.add(button2);
        rowInline.add(button3);

        rowsInline.add(rowInline);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        SendMessage message = new SendMessage();

        message.setChatId(chatId.toString());
        message.setText("SELECT STATUS");
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void updateButton(Long chatId) {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(" \uD83D\uDFE2 UPDATE");
        button.setCallbackData("update");
        rowInline.add(button);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please update your registration:");
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void replayButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("CLICK YOU WANT");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("REGISTER \uD83D\uDFE2");
        row1.add("UPDATE \uD83D\uDFE2");
        row1.add("MOVIE \uD83D\uDFE2");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("MUSIC \uD83D\uDFE2");
        row2.add("CARTOON \uD83D\uDFE2");
        row2.add("NEWS \uD83D\uDFE2");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("WEATHER \uD83D\uDFE2");
        row3.add("SPORTS \uD83D\uDFE2");
        row3.add("CURRENCY RATE \uD83D\uDFE2");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("TEST \uD83D\uDFE2");
        row4.add("GIFT \uD83D\uDFE2");
        row4.add("GAME \uD83D\uDFE2");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chooseGIFT(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("✅CHOOSE YOUR GIF✅");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("DANCING \uD83D\uDFE2");
        row1.add("EXIT ✅");

        keyboard.add(row1);

        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void weatherButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("✅CLICK YOUR REGION✅");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("ANDIJON \uD83D\uDFE2");
        row1.add("NAMANGAN \uD83D\uDFE2");
        row1.add("FERGANA \uD83D\uDFE2");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("BUXORO \uD83D\uDFE2");
        row2.add("NAVOI \uD83D\uDFE2");
        row2.add("SAMARQAND \uD83D\uDFE2");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("QASHQADARYO \uD83D\uDFE2");
        row3.add("JIZZAX \uD83D\uDFE2");
        row3.add("KHORAZM \uD83D\uDFE2");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("TOSHKENT \uD83D\uDFE2");
        row4.add("SIRDARYO \uD83D\uDFE2");
        row4.add("EXIT ✅");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void currencyButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("✅CLICK YOUR CURRENCY✅");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("UZS \uD83D\uDFE2");
        row1.add("USD \uD83D\uDFE2");
        row1.add("RUB \uD83D\uDFE2");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("EXIT ✅");

        keyboard.add(row1);
        keyboard.add(row2);

        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void newsButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("✅CLICK YOUR INTEREST✅");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("BIRTHDAY \uD83D\uDFE2");
        row1.add("BURJ \uD83D\uDFE2");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("EXIT ✅");

        keyboard.add(row1);
        keyboard.add(row2);

        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void gameButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("✅CLICK GAME YOU WANT✅");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("FIND NUMBER \uD83D\uDFE2");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("EXIT ✅");

        keyboard.add(row1);
        keyboard.add(row2);

        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void birthdayButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("✅CLICK YOUR BIRTHDAY✅");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("1 \uD83D\uDFE2");
        row1.add("2 \uD83D\uDFE2");
        row1.add("3 \uD83D\uDFE2");
        row1.add("4 \uD83D\uDFE2");
        row1.add("5 \uD83D\uDFE2");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("6 \uD83D\uDFE2");
        row2.add("7 \uD83D\uDFE2");
        row2.add("8 \uD83D\uDFE2");
        row2.add("9 \uD83D\uDFE2");
        row2.add("10 \uD83D\uDFE2");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("11 \uD83D\uDFE2");
        row3.add("12 \uD83D\uDFE2");
        row3.add("13 \uD83D\uDFE2");
        row3.add("14 \uD83D\uDFE2");
        row3.add("15 \uD83D\uDFE2");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("16 \uD83D\uDFE2");
        row4.add("17 \uD83D\uDFE2");
        row4.add("18 \uD83D\uDFE2");
        row4.add("19 \uD83D\uDFE2");
        row4.add("20 \uD83D\uDFE2");

        KeyboardRow row5 = new KeyboardRow();
        row5.add("21 \uD83D\uDFE2");
        row5.add("22 \uD83D\uDFE2");
        row5.add("23 \uD83D\uDFE2");
        row5.add("24 \uD83D\uDFE2");
        row5.add("25 \uD83D\uDFE2");

        KeyboardRow row6 = new KeyboardRow();
        row6.add("26 \uD83D\uDFE2");
        row6.add("27 \uD83D\uDFE2");
        row6.add("28 \uD83D\uDFE2");
        row6.add("29 \uD83D\uDFE2");
        row6.add("30 \uD83D\uDFE2");


        KeyboardRow row7 = new KeyboardRow();
        row7.add("EXIT ✅");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboard.add(row5);
        keyboard.add(row6);
        keyboard.add(row7);

        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPrivateMessageWithButton(String text, String targetUserId) {
        SendMessage message = new SendMessage();
        message.setChatId(targetUserId); // SIZ HABAR JONATMOQCHI BOLGAN USERNING ID SI
        message.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("TEST");
        button1.setCallbackData("TEST");

        row.add(button1);
        rows.add(row);
        markup.setKeyboard(rows);

        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void updateFields(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("✅CLICK YOUR UPDATE FIELD✅");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("DRIVER NAME \uD83D\uDFE2");
        row1.add("OFFICE \uD83D\uDFE2");
        row1.add("DSP \uD83D\uDFE2");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("STATUS \uD83D\uDFE2");
        row2.add("FROM DATE \uD83D\uDFE2");
        row2.add("TO DATE \uD83D\uDFE2");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("ADDRESS \uD83D\uDFE2");
        row3.add("NOTICE \uD83D\uDFE2");
        row3.add("EXIT ✅");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void movieButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("✅CLICK YOUR MOVIE✅");


        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("IP_MAN 1 \uD83D\uDFE2");
        button1.setUrl("https://youtu.be/y3ySZkSgWik");

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("IP_MAN 2 \uD83D\uDFE2");
        button2.setUrl("https://youtu.be/y3ySZkSgWik");

        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("IP_MAN 3 \uD83D\uDFE2");
        button3.setUrl("https://youtu.be/y3ySZkSgWik");

        rowInline.add(button1);
        rowInline.add(button2);
        rowInline.add(button3);


        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("BOYKA 1 \uD83D\uDFE2");
        button4.setUrl("https://youtu.be/y3ySZkSgWik");

        InlineKeyboardButton button5 = new InlineKeyboardButton();
        button5.setText("BOYKA 2 \uD83D\uDFE2");
        button5.setUrl("https://youtu.be/y3ySZkSgWik");

        InlineKeyboardButton button6 = new InlineKeyboardButton();
        button6.setText("BOYKA 3 \uD83D\uDFE2");
        button6.setUrl("https://youtu.be/y3ySZkSgWik");

        rowInline1.add(button4);
        rowInline1.add(button5);
        rowInline1.add(button6);

        InlineKeyboardButton button7 = new InlineKeyboardButton();
        button7.setText("JAVA BACKEND GROUP \uD83D\uDFE2");
        button7.setUrl("https://t.me/java_community_chat");

        rowInline2.add(button7);

        rowsInline.add(rowInline);
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        inlineKeyboardMarkup.setKeyboard(rowsInline);

        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCurrencyRates(String chatId, String currencies) {
        String response = restTemplate.getForObject("https://v6.exchangerate-api.com/v6/2bbdcd113af69bfe73ebadc7/latest/" + currencies, String.class);
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            String baseCode = jsonNode.get("base_code").asText();
            JsonNode conversionRates = jsonNode.get("conversion_rates");

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Base Code: ").append(baseCode).append("\n\n");
            messageBuilder.append("Currency Rates:\n");


            conversionRates.fieldNames().forEachRemaining(currency -> {
                double rate = conversionRates.get(currency).asDouble();
                messageBuilder.append(currency).append(": ").append(rate).append("\n");
            });
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageBuilder.toString());

            execute(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToChannel(String text) {
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

    public void sendVoice(String chatId, InputStream voiceStream, String fileName) {
        SendVoice sendVoice = new SendVoice();
        sendVoice.setChatId(chatId);
        sendVoice.setVoice(new InputFile(voiceStream, fileName)); // InputStream and file name
        try {
            execute(sendVoice);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotToken();
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            sendMessageToChannel(message.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
