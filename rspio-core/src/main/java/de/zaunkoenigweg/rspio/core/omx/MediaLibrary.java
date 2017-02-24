package de.zaunkoenigweg.rspio.core.omx;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanCreationException;

public class MediaLibrary {

    private static final String PLAYLIST_FILENAME = "playlist.txt";

    private static final String DURATION_FILENAME = "duration.txt";

    private final static Log LOG = LogFactory.getLog(MediaLibrary.class);

    private Path rootFolder;
    
    /**
     * After construction the attributes of the MediaLibrary are checked.
     */
    @PostConstruct
    public void init() {
        if(rootFolder==null) {
            String errorMessage = "MediaLibrary could not be initialized: rootFolder is missing.";
            LOG.error(errorMessage);
            throw new BeanCreationException(errorMessage);
        }
        if(!Files.isDirectory(rootFolder)) {
            String errorMessage = String.format("MediaLibrary could not be initialied: '%s' is not a directory.", rootFolder);
            LOG.error(errorMessage);
            throw new BeanCreationException(errorMessage);
        }
        LOG.info(String.format("MediaLibrary initialied for folder '%s'.", rootFolder));
    }
    
    public List<AudioTrack> getPlaylist() {
        Path playlistPath = rootFolder.resolve(PLAYLIST_FILENAME);
        if(!Files.exists(playlistPath) || Files.isDirectory(playlistPath)) {
            LOG.error(String.format("MediaLibrary could not load playlist from '%s' because file does not exist or is a directory.", playlistPath));
            return Collections.emptyList();
        }
        try {
            return FileUtils.readLines(playlistPath.toFile(), StandardCharsets.ISO_8859_1)
                    .stream()
                    .filter(StringUtils::isNotBlank)
                    .map(rootFolder::resolve)
                    .filter(Files::exists)
                    .filter(Files::isRegularFile)
                    .map(AudioTrack::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error(String.format("MediaLibrary could not load playlist from '%s' because an I/O error occurred.", playlistPath), e);
            return Collections.emptyList();
        }
    }
    
    public Duration getDuration() {
        Path durationFilePath = rootFolder.resolve(DURATION_FILENAME);
        if(!Files.exists(durationFilePath) || Files.isDirectory(durationFilePath)) {
            LOG.error(String.format("MediaLibrary could not load duration from '%s' because file does not exist or is a directory.", durationFilePath));
            return null;
        }
        try {
            return Duration.ofSeconds(Integer.valueOf(StringUtils.trim(FileUtils.readFileToString(durationFilePath.toFile(), StandardCharsets.ISO_8859_1))));
        } catch (Exception e) {
            LOG.error(String.format("MediaLibrary could not load duration from '%s' because an I/O or conversion error occurred.", durationFilePath), e);
            return null;
        }
    }
    
    public Path getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(Path rootFolder) {
        this.rootFolder = rootFolder;
    }
}
