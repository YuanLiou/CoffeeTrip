package tw.com.louis383.coffeefinder.view.behavior

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.viewpager.widget.ViewPager
import tw.com.louis383.coffeefinder.R

class FollowerBehavior : CoordinatorLayout.Behavior<View> {

    constructor()

    constructor(context: Context, attr: AttributeSet): super(context, attr)

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is ViewPager && dependency.id == R.id.main_bottom_sheet
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val paddingBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, parent.resources.displayMetrics)
        child.y = dependency.top.toFloat() - child.height - paddingBottom
        return true
    }
}