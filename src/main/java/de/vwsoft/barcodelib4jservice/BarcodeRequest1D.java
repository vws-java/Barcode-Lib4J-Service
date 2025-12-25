package de.vwsoft.barcodelib4jservice;

import java.awt.Font;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import de.vwsoft.barcodelib4j.image.ImageColorModel;
import de.vwsoft.barcodelib4j.image.ImageFormat;
import de.vwsoft.barcodelib4j.image.ImageTransform;
import de.vwsoft.barcodelib4j.oned.BarcodeType;


public class BarcodeRequest1D extends BarcodeRequest {

  @NotNull(message = "Type is required")
  public final BarcodeType type;

  public final Boolean autoComplete;

  public final Boolean appendOptionalChecksum;

  public final Boolean showOptionalChecksum;

  public final String addon;

  public final Boolean textVisible;

  public final Boolean textOnTop;

  @Min(value = -100, message = "Text offset must be between -100 and 100")
  @Max(value =  100, message = "Text offset must be between -100 and 100")
  public final Float textOffset;

  public final String fontName;

  @Min(value = 0, message = "Font size must be 0 (auto) or greater")
  public final Float fontSize;

  @Min(value = 2, message = "Ratio must be between 2.0 and 3.0")
  @Max(value = 3, message = "Ratio must be between 2.0 and 3.0")
  public final Float ratio;


  //----
  @JsonCreator
  public BarcodeRequest1D(
      // Common parameters (from BarcodeRequest)
      @JsonProperty("content") String content,
      @JsonProperty("width") Double width,
      @JsonProperty("height") Double height,
      @JsonProperty("marginLeft") Double marginLeft,
      @JsonProperty("marginRight") Double marginRight,
      @JsonProperty("marginTop") Double marginTop,
      @JsonProperty("marginBottom") Double marginBottom,
      @JsonProperty("format") ImageFormat format,
      @JsonProperty("formatInlineSVG") Boolean formatInlineSVG,
      @JsonProperty("formatPreviewDpiEPS") Integer formatPreviewDpiEPS,
      @JsonProperty("colorModel") ImageColorModel colorModel,
      @JsonProperty("foreground") List<Integer> foreground,
      @JsonProperty("background") List<Integer> background,
      @JsonProperty("opaque") Boolean opaque,
      @JsonProperty("transform") ImageTransform transform,
      @JsonProperty("dpi") Integer dpi,
      // 1D type-specific parameters
      @JsonProperty("type") BarcodeType type,
      @JsonProperty("autoComplete") Boolean autoComplete,
      @JsonProperty("appendOptionalChecksum") Boolean appendOptionalChecksum,
      @JsonProperty("showOptionalChecksum") Boolean showOptionalChecksum,
      @JsonProperty("addon") String addon,
      @JsonProperty("textVisible") Boolean textVisible,
      @JsonProperty("textOnTop") Boolean textOnTop,
      @JsonProperty("textOffset") Float textOffset,
      @JsonProperty("fontName") String fontName,
      @JsonProperty("fontSize") Float fontSize,
      @JsonProperty("ratio") Float ratio) {
    super(content, width, height, marginLeft, marginRight, marginTop, marginBottom, format,
        formatInlineSVG, formatPreviewDpiEPS, colorModel, foreground, background, opaque, transform,
        dpi);

    this.type = type;
    this.autoComplete = autoComplete != null ? autoComplete : false;
    this.appendOptionalChecksum = appendOptionalChecksum != null ? appendOptionalChecksum : false;
    this.showOptionalChecksum = showOptionalChecksum != null ? showOptionalChecksum : false;
    this.addon = addon;
    this.textVisible = textVisible != null ? textVisible : true;
    this.textOnTop = textOnTop != null ? textOnTop : false;
    this.textOffset = textOffset != null ? textOffset : 0F;
    this.fontName = fontName != null ? fontName : Font.SANS_SERIF;
    this.fontSize = fontSize != null ? fontSize : 0F;
    this.ratio = ratio != null ? ratio : 2.5F;
  }


  //----
  public String getTypeName() {
    return this.type.getTypeNameShort();
  }

}
