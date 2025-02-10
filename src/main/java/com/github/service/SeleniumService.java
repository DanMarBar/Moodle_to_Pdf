package com.github.service;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Set;

public class SeleniumService {

    private static SeleniumService seleniumService;

    private final WebDriver webDriver;
    private final Path savedHtmlFilePath = Paths.get("src/main/resources/temporal/html");
    private final Path savedCookieFilePath = Paths.get("src/main/resources/temporal/cookie");
    private final String filenameFormat = "%s.html";
    private final String cookiesFilename = "cookies.txt";
    private final String email = PropertiesService.getInstance().getProperty("email");
    private final String mainPageUrl = PropertiesService.getInstance().getProperty("main.page.url");

    private SeleniumService() {
        this.webDriver = new ChromeDriver();
    }

    public static SeleniumService getInstance() {
        if (seleniumService == null) {
            seleniumService = new SeleniumService();
        }
        return seleniumService;
    }

    public WebDriver setupSeleniumDriver() {
        System.setProperty("webdriver.chrome.driver",
                "C:\\Users\\G513\\Downloads\\chromedriver-win64");
        webDriver.manage().window().maximize();
        webDriver.get(mainPageUrl);
        return webDriver;
    }

    // Método de login
    public void authenticateWithOauth() {
        final WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

        final WebElement googleLoginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href, 'auth/oauth2/login.php')]")));
        googleLoginButton.click();

        final WebElement emailInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("identifierId")));
        emailInput.sendKeys(email);

        final WebElement nextButton = webDriver.findElement(By.xpath("//span[text()='Siguiente']"));
        nextButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("exe-web-site")));
    }

    public boolean isNoMoreNextButton() {
        final List<WebElement> nextButtons = webDriver.findElements(By.className("exe-web-site"));
        return !nextButtons.isEmpty();
    }

    public void goToNextPage() {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(4));
        WebElement nextButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.next span")));
        final JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript("arguments[0].click();", nextButton);
    }

    public void copyHtmlOnRemoteFile() {
        final byte[] pageSource = ((String) ((JavascriptExecutor) webDriver)
                .executeScript("return new XMLSerializer().serializeToString(document);"))
                .getBytes(StandardCharsets.UTF_8);
        final String filename = String.format(filenameFormat, webDriver.getTitle());
        final Path filePath = savedHtmlFilePath.resolve(Paths.get(filename));

        try {
            Files.write(filePath, pageSource);

        } catch (IOException e) {
            throw new RuntimeException("No se ha podido guardar el html en la ruta: " + filePath,
                    e);
        } finally {
            filePath.toFile().deleteOnExit();
        }
    }

    public String saveCookiesOnFile() {
        final Set<Cookie> cookies = webDriver.manage().getCookies();
        final String filePath = savedCookieFilePath.resolve(Paths.get(cookiesFilename)).toString();

        try (final FileWriter writer = new FileWriter(filePath)) {
            for (final Cookie cookie : cookies) {
                writer.write(String.format(
                        "%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
                        cookie.getDomain(),
                        "TRUE",
                        cookie.getPath(),
                        cookie.isSecure() ? "TRUE" : "FALSE",
                        cookie.getExpiry() != null ? cookie.getExpiry().getTime() / 1000 : "0",
                        cookie.getName(),
                        cookie.getValue()
                ));
            }
            System.out.println("Cookies guardadas en: " + filePath);
            return filePath;

        } catch (IOException e) {
            throw new RuntimeException(
                    "No se han podido guardar las cookies en la ruta: " + filePath, e);
        }
    }
}
