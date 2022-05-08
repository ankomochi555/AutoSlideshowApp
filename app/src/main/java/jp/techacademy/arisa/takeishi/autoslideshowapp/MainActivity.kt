package jp.techacademy.arisa.takeishi.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null

    //タイマー用の時間のための変数
    private var mTimerSec = 0.0

    private var mHandler = Handler()

    private var cursor : Cursor? = null //宣言

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start_button.setOnClickListener {
            Log.d("UI_PARTS", "進むをタップしました")

            getNextInfo()
        }

        back_button.setOnClickListener {
            Log.d("UI_PARTS", "戻るをタップしました")

            getPreviousInfo()
        }

        pause_button.setOnClickListener {
            Log.d("UI_PARTS", "再生/停止をタップしました")

            getAllContentsInfo()
        }

        //Android 6.0 以降の場合
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //パーミッションの許可状態を確認
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //許可されている
                //getContentsInfo()
                cursor = getCursor()  //cursorの権限を得る　初期化
            } else {
                //許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            //Android 5以下の場合
        } else {
             //getContentsInfo()
             cursor = getCursor()  //cursorの権限を得る　初期化
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //getContentsInfo()
                    cursor = getCursor() //cursorの権限を得る　初期化

                }
        }
    }

    /*
    private fun getContentsInfo() {
        //画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, //データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        //最初の一枚目だけを表示
        if (cursor!!.moveToFirst()) {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                Log.d("ANDROID", "URI : " + imageUri.toString()) // URI : content://media/external/images/media/57 ~ 66
                imageView.setImageURI(imageUri)
        }
        cursor.close()
    }
     */

    /**
     * ImageViewにイメージを表示する
     */
    private fun showImage() {
        if(cursor != null){

                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                Log.d("ANDROID", "URI : " + imageUri.toString()
                ) // URI : content://media/external/images/media/57 ~ 66
                imageView.setImageURI(imageUri)
        }
    }

    /**
     * カーソルを取得する
     * @return カーソル。取得できない場合はnullが返る
     */
    private fun getCursor(): Cursor? {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        return if (cursor!!.moveToFirst()) { //if文が値を返す
            cursor
        } else {
            cursor.close()
            null
        }
    }

    //2枚目から進まない →cursol.closeを消すと画像は進むが乱数？になっている気がする。
    private fun getNextInfo(){

        if (cursor!!.moveToNext()) {
            showImage()
        }
        if (!cursor!!.moveToNext()){
            cursor!!.moveToFirst()
            showImage()
        }
        //cursor!!.close()
    }

    //戻らない
    private fun getPreviousInfo(){

        if (cursor!!.moveToPrevious()){
            showImage()
        }
        if (!cursor!!.moveToPrevious()){
            cursor!!.moveToLast()
            showImage()
        }
        //cursor!!.close()
    }

    //タイマーを設定できない、最初と最後しか画像が表示されない
    private fun getAllContentsInfo() {
        
        if (cursor!!.moveToFirst()) {

            do {
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                Log.d("ANDROID", "URI : " + imageUri.toString()
                ) // URI : content://media/external/images/media/57 ~ 66
                imageView.setImageURI(imageUri)
            } while (cursor!!.moveToNext()) //cursor.moveToNext()は現在のデータの次のデータを指します。do~while文で次のカラムがなくなり、falseを返すまでこれを繰り返します。

            if (cursor!!.moveToLast()){
                cursor!!.moveToFirst()
                showImage()
            }
        }
        cursor!!.close()
    }
}