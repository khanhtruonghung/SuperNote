package com.truongkhanh.musicapplication.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

abstract class BaseFragment: Fragment() {
    private var mActivity: BaseActivity? = null


    interface Callback {

        fun onFragmentAttached()

        fun onFragmentDetached(tag: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity) {
            mActivity = context
            context.onFragmentAttached()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView(view, savedInstanceState)
    }

    protected abstract fun setUpView(view: View, savedInstanceState: Bundle?)

    override fun onDetach() {
        mActivity = null
        super.onDetach()
    }

    protected fun addFragment(containerViewId: Int, fragment: Fragment) {
        val fragmentTransaction = fragmentManager?.beginTransaction()
        fragmentTransaction?.add(containerViewId, fragment)
        fragmentTransaction?.commitAllowingStateLoss()
    }

    protected fun removeFragment(fragment: Fragment) {
        val fragmentTransaction = fragmentManager?.beginTransaction()
        fragmentTransaction?.remove(fragment)
        fragmentTransaction?.commitAllowingStateLoss()
    }

    protected fun replaceFragment(containerViewId: Int, fragment: Fragment) {
        val fragmentTransaction = fragmentManager?.beginTransaction()
        fragmentTransaction?.replace(containerViewId, fragment)
        fragmentTransaction?.commitAllowingStateLoss()
    }


    fun replaceFragmentWithAnimation(containerViewId: Int, fragment: Fragment, addToBackStack: Boolean, tag: String) {

        val fragment1 = fragmentManager?.findFragmentByTag(tag)

        var transaction = fragmentManager?.beginTransaction()
        /*  transaction?.setCustomAnimations(
              R.anim.slide_in_left,
              R.anim.slide_out_right,
              R.anim.slide_in_right,
              R.anim.slide_out_left
          )*/
        if (fragment1 != null) {
            transaction?.remove(fragment1)
            transaction?.commit()
            transaction = fragmentManager?.beginTransaction()
        }
        if (addToBackStack) {
            transaction?.addToBackStack(tag)
        }
        transaction?.replace(containerViewId, fragment, tag)
        transaction?.commitAllowingStateLoss()
    }

    fun popFragment(): Boolean {
        var isPop = false
        if (fragmentManager?.backStackEntryCount!! > 0) {
            isPop = true
            fragmentManager?.popBackStack()
        }
        return isPop
    }
}