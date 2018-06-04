package ng.canon.singsave.Auto

import android.Manifest
import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Toast
import com.esafirm.rxdownloader.RxDownloader
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.shashank.sony.fancygifdialoglib.FancyGifDialog
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.anchor.*
import ng.canon.singsave.R
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class Anchor : AppCompatActivity() {

    var saveURL = ""
    var linkBox: ArrayList<String>? = null
    val clipBox = ArrayList<String>()
    var observable: Observable<String>? = null
    var mu: Array<String>? = null
    var version = ""
    var Primaryresponse: Response? = null
    var showing = false
    var track = ""
    var topic = ""
    var app_version = "1470648201"
    var tlc = ""
    var videoID = ""
    var isVideos = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.anchor)

        val intents = intent
        videoID = intents.getStringExtra(Intent.EXTRA_TEXT)

        looku()
    }


    fun looku(){


        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            linkCore(videoID)


        }else{
            runOnUiThread {

                lasma()

            }
        }

    }

    fun linkCore(videoID: String) {
        observable = Observable.create(object : ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {


                try {


                    val instaUrl = pullLinks(videoID)
                    val docs = Jsoup.connect(instaUrl[0])
                            .userAgent("Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
                            .get()

                    val videoType = docs.select("meta[name=twitter:player:stream:content_type]").attr("content")
                    val temp =  docs.select("meta[name=twitter:player:stream]").attr("content")

                    if(videoType.contains("audio/mp4")){
                       saveURL = temp.replace("amp;","")

                        isVideos = false
                       subscriber.onNext(saveURL)

                   }else{

                        val elements = docs.getElementsByTag("script")

                        for (element in elements) {


                            if (element.data().contains("window.DataStore =")){

                                val fulltext = element.data()
                                val user_respond = fulltext.replace("window.DataStore = ","")
                                val user_json =  JSONObject(user_respond)
                                val recordID = user_json.getJSONObject("Pages").getJSONObject("Recording").getJSONObject("performance").getString("video_media_mp4_url").replace("+","%2B")
                                val semi =  temp.substring(0, temp.lastIndexOf("."))
                                saveURL = semi+".tw_stream&url="+recordID
                                isVideos = true

                                subscriber.onNext(saveURL)


                            }
                        }

                   }


                } catch (e: Exception) {

                    subscriber.onError(e)
                }


                subscriber.onComplete()
            }
        })

        observable!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<String> {
                    override fun onSubscribe(d: Disposable) {


                    }

                    override fun onComplete() {





                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(applicationContext, ""+e.message, Toast.LENGTH_LONG).show()
                        finish()

                    }

                    override fun onNext(response: String) {
                        mrSave(saveURL)


                    }
                })

    }



    // EXTRACT LINKS FROM STRINGS
    fun pullLinks(text: String): ArrayList<String> {
        val links = ArrayList<String>()
        //String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
        val regex = "\\(?\\b(https?://|www[.]|ftp://)[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]"

        val p = Pattern.compile(regex)
        val m = p.matcher(text)

        while (m.find()) {
            var urlStr = m.group()

            if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                urlStr = urlStr.substring(1, urlStr.length - 1)
            }

            links.add(urlStr)
        }

        return links
    }



    fun SaveDit(link: String): String {

        var pink = ""
        val saveclient = OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS).build()
        val saverequest = Request.Builder()
                .url(link)
                .build()
        val response = saveclient.newCall(saverequest).execute()


        val json = JSONObject(response.body()!!.string())


        return json.toString()
    }



    fun mrSave(urld:String) {

        Toast.makeText(this@Anchor,""+getString(R.string.starts),Toast.LENGTH_LONG).show()
        val rxDownloader = RxDownloader(this@Anchor)
        var extensions = ""
        if (isVideos){

             extensions = "mp4"

        }else{

            extensions = "m4a"
        }

        var desc = getString(R.string.downloadVideo)
        val timeStamp = System.currentTimeMillis()
        val name = "sing_$timeStamp.$extensions"
        val dex = File(Environment.getExternalStorageDirectory().absolutePath, "/singsave")
        if (!dex.exists())
            dex.mkdirs()

        val filed = File(dex, name)


        val Download_Uri = Uri.parse(urld)
        val downloadManager =  getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request =  DownloadManager.Request(Download_Uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false)
        request.setTitle(name)
        request.setDescription(desc)
        request.setVisibleInDownloadsUi(true)
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir("/singsave",  name)

        rxDownloader.download(request).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<String> {
                    override fun onComplete() {


                    }

                    override fun onError(e: Throwable) {


                    }

                    override fun onNext(t: String) {


                    }

                    override fun onSubscribe(d: Disposable) {

                        finish()

                    }


                })

    }








    fun lasma(){
        val request = permissionsBuilder(Manifest.permission.WRITE_EXTERNAL_STORAGE).build()
        request.send()
        request.listeners {

            onAccepted { permissions ->

                looku()

            }

            onDenied { permissions ->

                permissionDialog()
            }

            onPermanentlyDenied { permissions ->
                permissionDialog()

            }

            onShouldShowRationale { permissions, nonce ->
                permissionDialog()

            }
        }
        // load permission methods here
    }





    fun permissionDialog(){


        runOnUiThread {
            FancyGifDialog.Builder(this@Anchor)
                    .setTitle(getString(R.string.permissionTitle))
                    .setMessage(getString(R.string.permissionMessage))
                    .setNegativeBtnText(getString(R.string.permissionNegative))
                    .setPositiveBtnBackground("#FF4081")
                    .setPositiveBtnText(getString(R.string.permissionPositive))
                    .setNegativeBtnBackground("#FFA9A7A8")
                    .setGifResource(R.drawable.red)   //Pass your Gif here
                    .isCancellable(false)
                    .OnPositiveClicked(object : FancyGifDialogListener {
                        override fun OnClick() {

                            lasma()


                        }


                    })

                    .OnNegativeClicked(object : FancyGifDialogListener {
                        override fun OnClick() {

                            Toast.makeText(this@Anchor,""+getString(R.string.permissionMessage),Toast.LENGTH_LONG).show()
                            finish()

                        }


                    })
                    .build()
        }


    }

}
