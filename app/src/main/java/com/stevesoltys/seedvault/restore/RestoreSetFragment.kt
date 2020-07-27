package com.stevesoltys.seedvault.restore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.stevesoltys.seedvault.R
import kotlinx.android.synthetic.main.fragment_restore_set.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class RestoreSetFragment : Fragment() {

    private val viewModel: RestoreViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_restore_set, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // decryption will fail when the device is locked, so keep the screen on to prevent locking
        requireActivity().window.addFlags(FLAG_KEEP_SCREEN_ON)

        viewModel.restoreSetResults.observe(this, Observer { result -> onRestoreResultsLoaded(result) })

        backView.setOnClickListener { requireActivity().finishAfterTransition() }
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.recoveryCodeIsSet() && viewModel.validLocationIsSet()) {
            viewModel.loadRestoreSets()
        }
    }

    private fun onRestoreResultsLoaded(results: RestoreSetResult) {
        if (results.hasError()) {
            errorView.visibility = VISIBLE
            listView.visibility = INVISIBLE
            progressBar.visibility = INVISIBLE

            errorView.text = results.errorMsg
        } else {
            errorView.visibility = INVISIBLE
            listView.visibility = VISIBLE
            progressBar.visibility = INVISIBLE

            listView.adapter = RestoreSetAdapter(viewModel, results.restorableBackups)
        }
    }

}

internal interface RestorableBackupClickListener {
    fun onRestorableBackupClicked(restorableBackup: RestorableBackup)
}
