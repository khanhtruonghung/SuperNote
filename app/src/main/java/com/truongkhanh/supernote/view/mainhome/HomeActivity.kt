package com.truongkhanh.supernote.view.mainhome

import android.os.Bundle
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseNoAppBarActivity
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.utils.MY_CALENDAR_BUNDLE
import com.truongkhanh.supernote.view.createtodo.CreateTodoActivity
import org.jetbrains.anko.intentFor

class HomeActivity : BaseNoAppBarActivity(), HomeFragment.InteractionListener {

    private lateinit var homeFragment: HomeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            homeFragment = HomeFragment.getInstance()
            if (intent != null)
                homeFragment.arguments = intent.extras
            setFragment(homeFragment)
        }
    }

    private fun setFragment(homeFragment: HomeFragment) {
        replaceFragment(R.id.fragmentContainer, homeFragment)
    }

    override fun navigateToCreateTodo(calendar: MyCalendar?) {
        startActivity(intentFor<CreateTodoActivity>(MY_CALENDAR_BUNDLE to calendar))
    }
}
