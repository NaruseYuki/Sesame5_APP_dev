package com.yushin.lockapplication.activities

import android.Manifest
import com.yushin.lockapplication.R
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import co.candyhouse.sesame.open.CHBleManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.yushin.lockapplication.constants.Constants
import com.yushin.lockapplication.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN ,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private var isScanning:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // navController = Navigation.findNavController(this,R.id.nav_host_fragment_content_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        val drawerLayout: DrawerLayout =binding.drawerLayout
        val navView: NavigationView = binding.navView
        val toolbar:Toolbar = binding.toolbar
        // 3点リーダを非表示にする
        toolbar.overflowIcon = null
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true) // バックボタンを有効にする
            setHomeAsUpIndicator(R.drawable.ic_menu) // ハンバーガーアイコンを設定
            //setDisplayShowTitleEnabled(false) //
        }
        //追加ボタン
        binding.fab.setOnClickListener {
            when(navController.currentDestination?.id){
                R.id.registeredLocksFragment->{
                    navController.navigate(R.id.action_registeredLocksFragment_to_searchLocksFragment)
                }
            }
        }
        //キーボード非表示リスナー
        binding.root.setOnClickListener{
            binding.root.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
        // NavController の状態変化を監視するリスナーを設定
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // FirstFragment 以外の場合は FAB を非表示にする
            if (destination.id != R.id.registeredLocksFragment ){
                //destination.id != R.id.FirstFragment) {
                binding.fab.hide()
             } else {
                binding.fab.show()
            }
        }
        //ハンバーガーメニュー
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
        } else {
            startBleScan()
            getLastLocation()
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
/*                R.id.nav_item1 -> {
                    // メニューアイテム1が選択されたときの処理
                    // 例: フラグメントの切り替え、アクティビティの起動など
                    navController.navigate(R.id.FirstFragment)
                }*/
                R.id.nav_item2 -> {
                    // メニューアイテム2が選択されたときの処理
                    navController.navigate(R.id.registeredLocksFragment)
                }
                R.id.nav_item3 -> {
                    // メニューアイテム3が選択されたときの処理
                    navController.navigate(R.id.searchLocksFragment)
                }
            }
            // メニューを閉じる
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
    //位置情報の取得
    private fun getLastLocation() {
        Log.d("getLastLocation", "位置情報の取得 ")

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("getLastLocation", "終了")
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // 位置情報の取得に成功した場合の処理
                    val latitude = location.latitude
                    val longitude = location.longitude
                    //位置情報の取得
                    Log.d("location.latitude.", latitude.toString())
                    Log.d("location.longitude", longitude.toString())
                    // ジオフェンシングとBLEスキャンのコードをここに追加する
                }
            }
            .addOnFailureListener { e ->
                // 位置情報の取得に失敗した場合の処理
                Log.d("location.longitude", e.toString())
                //TODO:位置情報の取得に失敗したが、施錠開錠機能は使えるようにする。
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Bluetoothスキャンを開始
                startBleScan()
                // 位置情報のパーミッションが許可された場合、位置情報を取得
                getLastLocation()
            } else {
                // ユーザーが権限を拒否した場合の処理
                showPermissionDeniedDialog(
                    getString(R.string.permission_title),
                    getString(R.string.permission_message),
                    getString(R.string.permission_positive_button))
            }
        }
    }
    private fun showPermissionDeniedDialog(title:String,
                                           message:String,
                                           button:String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton(button) { dialogInterface: DialogInterface, _: Int ->
            // 設定画面を開くインテントを作成
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
            dialogInterface.dismiss()
        }
        alertDialogBuilder.setNegativeButton("キャンセル") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun hasBluetoothPermissions(): Boolean {
        return bluetoothPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        ActivityCompat.requestPermissions(this, bluetoothPermissions, Constants.REQUEST_BLUETOOTH_PERMISSIONS)
    }

    private fun startBleScan() {
        // Bluetoothスキャンを開始する処理をここに追加
        isScanning = true
        CHBleManager.enableScan {}
    }

    private fun stopBleScan() {
        // Bluetoothスキャンを開始する処理をここに追加
        isScanning = false
        CHBleManager.disableScan {}
    }

    override fun onPause() {
        super.onPause()

        // スキャンが実行中であれば停止
        if (isScanning) {
            stopBleScan()
        }
    }

    override fun onResume() {
        super.onResume()

        // Bluetoothスキャンを開始する前にパーミッションを確認
        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
        } else {
//            // スキャンが停止中であれば再開
            if (!isScanning) {
                startBleScan()
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return true
    }

}