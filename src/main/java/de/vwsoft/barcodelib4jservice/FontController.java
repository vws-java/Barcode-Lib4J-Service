package de.vwsoft.barcodelib4jservice;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


// This controller instance is managed as a singleton by Spring. Only one instance exists for the
// entire application lifecycle and it is reused across all HTTP requests. Therefore, any instance
// fields (if present) would be shared across threads and must be thread-safe.
@RestController
public class FontController {
  private final List<String> AVAILABLE_FONT_NAMES;


  //----
  public FontController() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

    // Collect system fonts and filter out 1) mapped and 2) unusable ones (symbols, emojis, etc.)
    Stream<String> systemFonts = Arrays.stream(ge.getAvailableFontFamilyNames())
        .filter(fontName -> {
          Font font = new Font(fontName, Font.PLAIN, 0);
          if (!font.canDisplay('A') || !font.canDisplay('9')) // 9 = a random digit
            return false;
          String lower = fontName.toLowerCase();
          return !lower.contains("symbol") && !lower.contains("wingding") &&
                 !lower.contains("webding") && !lower.contains("dingbat") &&
                 !lower.contains("emoji") && !lower.contains("marlett") &&
                 !lower.contains("mt extra") &&
                 !lower.equals("dialog") &&
                 !lower.equals("dialoginput") &&
                 !lower.equals("monospaced") &&
                 !lower.equals("serif") &&
                 !lower.equals("sansserif");
        });

    // Add additional fonts loaded at application startup (e.g., OCR-B from TTF files).
    // Then combine, remove duplicates, and sort
    AVAILABLE_FONT_NAMES = Stream.concat(systemFonts,
            B4JServiceApplication.additionalFonts.keySet().stream()).distinct().sorted().toList();
  }


  //----
  @GetMapping("/fonts")
  public ResponseEntity<List<String>> getAvailableFonts() {
    return ResponseEntity.ok(AVAILABLE_FONT_NAMES);
  }

}
