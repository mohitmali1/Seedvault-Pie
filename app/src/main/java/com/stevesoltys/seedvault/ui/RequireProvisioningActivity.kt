package com.stevesoltys.seedvault.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.CallSuper
import com.stevesoltys.seedvault.ui.recoverycode.RecoveryCodeActivity
import com.stevesoltys.seedvault.ui.storage.StorageActivity

const val REQUEST_CODE_OPEN_DOCUMENT_TREE = 1
const val REQUEST_CODE_BACKUP_LOCATION = 2
const val REQUEST_CODE_RECOVERY_CODE = 3

const val INTENT_EXTRA_IS_RESTORE = "isRestore"
const val INTENT_EXTRA_IS_SETUP_WIZARD = "isSetupWizard"

private const val ACTION_SETUP_WIZARD = "com.stevesoltys.seedvault.RESTORE_BACKUP"

private val TAG = RequireProvisioningActivity::class.java.name

/**
 * An Activity that requires the recovery code and the backup location to be set up
 * before starting.
 */
abstract class RequireProvisioningActivity : BackupActivity() {

    protected val isSetupWizard: Boolean
        get() = intent?.action == ACTION_SETUP_WIZARD

    protected abstract fun getViewModel(): RequireProvisioningViewModel

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getViewModel().chooseBackupLocation.observeEvent(this, LiveEventHandler { show ->
            if (show) showStorageActivity()
        })
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        if (requestCode == REQUEST_CODE_BACKUP_LOCATION && resultCode != RESULT_OK) {
            Log.w(TAG, "Error in activity result: $requestCode")
            if (!getViewModel().validLocationIsSet()) {
                finishAfterTransition()
            }
        } else if (requestCode == REQUEST_CODE_RECOVERY_CODE && resultCode != RESULT_OK) {
            Log.w(TAG, "Error in activity result: $requestCode")
            if (!getViewModel().recoveryCodeIsSet()) {
                finishAfterTransition()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, result)
        }
    }

    protected fun showStorageActivity() {
        val intent = Intent(this, StorageActivity::class.java)
        intent.putExtra(INTENT_EXTRA_IS_RESTORE, getViewModel().isRestoreOperation)
        intent.putExtra(INTENT_EXTRA_IS_SETUP_WIZARD, isSetupWizard)
        startActivityForResult(intent, REQUEST_CODE_BACKUP_LOCATION)
    }

    protected fun showRecoveryCodeActivity() {
        val intent = Intent(this, RecoveryCodeActivity::class.java)
        intent.putExtra(INTENT_EXTRA_IS_RESTORE, getViewModel().isRestoreOperation)
        intent.putExtra(INTENT_EXTRA_IS_SETUP_WIZARD, isSetupWizard)
        startActivityForResult(intent, REQUEST_CODE_RECOVERY_CODE)
    }

}
