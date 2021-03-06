package org.stepic.droid.view.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import butterknife.ButterKnife
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.view.fragments.SettingsFragment

class SettingsActivity : SingleFragmentActivity() {

    var mToolbar: Toolbar? = null;

    override fun getLayoutResId(): Int {
        return R.layout.activity_settings
    }

    override fun createFragment(): Fragment? {
        return SettingsFragment.newInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        mToolbar = findViewById(R.id.toolbar) as Toolbar
        setUpToolbar()
    }


    private fun setUpToolbar() {
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (mSharedPreferenceHelper.getAuthResponseFromStore() == null) {
                    finish();
                    return true
                } else {
                    return super.onOptionsItemSelected(item)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }
}