package de.vwsoft.barcodelib4jservice;

import java.awt.Font;
import java.awt.Graphics2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import de.vwsoft.barcodelib4j.image.BarExporter;
import de.vwsoft.barcodelib4j.image.CompoundColor;
import de.vwsoft.barcodelib4j.image.ImageColorModel;
import de.vwsoft.barcodelib4j.image.ImageFormat;
import de.vwsoft.barcodelib4j.oned.Barcode;
import de.vwsoft.barcodelib4j.oned.BarcodeException;
import de.vwsoft.barcodelib4j.oned.BarcodeType;
import de.vwsoft.barcodelib4j.oned.GS1Validator;
import de.vwsoft.barcodelib4j.oned.ImplCode128;
import de.vwsoft.barcodelib4j.twod.AztecSize;
import de.vwsoft.barcodelib4j.twod.DataMatrixSize;
import de.vwsoft.barcodelib4j.twod.PDF417ErrorCorrection;
import de.vwsoft.barcodelib4j.twod.PDF417Size;
import de.vwsoft.barcodelib4j.twod.QRCodeVersion;
import de.vwsoft.barcodelib4j.twod.TwoDCode;
import de.vwsoft.barcodelib4j.twod.TwoDSymbol;


// This controller instance is managed as a singleton by Spring. Only one instance exists for the
// entire application lifecycle and it is reused across all HTTP requests. Therefore, any instance
// fields (if present) would be shared across threads and must be thread-safe.
@RestController
public class BarcodeController {
  private final MediaType TEXT_PLAIN_UTF8 = MediaType.parseMediaType("text/plain; charset=UTF-8");

  private final Map<ImageFormat,MediaType> CONTENT_TYPES = Map.of(
    ImageFormat.PDF, MediaType.APPLICATION_PDF,
    ImageFormat.EPS, MediaType.parseMediaType("application/postscript"),
    ImageFormat.SVG, MediaType.parseMediaType("image/svg+xml"),
    ImageFormat.PNG, MediaType.IMAGE_PNG,
    ImageFormat.BMP, MediaType.parseMediaType("image/bmp"),
    ImageFormat.JPG, MediaType.IMAGE_JPEG);


  //----
  public BarcodeController() {
  }


  //----
  @PostMapping("/create1d")
  public ResponseEntity<?> createBarcode1D(@Valid @RequestBody BarcodeRequest1D r,
      @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {

    // Validate and set up common properties (used by both 1D and 2D barcodes)
    BarExporter exporter = new BarExporter(r.width, r.height);
    ResponseEntity<?> errorResponse = setupCommonParams(exporter, r);
    if (errorResponse != null)
      return errorResponse;

    // Create the barcode instance
    final String content = r.type == BarcodeType.EAN128 ?
        preprocessGS1Data(r.content, ImplCode128.FNC1) : r.content;
    Barcode barcode;
    try {
      barcode = Barcode.newInstance(r.type, content, r.autoComplete, r.appendOptionalChecksum);
      barcode.setAddOn(r.addon);
    } catch (BarcodeException e) {
      return ResponseEntity.unprocessableEntity().contentType(TEXT_PLAIN_UTF8) // HTTP 422
          .body(language.startsWith("de") ? e.getLocalizedMessage() : e.getMessage());
    }
    barcode.setTextVisible(r.textVisible);
    barcode.setTextOnTop(r.textOnTop);
    barcode.setTextOffset(r.textOffset);
    barcode.setFont(resolveFont(r));
    barcode.setFontSizeAdjusted(r.fontSize == 0F);

    // Draw the barcode
    Graphics2D g2d = exporter.getGraphics2D();
    barcode.draw(g2d, 0.0, 0.0, r.width, r.height, r.dpi > 0 ? 25.4 / r.dpi : 0.0, 0.0, 0.0);
    g2d.dispose();

    // Encode the barcode into the output format and return
    return createFileResponse(exporter, r);
  }


  //----
  @PostMapping("/create2d")
  public ResponseEntity<?> createBarcode2D(@Valid @RequestBody BarcodeRequest2D r,
      @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {

    // Validate and set up common properties (used by both 1D and 2D barcodes)
    BarExporter exporter = new BarExporter(r.width, r.height);
    ResponseEntity<?> errorResponse = setupCommonParams(exporter, r);
    if (errorResponse != null)
      return errorResponse;

    // Validate PDF417 dimensions (too complex for annotation validation)
    if (r.pdf417Cols != 0 && (r.pdf417Cols < 1 || r.pdf417Cols > 30))
      return ResponseEntity.badRequest().contentType(TEXT_PLAIN_UTF8) // HTTP 400
          .body("PDF417 columns must be 0 (AUTO) or between 1 and 30");
    if (r.pdf417Rows != 0 && (r.pdf417Rows < 3 || r.pdf417Rows > 90))
      return ResponseEntity.badRequest().contentType(TEXT_PLAIN_UTF8) // HTTP 400
          .body("PDF417 rows must be 0 (AUTO) or between 3 and 90");

    // Localize error messages of type HTTP 422 (invalid user input)
    final boolean useGerman = language.startsWith("de");
    final String invalidText = useGerman ? "ung\u00FCltig" : "invalid";

    // Validate 'content' and 'charset'
    String errMsg = null; // Let's see if there is an error to collect within the two if-blocks
    String content = r.content;
    Charset charset = null;
    if (r.type.isGS1() || content.isEmpty()) {        // GS1 validation. Also validate empty content
      content = preprocessGS1Data(content, (char)29); // to get localized error message "for free"
      try {                                           // from the 'BarcodeException' class
        content = new GS1Validator(content, (char)29).getContent();
      } catch (BarcodeException e) {
        errMsg = useGerman ? e.getLocalizedMessage() : e.getMessage();
      }
    } else if (r.charset != null) { // ECI will be inserted into the symbol
      try {
        charset = Charset.forName(r.charset);
      } catch (Exception e) {
        errMsg = invalidText;
      }
    }

    // Set up 2D Code and build the symbol
    TwoDSymbol symbol = null;
    if (errMsg == null) {
      TwoDCode twoDCode = new TwoDCode(r.type);
      twoDCode.setContent(content);
      twoDCode.setCharset(charset);
      if (twoDCode.canEncode()) { // This should be called AFTER setting content and charset
        twoDCode.setQuietZone(r.quietZone);
        twoDCode.setQRCodeVersion(QRCodeVersion.valueOf(r.qrVersion));
        twoDCode.setQRCodeErrCorr(r.qrErrorCorrection);
        twoDCode.setDataMatrixSize(DataMatrixSize.valueOf(r.dmSize));
        twoDCode.setDataMatrixShape(r.dmShape);
        twoDCode.setPDF417Size(new PDF417Size(r.pdf417Cols, r.pdf417Rows));
        twoDCode.setPDF417ErrCorr(PDF417ErrorCorrection.valueOf(r.pdf417ErrorCorrection));
        twoDCode.setAztecSize(AztecSize.valueOf(r.aztecSize));
        twoDCode.setAztecErrCorr(r.aztecErrorCorrection);
        try {
          symbol = twoDCode.buildSymbol();
        } catch (Exception ex) {
          errMsg = invalidText;
        }
      } else {
        errMsg = invalidText;
      }
    }

    if (errMsg != null) // HTTP 422
      return ResponseEntity.unprocessableEntity().contentType(TEXT_PLAIN_UTF8).body(errMsg);

    // Draw the symbol
    Graphics2D g2d = exporter.getGraphics2D();
    symbol.draw(g2d, 0.0, 0.0, r.width, r.height, r.dpi > 0 ? 25.4 / r.dpi : 0.0);
    g2d.dispose();

    // Encode the symbol into the output format and return
    return createFileResponse(exporter, r);
  }


  //---- Validates parameters that apply to both 1D and 2D code types.
  //     Returns 'null' when validation succeeds, otherwise returns an error response.
  private ResponseEntity<?> setupCommonParams(BarExporter exporter, BarcodeRequest r) {
    if (r.format.isRasterFormat() && (r.dpi < 72 || r.dpi > 2400))
      return ResponseEntity.badRequest().contentType(TEXT_PLAIN_UTF8) // HTTP 400
        .body("Raster formats require DPI between 72 and 2400");

    String errMsgPrefix = "Foreground: ";
    try {
      exporter.setForeground(toCompoundColor(r.foreground, r.colorModel));
      errMsgPrefix = "Background: ";
      exporter.setBackground(toCompoundColor(r.background, r.colorModel));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().contentType(TEXT_PLAIN_UTF8) // HTTP 400
          .body(errMsgPrefix + e.getMessage());
    }

    exporter.setTitle(r.getTypeName());
    exporter.setCreator("Barcode-Lib4J Service");
    exporter.setOpaque(r.opaque);
    exporter.setTransform(r.transform);

    return null;
  }


  //----
  private CompoundColor toCompoundColor(List<Integer> c, ImageColorModel colorModel)
      throws IllegalArgumentException { // ... throws it on 4 lines!
    if (colorModel == ImageColorModel.RGB) {
      if (c.size() != 3)
        throw new IllegalArgumentException("RGB colors must have exactly 3 values [R,G,B]");
      return new CompoundColor(c.get(0), c.get(1), c.get(2)); // Out of range > throws IAE
    } else {
      if (c.size() != 4)
        throw new IllegalArgumentException("CMYK colors must have exactly 4 values [C,M,Y,K]");
      return new CompoundColor(c.get(0), c.get(1), c.get(2), c.get(3)); // Out of range > throws IAE
    }
  }


  //----
  private Font resolveFont(BarcodeRequest1D r) {
    Font font = B4JServiceApplication.additionalFonts.get(r.fontName);
    return (font != null ? font : new Font(r.fontName, Font.PLAIN, 0)).deriveFont(r.fontSize);
  }


  //----
  private final Pattern AI_ONLY_PATTERN = Pattern.compile("^\\(\\d{2,4}\\)$");
  private String preprocessGS1Data(String content, char fnc1) {
    String[] lines = content.split("\n");
    StringBuilder sb = new StringBuilder(content.length() + lines.length);
    for (int i=0; i<lines.length; i++) {
      String s = lines[i].trim();
      if (!AI_ONLY_PATTERN.matcher(s).matches()) {
        sb.append(s);
        if (i != lines.length - 1 && s.charAt(s.length() - 1) != fnc1)
          sb.append(fnc1);
      }
    }
    return sb.toString();
  }


  //----
  private ResponseEntity<?> createFileResponse(BarExporter exporter, BarcodeRequest r) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      exporter.write(baos, r.format, r.colorModel, r.dpi, r.dpi);
    } catch (IOException e) { /* Should never occur - parameters are validated beforehand */ }

    String fileName = r.getTypeName().replace(' ', '-') + '.' + r.format.name().toLowerCase();
    return ResponseEntity.ok().contentType(CONTENT_TYPES.get(r.format)) // HTTP 200
        .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
        .body(baos.toByteArray());
  }

}
