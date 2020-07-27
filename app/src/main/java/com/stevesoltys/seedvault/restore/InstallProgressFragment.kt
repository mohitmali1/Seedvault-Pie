package com.stevesoltys.seedvault.restore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.stevesoltys.seedvault.R
import com.stevesoltys.seedvault.transport.restore.ApkRestoreStatus.QUEUED
import com.stevesoltys.seedvault.transport.restore.InstallResult
import com.stevesoltys.seedvault.transport.restore.getInProgress
import kotlinx.android.synthetic.main.fragment_restore_progress.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class InstallProgressFragment : Fragment() {

    private val viewModel: RestoreViewModel by sharedViewModel()

    private val layoutManager = LinearLayoutManager(context)
    private val adapter = InstallProgressAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_restore_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        titleView.setText(R.string.restore_installing_packages)

        appList.apply {
            layoutManager = this@InstallProgressFragment.layoutManager
            adapter = this@InstallProgressFragment.adapter
            addItemDecoration(DividerItemDecoration(context, VERTICAL))
        }
        button.setText(R.string.restore_next)
        button.setOnClickListener { viewModel.onNextClicked() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.chosenRestorableBackup.observe(this, Observer { restorableBackup ->
            backupNameView.text = restorableBackup.name
        })

        viewModel.installResult.observe(this, Observer { result ->
            onInstallResult(result)
        })

        viewModel.nextButtonEnabled.observe(this, Observer { enabled ->
            button.isEnabled = enabled
        })
    }

    private fun onInstallResult(installResult: InstallResult) {
        // skip this screen, if there are no apps to install
        if (installResult.isEmpty()) viewModel.onNextClicked()

        val result = installResult.filterValues { it.status != QUEUED }
        val position = layoutManager.findFirstVisibleItemPosition()
        adapter.update(result.values)
        if (position == 0) layoutManager.scrollToPosition(0)

        result.getInProgress()?.let {
            progressBar.progress = it.progress
            progressBar.max = it.total
        }
    }

}
