package de.danielkoellgen.srscscollabservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class LogController {

    private final Logger logger = LoggerFactory.getLogger(LogController.class);

    @GetMapping(value = "/logs", produces = {MediaType.TEXT_PLAIN_VALUE})
    public String getLog(@RequestParam("file") String filename) {
        String logPath = "./logs/"+filename+".log";
        return logs(logPath);
    }

    private String logs(String logPath) {
        Path path = Paths.get(logPath);
        try {
            Stream<String> lines = Files.lines(path);
            String data = lines.collect(Collectors.joining("\n"));
            lines.close();
            return data;
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
