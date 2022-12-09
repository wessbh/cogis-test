package com.wassimbh.baseproject.utilities

import android.app.Activity
import android.text.TextUtils.replace
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.wassimbh.baseproject.R

fun Fragment.launchFragment(fragment: Fragment, backStackName: String? = null, isAdd: Boolean = false, withAnimation: Boolean = false) {

    activity?.let {
        it.supportFragmentManager.beginTransaction().apply {
            if(withAnimation) {
                setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.fade_out,
                    R.anim.slide_in,
                    R.anim.slide_out
                )
            }
            if (isAdd) {
                add(R.id.main_container, fragment)
            } else {
                replace(R.id.main_container, fragment)
            }
            if (backStackName != null)
                addToBackStack(backStackName)
            commit()
        }
    }
}
fun AppCompatActivity.launchFragment(fragment: Fragment, backStackName: String? = null, isAdd: Boolean = false, withAnimation: Boolean = false) {

    this.supportFragmentManager.beginTransaction().apply {
        if(withAnimation) {
            setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.slide_in,
                R.anim.slide_out
            )
        }
        if (isAdd) {
            add(R.id.main_container, fragment)
        } else {
            replace(R.id.main_container, fragment)
        }
        if (backStackName != null)
            addToBackStack(backStackName)
        commit()
    }
}