package ec.huertos.storage;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalPhotoStorage implements PhotoStorage {
  private final Path root;

  public LocalPhotoStorage(@Value("${huertos.storage}") String path) {
    root = Path.of(path).toAbsolutePath().normalize();
  }

  public String save(MultipartFile file) {
    if (file == null || file.isEmpty() || file.getSize() > 5 * 1024 * 1024)
      throw new IllegalArgumentException("Adjunta una fotografía PNG o JPEG de hasta 5 MB");
    try {
      byte[] bytes = file.getBytes();
      boolean png =
          bytes.length > 8
              && bytes[0] == (byte) 137
              && bytes[1] == 80
              && bytes[2] == 78
              && bytes[3] == 71;
      boolean jpg =
          bytes.length > 3
              && bytes[0] == (byte) 255
              && bytes[1] == (byte) 216
              && bytes[2] == (byte) 255;
      if (!png && !jpg)
        throw new IllegalArgumentException("El archivo debe ser una imagen PNG o JPEG real");
      // Inspect dimensions before decoding to avoid allocating a decompression bomb.
      try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
        var readers = ImageIO.getImageReaders(input);
        if (!readers.hasNext()) throw new IllegalArgumentException("Imagen no válida");
        var reader = readers.next();
        try {
          reader.setInput(input);
          if ((long) reader.getWidth(0) * reader.getHeight(0) > 20000000)
            throw new IllegalArgumentException("La imagen supera 20 megapíxeles");
        } finally {
          reader.dispose();
        }
      }
      var image = ImageIO.read(new ByteArrayInputStream(bytes));
      if (image == null) throw new IllegalArgumentException("Imagen dañada");
      Files.createDirectories(root);
      String key = UUID.randomUUID() + ".png";
      // Re-encode to remove metadata and avoid serving arbitrary uploaded content.
      ImageIO.write(image, "png", root.resolve(key).toFile());
      return key;
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo guardar la fotografía", e);
    }
  }

  public byte[] read(String key) {
    try {
      if (!key.matches("[a-f0-9-]{36}\\.png"))
        throw new IllegalArgumentException("Identificador inválido");
      return Files.readAllBytes(root.resolve(key));
    } catch (IOException e) {
      throw new IllegalStateException("Fotografía no disponible", e);
    }
  }
}
