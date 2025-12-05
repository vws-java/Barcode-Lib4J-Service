package de.vwsoft.barcodelib4jservice;

import java.awt.Font;
import java.util.HashMap;
import java.util.Locale;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;


@SpringBootApplication
public class B4JServiceApplication {
  public static HashMap<String,Font> additionalFonts = new HashMap<>();


  //----
  @PostConstruct
  public void postConstruct() {

    // Sets the default locale to German to enable bilingual error messages from Barcode-Lib4J:
    //   - BarcodeException#getMessage() returns English
    //   - BarcodeException#getLocalizedMessage() returns German
    Locale.setDefault(Locale.GERMAN);

    // Load additional fonts. Since the application runs in headless mode, fonts cannot be
    // registered globally via GraphicsEnvironment.registerFont(). Instead, we load them into
    // a static map and make them available to controllers that need them.
    final String[] fontFileNames = { "OCR_B.ttf" };
    for (String fileName : fontFileNames) {
      try (var is = new ClassPathResource(fileName).getInputStream()) {
        if (is != null) {
          Font font = Font.createFont(Font.TRUETYPE_FONT, is);
          additionalFonts.put(font.getFamily(), font);
        } else {
          System.err.println("Font file not found: " + fileName);
        }
      } catch (Exception e) {
        System.err.println("Failed to load font: " + fileName);
      }
    }

  }


  //----
  public static void main(String[] args) {
    SpringApplication.run(B4JServiceApplication.class, args);
  }

}
