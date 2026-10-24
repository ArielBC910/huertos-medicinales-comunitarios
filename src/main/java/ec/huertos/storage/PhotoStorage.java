package ec.huertos.storage;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoStorage {
  String save(MultipartFile file);

  byte[] read(String key);
}
