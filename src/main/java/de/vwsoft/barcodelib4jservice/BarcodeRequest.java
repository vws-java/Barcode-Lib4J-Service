package de.vwsoft.barcodelib4jservice;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import de.vwsoft.barcodelib4j.image.ImageColorModel;
import de.vwsoft.barcodelib4j.image.ImageFormat;
import de.vwsoft.barcodelib4j.image.ImageTransform;


public abstract class BarcodeRequest {
  private static final List<Integer>
      BLACK_RGB = List.of(0, 0, 0),        BLACK_CMYK = List.of(0, 0, 0, 100),
      WHITE_RGB = List.of(255, 255, 255),  WHITE_CMYK = List.of(0, 0, 0, 0);

  @NotNull(message = "Content is required")
  public final String content;

  @NotNull(message = "Width is required")
  @Positive(message = "Width must be greater than 0")
  public final Double width;

  @NotNull(message = "Height is required")
  @Positive(message = "Height must be greater than 0")
  public final Double height;

  @Min(value = 0, message = "Margin left must be 0 or greater")
  public final Double marginLeft;

  @Min(value = 0, message = "Margin right must be 0 or greater")
  public final Double marginRight;

  @Min(value = 0, message = "Margin top must be 0 or greater")
  public final Double marginTop;

  @Min(value = 0, message = "Margin bottom must be 0 or greater")
  public final Double marginBottom;

  @NotNull(message = "Format is required")
  public final ImageFormat format;

  public final Boolean formatInlineSVG;

  @Min(value = 0, message = "EPS Preview DPI must be 0 (no preview) or positive")
  public final Integer formatPreviewDpiEPS;

  public final ImageColorModel colorModel;

  public final List<Integer> foreground;

  public final List<Integer> background;

  public final Boolean opaque;

  public final ImageTransform transform;

  public final Integer dpi;


  //----
  protected BarcodeRequest(
      String content,
      Double width,
      Double height,
      Double marginLeft,
      Double marginRight,
      Double marginTop,
      Double marginBottom,
      ImageFormat format,
      Boolean formatInlineSVG,
      Integer formatPreviewDpiEPS,
      ImageColorModel colorModel,
      List<Integer> foreground,
      List<Integer> background,
      Boolean opaque,
      ImageTransform transform,
      Integer dpi) {

    this.content = content;
    this.width = width;
    this.height = height;
    this.marginLeft = marginLeft != null ? marginLeft : 0.0;
    this.marginRight = marginRight != null ? marginRight : 0.0;
    this.marginTop = marginTop != null ? marginTop : 0.0;
    this.marginBottom = marginBottom != null ? marginBottom : 0.0;
    this.format = format;
    this.formatInlineSVG = formatInlineSVG != null && format == ImageFormat.SVG ?
        formatInlineSVG : false;
    this.formatPreviewDpiEPS = formatPreviewDpiEPS != null && format == ImageFormat.EPS ?
        formatPreviewDpiEPS : 0;
    this.colorModel = colorModel != null ? colorModel : ImageColorModel.RGB;

    // Note: Requires colorModel to be initialized first
    this.foreground = foreground != null ? foreground :
        (this.colorModel == ImageColorModel.RGB ? BLACK_RGB : BLACK_CMYK);
    this.background = background != null ? background :
        (this.colorModel == ImageColorModel.RGB ? WHITE_RGB : WHITE_CMYK);

    this.opaque = opaque != null ? opaque : true;
    this.transform = transform != null ? transform : ImageTransform.ROTATE_0;
    this.dpi = dpi != null ? dpi : 0;
  }


  //----
  abstract String getTypeName();

}
