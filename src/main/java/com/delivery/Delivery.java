package com.delivery;

import okhttp3.*;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.util.Properties;

public class Delivery {
    final static String LOGIN_KEY = "login";
    final static String PASSWORD_KEY = "password";
    final static String CHATID_KEY = "chatId";
    final static String BASE_URL_KEY = "baseUrl";
    final static String RESERVATION_URL_KEY = "reservationUrl";
    final static String INTERVAL_BETWEEN_ATTEMPTS_KEY = "interval.between.attempts"; //miliseconds
    final static String DRIVER_PROP_KEY = "driver.prop";
    final static String DRIVER_PATH_KEY = "driver.path";
    final static String PAGE_LOAD_TIMEOUT_KEY = "page.load.timeout"; //seconds

    final static String LOGIN_BLUE_BUTTON_MAIN_PAGE_XPATH = "loginBlueButton.MainPage";
    final static String ENTER_LINK_MAIN_PAGE_POPUP_XPATH = "enterLink.MainPage.Popup";
    final static String LOGIN_FIELD_XPATH = "loginFiled";
    final static String PASSWORD_FIELD_XPATH = "passwordFiled";
    final static String ENTER_ORANGE_BUTTON_ACCOUNT_PAGE_XPATH = "enterOrangeButton.AccountPage";
    final static String SELECT_TIME_RED_BUTTON_RESERVATION_PAGE_XPATH = "selectTimeRedButton.ReservationPage";
    final static String DELIVERY_DROP_INFO_XPATH = "deliveryDopInfo";
    final static String SPAN_COMPONENT_RESERVATION_POPUP_XPATH = "spanComponent.Reservation.Popup";
    final static String UL_COMPONENT_RESERVATION_POPUP_XPATH = "ulComponent.Reservation.Popup";
    final static String CONTINUE_BUTTON_RESERVATION_POPUP_XPATH = "continueButton.Reservation.Popup";
    final static String UPDATE_BUTTON_XPATH = "updateButton";
    final static String POPUP_BUTTON_XPATH = "popupButton";

    private static Properties props;
    private static Properties xpath_props;
    private static WebDriver driver;
    private static WebDriverWait wait;

    public static void main(String[] args) {
        System.setProperty(getProperties(DRIVER_PROP_KEY), getProperties(DRIVER_PATH_KEY));
        driver = new ChromeDriver();
        //open e-dostavka main page
        driver.get(getProperties(BASE_URL_KEY));
        //select blue button "Enter" in the upper right corner of the Main Page
/*        WebElement popup_MainPage = driver.findElement(By.xpath(getXPATHProperties(POPUP_BUTTON_XPATH)));
        popup_MainPage.click();*/

        WebElement loginBlueButton_MainPage = driver.findElement(By.xpath(getXPATHProperties(LOGIN_BLUE_BUTTON_MAIN_PAGE_XPATH)));
        loginBlueButton_MainPage.click();

        //set loading page timeout, because we need to wait until the page loads
        wait = new WebDriverWait(driver, Integer.parseInt(getProperties(PAGE_LOAD_TIMEOUT_KEY)));

        //there is PopUp with notification was opened
        //select green link "Enter" that is located below on the popup was opened
/*        WebElement enterLink_MainPage_PopUp = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath(getXPATHProperties(ENTER_LINK_MAIN_PAGE_POPUP_XPATH))
                )
        );

        enterLink_MainPage_PopUp.click();*/

        authorisation();
        int iterationNumber=0;
        helloRecursion(iterationNumber);
    }

    private static void authorisation() {
        WebElement loginField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath(getXPATHProperties(LOGIN_FIELD_XPATH))
                )
        );
        WebElement passwordField = driver.findElement(By.xpath(getXPATHProperties(PASSWORD_FIELD_XPATH)));

        loginField.sendKeys(getProperties(LOGIN_KEY));
        passwordField.sendKeys(getProperties(PASSWORD_KEY));

        WebElement enterOrangeButton_AccountPage = driver.findElement(
                By.xpath(
                        getXPATHProperties(ENTER_ORANGE_BUTTON_ACCOUNT_PAGE_XPATH)
                )
        );
        enterOrangeButton_AccountPage.click();
    }

    private static void helloRecursion(int iterationNumber){
        try {
            runCycleToSelectDeliveryTime();
        } catch (InterruptedException e) {
            e.printStackTrace();
            helloRecursion(iterationNumber);
        }
    }

    private static void runCycleToSelectDeliveryTime() throws InterruptedException {
        // go to the page "Process order"
        driver.get(getProperties(RESERVATION_URL_KEY));
        int iterationNumber = 0;
        long intervalBetweenAttempts = Long.parseLong(getProperties(INTERVAL_BETWEEN_ATTEMPTS_KEY));
        while (true) {
            System.out.println(System.currentTimeMillis() + ": Iteration " + ++iterationNumber);
            //select red button "Select time" on the right
            WebElement selectTimeRedButton_ReservationPage = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath(getXPATHProperties(SELECT_TIME_RED_BUTTON_RESERVATION_PAGE_XPATH))
                    )
            );
            selectTimeRedButton_ReservationPage.click();

            WebElement deliveryDopInfo = null;
            try {
                //trying to select element with error "there are no available time"
                deliveryDopInfo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(getXPATHProperties(DELIVERY_DROP_INFO_XPATH))));
            } catch (TimeoutException e) {
                System.out.println("Element hadn't find");
            }
            if (deliveryDopInfo == null || !deliveryDopInfo.getText().contains("Доступное время отсутствует")) {
                // if deliveryDopInfo element not found on the page, trying to select a first available time
                selectDeliveryTime();
                //send message on the Telegram about reservation the time-slot
                sendMessage();
                break;
            }
            WebElement continueButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(getXPATHProperties(UPDATE_BUTTON_XPATH))));
            continueButton.click();
            Thread.sleep(intervalBetweenAttempts);
        }
    }

    //select first available time from select
    private static void selectDeliveryTime() {
        // select element with available time
        WebElement spanComponent_Reservation_Popup = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath(getXPATHProperties(SPAN_COMPONENT_RESERVATION_POPUP_XPATH))
                )
        );
        spanComponent_Reservation_Popup.click();

        // select first available time
        WebElement ulComponent_Reservation_Popup = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath(getXPATHProperties(UL_COMPONENT_RESERVATION_POPUP_XPATH))
                )
        );
        System.out.println(ulComponent_Reservation_Popup.getText());
        ulComponent_Reservation_Popup.click();


        //select button "continue"
        WebElement continueButton_Reservation_Popup = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath(getXPATHProperties(CONTINUE_BUTTON_RESERVATION_POPUP_XPATH))
                )
        );
        continueButton_Reservation_Popup.click();
    }

    //read properties from configuration file config.properties
    private static String getProperties(String key) {
        if (props == null) {
            props = new Properties();
            File configFile = new File("src/main/resources/config.properties");
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(configFile);
                props.load(inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return props.getProperty(key);
    }

    //read properties from configuration file xpaths.properties
    private static String getXPATHProperties(String key) {
        if (xpath_props == null) {
            xpath_props = new Properties();
            File configFile = new File("src/main/resources/xpaths.properties");
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(configFile);
                xpath_props.load(inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return xpath_props.getProperty(key);
    }

    //send message on the Telegram about reservation the time-slot
    private static void sendMessage() {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "{\"chat_id\": \"" + getProperties(CHATID_KEY) + "\","
                        + "\"text\": \"Waste Python's money! Maaars' \", "
                        + "\"disable_notification\": false}");
        Request request = new Request.Builder()
                .url("https://api.telegram.org/bot799715236:AAF62XWKHBR71PB_zmu_21XguMA4rDgUIuk/sendMessage")
                .post(body)
                .addHeader("content-type", "application/json")
                .build();

        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
