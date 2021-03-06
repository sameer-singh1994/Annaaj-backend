package com.annaaj.store.controller;

import com.annaaj.store.enums.Role;
import com.annaaj.store.service.AuthenticationService;
import com.annaaj.store.service.FIleStoreService;
import com.annaaj.store.model.FileInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/fileUpload")
public class FileUploadController {

    @Autowired
    FIleStoreService fileStoreService;

    @Autowired
    AuthenticationService authenticationService;

    @ApiOperation(value = "upload file, ROLE = ADMIN, USER, COMMUNITY_LEADER")
    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @ApiParam @RequestParam("token") String token) {
        authenticationService.authenticate(token, Arrays.asList(Role.admin, Role.user, Role.communityLeader));
        return fileStoreService.store(file);
    }


    @ApiOperation(value = "get all files, ROLE = ADMIN")
    @GetMapping("/")
    public ResponseEntity<List<FileInfo>> getListFiles(@ApiParam @RequestParam("token") String token) {
        authenticationService.authenticate(token, Collections.singletonList(Role.admin));
        List<FileInfo> fileInfos = fileStoreService.loadAll().map(path -> {
            String filename = path.getFileName().toString();
            String url = MvcUriComponentsBuilder
                    .fromMethodName(FileUploadController.class, "getFile", path.getFileName().toString()).build().toString();

            return new FileInfo(filename, url);
        }).collect(Collectors.toList());

        Stream<Path> pathStream = fileStoreService.loadAll();
        return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
    }

    @ApiOperation(value = "get file")
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<byte[]> getFile(
        @ApiParam(value = "name of the file, only the name with extension(can be extracted from the upload file response by getting the string after the last /)") @PathVariable String filename) throws IOException {
        Resource file = fileStoreService.load(filename);
        InputStream in = file.getInputStream();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(
                IOUtils.toByteArray(in));
    }

}
