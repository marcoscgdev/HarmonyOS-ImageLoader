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

public class ImageLoader {

    private final AbilityContext ability;
    private String url;
    private Image image;
    private boolean cacheEnabled = true;

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

    public void into(Image image) {
        this.image = image;
        loadImage();
    }

    private void loadImage() {
        ability.getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(new Runnable() {
            @Override
            public void run() {
                ImageSource.DecodingOptions decodingOpts = new ImageSource.DecodingOptions();
                decodingOpts.desiredSize = new Size(image.getWidth(), image.getHeight());

                try {
                    ImageSource imageSource = cacheEnabled ? getCachedImageSource() : getRemoteImageSource();

                    PixelMap pixelMap = imageSource.createPixelmap(decodingOpts);
                    imageSource.release();

                    ability.getUITaskDispatcher().asyncDispatch(new Runnable() {

                        @Override
                        public void run() {
                            image.setPixelMap(pixelMap);
                            pixelMap.release();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private ImageSource getCachedImageSource() throws IOException {
        File cachedFile = new File(ability.getCacheDir(), getCacheFileName(url));

        if (!cachedFile.exists()) {
            Files.copy(new URL(url).openStream(), cachedFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }

        return ImageSource.create(cachedFile, null);
    }

    private ImageSource getRemoteImageSource() throws IOException {
        return ImageSource.create(new URL(url).openStream(), null);
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
