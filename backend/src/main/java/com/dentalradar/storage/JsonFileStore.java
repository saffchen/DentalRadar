package com.dentalradar.storage;

import com.dentalradar.model.DatabaseFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.function.Consumer;

@Component
public class JsonFileStore {

    private final Object lock = new Object();
    private final ObjectMapper mapper;
    private final Path path;

    public JsonFileStore(ObjectMapper objectMapper, @Value("${app.storage.path}") String storagePath) {
        this.mapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
        this.path = resolvePath(storagePath);
    }

    private static Path resolvePath(String storagePath) {
        Path path = Path.of(storagePath);
        if (path.isAbsolute()) {
            return path;
        }
        Path direct = path.toAbsolutePath();
        if (Files.exists(direct)) {
            return direct;
        }
        Path nested = Path.of("backend").resolve(path).toAbsolutePath();
        if (Files.exists(nested)) {
            return nested;
        }
        return direct;
    }

    public DatabaseFile read() {
        synchronized (lock) {
            return copy(readUnlocked());
        }
    }

    public void update(Consumer<DatabaseFile> mutator) {
        synchronized (lock) {
            DatabaseFile database = readUnlocked();
            mutator.accept(database);
            writeUnlocked(database);
        }
    }

    private DatabaseFile readUnlocked() {
        try {
            if (Files.notExists(path) || Files.size(path) == 0) {
                DatabaseFile empty = new DatabaseFile();
                writeUnlocked(empty);
                return empty;
            }
            DatabaseFile database = mapper.readValue(path.toFile(), DatabaseFile.class);
            normalize(database);
            return database;
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось прочитать файл данных", exception);
        }
    }

    private void writeUnlocked(DatabaseFile database) {
        try {
            normalize(database);
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
            mapper.writeValue(temporary.toFile(), database);
            try {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось записать файл данных", exception);
        }
    }

    private DatabaseFile copy(DatabaseFile database) {
        return mapper.convertValue(database, DatabaseFile.class);
    }

    private void normalize(DatabaseFile database) {
        if (database.getUsers() == null) {
            database.setUsers(new ArrayList<>());
        }
        if (database.getClinics() == null) {
            database.setClinics(new ArrayList<>());
        }
        if (database.getServices() == null) {
            database.setServices(new ArrayList<>());
        }
        if (database.getReviews() == null) {
            database.setReviews(new ArrayList<>());
        }
    }
}
