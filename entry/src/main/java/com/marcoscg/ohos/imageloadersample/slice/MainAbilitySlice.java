package com.marcoscg.ohos.imageloadersample.slice;

import com.marcoscg.ohos.imageloader.ImageLoader;
import com.marcoscg.ohos.imageloadersample.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Image;

public class MainAbilitySlice extends AbilitySlice {

    private Image image;

    private final String imageUrl = "https://images.unsplash.com/photo-1623850015197-9aa664d33e66?fit=crop&w=640&q=80";

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

        image = (Image) findComponentById(ResourceTable.Id_image);

        ImageLoader.with(this)
                .load(imageUrl)
                .setValidCacheDays(7) // Cache will be valid for the next 7 days
                .into(image);
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
