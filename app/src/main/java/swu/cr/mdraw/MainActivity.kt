package swu.cr.mdraw

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private var colorIfOpen = false
    private var brushIfOpen = false


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        brushEvent()
        brushSizeEvent()
        otherBtnSizeBack()
        showColors()
        colorChange()
        eraserEvent()
        undoEvent()
        photo()
        saveEvent()
    }
    //将图片保存到本地，并分享
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveEvent(){
        saveimage.setOnClickListener {
            //先转换成图片
            val bitMap = viewToPic(drawView)
            //保存到相册
            ifInAlbum(bitMap)
        }

    }
    //图片写入相册
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun ifInAlbum(bitmap: Bitmap){
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            //保存到相册
                //主协程
            lifecycleScope.launch {
                val uri = saveIn(bitmap)
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM,uri)
                    type = "image/jpg"
                }
                startActivity(Intent.createChooser(shareIntent,"cr的分享"))
            }
        }else{
            requestPermission()
        }
    }

    /**
     * 保存图片
     * 线程 协程
     * supend：表示费时
     * lifecycleScope:作用域
     *
     * */
    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveIn(bitmap: Bitmap): Uri? {
        var result = false
        var uri:Uri? = null
        withContext(Dispatchers.IO){
            //插入图片的基本属性
            val contents = ContentValues().apply {
                put(MediaStore.Images.ImageColumns.DISPLAY_NAME,"cr.jpg")
                put(MediaStore.Images.ImageColumns.MIME_TYPE,"image/jpg")
                put(MediaStore.Images.ImageColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES)
            }
            //内容解析器.插入(插到哪去,插入的图片的具体属性)
            val imgUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contents)
            if (imgUri != null) {
                val fos = contentResolver.openOutputStream(imgUri)
                result = bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos)
                uri = imgUri
            }
        }
        if (result){
            Toast.makeText(this,"已保存到相册",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this,"保存失败",Toast.LENGTH_LONG).show()
        }
        return uri
    }

    //申请权限
    private fun requestPermission(){
        requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,),
        1)
    }

    //视图转化为图片
    private fun viewToPic(view: View):Bitmap{
        /**
         * 根据视图大小创建大小一致的bitmap
         * 通过canvas将视图的内容画到bitmap上去
         *
         * */
        val bitMap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitMap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitMap
    }



    //照相
    private fun photo(){
        pickImage.setOnClickListener {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    //橡皮擦
    private fun eraserEvent(){
        eraser.setOnClickListener {
            drawView.erase()
        }
    }

    //撤销
    private fun undoEvent(){
        undo.setOnClickListener {
            drawView.undo()
        }
    }

    //颜色的改变
    private fun colorChange(){
        arrayOf(bule,pink,orange,purple).forEach {
            it.setOnClickListener { _ ->
                drawView.changeColor(it.tag as String)
            }
        }
    }

    //笔刷大小的弹出与收回
    private fun brushEvent() {
        brush.setOnClickListener {
            showOfHideBrushSize(!brushIfOpen)
        }
    }

    //画笔大小的touch
    private fun brushSizeEvent() {
        arrayOf(l, xl, xxl, xxxl).forEach {
            it.setOnClickListener {_ ->
                if (brushIfOpen) {
                    //选择后隐藏容器
                    showOfHideBrushSize(false)
                    //改变画笔的大小
                    drawView.changeSize((it.tag as String).toFloat())
                }
            }
        }
    }

    //点击其他按钮，画笔大小也收回
    private fun otherBtnSizeBack(){
        arrayOf(pickImage,saveimage,undo,eraser,colors).forEach {
            it.setOnClickListener {
                if (brushIfOpen)
                showOfHideBrushSize(false)
            }
        }
    }

    //弹出颜色
    private fun showColors(){
        colors.setOnClickListener {
            var move = 0f
            move = if (colorIfOpen){
                //关闭
                dp2px(this,70)
            }else{
                //打开
                -dp2px(this,70)
            }

            val colorsArray = arrayOf(bule,purple,pink,orange)
            /***/
            colorsArray.forEach {
                it.visibility = View.VISIBLE
                val index = colorsArray.indexOf(it)
                it.animate()
                        .translationYBy(move * (index + 1))
                        .setDuration(300)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
            }
            colorIfOpen = !colorIfOpen
        }
    }

    //是否隐藏
    private fun showOfHideBrushSize(ifOpen:Boolean){
        var startY = 0f
        var endY = 1f
        if (ifOpen) {
            startY = 1f
            endY = 0f
            brusSize.visibility = View.VISIBLE
        }else {
            brusSize.visibility = View.GONE
        }
        val anim = TranslateAnimation(
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, 0f,
                Animation.RELATIVE_TO_SELF, startY,
                Animation.RELATIVE_TO_SELF, endY
        ).apply {
            duration = 300
            fillAfter = true
            interpolator = DecelerateInterpolator()
        }
        brusSize.startAnimation(anim)
        brushIfOpen = !brushIfOpen

    }

}