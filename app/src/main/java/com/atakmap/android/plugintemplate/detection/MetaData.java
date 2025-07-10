package com.atakmap.android.plugintemplate.detection;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MetaData {

    public static List<String> extractNamesFromLabelFile(Context context, String labelPath) {
        List<String> labels = new ArrayList<>();
        try {
            InputStream inputStream = context.getAssets().open(labelPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                labels.add(line);
            }

            reader.close();
            inputStream.close();
            return labels;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static final List<String> TEMP_CLASSES = new ArrayList<>();
    static {
        for (int i = 1; i <= 1000; i++) {
            TEMP_CLASSES.add("class" + i);
        }
    }
}

