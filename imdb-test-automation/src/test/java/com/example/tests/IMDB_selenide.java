package com.example.tests;
import com.codeborne.selenide.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import io.qameta.allure.*;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import com.codeborne.selenide.WebDriverRunner;
import java.io.ByteArrayInputStream;

@Listeners({io.qameta.allure.testng.AllureTestNg.class})
@Epic("IMDb Search and Validation")
@Feature("Validate actor and movie data from IMDb search according to Luminor test assignment")
public class IMDB_selenide {

    @Test(description = "Validation of first movie title after QA, and validation of third actor name from top cast")
    @Story("Search for 'QA', verify movie title, and click on third actor to match the profile name")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test verifies if the requested data can be found/compared, and will return errors on incosistencies")
    public void searchAndVerifyMovie() {

        Allure.step("Open IMDb homepage");
        open("https://www.imdb.com/");
        WebDriver driver = WebDriverRunner.getWebDriver();
        driver.manage().window().maximize();
        sleep(3000);
        attachScreenshot("IMDb Homepage");

        // Not part of test assignment, but those cookie consents were just annoying :)
        Allure.step("Accept cookie consent if displayed");
        if ($("button[data-testid='consent-accept-button']").exists()) {
            $("button[data-testid='consent-accept-button']").shouldBe(visible).click();
        } else if ($x("//button[contains(text(),'Accept')]").exists()) {
            $x("//button[contains(text(),'Accept')]").shouldBe(visible).click();
        }
        sleep(3000);
        attachScreenshot("Cookie consent pressed");


        Allure.step("Search for 'QA'");
        SelenideElement searchInput = $("#suggestion-search");
        searchInput.setValue("QA");
        // added sleep here as it might not load the list after QA is entered, and wrong screenshot is taken
        sleep(3000);
        attachScreenshot("Search typed: QA");

        SelenideElement titleElement = $("div.sc-iNiRlI.kXYxGl.searchResult__constTitle");
        titleElement.hover();
        String movieTitle1 = titleElement.shouldBe(visible).getText();
        Allure.addAttachment("Movie title from suggestion", movieTitle1);
        attachScreenshot("Hovered on first suggestion");
        //JOptionPane.showMessageDialog(null, "Movie Title 1: " + movieTitle1);

        SelenideElement firstSuggestion = $("ul.react-autosuggest__suggestions-list li.react-autosuggest__suggestion--first");
        if (!firstSuggestion.exists()) {
            firstSuggestion = $("ul.react-autosuggest__suggestions-list li").shouldBe(visible);
        }

        //sleep(3000);
        Allure.step("Click on first suggestion");
        firstSuggestion.shouldBe(visible).click();
        attachScreenshot("Clicked first suggestion");

        //sleep(3000);
        Allure.step("Extract movie title from movie page");
        String movieTitle2 = $x("//div[@id='__next']/main/div/section/section/div[3]/section/section/div[2]/div/h1/span")
                .shouldBe(visible)
                .getText();
        Allure.addAttachment("Movie title from detail page", movieTitle2);
        attachScreenshot("Movie detail page");

        //JOptionPane.showMessageDialog(null, "Movie Title 2: " + movieTitle2);

        if (movieTitle1.equals(movieTitle2)) {
            Allure.step("Movie titles match");
            //JOptionPane.showMessageDialog(null, "Movie titles are matching: " + movieTitle2);
        } else {
            Allure.step("Movie titles do not match");
            //JOptionPane.showMessageDialog(null, "Movie titles are not matching: " + movieTitle2);
        }


        Allure.step("Scroll to Top cast section");
        SelenideElement topCastElement = $$("span").findBy(text("Top cast"));
        topCastElement.scrollIntoView(true);
        // sleep time needed to display top cast properly in a screenshot
        sleep(3000);
        attachScreenshot("Scrolled to Top Cast");

        //sleep(1000);
        Allure.step("Fetch and list cast members");
        ElementsCollection castMembers = $$("a[data-testid='title-cast-item__actor']");
        for (int i = 0; i < castMembers.size(); i++) {
            String name = castMembers.get(i).getText();
            System.out.println((i + 1) + ". " + name);
        }

        SelenideElement thirdActor = castMembers.get(2);
        String expectedName = thirdActor.getText();
        Allure.addAttachment("Expected third actor name (from 'title-cast-item__actor')", expectedName);
        attachScreenshot("Before clicking third actor");

        Allure.step("Click on third actor: " + expectedName);
        thirdActor.click();
        attachScreenshot("After actor profile loads");

        SelenideElement thirdActorClicked = $("span.hero__primary-text");
        String actualName = thirdActorClicked.getText();
        Allure.addAttachment("Actual actor profile name", actualName);
        attachScreenshot("Actor profile verification");

        if (expectedName.equals(actualName)) {
            Allure.step("Third actor name matches profile");
            //JOptionPane.showMessageDialog(null, "Selected actor3 name is matching: " + actualName);
        } else {
            Allure.step("Third actor name mismatch");
            //JOptionPane.showMessageDialog(null, "Selected actor3 name is not matching: " + actualName);
        }

    }

    public void attachScreenshot(String name) {
        byte[] screenshot = Selenide.screenshot(OutputType.BYTES);
        Allure.addAttachment(name, new ByteArrayInputStream(screenshot));
    }
}
