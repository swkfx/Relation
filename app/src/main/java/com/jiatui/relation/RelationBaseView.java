package com.jiatui.relation;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public abstract class RelationBaseView extends View {
    public RelationBaseView(Context context) {
        super(context);
    }

    public RelationBaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RelationBaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
