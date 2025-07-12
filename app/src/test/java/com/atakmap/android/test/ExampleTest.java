
package com.atakmap.android.test;

import static junit.framework.Assert.assertEquals;

import android.util.Size;
import android.util.SizeF;

import com.atakmap.android.plugintemplate.detection.BoundingBox;
import com.atakmap.android.plugintemplate.araaftorPlugin.activities.AraaftorActivity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ExampleTest {
    @Test
    public void testCalculateDistanceAngleMethod() {
        AraaftorActivity calculator = new AraaftorActivity();
        calculator.objectRealHeight = 2.0f;
        calculator.focalLenght = 4.0f;
        calculator.sensorDimension = new SizeF(36.0f, 24.0f);
        calculator.imageDimension = new Size(400, 3000);

        BoundingBox bbox = new BoundingBox(100, 200, 150, 300,125,250,50,100,100,1,"soldier"); // y1 = 200, y2 = 300
        List<BoundingBox> boxes = new ArrayList<>();
        boxes.add(bbox);


        List<Double> result = calculator.calculateDistanceAngleMethodTest(boxes, 24.0f, 3000);
        assertEquals(0.8533, result.get(0), 0.1);
    }
}
