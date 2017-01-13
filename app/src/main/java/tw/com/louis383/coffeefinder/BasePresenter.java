package tw.com.louis383.coffeefinder;

/**
 * Created by louis383 on 2017/1/13.
 */

public abstract class BasePresenter<T> {

    protected T view;
    private boolean viewAttached;

    public void attachView(T view) {
        this.view = view;
    }

    public void detachView() {
        if (viewAttached) {
            this.view = null;
        }
    }

    public boolean isViewAttached() {
        return viewAttached;
    }
}
