package com.marcoscg.ohos.imageloader;

/**
 * OHOS Image Loader
 * Author: Marcos Calvo Garcia
 * Github: @marcoscgdev
 */

import ohos.agp.components.Image;
import ohos.app.AbilityContext;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.Size;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class ImageLoader {

    private static final String CACHED_IMAGES_PATH = "cached_images/";

    private final AbilityContext ability;
    private String url;
    private Image image;
    private boolean cacheEnabled = true;
    private int validCacheDays = -1;

    public static ImageLoader with(AbilityContext ability) {
        return new ImageLoader(ability);
    }

    private ImageLoader(AbilityContext ability) {
        this.ability = ability;
    }

    public ImageLoader load(String url) {
        this.url = url;
        return this;
    }

    public ImageLoader disableCache() {
        cacheEnabled = false;
        return this;
    }

    public ImageLoader setValidCacheDays(int validCacheDays) {
        this.validCacheDays = validCacheDays;
        return this;
    }

    public void into(Image image) {
        this.image = image;
        loadImage();
    }

    public void clearCache() {
        File cacheDir = new File(ability.getCacheDir(), CACHED_IMAGES_PATH);

        if (!cacheDir.exists() || !cacheDir.isDirectory()) {
            return;
        }

        String[] images = cacheDir.list();

        for (String image : images) {
            File currentFile = new File(cacheDir.getPath(), image);
            currentFile.delete();
        }
    }

    private void loadImage() {
        ability.getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(new Runnable() {
            @Override
            public void run() {
                ImageSource.DecodingOptions decodingOpts = new ImageSource.DecodingOptions();

                try {
                    ImageSource imageSource = cacheEnabled ? getCachedImageSource() : getRemoteImageSource();
                    Size sourceImageSize = imageSource.getImageInfo().size;
                    int maxSize = Math.max(image.getWidth(), image.getHeight());

                    if (sourceImageSize.width >= sourceImageSize.height) {
                        // Landscape
                        int width = (sourceImageSize.width * maxSize) / sourceImageSize.height;
                        decodingOpts.desiredSize = new Size(width, maxSize);
                    } else {
                        // Portrait
                        int height = (sourceImageSize.height * maxSize) / sourceImageSize.width;
                        decodingOpts.desiredSize = new Size(maxSize, height);
                    }

                    PixelMap pixelMap = imageSource.createPixelmap(decodingOpts);
                    imageSource.release();

                    ability.getUITaskDispatcher().asyncDispatch(() -> {
                        image.setPixelMap(pixelMap);
                        pixelMap.release();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private ImageSource getCachedImageSource() throws IOException {
        File cachedFile = new File(ability.getCacheDir(),
                CACHED_IMAGES_PATH + getCacheFileName(url));

        if (!cachedFile.getParentFile().exists()) {
            cachedFile.getParentFile().mkdir();
        }

        if (!cachedFile.exists() || !isValidCache(cachedFile)) {
            Files.copy(new URL(url).openStream(), cachedFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }

        return ImageSource.create(cachedFile, null);
    }

    private ImageSource getRemoteImageSource() throws IOException {
        return ImageSource.create(new URL(url).openStream(), null);
    }

    private boolean isValidCache(File cachedFile) {
        Date date1 = new Date(cachedFile.lastModified());
        Date date2 = new Date();

        long days = ChronoUnit.DAYS.between(date1.toInstant(), date2.toInstant());

        return validCacheDays == -1 || days <= validCacheDays;
    }

    private String getCacheFileName(String url) {
        String extension = url.substring(url.lastIndexOf("."));
        return getMd5(url) + extension;
    }

    private String getMd5(String string) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(string.getBytes(), 0, string.length());
            return String.format("%032x", new BigInteger(1, md5.digest()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return string;
    }

}
