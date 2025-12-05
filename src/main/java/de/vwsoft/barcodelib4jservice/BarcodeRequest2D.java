package de.vwsoft.barcodelib4jservice;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import de.vwsoft.barcodelib4j.image.ImageColorModel;
import de.vwsoft.barcodelib4j.image.ImageFormat;
import de.vwsoft.barcodelib4j.image.ImageTransform;
import de.vwsoft.barcodelib4j.twod.DataMatrixShape;
import de.vwsoft.barcodelib4j.twod.QRCodeErrorCorrection;
import de.vwsoft.barcodelib4j.twod.TwoDType;


public class BarcodeRequest2D extends BarcodeRequest {

  @NotNull(message = "Type is required")
  public final TwoDType type;

  public final String charset;

  @Min(value = 0, message = "Quiet zone must be 0 or greater")
  public final Integer quietZone;

  @Min(value = 0,  message = "QR Code version must be between 0 (AUTO) and 40")
  @Max(value = 40, message = "QR Code version must be between 0 (AUTO) and 40")
  public final Integer qrVersion;

  public final QRCodeErrorCorrection qrErrorCorrection;

  @Min(value = 0,  message = "DataMatrix size must be between 0 (AUTO) and 30")
  @Max(value = 30, message = "DataMatrix size must be between 0 (AUTO) and 30")
  public final Integer dmSize;

  public final DataMatrixShape dmShape;

  public final Integer pdf417Cols; // Validated in controller
  public final Integer pdf417Rows; // Validated in controller

  @Min(value = 0, message = "PDF417 error correction must be between 0 and 8")
  @Max(value = 8, message = "PDF417 error correction must be between 0 and 8")
  public final Integer pdf417ErrorCorrection;

  @Min(value = -4, message = "Aztec size must be between -4 and 32, or 0 (AUTO)")
  @Max(value = 32, message = "Aztec size must be between -4 and 32, or 0 (AUTO)")
  public final Integer aztecSize;

  @Min(value = 5,  message = "Aztec error correction must be between 5 and 95")
  @Max(value = 95, message = "Aztec error correction must be between 5 and 95")
  public final Integer aztecErrorCorrection;


  //----
  @JsonCreator
  public BarcodeRequest2D(
      // Common parameters (from BarcodeRequest)
      @JsonProperty("content") String content,
      @JsonProperty("width") Double width,
      @JsonProperty("height") Double height,
      @JsonProperty("format") ImageFormat format,
      @JsonProperty("colorModel") ImageColorModel colorModel,
      @JsonProperty("foreground") List<Integer> foreground,
      @JsonProperty("background") List<Integer> background,
      @JsonProperty("opaque") Boolean opaque,
      @JsonProperty("transform") ImageTransform transform,
      @JsonProperty("dpi") Integer dpi,
      // 2D type-specific parameters
      @JsonProperty("type") TwoDType type,
      @JsonProperty("charset") String charset,
      @JsonProperty("quietZone") Integer quietZone,
      @JsonProperty("qrVersion") Integer qrVersion,
      @JsonProperty("qrErrorCorrection") QRCodeErrorCorrection qrErrorCorrection,
      @JsonProperty("dmSize") Integer dmSize,
      @JsonProperty("dmShape") DataMatrixShape dmShape,
      @JsonProperty("pdf417Cols") Integer pdf417Cols,
      @JsonProperty("pdf417Rows") Integer pdf417Rows,
      @JsonProperty("pdf417ErrorCorrection") Integer pdf417ErrorCorrection,
      @JsonProperty("aztecSize") Integer aztecSize,
      @JsonProperty("aztecErrorCorrection") Integer aztecErrorCorrection) {
    super(content, width, height, format, colorModel, foreground, background, opaque,
        transform, dpi);

    this.type = type;
    this.charset = charset;
    this.quietZone = quietZone != null ? quietZone : 1;
    this.qrVersion = qrVersion != null ? qrVersion : 0;
    this.qrErrorCorrection = qrErrorCorrection != null ?
        qrErrorCorrection : QRCodeErrorCorrection.M;
    this.dmSize = dmSize != null ? dmSize : 0;
    this.dmShape = dmShape != null ? dmShape : DataMatrixShape.AUTO;
    this.pdf417Cols = pdf417Cols != null ? pdf417Cols : 0;
    this.pdf417Rows = pdf417Rows != null ? pdf417Rows : 0;
    this.pdf417ErrorCorrection = pdf417ErrorCorrection != null ? pdf417ErrorCorrection : 2;
    this.aztecSize = aztecSize != null ? aztecSize : 0;
    this.aztecErrorCorrection = aztecErrorCorrection != null ? aztecErrorCorrection : 23;
  }


  //----
  public String getTypeName() {
    return this.type.getTypeName();
  }

}
