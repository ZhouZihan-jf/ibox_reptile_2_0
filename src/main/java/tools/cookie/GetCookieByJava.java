package tools.cookie;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import storage.MongoOperation;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GetCookieByJava {
    //���ݻ���
    private static void move(WebDriver driver) throws InterruptedException {
        WebElement slider = null;
        try{
            slider = driver.findElement(By.id("nc_1_n1z"));
        }catch (Exception e){
            System.out.println("δ�ҵ�Ԫ��");
            System.out.println(e.toString());
            return;
        }
        Actions actions = new Actions(driver);
        actions.clickAndHold(slider).perform();
        for(int x = 0; x <= 70; x=x+5){
            actions.moveByOffset(x,0).perform();
        }
        TimeUnit.SECONDS.sleep(1);
        actions.release().perform();
    }

    public static String getCookie() throws InterruptedException {
        //�������
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-headless");
        options.addArguments("--window-size=800,600");
        //��������
        FirefoxDriver firefoxDriver = new FirefoxDriver(options);
        //����
        firefoxDriver.get("https://api-h5.ibox.art/nft-mall-web/v1.2/nft/product/");
        firefoxDriver.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined,})");
        TimeUnit.SECONDS.sleep(3);
        //���ݻ���
        move(firefoxDriver);
        //��û���
        Set<Cookie> cookies = firefoxDriver.manage().getCookies();
        //�ر������
        firefoxDriver.close();

        String cookie = "";
        for (Cookie c : cookies){
            cookie += c.getName()+"="+c.getValue()+";";
        }
        System.out.println(cookie);

        return cookie;
    }
}
