package com.fz.toast;

import android.view.View;

/**
 * toast数据对象
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/5/15 20:14
 */
class ToastMsg {
    View view;
    CharSequence message;
    Integer duration;
    int gravity;
    int xOffset;
    int yOffset;

    private ToastMsg(Builder builder) {
        view = builder.view;
        message = builder.message;
        duration = builder.duration;
        gravity = builder.gravity;
        xOffset = builder.xOffset;
        yOffset = builder.yOffset;
    }

    static Builder crateBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private View view;
        private CharSequence message;
        private int duration = ToastCompat.LENGTH_SHORT;
        private int gravity = ToastCompat.mGravity;
        private int xOffset = ToastCompat.mXOffset;
        private int yOffset = ToastCompat.mYOffset;

        public Builder() {
        }

        public Builder view(View val) {
            view = val;
            return this;
        }

        public Builder message(CharSequence val) {
            message = val;
            return this;
        }

        public Builder duration(int val) {
            if (val > 0) {
                duration = val;
            }
            return this;
        }

        public Builder gravity(int val) {
            if (val != 0) {
                gravity = val;
            }
            return this;
        }

        public Builder xOffset(int val) {
            if (val > 0) {
                xOffset = val;
            }
            return this;
        }

        public Builder yOffset(int val) {
            if (val > 0) {
                yOffset = val;
            }
            return this;
        }

        public ToastMsg build() {
            return new ToastMsg(this);
        }
    }
}
