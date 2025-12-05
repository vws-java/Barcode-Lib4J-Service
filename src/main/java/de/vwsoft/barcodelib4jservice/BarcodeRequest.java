package de.vwsoft.barcodelib4jservice;

import java.util.List;

import jakarta.validation.constraints.Max;
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

  @NotNull(message = "Format is required")
  public final ImageFormat format;

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
      ImageFormat format,
      ImageColorModel colorModel,
      List<Integer> foreground,
      List<Integer> background,
      Boolean opaque,
      ImageTransform transform,
      Integer dpi) {

    this.content = content;
    this.width = width;
    this.height = height;
    this.format = format;
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
