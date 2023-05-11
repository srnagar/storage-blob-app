package org.storage.blob.app;

import org.junit.jupiter.api.Test;

import java.io.File;


public class DownloadBlobTest {
    @Test
    public void testFilePath() {
        System.out.println(System.getProperty("user.dir"));
        System.out.println(this.getClass().getClassLoader().getResource("").getPath());
    }
}
