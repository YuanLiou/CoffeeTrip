package tw.com.louis383.coffeefinder;

import android.support.annotation.Nullable;

/**
 * Created by louis383 on 2017/1/13.
 */

public abstract class BasePresenter<T> {

    @Nullable
    protected T view;
    private boolean viewAttached;

    public void attachView(T view) {
        this.view = view;
    }

    public void detachView() {
        if (isViewAttached()) {
            this.view = null;
        }
    }

    public boolean isViewAttached() {
        return view != null;
    }
}
