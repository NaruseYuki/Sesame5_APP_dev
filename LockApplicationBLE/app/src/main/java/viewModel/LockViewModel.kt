package viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.candyhouse.sesame.open.device.CHDevices
import com.yushin.lockapplication.entities.LockEntity
import com.yushin.lockapplication.model.LockModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class LockViewModel : ViewModel() {

    /*ロックエンティティ*/
    private val _selectedLock = MutableLiveData<LockEntity?>()
    /*接続中ロック*/
    private var _connectedLock = MutableLiveData<CHDevices>()

    /*ロックリスト*/
    private var _lockList = MutableLiveData<Map<UUID?, String>>()
    private var lockModel: LockModel = LockModel.getInstance()

    val selectedLock: LiveData<LockEntity?>
        get() = _selectedLock

    val connectedLock:LiveData<CHDevices>
        get() = _connectedLock

    val lockList:LiveData<Map<UUID?, String>>
        get() {
            CoroutineScope(Dispatchers.IO).launch{
                _lockList.postValue(lockModel.getAllLocks().associate { it.uuid to it.name })
            }
            return _lockList
        }


    fun setSelectedLock(lockEntity: LockEntity?) {
        _selectedLock.value = lockEntity
    }

    fun setConnectedLock(connectedLock:CHDevices){
        _connectedLock.value = connectedLock
    }

}
