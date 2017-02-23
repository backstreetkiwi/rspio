package de.zaunkoenigweg.rspio.core.omx;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanCreationException;

public class MediaLibrary {

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
    
    public Path getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(Path rootFolder) {
        this.rootFolder = rootFolder;
    }
}
