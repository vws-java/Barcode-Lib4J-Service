package de.vwsoft.barcodelib4jservice;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import de.vwsoft.barcodelib4j.image.ImageFormat;
import de.vwsoft.barcodelib4j.oned.BarcodeType;
import de.vwsoft.barcodelib4j.twod.TwoDType;


// This controller instance is managed as a singleton by Spring. Only one instance exists for the
// entire application lifecycle and it is reused across all HTTP requests. Therefore, any instance
// fields (if present) would be shared across threads and must be thread-safe.
@RestController
public class MetadataController {

  //----
  public MetadataController() {
  }


  //----
  public record EnumInfo(String name, Map<String, Object> properties) {}
  private static <E extends Enum<E>> List<EnumInfo> enumInfoFrom(E[] values,
      Function<E, Map<String, Object>> propertiesMap) {
    return Arrays.stream(values).map(e -> new EnumInfo(e.name(), propertiesMap.apply(e))).toList();
  }


  //----
  @GetMapping("/{path}")
  public ResponseEntity<List<EnumInfo>> getEnumInfo(@PathVariable String path) {
    List<EnumInfo> result = switch(path) {
      case "formats" -> enumInfoFrom(ImageFormat.values(), f -> Map.of(
          "isRaster", f.isRasterFormat(),
          "supportsTransparency", f.supportsTransparency(),
          "supportsCMYK", f.supportsCMYK()
      ));
      case "types-1d" -> enumInfoFrom(BarcodeType.values(), t -> Map.of(
          "typeName", t.getTypeName(),
          "typeNameShort", t.getTypeNameShort(),
          "supportsCustomText", t.supportsCustomText(),
          "supportsAddOn", t.supportsAddOn(),
          "supportsTextOnTop", t.supportsTextOnTop(),
          "supportsRatio", t.supportsRatio(),
          "supportsAutoCompletion", t.supportsAutoCompletion(),
          "supportsOptionalChecksum", t.supportsOptionalChecksum()
      ));
      case "types-2d" -> enumInfoFrom(TwoDType.values(), t -> Map.of(
          "typeName", t.getTypeName(),
          "isGS1", t.isGS1(),
          "defaultQuietZone", t.getDefaultQuietZone()
      ));
      default -> List.of();
    };

    return ResponseEntity.ok(result);
  }


  //----
  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(Map.of("status", "UP"));
  }

}
