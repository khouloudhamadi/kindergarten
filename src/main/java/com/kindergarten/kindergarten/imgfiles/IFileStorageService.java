package com.kindergarten.kindergarten.imgfiles;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface IFileStorageService {

    /** Stocker un fichier et retourner son entité */
    FileDB store(@NonNull MultipartFile file) throws IOException;

    /** Récupérer un fichier par son identifiant */
    FileDB getFile(@NonNull String id);

    /** Lister tous les fichiers stockés */
    @NonNull List<FileDB> getAllFiles();
}