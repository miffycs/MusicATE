package com.chunchiehliang.lhd.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.palette.graphics.Palette;

public class PaletteUtil {
    public static int getDominantColor(Bitmap bitmap) {
        List<Palette.Swatch> swatchesTemp = Palette.from(bitmap).generate().getSwatches();
        List<Palette.Swatch> swatches = new ArrayList<>(swatchesTemp);
        Collections.sort(swatches, (swatch1, swatch2) -> swatch2.getPopulation() - swatch1.getPopulation());
        return swatches.size() > 0 ? swatches.get(0).getRgb() : 0;
    }

    public static int getVibrantColor(Bitmap bitmap) {
        Palette.Swatch vibrantSwatch = Palette.from(bitmap).generate().getLightVibrantSwatch();
        if (vibrantSwatch != null) {
            return vibrantSwatch.getRgb();
        } else {
            return Color.WHITE;
        }
    }

    public static int getColorText(Bitmap bitmap) {
        Palette.Swatch vibrantSwatch = Palette.from(bitmap).generate().getLightVibrantSwatch();
        if (vibrantSwatch != null) {
            return vibrantSwatch.getBodyTextColor();
        } else {
            return Color.BLACK;
        }
    }
}
