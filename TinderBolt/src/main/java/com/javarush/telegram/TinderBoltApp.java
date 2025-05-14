package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "projectTinder_danik_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "8188216879:AAG_hpYkDEUsR4b8ke77iWnR0AqaMz8_M3U"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "gpt:AUFMFhaIxsLXr0B-4fw5Ik4FWbxdKoFXzX3lK0-eGXhH5LQrJfGAQzs5F44VgDQ4LEuCL6c6A4JFkblB3TJujlqox1oLzy0gB77UvCxpwpxiYZ5mnUTAaIold1B09jsDr0EPRetAXxuWnkyDagxA9OMCJZ__"; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();
    private UserInfo me;
    private UserInfo she;
    private int questionCount;


    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        String message = getMessageText();

        if (message.equals("/start")) { // если пользователь отправил команду /start, то отправляем ему приветственное сообщение и кнопки
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main"); // отправляем фото
            String text = loadMessage("main"); // загружаем текст из файла main.txt
            sendTextMessage(text); // отправляем текст


            showMainMenu("главное меню бота", "/start",
                    "генерация Tinder-профля \uD83D\uDE0E", "/profile",
                    "сообщение для знакомства \uD83E\uDD70", "/opener",
                    "переписка от вашего имени \uD83D\uDE08", "/message",
                    "переписка со звездами \uD83D\uDD25", "/date",
                    "задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
            return; // выходим из метода
        }
        //GPT
        if (message.equals("/gpt")) { // если пользователь отправил команду /gpt, то отправляем ему приветственное сообщение и кнопки
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt"); // отправляем фото
            String text = loadMessage("gpt"); // загружаем текст из файла gpt.txt
            sendTextMessage(text); // отправляем текст
            return; // выходим из метода
        }

        if (currentMode == DialogMode.GPT && !isMessageCommand()) { // если пользователь отправил команду /gpt, то отправляем ему приветственное сообщение и кнопки если это не команда /...
            String prompt = loadPrompt("gpt"); // загружаем текст из файла gpt.txt

            Message msg = sendTextMessage("Подождите пару секунд - ChatGPT собирает ответ\uD83D\uDD0D"); // отправляем сообщение

            String answer = chatGPT.sendMessage(prompt, message); // отправляем сообщение ChatGPT'у
            updateTextMessage(msg, answer); // отправляем ответ ChatGPT'у
            return; // выходим из метода
        }
        //DATE
        if (message.equals("/date")) { // если пользователь отправил команду /date, то отправляем ему приветственное сообщение и кнопки
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date"); // отправляем фото
            String text = loadMessage("date"); // загружаем текст из файла date.txt
            sendTextButtonsMessage(text,
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендая", "date_zendaya",
                    "Райн Гослинг", "date_gosling",
                    "Том Харди", "date_hardy");
            return; // выходим из метода
        }

        if (currentMode == DialogMode.DATE && !isMessageCommand()) { // если пользователь отправил команду /date, то отправляем ему приветственное сообщение и кнопки
            String query = getCallbackQueryButtonKey(); // получаем данные из кнопки
            if (query.startsWith("date_")) { // если пользователь нажал на кнопку с девушкой
                sendPhotoMessage(query); // отправляем фото
                sendTextMessage("Отлично! Вы выбрали девушку\uD83E\uDD29. \nТеперь ваша задача - Пригласить её на свидание за 5 сообщений\uD83D\uDC69\u200D❤\uFE0F\u200D\uD83D\uDC68");

                String prompt = loadPrompt(query); // загружаем текст из файла query.txt
                chatGPT.setPrompt("Диалог с девушкой"); // устанавливаем новый промпт
                return; // выходим из метода
            }

            Message msg = sendTextMessage("Подождите, девушка набирает текст..."); // отправляем сообщение

            String answer = chatGPT.addMessage(message); // отправляем сообщение ChatGPT'у
            updateTextMessage(msg, answer); // отправляем ответ ChatGPT'у
            return; // выходим из метода
        }
        //Command Message
        if (message.equals("/message")) { // если пользователь отправил команду /message, то отправляем ему приветственное сообщение и кнопки
            currentMode = DialogMode.MESSAGE; // устанавливаем режим работы бота
            sendPhotoMessage("message"); // отправляем фото
            sendTextButtonsMessage("Пришлите в чат вашу переписку",
                    "Следующее сообщение", "message_next",
                    "Пригласить на сивдание", "message_date");
            return; // выходим из метода
        }
        if (currentMode == DialogMode.MESSAGE && !isMessageCommand()) { // если пользователь отправил команду /message, то отправляем ему приветственное сообщение и кнопки
            String query = getCallbackQueryButtonKey(); // получаем данные из кнопки
            if (query.startsWith("message_")) { // если пользователь нажал на кнопку
                String prompt = loadPrompt(query); // загружаем текст из файла query.txt
                String userChatHistory = String.join("\n\n", list); // объединяем все сообщения в одну строку

                Message msg = sendTextMessage("Подождите пару секунд - ChatGPT обрабатывает запрос..."); // отправляем сообщение

                String answer = chatGPT.sendMessage(prompt, userChatHistory); // отправляем сообщение ChatGPT'у
                updateTextMessage(msg, answer); // отправляем ответ ChatGPT'у
                return; // выходим из метода
            }

            list.add(message); // добавляем сообщение в список
            return; // выходим из метода
        }

        // command PROFILE
        if (message.equals("/profile")) { // если пользователь отправил команду /profile, то отправляем ему приветственное сообщение и кнопки
            currentMode = DialogMode.PROFILE; // устанавливаем режим работы бота
            sendPhotoMessage("profile"); // отправляем фото
            me = new UserInfo();
            questionCount = 1; // устанавливаем счетчик вопросов
            sendTextMessage("Cколько вам лет?"); // 1 вопрос
            return;
        }
        if (currentMode == DialogMode.PROFILE && !isMessageCommand()) { // если пользователь отправил команду /profile, то отправляем ему приветственное сообщение и кнопки
            switch (questionCount) {
                case 1:
                    me.age = message; // сохраняем в поле age, ответ на вопрос 1
                    questionCount = 2; // меняем счетчик на 2
                    sendTextMessage("Какая у вас профессия?"); // 2 вопрос
                    return;
                case 2:
                    me.occupation = message; // ответ на вопрос 2
                    questionCount = 3; // меняем счетчик на 3
                    sendTextMessage("Есть у вас хобби?"); // 3 вопрос
                    return;
                case 3:
                    me.hobby = message; // ответ на вопрос 3
                    questionCount = 4; // меняем счетчик на 4
                    sendTextMessage("Что вам НЕ нравится в людях?"); // 4 вопрос
                    return;
                case 4:
                    me.annoys = message; // ответ на вопрос 4
                    questionCount = 5; // меняем счетчик на 5
                    sendTextMessage("Цель вашего знакомства?"); // 5 вопрос
                    return;
                case 5:
                    me.goals = message; // ответ на вопрос 5
                    String aboutMyself = me.toString(); // получаем данные из кнопки
                    String prompt = loadPrompt("profile"); // загружаем текст из файла profile.txt
                    Message msg = sendTextMessage("Подождите пару секунд - ChatGPT\uD83E\uDD16 обрабатывает запрос...\uD83D\uDC40"); // отправляем сообщение
                    String answer = chatGPT.sendMessage(prompt, aboutMyself); // отправляем сообщение ChatGPT'у
                    updateTextMessage(msg, answer);
                    return;
            }
            return; // выходим из метода
        }

        // Command OPENER
        if (message.equals("/opener")) { // если пользователь отправил команду /opener, то отправляем ему приветственное сообщение и кнопки
            currentMode = DialogMode.OPENER; // устанавливаем режим работы бота
            sendPhotoMessage("opener"); // отправляем фото
            she = new UserInfo();
            questionCount = 1; // устанавливаем счетчик вопросов
            sendTextMessage("Имя девушки?"); // 1 вопрос
            return; // выходим из метода
        }
        if (currentMode == DialogMode.OPENER && !isMessageCommand()) { // если пользователь отправил команду /opener, то отправляем ему приветственное сообщение и кнопки
            switch (questionCount) {
                case 1:
                    she.name = message; // сохраняем в поле name, ответ на вопрос 1
                    questionCount = 2; // меняем счетчик на 2
                    sendTextMessage("Сколько ей лет?"); // 2 вопрос
                    return;
                case 2:
                    she.age = message; // сохраняем в поле name, ответ на вопрос 1
                    questionCount = 3; // меняем счетчик на 2
                    sendTextMessage("Есть у нее хобби? И какие?"); // 2 вопрос
                    return;
                case 3:
                    she.hobby = message; // сохраняем в поле name, ответ на вопрос 1
                    questionCount = 4; // меняем счетчик на 2
                    sendTextMessage("Кем она работает?"); // 2 вопрос
                    return;
                case 4:
                    she.occupation = message; // сохраняем в поле name, ответ на вопрос 1
                    questionCount = 5; // меняем счетчик на 2
                    sendTextMessage("Цель знакомства?"); // 2 вопрос
                    return;
                case 5:
                    she.goals = message; // сохраняем в поле name, ответ на вопрос 1
                    String aboutFriend = she.toString(); // получаем данные из кнопки
                    String prompt = loadPrompt("opener"); // загружаем текст из файла opener.txt

                    Message msg = sendTextMessage("Подождите пару секунд - ChatGPT\uD83E\uDD16 обрабатывает запрос...\uD83D\uDC40"); // отправляем сообщение
                    String answer = chatGPT.sendMessage(prompt, aboutFriend); // отправляем сообщение ChatGPT'у
                    updateTextMessage(msg, answer);
                    return; // выходим из метода
            }
            return; // выходим из метода
        }


        sendTextMessage("Привет!"); // отправляем сообщение
        sendTextMessage("Вы написали мне: " + message);

        sendTextButtonsMessage("Выберите режим работы:",
                "Старт", "start",
                "Стоп", "stop");


    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
