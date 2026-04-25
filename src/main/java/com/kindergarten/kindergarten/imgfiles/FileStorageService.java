package com.kindergarten.kindergarten.imgfiles;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.lang.NonNull;

@Service
public class FileStorageService implements IFileStorageService {

    @Autowired
    private FileDBRepo fileDBRepository;

    @Override
@NonNull
public List<FileDB> getAllFiles() {
    return (List<FileDB>) fileDBRepository.findAll();
}

@Override
public FileDB store(@NonNull MultipartFile file) throws IOException {
    String fileName = StringUtils.cleanPath(file.getOriginalFilename());
    FileDB fileDB = new FileDB(fileName, file.getContentType(), file.getBytes());
    return fileDBRepository.save(fileDB);
}

@Override
public FileDB getFile(@NonNull String id) {
    return fileDBRepository.findById(id).get();
}
}