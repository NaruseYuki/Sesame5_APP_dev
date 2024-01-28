package viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.candyhouse.sesame.open.device.CHDevices
import co.candyhouse.sesame.open.device.CHSesame2
import co.candyhouse.sesame.open.device.CHSesame5
import co.candyhouse.sesame.open.device.CHSesameBot
import com.yushin.lockapplication.entities.LockEntity
import com.yushin.lockapplication.model.LockModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    fun connect(){
        _connectedLock.value?.connect {  }
    }

    fun disconnect(){
        connectedLock.value?.disconnect {  }
    }

    suspend fun getAllLocks(): List<LockEntity> {
        return lockModel.getAllLocks()
    }

    suspend fun insertLock(lockEntity: LockEntity) {
        lockModel.insertLock(lockEntity)
    }

    suspend fun deleteLock(lockEntity: LockEntity) {
        lockModel.deleteLock(lockEntity)
    }

    suspend fun updateLock(lockEntity: LockEntity) {
        lockModel.updateLock(lockEntity)
    }

    fun controlLock(device:CHDevices,value:Int){
        when (device) {
            is CHSesameBot -> {
                var checkBLELock = true
                CoroutineScope(Dispatchers.IO).launch {
                    for (index in 0 until 5) {
                        if (checkBLELock) {
                            device.click {
                                it.onSuccess {
                                    checkBLELock = false
                                }
                            }
                            delay(2000)
                        }
                    }
                }
            }
            is CHSesame2 -> {//sesameOS2 ==> model--> sesame4  sesame2(客服認知sesame3)
                var checkBLELock = true// 記錄開鎖成功沒？
                CoroutineScope(Dispatchers.IO).launch {
                    for (index in 0 until 5) {//每隔兩秒開一次開開
                        if (checkBLELock) {
                            when(value){
                                1 ->{
                                    device.unlock {
                                        it.onSuccess {
                                            checkBLELock = false
                                            Log.d("controlLock","complete unlock")
                                        }
                                        it.onFailure {}
                                    }
                                }
                                2->{
                                    device.lock {
                                        it.onSuccess {
                                            checkBLELock = false
                                            Log.d("controlLock","complete lock")
                                        }
                                        it.onFailure {}
                                    }
                                }
                            }

                            delay(2000)
                        }
                    }
                }
            }
            is CHSesame5 -> {
                var checkBLELock = true// 記錄開鎖成功沒？
                CoroutineScope(Dispatchers.IO).launch {
                    for (index in 0 until 5) {//每隔兩秒開一次開開
                        if (checkBLELock) {
                            when(value){
                                1 ->{
                                    device.unlock {
                                        it.onSuccess {
                                            checkBLELock = false
                                        }
                                        it.onFailure {}
                                    }
                                }
                                2->{
                                    device.lock {
                                        it.onSuccess {
                                            checkBLELock = false
                                        }
                                        it.onFailure {}
                                    }
                                }
                            }

                            delay(2000)
                        }
                    }
                }
            }
        }
    }
    fun doRegisterDevice(device: CHDevices) {
        device.register {
            it.onSuccess {
                //  登録成功
                Log.d("doRegisterDevice", "登録成功")
                //初期値の角度を登録しておく
                (device as? CHSesame2)?.let {
                    device.configureLockPosition(0, 256) {}//1024 --> 360,256-->90
                    device.setHistoryTag("Test1".toByteArray()) {}
                    Log.d("doRegisterDevice", "CHSesame2")
                }
                (device as? CHSesame5)?.let {
                    device.configureLockPosition(0, 256) {}//1024 --> 360,256-->90
                    device.setHistoryTag("Test2".toByteArray()) {}
                    Log.d("doRegisterDevice", "CHSesame5")
                }
            }
            it.onFailure {
                //  登録失敗
                Log.d("doRegisterDevice", "登録失敗")
            }
        }
    }
}
