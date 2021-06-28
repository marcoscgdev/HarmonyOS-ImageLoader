# ImageLoader&nbsp;&nbsp;![HarmonyOS](https://img.shields.io/badge/-HarmonyOS-red)
An image loading and caching library for HarmonyOS based applications.

## Download

At this moment it is only possible to use the library by importing the har file. You can find the [latest har here](https://github.com/marcoscgdev/ImageLoader/releases).

## Usage

```java
ImageLoader.with(abilityContext)
        .load(imageUrl)
        .into(imageComponent);
```
