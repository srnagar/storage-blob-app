package org.storage.blob.app;

import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.stream.IntStream;

public class DownloadBlob {

    public static final int DOWNLOAD_ATTEMPTS = 100;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        File uploadToFile = new File("file-to-upload");

        MessageDigest uploadMessageDigest = MessageDigest.getInstance("MD5");
        uploadMessageDigest.update(Files.readAllBytes(uploadToFile.toPath()));
        byte[] digest = uploadMessageDigest.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String uploadMd5 = bigInt.toString(16);
        System.out.println("Upload file md5: " + uploadMd5);

        // upload once
        System.out.println("Starting upload...");
        BlobClient blobClient = setup(uploadToFile, digest);
        System.out.println("Done uploading.");

        System.out.println("Starting download...");
        // download n times
        IntStream.range(0, DOWNLOAD_ATTEMPTS)
                .parallel()
                .forEach(i -> {
                    try {
                        File downloadFile = new File("test-download-" + i);
                        blobClient.downloadToFile(downloadFile.getAbsolutePath(), true);
                        MessageDigest downloadMessageDigest = MessageDigest.getInstance("MD5");
                        downloadMessageDigest.update(Files.readAllBytes(downloadFile.toPath()));
                        byte[] downloadDigest = downloadMessageDigest.digest();
                        BigInteger downloadBigInt = new BigInteger(1, downloadDigest);
                        String downloadMd5 = downloadBigInt.toString(16);
                        System.out.println("Downloaded file md5: " + downloadMd5);
                        if (!uploadMd5.equals(downloadMd5)) {
                            System.out.println("Upload and download files md5 do not match " + uploadMd5 + " " + downloadMd5);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static BlobClient setup(File file, byte[] digest) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(Configuration.getGlobalConfiguration().get("AZURE_STORAGE_CONNECTION_STRING"))
                .buildClient();

        String containerName = "srnagar-test-" + UUID.randomUUID();

        blobServiceClient.createBlobContainer(containerName);
        BlobContainerClient srnagarcontainer = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = srnagarcontainer.getBlobClient("test-file");
        BlobUploadFromFileOptions uploadOptions = new BlobUploadFromFileOptions(file.toPath().toString())
                .setHeaders(new BlobHttpHeaders().setContentMd5(digest));
        blobClient.uploadFromFileWithResponse(uploadOptions, null, Context.NONE);

        return blobClient;
    }
}
