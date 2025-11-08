package com.yatra.automation;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class YatraAutomationScript {

    public static void main(String[] args) {

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--disable-notifications");

        // Step 1: Launch the browser
        WebDriver wd = new ChromeDriver(chromeOptions);
        WebDriverWait wait = new WebDriverWait(wd, Duration.ofSeconds(15));

        try {
            // Step 2: Load the page
            wd.get("https://www.yatra.com");
            wd.manage().window().maximize();

            // ✅ Step 3: Close login popup if present
            try {
                By closeButtonLocator = By.xpath("(//span[contains(@class, 'style_cross')]/img)[1]");
                WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(closeButtonLocator));
                closeButton.click();
                System.out.println("✅ Closed login popup.");
            } catch (Exception e) {
                System.out.println("⚠️ No login popup appeared.");
            }

            // ✅ Step 4: Open calendar
            By departureDateButtonLocator = By.xpath("//div[@aria-label='Departure Date inputbox' and @role='button']");
            WebElement departureDateButton = wait.until(ExpectedConditions.elementToBeClickable(departureDateButtonLocator));
            departureDateButton.click();
            System.out.println("✅ Calendar opened.");

            // Wait for calendar to load
            Thread.sleep(1000);

            // ✅ Step 5: Find all date elements with prices
            List<WebElement> dateElements = wd.findElements(By.xpath("//div[contains(@class,'DayPicker-Day') and not(contains(@class,'DayPicker-Day--disabled'))]"));

            int lowestPrice = Integer.MAX_VALUE;
            WebElement lowestPriceDateElement = null;

            for (WebElement dateEl : dateElements) {
                try {
                    List<WebElement> priceElements = dateEl.findElements(By.xpath(".//p[contains(text(),'₹')]"));

                    if (!priceElements.isEmpty()) {
                        String priceText = priceElements.get(0).getText().replace("₹", "").replace(",", "").trim();

                        if (!priceText.isEmpty()) {
                            int price = Integer.parseInt(priceText);

                            if (price < lowestPrice) {
                                lowestPrice = price;
                                lowestPriceDateElement = dateEl;
                            }
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            if (lowestPriceDateElement != null) {
                System.out.println("💰 Lowest price found: ₹" + lowestPrice);

                ((JavascriptExecutor) wd).executeScript("arguments[0].scrollIntoView({block: 'center'});", lowestPriceDateElement);
                Thread.sleep(500);

                try {
                    wait.until(ExpectedConditions.elementToBeClickable(lowestPriceDateElement));
                    lowestPriceDateElement.click();
                    System.out.println("✅ Selected lowest-price date.");
                } catch (Exception e) {
                    try {
                        ((JavascriptExecutor) wd).executeScript("arguments[0].click();", lowestPriceDateElement);
                        System.out.println("✅ Selected lowest-price date (using JS click).");
                    } catch (Exception jsException) {
                        try {
                            WebElement clickableChild = lowestPriceDateElement.findElement(By.xpath(".//*[contains(@class,'date') or contains(@class,'day')]"));
                            clickableChild.click();
                            System.out.println("✅ Selected lowest-price date (via child element).");
                        } catch (Exception childException) {
                            System.out.println("❌ Could not click on lowest price date: " + childException.getMessage());
                        }
                    }
                }
            } else {
                System.out.println("⚠️ No prices found in calendar.");
            }

            Thread.sleep(1000);

            // ✅ Step 6: Click on Search button (with JS fallback)
            try {
                By searchButtonLocator = By.xpath("//button[contains(@id,'BE_flight_flsearch_btn') or contains(text(),'Search')]");
                WebElement searchButton = wait.until(ExpectedConditions.visibilityOfElementLocated(searchButtonLocator));

                ((JavascriptExecutor) wd).executeScript("arguments[0].scrollIntoView({block: 'center'});", searchButton);
                Thread.sleep(500);

                wait.until(ExpectedConditions.elementToBeClickable(searchButton));

                try {
                    searchButton.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) wd).executeScript("arguments[0].click();", searchButton);
                }

                System.out.println("✅ Search button clicked successfully.");
            } catch (Exception e) {
                System.out.println("❌ Could not click Search button: " + e.getMessage());
            }

            Thread.sleep(5000);

            // ✅ Step 7: Click on "Book Now" button
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(text(),'Book Now')]")));

                WebElement bookNowButton = null;

                try {
                    bookNowButton = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(@class,'bookNow-mobile') and contains(text(),'Book Now')]")));
                    System.out.println("✅ Found Book Now button using class and text.");
                } catch (Exception e1) {
                    try {
                        bookNowButton = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[@autom='select' and contains(text(),'Book Now')]")));
                        System.out.println("✅ Found Book Now button using autom attribute.");
                    } catch (Exception e2) {
                        try {
                            bookNowButton = wait.until(ExpectedConditions.elementToBeClickable(
                                    By.xpath("//button[contains(text(),'Book Now')]")));
                            System.out.println("✅ Found Book Now button using text only.");
                        } catch (Exception e3) {
                            // Strategy 4: By select-button class
                            try {
                                By bookNowButtonLocator = By.xpath("(//div[contains(@class, 'booking-section')]//button[contains(text(), 'Book Now')])[1]");
                                WebElement bookNowButton1 = wait.until(ExpectedConditions.elementToBeClickable(bookNowButtonLocator));
                                bookNowButton = bookNowButton1;
                                System.out.println("✅ Found Book Now button using select-button class.");
                            } catch (Exception e4) {
                                throw new Exception("Could not find Book Now button with any strategy.");
                            }
                        }
                    }
                }

                if (bookNowButton != null) {
                    ((JavascriptExecutor) wd).executeScript("arguments[0].scrollIntoView({block: 'center'});", bookNowButton);
                    Thread.sleep(1000);
                    wait.until(ExpectedConditions.elementToBeClickable(bookNowButton));

                    try {
                        bookNowButton.click();
                        System.out.println("✅ Book Now button clicked successfully.");
                    } catch (Exception e) {
                        ((JavascriptExecutor) wd).executeScript("arguments[0].click();", bookNowButton);
                        System.out.println("✅ Book Now button clicked successfully (using JS click).");
                    }

                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                System.out.println("❌ Could not click Book Now button: " + e.getMessage());
                e.printStackTrace();
            }

            Thread.sleep(2000);

            // ✅ Step 8: Open a new browser tab, switch to it, and navigate to Google
            try {
                String originalWindow = wd.getWindowHandle();
                System.out.println("📌 Original window handle: " + originalWindow);

                ((JavascriptExecutor) wd).executeScript("window.open();");
                System.out.println("✅ New tab opened.");

                Set<String> windowHandles = wd.getWindowHandles();
                String newWindow = null;
                for (String handle : windowHandles) {
                    if (!handle.equals(originalWindow)) {
                        newWindow = handle;
                        break;
                    }
                }

                if (newWindow != null) {
                    wd.switchTo().window(newWindow);
                    System.out.println("✅ Switched to new tab.");

                    wd.get("https://www.google.com");
                    System.out.println("✅ Navigated to Google.");

                    Thread.sleep(2000);
                } else {
                    System.out.println("❌ Could not find new window handle.");
                }
            } catch (Exception e) {
                System.out.println("❌ Error opening new tab: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.out.println("❌ Error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // wd.quit(); // Uncomment if you want to close browser automatically
        }
    }
}
