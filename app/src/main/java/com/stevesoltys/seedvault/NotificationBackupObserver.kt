package com.stevesoltys.seedvault

import android.app.backup.BackupProgress
import android.app.backup.IBackupObserver
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.util.Log
import android.util.Log.INFO
import android.util.Log.isLoggable
import com.stevesoltys.seedvault.metadata.MetadataManager
import org.koin.core.KoinComponent
import org.koin.core.inject

private val TAG = NotificationBackupObserver::class.java.simpleName

class NotificationBackupObserver(
        private val context: Context,
        private val expectedPackages: Int,
        private val userInitiated: Boolean) : IBackupObserver.Stub(), KoinComponent {

    private val nm: BackupNotificationManager by inject()
    private val metadataManager: MetadataManager by inject()
    private var currentPackage: String? = null
    private var numPackages: Int = 0

    init {
        // we need to show this manually as [onUpdate] isn't called for first @pm@ package
        nm.onBackupUpdate(getAppName(MAGIC_PACKAGE_MANAGER), 0, expectedPackages, userInitiated)
    }

    /**
     * This method could be called several times for packages with full data backup.
     * It will tell how much of backup data is already saved and how much is expected.
     *
     * @param currentBackupPackage The name of the package that now being backed up.
     * @param backupProgress Current progress of backup for the package.
     */
    override fun onUpdate(currentBackupPackage: String, backupProgress: BackupProgress) {
        showProgressNotification(currentBackupPackage)
    }

    /**
     * Backup of one package or initialization of one transport has completed.  This
     * method will be called at most one time for each package or transport, and might not
     * be not called if the operation fails before backupFinished(); for example, if the
     * requested package/transport does not exist.
     *
     * @param target The name of the package that was backed up, or of the transport
     *                  that was initialized
     * @param status Zero on success; a nonzero error code if the backup operation failed.
     */
    override fun onResult(target: String, status: Int) {
        if (isLoggable(TAG, INFO)) {
            Log.i(TAG, "Completed. Target: $target, status: $status")
        }
        // often [onResult] gets called right away without any [onUpdate] call
        showProgressNotification(target)
    }

    /**
     * The backup process has completed.  This method will always be called,
     * even if no individual package backup operations were attempted.
     *
     * @param status Zero on success; a nonzero error code if the backup operation
     *   as a whole failed.
     */
    override fun backupFinished(status: Int) {
        if (isLoggable(TAG, INFO)) {
            Log.i(TAG, "Backup finished $numPackages/$expectedPackages. Status: $status")
        }
        val success = status == 0
        val notBackedUp = if (success) metadataManager.getPackagesNumNotBackedUp() else null
        nm.onBackupFinished(success, notBackedUp, userInitiated)
    }

    private fun showProgressNotification(packageName: String) {
        if (currentPackage == packageName) return

        if (isLoggable(TAG, INFO)) {
            Log.i(TAG, "Showing progress notification for $currentPackage $numPackages/$expectedPackages")
        }
        currentPackage = packageName
        val app = getAppName(packageName)
        numPackages += 1
        nm.onBackupUpdate(app, numPackages, expectedPackages, userInitiated)
    }

    private fun getAppName(packageId: String): CharSequence = getAppName(context, packageId)

}

fun getAppName(context: Context, packageId: String): CharSequence {
    if (packageId == MAGIC_PACKAGE_MANAGER) return context.getString(R.string.restore_magic_package)
    return try {
        val appInfo = context.packageManager.getApplicationInfo(packageId, 0)
        context.packageManager.getApplicationLabel(appInfo) ?: packageId
    } catch (e: NameNotFoundException) {
        packageId
    }
}
