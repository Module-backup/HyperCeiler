/*
  * This file is part of HyperCeiler.
  
  * HyperCeiler is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.

  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.

  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <https://www.gnu.org/licenses/>.

  * Copyright (C) 2023-2024 HyperCeiler Contributions
*/
package com.sevtinge.hyperceiler.module.hook.gallery;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;

import com.sevtinge.hyperceiler.module.base.BaseHook;

import de.robv.android.xposed.XposedHelpers;

public class UnPrivacyWatermark extends BaseHook {

    public int num = mPrefsMap.getInt("gallery_enable_un_privacy_watermark_value", 14);

    @Override
    public void init() throws NoSuchMethodException {
        findAndHookMethod("com.miui.gallery.editor.photo.app.PrivacyWatermarkActivity",
            "setWordMaxLength", int.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    // logE(TAG, "max: " + param.args[0]);
                    param.args[0] = num;
                }
            }
        );

        findAndHookMethod("com.miui.gallery.editor.photo.app.privacy.PrivacyWatermarkHelper",
            "drawWatermark",
            Canvas.class, String.class, int.class, int.class, int.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    drawWatermark(
                        (Canvas) param.args[0],
                        (String) param.args[1],
                        (int) param.args[2],
                        (int) param.args[3],
                        (int) param.args[4]);
                    param.setResult(null);
                }
            }
        );
    }

    public void drawWatermark(Canvas canvas, String text, int mWidth, int mHeight, int angle) {
        // logE(TAG, "can: " + canvas + " text: " + text + " wid: " + mWidth + " hei: " + mHeight + " an: " + angle);
        int i4 = 0;
        int i5 = 0;
        int i6 = mWidth;
        if (canvas == null || TextUtils.isEmpty(text)) {
            return;
        }
        float min = Math.min(mWidth, mHeight) * 0.02037037f;
        float f2 = 7.0f * min;
        Paint initialPaint = (Paint) XposedHelpers.callStaticMethod(
            findClassIfExists("com.miui.gallery.editor.photo.app.privacy.PrivacyWatermarkHelper"),
            "getInitialPaint");
        Rect rect = new Rect();
        initialPaint.setTextSize(min);
        initialPaint.getTextBounds(text, 0, text.length(), rect);
        double abs = (float) ((Math.abs(-30.0f) / 180.0f) * 3.141592653589793d);
        int width = (int) ((rect.width() + f2) * 2.0f * Math.sin(abs) * Math.cos(abs));
        int height = (int) ((rect.height() + width) * Math.tan(abs));
        float height2 = (float) (rect.height() * Math.sin(abs));
        float height3 = (float) ((rect.height() * Math.cos(abs)) + (rect.width() * Math.sin(abs)));
        float max = (float) ((Math.max(i6 / mHeight, mHeight / i6) * Math.sin(abs)) + 1.0d);
        if (angle != 90) {
            if (angle == 180) {
                float f3 = mHeight;
                i4 = (int) (max * f3);
                canvas.translate(i6 - height2, f3 - height3);
            } else if (angle == 270) {
                float f4 = i6;
                i4 = (int) (max * f4);
                canvas.translate(f4 - height3, height2);
                i6 = mHeight;
            } else {
                if (angle != 0) {
                    logE(TAG, "Not standard orientation degree: " + angle);
                }
                i4 = (int) (mHeight * max);
                canvas.translate(height2, height3);
            }
            // i4 = i5;
        } else {
            canvas.translate(height3, mHeight - height2);
            i4 = (int) (i6 * max);
            i6 = mHeight;
        }
        canvas.rotate((-30.0f) - angle);
        int i7 = 0;
        int i8 = 0;
        while (i7 <= i4) {
            int i9 = i8;
            while (i9 <= i6) {
                float f5 = i9;
                canvas.drawText(text, f5, i7, initialPaint);
                i9 = (int) (f5 + rect.width() + f2);
            }
            i7 = (i7 + rect.height() + width / 2);
            // logE(TAG, "i7: " + i7 + " re: " + rect.height() + " wid: " + width + " i4: " + i4);
            i8 = i8 - height;
        }
    }
}
