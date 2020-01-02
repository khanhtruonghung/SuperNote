package com.truongkhanh.supernote.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

abstract class BaseContainerFragment : Fragment() {

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
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.add(containerViewId, fragment)
        fragmentTransaction.commitAllowingStateLoss()
    }

    protected fun removeFragment(fragment: Fragment) {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.remove(fragment)
        fragmentTransaction.commitAllowingStateLoss()
    }

    protected fun replaceFragment(containerViewId: Int, fragment: Fragment) {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(containerViewId, fragment)
        fragmentTransaction.commitAllowingStateLoss()
    }


    fun addFragmentWithAnimation(containerViewId: Int, fragment: Fragment, addToBackStack: Boolean, tag: String) {

        val fragment1 = childFragmentManager.findFragmentByTag(tag)

        var transaction = childFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if (fragment1 != null) {
            transaction.remove(fragment1)
            transaction.commit()
            transaction = childFragmentManager.beginTransaction()
        }
        if (addToBackStack) {
            transaction.addToBackStack(tag)
        }
        transaction.replace(containerViewId, fragment, tag)
        transaction.commitAllowingStateLoss()
    }

    fun popFragment(): Boolean {
        var isPop = false
        if (childFragmentManager.backStackEntryCount > 0) {
            isPop = true
            childFragmentManager.popBackStack()
        }
        return isPop
    }
}
